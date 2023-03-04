import numpy as np
import scipy
import pedalboard as pb
import backend as be
import frontend as fe
from multiprocessing import Process
import time

be.load_song("sine.mp3")

be.load_brush("/usr/lib/vst3/Auburn Sounds Panagement 2.vst3")
# be.isolate()
# be.get_spectrogram()
# be.show_spec()

player = Process(target=be.play)
player.start()
time.sleep(5)
player.terminate()
# app = fe.App()
# app.mainloop()
