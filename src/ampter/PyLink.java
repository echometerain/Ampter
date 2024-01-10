/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ampter;

import jep.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.logging.*;

/**
 *
 * @author hhwl
 */
public class PyLink implements Runnable {

    static BufferedImage[] specs;
    static Queue<Object[]> q = new LinkedList<>(); // py command queue
    SharedInterpreter interp; // python interpreter
    int delta = 25; // frame pause

//    byte[] btDataFile = new sun.misc.BASE64Decoder().decodeBuffer(base64);
//BufferedImage image = ImageIO.read(new ByteArrayInputStream(btDataFile));
    public void call(Object[] args) {
        switch ((PyCalls) args[0]) {
            case METHOD:
                if (args.length == 2) {
                    interp.invoke((String) args[1]);
                } else {
                    interp.invoke((String) args[1], Arrays.copyOfRange(args, 2, args.length));
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
                Logger.getLogger(PyLink.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!q.isEmpty()) {
                call(q.poll());
            }
        }
    }

}
