import numpy as np
import pedalboard as pb
import backend as be
from multiprocessing import Process
import time
from PIL import Image

be.load_song("sewersample.mp3")
be.pb_effect("Gain")
be.paint(500, 1000, 50000, 10000, 3)
be.save_song("ssam-test.mp3")

# be.load_brush("/usr/lib/vst3/Auburn Sounds Panagement 2.vst3")
# be.isolate()
# be.get_spectrogram()
# be.show_spec()

# player = Process(target=be.play)
# player.start()
# time.sleep(5)
# player.terminate()
