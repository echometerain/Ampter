import numpy as np
import scipy
import pedalboard as pb
import backend as be
import frontend as fe

be.load_song("sine.mp3")

be.load_brush("/usr/lib/vst3/Auburn Sounds Panagement 2.vst3")
# be.isolate()
be.play()
# be.get_spectrogram()
# be.show_spec()


# app = fe.App()
# app.mainloop()
