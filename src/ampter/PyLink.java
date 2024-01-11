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
public class PyLink implements Runnable {

    static Queue<Object[]> q = new LinkedList<>(); // py command queue
    SharedInterpreter interp; // python interpreter
    int delta = 25; // backend processing latency

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
                try {
                    byte[] data;
                    for (int i = 0; i < Ampter.getNum_bl() - 1; i++) {
                        data = (byte[]) interp.invoke("calc_spec", i, 0);
                        Ampter.specs[0][i] = ImageIO.read(new ByteArrayInputStream(data));
                        data = (byte[]) interp.invoke("calc_spec", i, 1);
                        Ampter.specs[1][i] = ImageIO.read(new ByteArrayInputStream(data));
                        System.out.println("here!!!");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
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
            }
        }
    }

}
