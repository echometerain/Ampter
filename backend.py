import numpy as np
from PIL import Image
import librosa as lr
import librosa.display as lrdp
from matplotlib import pyplot as plt
import io
import math

import pedalboard as pb
from pedalboard.io import AudioFile as AF
import pyaudio


effect: pb.Plugin           # vst3 plugin object from pedalboard
ef_selected = False
song = np.empty(0)          # song object as np array, 0 => left
spec = np.empty(0)          # spectrogram images
bl_size = 0                 # draws every x audio frames
sample_rate = 0
num_frames = 0


def play():  # untested
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paFloat32, channels=1,
                    rate=int(sample_rate), output=True)
    stream.write(song.tobytes())
    stream.stop_stream()
    stream.close()
    p.terminate()


def pb_effect(name):
    global effect
    if name == "Gain":
        effect = pb.Gain(20)


def load_effect(path):  # load effect from VST3 path
    global effect, ef_selected
    if path == None:
        return False
    effect = pb._pedalboard.load_plugin(path)
    ef_selected = True


def load_song(path):  # load song from song path
    global song, sample_rate, num_frames, spec, bl_size
    if path == None:
        return False
    with AF(path, "r") as f:  # type: ignore
        sample_rate = int(f.samplerate)
        bl_size = sample_rate//4
        num_frames = (f.frames//bl_size + 1)*bl_size
        song = np.empty((2, num_frames))
        spec = np.empty((2, num_frames//bl_size), dtype=object)
        padding = np.zeros(num_frames - f.frames + 1)
        frames = f.read(f.frames-1)
        song[0] = np.append(frames[0], padding)
        song[1] = np.append(frames[1], padding)


def save_song(path):  # saves song to a path
    global song
    if path == None:
        return False
    with AF(path, "w", num_channels=2, samplerate=sample_rate) as file:  # type: ignore
        file.write(song)


def calc_spec(block):  # get spectrogram, working
    global song
    for i in range(2):
        fig = plt.figure(frameon=False)  # remove axis
        ax = plt.Axes(fig, [0., 0., 1., 1.])
        ax.set_axis_off()
        fig.add_axes(ax)
        # render spectrograms
        spec_num = lr.feature.melspectrogram(y=song[i][block*bl_size:(block+1) * bl_size], sr=sample_rate,
                                             n_fft=2048, hop_length=64, fmax=sample_rate//2 if sample_rate <= 48000 else 24000)
        lrdp.specshow(lr.power_to_db(spec_num, 100), x_axis='time',
                      y_axis='mel', sr=sample_rate, ax=ax)
        # plt.show()

        # save in memory
        buffer = io.BytesIO()
        fig.savefig(buffer, format='png')
        buffer.seek(0)
        spec[i][block] = Image.open(buffer)
        plt.close(fig)


def second_order_allpass_filter(freq, BW):
    tan = np.tan(np.pi * BW / sample_rate)
    c = (tan - 1) / (tan + 1)
    d = - np.cos(2 * np.pi * freq / sample_rate)
    b = [-c, d * (1 - c), 1]
    a = [1, d * (1 - c), -c]
    return b, a


def paint(ax, ay, bx, by, Q):
    for i in range(2):
        unfiltered = song[i][ax:bx]
        filtered = np.zeros(bx-ax)
        x1 = 0
        x2 = 0
        y1 = 0
        y2 = 0

        # exp regression https://www.desmos.com/calculator/njktci6nl3
        base = (ay/by)**(1/(ax-bx))

        for j in range(bx-ax):  # each sample
            freq = ay*base**j
            BW = freq / Q
            m, n = second_order_allpass_filter(freq, BW)
            x = unfiltered[j]
            y = m[0] * x + m[1] * x1 + m[2] * x2 - n[1] * y1 - n[2] * y2
            if math.isnan(y) or math.isinf(y) or y > 2 or y < -2:
                y = 0
            y2 = y1
            y1 = y
            x2 = x1
            x1 = x

            filtered[j] = y

        # filtered = np.ma.fix_invalid(filtered, fill_value=0)
        bandpass = 0.5 * (unfiltered - filtered)
        bandstop = 0.5 * (unfiltered + filtered)
        song[i][ax:bx] = bandstop + \
            pb.process(bandpass, sample_rate, [effect])  # processed

    return True
