import ampter as amp
import numpy as np
import scipy.fft as sfft
import pedalboard as pb
from pedalboard.io import AudioFile as AF
import ffmpeg
import tkinter as tk
import customtkinter as ctk
from matplotlib import pyplot as plt

brush = None  # vst3 plugin object from pedalboard
song = np.array([])  # song object as np array
spectrogram = None  # spectrogram image


def load_brush(path):
    if path == None:
        return False


def load_song(path):
    if path == None:
        return False
    with AF(path, "r") as f:
        song = f.read(f.frames)


def save_song(path):
    if path == None:
        return False
    with AF(path, "w") as f:
        f.write(song)


def get_spectrogram():
    return True


def paint(a, b, slope, a_int):  # call this when the user paints
    if brush == None or song.size == 0:
        return False
    song_freq = sfft.fft(song)
