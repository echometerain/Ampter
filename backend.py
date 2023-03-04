import numpy as np
import io
from PIL import Image
from scipy import signal
import scipy.fft as sfft
import pedalboard as pb
from pedalboard.io import AudioFile as AF
from matplotlib import pyplot as plt
import pyaudio


FOURIER_WSIZE = 2048    # fourier window size for the fft
brush = None            # vst3 plugin object from pedalboard
song = np.array([])     # song object as np array
spec = io.BytesIO()     # spectrogram image
sample_rate = 0
num_frames = 0


def play():  # doesn't work
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paFloat32, channels=1,
                    rate=sample_rate, output=True)
    # stream.write(song.tobytes())
    # stream.stop_stream()
    # stream.close()
    # p.terminate()


def load_brush(path):  # load brush from VST3 path, working
    global brush
    if path == None:
        return False
    brush = pb._pedalboard.VST3Plugin(path)


def load_song(path):  # load song from song path, working
    global song, sample_rate, num_frames
    if path == None:
        return False
    with AF(path, "r") as f:
        song = f.read(f.frames-1)[0]  # read only the left channel
        sample_rate = f.samplerate
        num_frames = f.frames


def save_song(path):  # saves song to a path, working
    global song
    if path == None:
        return False
    with AF(path, "w", num_channels=1, samplerate=sample_rate) as f:
        f.write(song)


def get_spectrogram():  # get spectrogram, working
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
    plt.savefig(spec, format='png')


def show_spec():
    spec.seek(0)
    im = Image.open(spec)
    im.show()


def butter_bandpass(lowcut, highcut, order=5):
    nyq = 2 * sample_rate
    low = lowcut / nyq
    high = highcut / nyq
    b, a = signal.butter(order, [low, high], btype='band')
    return b, a


def butter_bandpass_filter(data, lowcut, highcut, order=5):
    b, a = butter_bandpass(lowcut, highcut, order=order)
    y = signal.lfilter(b, a, data)
    return y


def isolate():
    with AF("test.mp3", "w", num_channels=1, samplerate=sample_rate) as f:
        f.write(sfft.ifft(butter_bandpass_filter(sfft.fft(song), 5, 10000)).real)


def paint(a, b, slope, a_int, p):  # call this when the user paints
    # a and b is stored in terms of fourier_wsize
    # if brush == None or song.size == 0:
    #     return False

    for i in range(a, b-2):
        song_freq = sfft.fft(song[i*FOURIER_WSIZE: (i+2)*FOURIER_WSIZE])
        plt.plot(np.arange(0, len(song_freq)), song_freq)
        plt.show()
