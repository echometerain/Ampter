import ampter as amp
import numpy as np
import scipy
import pedalboard as pb
import tkinter as tk
import customtkinter as ctk

song_path = None  # path of the song
brush_path = None  # path of the VST plugin
Y_PADDING = 500  # how thick the brush is (y direction)

window = tk.Tk()
window.geometry("2500x2000")

uploadButton = tk.Button(text = "Upload File",
                width = 10,
                height = 10)
uploadButton.pack()

window.mainloop()