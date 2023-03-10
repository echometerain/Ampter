import backend as be
import numpy as np
import scipy
import pedalboard as pb
import tkinter as tk
import customtkinter as ctk
from PIL import Image
import os

song_path = None   # path of the song
brush_path = None  # path of the VST plugin
y_padding = 500    # how thick the brush is (y direction)
playhead_pos = 0   # where the playhead is (# of FOURIER_WSIZEs)
playing = [False]

ctk.set_default_color_theme("blue")  # Green, Dark Blue, Blue


class App(ctk.CTk):
    # Layout of the GUI will be written in the init itself.
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        assetsPath = os.path.join(os.path.dirname(
            os.path.realpath(__file__)), "assets")
        ampterIcon = tk.PhotoImage(file="./assets/ampterIcon.png")

        self.title("Ampter - Audio Mastering Painter")
        self.iconphoto(False, ampterIcon)
        self.geometry("500x800")  # Dimensions = 500x800

        # Grid Layout - Navigation Pane and Workspace
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)

        """
        NAVIGATION PANE
        """

        self.navigationPane = ctk.CTkFrame(self, corner_radius=0)
        self.navigationPane.grid(row=0, column=0, sticky="nsew")
        self.navigationPane.grid_rowconfigure(4, weight=1)

        # Upload File Button
        self.generateFileButton = ctk.CTkButton(self.navigationPane, width=190, height=30, corner_radius=0, text="Upload File", font=(
            "Trebuchet MS", 20), command=self.generateResults)
        self.generateFileButton.grid(
            row=1, column=0, columnspan=4, padx=20, pady=0, sticky="ew")

        # Display File Name Label
        self.displayBox = ctk.CTkTextbox(
            self.navigationPane, width=200, height=50)
        self.displayBox.grid(row=0, column=0, columnspan=4,
                             padx=20, pady=10, sticky="ew")

        # Open VST UI Button
        self.vstIcon = ctk.CTkImage(Image.open(
            os.path.join(assetsPath, "vstIcon.png")), size=(25, 25))
        self.openVST = ctk.CTkButton(self.navigationPane, corner_radius=0, width=190, height=30, border_spacing=10, text="Open VST UI", font=("Trebuchet MS", 20), fg_color="transparent",
                                     text_color=("black", "white"), hover_color=("gray", "gray"), image=self.vstIcon, anchor=tk.CENTER, command=None)
        self.openVST.grid(row=2, column=0, padx=20, sticky="ns")

        # Upload Brush Button
        self.brushIcon = ctk.CTkImage(Image.open(
            os.path.join(assetsPath, "brushIcon.png")), size=(25, 25))
        self.openBrush = ctk.CTkButton(self.navigationPane, corner_radius=0, width=190, height=30, border_spacing=10, text="Upload Brush", font=("Trebuchet MS", 20), fg_color="transparent",
                                       text_color=("black", "white"), hover_color=("gray", "gray"), image=self.brushIcon, anchor=tk.CENTER, command=None)
        self.openBrush.grid(row=3, column=0, padx=20, sticky="ns")

        # Y-Padding Label
        self.yPaddingLabel = ctk.CTkLabel(
            self.navigationPane, text=f"Y-Padding: {y_padding}")
        self.yPaddingLabel.grid(row=4, column=0, padx=10, pady=10)

        # Y-Padding Slider
        self.yPaddingSlider = ctk.CTkSlider(
            self.navigationPane, width=200, height=15)
        self.yPaddingSlider.place(relx=0.5, rely=0.5, anchor="center")
        # 0.4 * pos = y-padding

        # Appearance Mode Label
        self.appearanceModeLabel = ctk.CTkLabel(
            self.navigationPane, text="Appearance Mode:")
        self.appearanceModeLabel.place()

        # Appearance Change Dropdown Menu
        self.appearanceMode = ctk.CTkOptionMenu(self.navigationPane, values=[
                                                "System", "Light", "Dark"], command=self.changeAppearance)
        self.appearanceMode.grid(row=6, column=0, padx=20, pady=20, sticky="s")

        """
        WORKSPACE AREA
        """

        self.workspace = ctk.CTkFrame(self, corner_radius=0)

    # This function is used to insert the details entered by users into the textbox.

    def generateResults(self):
        filePath = ctk.filedialog.askopenfilename(filetypes=(("All Files", "*.*"), ("3G2 File", "*.3g2"), ("3GP File", "*.3gp"),
                                                             ("AC3 File", "*.ac3"), ("ADTS File",
                                                                                     "*.adts"), ("AIF File", "*.aif"),
                                                             ("AIFC File", "*.aifc"), ("AIFF File",
                                                                                       "*.aiff"), ("AMR File", "*.amr"),
                                                             ("AU File", "*.au"), ("BWF File",
                                                                                   "*.bwf"), ("CAF File", "*.caf"),
                                                             ("EC3 File", "*.ec3"), ("FLAC File",
                                                                                     "*.flac"), ("LATM File", "*.latm"),
                                                             ("LOAS File", "*.loas"), ("M4A File",
                                                                                       "*.m4a"), ("M4B File", "*.m4b"),
                                                             ("M4R File", "*.m4r"), ("MOV File",
                                                                                     "*.mov"), ("MP1 File", "*.mp1"),
                                                             ("MP2 File", "*.mp2"), ("MP3 File",
                                                                                     "*.mp3"), ("MP4 File", "*.mp4"),
                                                             ("MPA File", "*.mpa"), ("MPEG File",
                                                                                     "*.mpeg"), ("OGG File", "*.ogg"),
                                                             ("QT File", "*.qt"), ("SD2 File",
                                                                                   "*.sd2"), ("SND File", "*.snd"),
                                                             ("W64 File", "*.w64"), ("WAV File",
                                                                                     "*.wav"), ("XHE File", "*.xhe"),
                                                             ("AAC File", "*.aac")))
        self.displayBox.delete("0.0", "200.0")
        self.displayBox.insert("0.0", filePath)

        be.load_song(filePath)
        be.get_spectrogram()
        self.spec = ctk.CTkImage(Image.open(be.spec), Image.open(
            be.spec), (be.num_frames//100, 1000))

    # def yPaddingSliderChange(self, position):
    #     y_padding = position * 1000
    #     print(y_padding)

    def changeAppearance(self, newOption):
        ctk.set_appearance_mode(newOption)


if __name__ == "__main__":
    app = App()
    # Used to run the application.
    app.mainloop()
