import ampter as amp
import numpy as np
import scipy.fft as sfft
import pedalboard as pb
import ffmpeg
import tkinter as tk
import customtkinter as ctk
from matplotlib import pyplot as plt


def paint(a, b, slope, a_int):
    if amp.brush == None or amp.song == None:
        return False
    song_freq = sfft.fft(amp.song)
