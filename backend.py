import numpy as np
import librosa as lr
import librosa.display as lrdp
from matplotlib import pyplot as plt
import io
import math
import base64
from PIL import Image

import pedalboard as pb
from pedalboard.io import AudioFile as AF
import pyaudio
from ampter import Ampter  # type: ignore


effect: pb.Plugin            # effect plugin
ef_selected: bool = False   # whether effect selected
song = np.empty(0)          # song object (left, right) as np array
# spec = np.empty(0)        # base64 spectrogram cache
bl_size = 0                 # draws every x audio frames
bl_freq = 4                 # width of bl in hertz
sample_rate = 0             # sample rate of song (usually 48khz)
num_frames = 0              # number of frames in entire song
num_bl = 0                  # number of blocks
redraw = False              # ask the gui to redraw everything
is_vst = False              # check if selected plugin is a vst

# load song from song path


def set_song(path):  # returns true if successful
    global song, sample_rate, num_frames, spec, bl_size, num_bl
    # quits if no path
    if path == None:
        return False
    with AF(path, "r") as f:  # type: ignore
        # get audio info from pedalboard
        sample_rate = int(f.samplerate)
        bl_size = sample_rate//bl_freq
        num_frames = (f.frames//bl_size + 1)*bl_size
        num_bl = num_frames//bl_size
        song = np.empty([2, num_frames], np.float32)

        # using obj dtype for variable str len
        spec = np.empty([2, num_bl], dtype=object)

        # pad frames to be int multiple of bl_size
        padding = np.zeros(num_frames - f.frames + 1)

        # read all frames
        frames = f.read(f.frames-1)
        song[0] = np.append(frames[0], padding)
        song[1] = np.append(frames[1], padding)
        return True


def play():  # tested
    global num_frames, bl_size, sample_rate
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paFloat32, channels=2,
                    rate=int(sample_rate), output=True)
    buf = np.empty(bl_size*2, np.float32)
    for start_frame in range(0, (num_frames//bl_size) * bl_size, bl_size):
        # awful way to stop playback
        if not Ampter.getPlaying():
            stream.stop_stream()
            stream.close()
            break
        end_frame = start_frame + bl_size-1
        # interleave 2 channels into 1 for playback
        # [1,3,5,7] + [2,4,6,8] => [1,2,3,4,5,6,7,8]
        buf[0:(2 * bl_size - 2):2] = song[0][start_frame: end_frame]
        buf[1:(2 * bl_size - 1):2] = song[1][start_frame: end_frame]
        stream.write(buf.tobytes())
    # p.terminate()


def set_gain(db):
    global effect, ef_selected
    ef_selected = True
    is_vst = False
    effect = pb.Gain(db)


def set_bitcrush(depth):
    global effect, ef_selected
    ef_selected = True
    is_vst = False
    effect = pb.Bitcrush(depth)


# load effect from VST3 path
def set_vst_effect(path):
    global effect, ef_selected
    if path == None:
        return False
    effect = pb._pedalboard.load_plugin(path)
    ef_selected = True
    is_vst = True


def open_vst_ui():
    global effect, ef_selected, is_vst
    if ef_selected and is_vst:
        effect.show_editor()  # type: ignore


def save_song(path):  # saves song to a path
    global song
    if path == None:
        return False
    with AF(path, "w", num_channels=2, samplerate=sample_rate) as file:  # type: ignore
        file.write(song)


# use librosa to calculate a spectrogram
# also put them in muxed cache
def calc_spec(block_pos, channel):  # channel ∈ [0,1]
    global song
    # remove matplotlib axis
    fig = plt.figure(frameon=False)
    ax = plt.Axes(fig, [0., 0., 1., 1.])  # type: ignore
    ax.set_axis_off()
    fig.add_axes(ax)

    # get audio block
    block = song[channel][block_pos*bl_size:(block_pos+1) * bl_size]
    # calculate spectrogram
    spec_num = lr.feature.melspectrogram(
        y=block, sr=sample_rate, n_fft=2048, hop_length=64)
    # fmax=sample_rate//2 if sample_rate <= 48000 else 24000

    # render spectrogram
    lrdp.specshow(lr.power_to_db(spec_num, top_db=100), x_axis='time',
                  y_axis='mel', sr=sample_rate, ax=ax, shading='gouraud', cmap=plt.colormaps['magma'])
    # plt.show()

    # save in memory
    buffer = io.BytesIO()
    fig.savefig(buffer, format="png")
    buffer.seek(0)
    plt.close(fig)

    # set alpha as average of rgb (for visualizing stereo audio)
    img = Image.open(buffer)
    pixels = img.load()
    for i in range(img.size[0]):
        for j in range(img.size[1]):
            p = pixels[i, j]
            pixels[i, j] = (p[0], p[1], p[2], (pixels[i, j][0] + pixels[i, j]
                                               [1] + pixels[i, j][2]) // 3)
    buffer = io.BytesIO()
    # img.save(f"{block_pos}_{channel}.png")
    img.save(buffer, format="png")
    # save to b64 so swing can access it
    return base64.b64encode(buffer.getvalue())


# from wolfsound's tutorial: https://youtu.be/wodumxEF9u0
# returns coefficients
def second_order_allpass_filter(freq, BW):
    tan = np.tan(np.pi * BW / sample_rate)
    c = (tan - 1) / (tan + 1)
    d = - np.cos(2 * np.pi * freq / sample_rate)
    b = [-c, d * (1 - c), 1]
    a = [1, d * (1 - c), -c]
    return b, a

# paint using brush
# Q makes bandwidth appear constant on log scales
# y denotes frequency so exp(y-coord) must be used


def paint(ax, ay, bx, by, Q, channel, wet) -> bool:  # wet ∈ [0,1]
    unfiltered = song[channel][ax:bx]
    filtered = np.zeros(bx-ax)
    x1 = 0
    x2 = 0
    y1 = 0
    y2 = 0

    # get exponential function from two points
    # looks like a line on a log scale
    # https://www.desmos.com/calculator/njktci6nl3
    base = (ay/by)**(1/(ax-bx))

    for j in range(bx-ax):  # each sample
        freq = ay*base**j   # break frequency
        BW = freq / Q       # bandwidth
        m, n = second_order_allpass_filter(freq, BW)
        x = unfiltered[j]
        y = m[0] * x + m[1] * x1 + m[2] * x2 - n[1] * y1 - n[2] * y2
        # prevents NaNs & infinities
        if math.isnan(y) or math.isinf(y) or y > 2 or y < -2:
            y = 0

        y2 = y1
        y1 = y
        x2 = x1
        x1 = x

        filtered[j] = y

    # exploiting destructive interference
    bandpass = 0.5 * (unfiltered - filtered)  # all freqs except selected
    bandstop = 0.5 * (unfiltered + filtered)  # only selected freqs
    # apply effects to it
    # wet <=> percent processed with effect
    song[channel][ax:bx] = bandstop + wet * \
        pb.process(bandpass, sample_rate, [effect]) + (1-wet)*bandstop

    # # recalculate spectrograms for changed blocks
    # for j in range(ax // bl_size, bx // bl_size):
    #     calc_spec(j, channel)

    return True


def set_bl_size(val):
    global bl_size
    bl_size = val


def get_bl_size():
    global bl_size
    return bl_size


def get_ef_selected():
    global ef_selected
    return ef_selected
