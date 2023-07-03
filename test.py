import numpy as np
import scipy
import pedalboard as pb
import backend as be
import ampter as fe
from multiprocessing import Process
import time
from PIL import Image

# be.load_song("sewersample.mp3")
# be.get_spectrogram()
# img = Image.open(be.spec)
# img.show()

be.load_brush("/usr/lib/vst3/Auburn Sounds Panagement 2.vst3")
# be.isolate()
# be.get_spectrogram()
# be.show_spec()

# player = Process(target=be.play)
# player.start()
# time.sleep(5)
# player.terminate()
