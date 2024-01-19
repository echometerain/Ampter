/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ampter;

import jep.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;
import java.util.logging.*;

/**
 *
 * @author hhwl
 */
// python interface singleton
public class PyLink implements Runnable {

	static final int DELTA = 25; // backend processing latency
	static final Queue<Object[]> q = new LinkedList<>(); // py command queue

	static SharedInterpreter interp; // python interpreter
	static int pointer = 0; // pointer which draws spectrogram blocks
	static Ampter parent;

	public PyLink(Ampter parent) {
		this.parent = parent;
	}

	// go to nearest unloaded block and calculate its spectrogram
	public void writeSpec() {
		if (Ampter.isFullCircle()) {
			return;
		}
		pointer = Ampter.getViewLeft() / Ampter.ppb;
		int origin = pointer;
		BufferedImage[][] specs = Ampter.getSpecs();
		int num_bl = Ampter.getNum_bl();

		// horribly inefficient but works
		// writes at the first block without a spectrogram
		while (specs[0][pointer] != null) {
			pointer = (pointer + 1) % num_bl;
			// check if pointer has gone full circle
			if (pointer == origin) {
				Ampter.setFullCircle(true);
				return;
			}
		}
		// get spec data from python
		byte[] data;
		try {
			// reads python image bytes into java image
			// thank god this works
			data = (byte[]) interp.invoke("calc_spec", pointer, 0);
			specs[0][pointer] = ImageIO.read(new ByteArrayInputStream(data));
			data = (byte[]) interp.invoke("calc_spec", pointer, 1);
			specs[1][pointer] = ImageIO.read(new ByteArrayInputStream(data));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// get python to do stuff
	public void call(Object[] args) {
		switch ((PyCalls) args[0]) {
			case METHOD:
				if (args.length == 2) {
					interp.invoke((String) args[1]);
				} else {
					// pass arguments to python functions
					interp.invoke((String) args[1], Arrays.copyOfRange(args, 2, args.length));
				}
				break;
			case LOAD_AUDIO:
				interp.invoke("set_song", args[1]);
				// re-create spectrogram array
				Ampter.setSpecs(new BufferedImage[2][Ampter.getNum_bl()]);
				Ampter.setAudioLoaded(true);
				// redraw realport
				((Ampter) args[2]).viewportChangePerformed(null);
				break;
			case PAINT:
				// do nothing if no effect selected
				if (!Ampter.isEf_selected()) {
					break;
				}
				// set up variables
				int x1 = (int) args[1];
				int y1 = (int) args[2];
				int x2 = (int) args[3];
				int y2 = (int) args[4];
				BufferedImage[][] specs = Ampter.getSpecs();

				// get location in frequencies and audio frames
				int x1Real = x1 / Ampter.ppb * Ampter.bl_size;
				double y1Real = Ampter.pixToFreq(y1);
				int x2Real = x2 / Ampter.ppb * Ampter.bl_size;
				double y2Real = Ampter.pixToFreq(y2);

				// using 11.0 (getSizeSliderLevel() <= 10) here to prevent division by zero
				// sorry for the magic number I have no idea how to name it
				double Q = 11.0 - parent.getSizeSliderLevel();

				// tell python to paint
				interp.invoke("paint", new Object[]{x1Real, y1Real, x2Real, y2Real, Q, 0, parent.getLeftSliderLevel()});
				interp.invoke("paint", new Object[]{x1Real, y1Real, x2Real, y2Real, Q, 1, parent.getRightSliderLevel()});

				// there are still more specs to draw!!!
				Ampter.setFullCircle(false);
				// remove outdated specs on painted area
				for (int i = x1 / Ampter.ppb; i <= x2 / Ampter.ppb; i++) {
					specs[0][i] = null;
					specs[1][i] = null;
				}
				break;
		}
	}

	@Override
	public void run() {
		// give python print to system.out
		jep.JepConfig conf = new JepConfig();
		conf.redirectStdErr(System.err);
		conf.redirectStdout(System.out);
		SharedInterpreter.setConfig(conf);
		// connect to backend
		interp = new SharedInterpreter();
		interp.runScript(Ampter.realLoc("backend.py"));

		while (true) {
			// sleep for delta ms
			try {
				Thread.sleep(DELTA);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				Logger.getLogger(PyLink.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (!q.isEmpty()) {
				// call queue if needed
				call(q.poll());
			} else {
				// draw specs if nothing to do
				if (Ampter.isAudioLoaded()) {
					writeSpec();
				}
			}
		}
	}

}
