import backend as be
import numpy as np
import pedalboard as pb
import tkinter as tk
from customtkinter import *  # type: ignore
from PIL import Image
import os

brush = None
song_path = None   # path of the song
custom_brush_path = None  # path of the VST plugin
y_padding = 500    # how thick the brush is (y direction)
playhead_pos = 0   # where the playhead is (# of FOURIER_WSIZEs)
playing = [False]

# Themes: "blue" (standard), "green", "dark-blue"
set_default_color_theme("blue")
set_appearance_mode("Dark")

# This function is used to insert the details entered by users into the textbox.

typelist = (("All Files", "*.*"), ("3G2 File", "*.3g2"), ("3GP File", "*.3gp"), ("AC3 File", "*.ac3"), ("ADTS File", "*.adts"), ("AIF File", "*.aif"), ("AIFC File", "*.aifc"), ("AIFF File", "*.aiff"), ("AMR File", "*.amr"), ("AU File", "*.au"), ("BWF File", "*.bwf"), ("CAF File", "*.caf"), ("EC3 File", "*.ec3"), ("FLAC File", "*.flac"), ("LATM File", "*.latm"), ("LOAS File", "*.loas"), ("M4A File",
            "*.m4a"), ("M4B File", "*.m4b"), ("M4R File", "*.m4r"), ("MOV File", "*.mov"), ("MP1 File", "*.mp1"), ("MP2 File", "*.mp2"), ("MP3 File", "*.mp3"), ("MP4 File", "*.mp4"), ("MPA File", "*.mpa"), ("MPEG File", "*.mpeg"), ("OGG File", "*.ogg"), ("QT File", "*.qt"), ("SD2 File", "*.sd2"), ("SND File", "*.snd"), ("W64 File", "*.w64"), ("WAV File", "*.wav"), ("XHE File", "*.xhe"), ("AAC File", "*.aac"))


def generateResults():
    filePath = filedialog.askopenfilename(filetypes=typelist)
    displayBox.delete("0.0", "200.0")
    displayBox.insert("0.0", filePath)

    be.load_song(filePath)
    be.get_spectrogram()
    spec = CTkImage(Image.open(be.spec), Image.open(
        be.spec), (be.num_frames//100, 1000))

# def yPaddingSliderChange(self, position):
#     y_padding = position * 1000
#     print(y_padding)


def changeAppearance(newOption):
    set_appearance_mode(newOption)


app = CTk()
assetsPath = os.path.join(os.path.dirname(
    os.path.realpath(__file__)), "assets")
ampterIcon = tk.PhotoImage(file="./assets/ampterIcon.png")

app.title("Ampter - Audio Mastering Painter")
app.iconphoto(False, ampterIcon)
app.geometry("500x800")  # Dimensions = 500x800
app.minsize(100, 100)

# Grid Layout - Navigation Pane and Workspace
app.grid_rowconfigure(0, weight=1)
app.grid_columnconfigure(1, weight=1)

"""
NAVIGATION PANE
"""

navigationPane = CTkFrame(app, corner_radius=0)
navigationPane.grid(row=0, column=0, sticky="nsew")
navigationPane.grid_rowconfigure(4, weight=1)

# Upload File Button
generateFileButton = CTkButton(navigationPane, width=190, height=30, corner_radius=0, text="Upload File", font=(
    "Trebuchet MS", 20), command=generateResults)
generateFileButton.grid(
    row=1, column=0, columnspan=4, padx=20, pady=0, sticky="ew")

# Display File Name Label
displayBox = CTkTextbox(
    navigationPane, width=200, height=50)
displayBox.grid(row=0, column=0, columnspan=4,
                padx=20, pady=10, sticky="ew")

# Open VST UI Button
vstIcon = CTkImage(Image.open(
    os.path.join(assetsPath, "vstIcon.png")), size=(25, 25))
openVST = CTkButton(navigationPane, corner_radius=0, width=190, height=30, border_spacing=10, text="Open VST UI", font=("Trebuchet MS", 20), fg_color="transparent",
                    text_color=("black", "white"), hover_color=("gray", "gray"), image=vstIcon, anchor=tk.CENTER, command=None)
openVST.grid(row=2, column=0, padx=20, sticky="ns")

# Upload Brush Button
brushIcon = CTkImage(Image.open(
    os.path.join(assetsPath, "brushIcon.png")), size=(25, 25))
openBrush = CTkButton(navigationPane, corner_radius=0, width=190, height=30, border_spacing=10, text="Upload Brush", font=("Trebuchet MS", 20), fg_color="transparent",
                      text_color=("black", "white"), hover_color=("gray", "gray"), image=brushIcon, anchor=tk.CENTER, command=None)
openBrush.grid(row=3, column=0, padx=20, sticky="ns")

# Y-Padding Label
yPaddingLabel = CTkLabel(
    navigationPane, text=f"Y-Padding: {y_padding}")
yPaddingLabel.grid(row=4, column=0, padx=10, pady=10)

# Y-Padding Slider
yPaddingSlider = CTkSlider(
    navigationPane, width=200, height=15)
yPaddingSlider.place(relx=0.5, rely=0.5, anchor="center")
# 0.4 * pos = y-padding

# Appearance Mode Label
appearanceModeLabel = CTkLabel(
    navigationPane, text="Appearance Mode:")
appearanceModeLabel.place()

# Appearance Change Dropdown Menu
appearanceMode = CTkOptionMenu(navigationPane, values=[
    "Dark", "Light", "System"], command=changeAppearance)
appearanceMode.grid(row=6, column=0, padx=20, pady=20, sticky="s")

"""
WORKSPACE AREA
"""

workspace = CTkFrame(app, corner_radius=0)
timeline = CTkCanvas(workspace)
editor = CTkCanvas(workspace)

# Used to run the application.
app.mainloop()
