import numpy as np
import scipy
import pedalboard as pb
import ffmpeg
import tkinter as tk
import customtkinter as ctk

FOURIER_WSIZE = 200
Y_PADDING = 500  # how thick the brush is (y direction)

brush_path = None  # path of the VST plugin
brush = None  # vst plugin object from pedalboard
song_path = None  # path of the song
song = None  # song object as np array
