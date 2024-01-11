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

    static Queue<Object[]> q = new LinkedList<>(); // py command queue
    static SharedInterpreter interp; // python interpreter
    static int delta = 25; // backend processing latency
    static int pointer = 0; // pointer which draws spectrogram blocks
    int lastViewLeft = 0;

    // go to nearest unloaded block and calculate its spectrogram
    public void writeSpec() {
        // adjust write pointer when viewport is scrolled left
        int vLeft = Ampter.getViewLeft();
        if (vLeft < lastViewLeft) {
            pointer = vLeft / Ampter.ppb;
        }
        // skip written blocks
        while (Ampter.specs[0][pointer] != null) {
            pointer++;
            // loop around if at end
            if (pointer >= Ampter.num_bl) {
                pointer = 0;
            }
        }
        // get spec data from python
        byte[] data;
        try {
            data = (byte[]) interp.invoke("calc_spec", pointer, 0);
            Ampter.specs[0][pointer] = ImageIO.read(new ByteArrayInputStream(data));
            data = (byte[]) interp.invoke("calc_spec", pointer, 1);
            Ampter.specs[1][pointer] = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void call(Object[] args) {
        switch ((PyCalls) args[0]) {
            case METHOD:
                if (args.length == 2) {
                    interp.invoke((String) args[1]);
                } else {
                    interp.invoke((String) args[1], Arrays.copyOfRange(args, 2, args.length));
                }
                break;
            case LOAD_AUDIO:
                interp.invoke("set_song", args[1]);
                Ampter.setSpecs(new BufferedImage[2][Ampter.getSample_rate()]);
                Ampter.setAudioLoaded(true);
                System.out.println(Ampter.getNum_bl() * Ampter.getPpb());
                ((Realport) args[2]).setSize(Ampter.getNum_bl() * Ampter.getPpb(), (int) (args[3]));
                System.out.println(((Realport) args[2]).getSize());

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
        interp.runScript("./backend.py");

        while (true) {
            // sleep for delta ms if nothing to do
            try {
                Thread.sleep(delta);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Logger.getLogger(PyLink.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!q.isEmpty()) {
                call(q.poll());
            } else {
                if (Ampter.isAudioLoaded()) {
                    writeSpec();
                }
            }
        }
    }

}
