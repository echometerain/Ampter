import numpy as np
import scipy.fft as sfft
import pedalboard as pb
from pedalboard.io import AudioFile as AF
import tkinter as tk
import customtkinter as ctk
from matplotlib import pyplot as plt

FOURIER_WSIZE = 200
brush = None  # vst3 plugin object from pedalboard
song = np.array([])  # song object as np array
spectrogram = None  # spectrogram image


def load_brush(path):
    if path == None:
        return False
    brush = pb._pedalboard.load_plugin(path)


def load_song(path):
    print("hi")
    if path == None:
        return False
    with AF(path, "r") as f:
        song = f.read(f.frames-1)[0][0]  # read only the left channel
    print(song)


def save_song(path):
    if path == None:
        return False
    with AF(path, "w", num_channels=1) as f:
        f.write(song)


def get_spectrogram():
    return True


def paint(a, b, slope, a_int, p):  # call this when the user paints
    # if brush == None or song.size == 0:
    #     return False
    song_freq = sfft.fft(song)
    print(song_freq)
    # plt.plot(np.arange(0, song_freq) ,song_freq)
    # plt.show()
