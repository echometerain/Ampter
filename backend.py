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


def play():
    p = pyaudio.PyAudio()
    stream = p.open(format=pyaudio.paFloat32, channels=1,
                    rate=int(sample_rate), output=True)
    stream.write(song.tobytes())
    stream.stop_stream()
    stream.close()
    p.terminate()


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
    with AF(path, "w", num_channels=1, samplerate=sample_rate) as file:
        file.write(song)


def get_spectrogram():  # get spectrogram, working
    global song

    a, b, temp_spec = signal.spectrogram(song, sample_rate, nfft=2048)
    # keep only frequencies of interest
    a = a[np.where(a < 2000)]
    temp_spec = temp_spec[np.where(a < 2000), :][0]

    fig = plt.figure(frameon=False)  # get rid of the axis
    fig.set_size_inches(num_frames/10000, 10)
    ax = plt.Axes(fig, [0., 0., 1., 1.])
    ax.set_axis_off()
    fig.add_axes(ax)

    # render spectrogram and save as image in memory
    plt.pcolormesh(b, a, temp_spec, shading='gouraud')
    plt.savefig(spec, format='png')
    spec.seek(0)


# def butter_bandpass(lowcut, highcut, order=5):
#     nyq = 3 * sample_rate
#     low = lowcut / nyq
#     high = highcut / nyq
#     sos = signal.butter(order, [low, high],
#                         analog=False, btype='band', output='sos')
#     return sos


# def butter_bandpass_filter(data, lowcut, highcut, order=5):
#     sos = butter_bandpass(lowcut, highcut, order=order)
#     y = signal.sosfilt(sos, data)
#     return y


# def isolate():
#     # Apply the FFT to the audio data
#     fft_data = sfft.fft(song)

#     # Determine the frequency range of the data
#     freq_range = np.linspace(0, len(fft_data), len(fft_data))

#     # Define the frequency range to isolate
#     freq_min = 10  # minimum frequency to isolate
#     freq_max = 2000  # maximum frequency to isolate

#     # Create a bandpass filter with the desired frequency range
#     nyquist_rate = 0.5 * sample_rate
#     cutoff_freqs = [freq_min / nyquist_rate, freq_max / nyquist_rate]
#     b, a = signal.butter(4, cutoff_freqs, btype="band")

#     # Apply the filter to the audio data
#     filtered_data = signal.filtfilt(b, a, song)

#     plt.plot(np.arange(0, len(filtered_data)), filtered_data)
#     plt.show()
#     with AF("test.mp3", "w", num_channels=1, samplerate=sample_rate) as f:
#         f.write(butter_bandpass_filter(filtered_data, 700, 300))


def paint(a, b, a_int):  # call this when the user paints
    # a and b is stored in terms of audio frame number
    # if brush == None or song.size == 0:
    #     return False
    signal = np.sin(2 * np.pi * a_int * np.linspace(0, (b-a) /
                    sample_rate, b-a, endpoint=False))

    # plt.plot(np.arange(0, len(signal)), signal)
    # plt.show()
    # with AF("test.mp3", "w", num_channels=1, samplerate=sample_rate) as f:
    #     f.write(signal)
    np.add.at(song, signal, a)
