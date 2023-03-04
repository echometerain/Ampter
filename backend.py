import numpy as np
import io
from PIL import Image
from scipy import signal
import scipy.fft as sfft
import pedalboard as pb
from pedalboard.io import AudioFile as AF
from matplotlib import pyplot as plt
import pyaudio


FOURIER_WSIZE = 2000     # fourier window size for the fft
brush = None            # vst3 plugin object from pedalboard
song = np.array([])     # song object as np array
spec = None             # spectrogram image
sample_rate = 0
num_frames = 0


def play():
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paInt16, channels=len(
        song), rate=sample_rate, output=True)
    stream.write(song.tobytes())
    stream.stop_stream()
    stream.close()
    p.terminate()


def load_brush(path):  # load brush from VST3 path
    global brush
    if path == None:
        return False
    brush = pb._pedalboard.load_plugin(path)


def load_song(path):  # load song from song path
    global song, sample_rate, num_frames
    if path == None:
        return False
    with AF(path, "r") as f:
        song = f.read(f.frames-1)[0]  # read only the left channel
        sample_rate = f.samplerate
        num_frames = f.frames


def save_song(path):
    global song
    if path == None:
        return False
    with AF(path, "w", num_channels=1, samplerate=sample_rate) as f:
        f.write(song)


def get_spectrogram():
    global song

    f, t, Sxx = signal.spectrogram(song, sample_rate, nfft=2048)
    # keep only frequencies of interest
    freq_slice = np.where(f <= 2000)
    f = f[freq_slice]
    Sxx = Sxx[freq_slice, :][0]

    fig = plt.figure(frameon=False)  # get rig of the axis
    fig.set_size_inches(num_frames/10000, 10)
    ax = plt.Axes(fig, [0., 0., 1., 1.])
    ax.set_axis_off()
    fig.add_axes(ax)

    # render spectrogram and save as image in memory
    plt.pcolormesh(t, f, Sxx, shading='gouraud')
    buf = io.BytesIO()
    plt.savefig(buf, format='png')
    buf.seek(0)
    im = Image.open(buf)
    buf.close()

    return im


def paint(a, b, slope, a_int, p, path):  # call this when the user paints
    # if brush == None or song.size == 0:
    #     return False
    # a and b is stored in terms of fourier_wsize

    for i in range(a, b):
        song_freq = sfft.fft(song[a, i])
        plt.plot(np.arange(0, len(song_freq)), song_freq)
        plt.show()
