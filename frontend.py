#import ampter as amp
import numpy as np
import scipy
import pedalboard as pb
import tkinter as tk
import customtkinter as ctk

song_path = None  # path of the song
brush_path = None  # path of the VST plugin
Y_PADDING = 500  # how thick the brush is (y direction)

#comments are to help me remember whats what lol


ctk.set_appearance_mode("System")       
ctk.set_default_color_theme("blue")   
class App(ctk.CTk):
# Layout of the GUI will be written in the init itself
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
# Sets the title of window 
        self.title("Ampter - Audio Mastering Painter")   
# Dimensions of the window will be 200x200
        self.geometry("2500x2000")   #it had to be at least 2000 pixels tall right?

        self.nameLabel = ctk.CTkLabel(self,
                                text="Upload A File")
        self.nameLabel.grid(row=0, column=0,
                            padx=20, pady=10,
                            sticky="ew")
        

if __name__ == "__main__":
    app = App()
    # Runs the app
    app.mainloop()  