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

        # Name Label
        self.nameLabel = ctk.CTkLabel(self,
                                text="File Name:")
        self.nameLabel.grid(row=0, column=0,
                            padx=20, pady=20,
                            sticky="ew")
 
        # Name Entry Field
        self.fileEntry = ctk.CTkEntry(self,
                          placeholder_text="enter here")
        self.fileEntry.grid(row=1, column=0,
                            columnspan=3, padx=20,
                            pady=20, sticky="ew")
        
        # Generate Button
        self.generateFileButton = ctk.CTkButton(self,
                                         text="Upload File",
                                         command=self.generateResults)
        self.generateFileButton.grid(row=0, column=1,
                                        columnspan=2, padx=20,
                                        pady=20, sticky="ew")
 
        # Text Box
        self.displayBox = ctk.CTkTextbox(self,
                                         width=200,
                                         height=100)
        self.displayBox.grid(row=2, column=0,
                             columnspan=4, padx=20,
                             pady=20, sticky="nsew")
 
 
    # This function is used to insert the
    # details entered by users into the textbox
    def generateResults(self):
        self.displayBox.delete("0.0", "200.0")
        text = self.createText()
        self.displayBox.insert("0.0", text)
 
    # This function is used to get the selected
    # options and text from the available entry
    # fields and boxes and then generates
    # a prompt using them
    def createText(self):
        checkboxValue = ""
        # Constructing the text variable
        text = f"{self.fileEntry.get()}"
 
        return text
 
if __name__ == "__main__":
    app = App()
    # Used to run the application
    app.mainloop()
