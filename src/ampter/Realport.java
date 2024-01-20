/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ampter;

import java.awt.image.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author hhwl
 */
public class Realport extends javax.swing.JPanel implements Runnable {

	final int DELTA = 25; // frame pause
	final int SMOOTH_PADDING = 2;
	final double BRUSHSTROKE_FACTOR = 50.0;
	final int PLAYHEAD_STROKE_SIZE = 4;

	static Ampter parent; // for calling non-static functions
	boolean dragging = false; // if user is dragging
	int dragFromX = 0;
	int dragFromY = 0;
	boolean wasPlaying = false; // if audio was playing before
	long startTime = 0; // nanosecond time when playback started

	/**
	 * Creates new form Realport
	 *
	 * @param parent Dirty hack for calling non-static functions
	 */
	public Realport(Ampter parent) {
		initComponents();
		Realport.parent = parent;
	}

	// draw realport
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// quit if no audio
		if (!Ampter.isAudioLoaded()) {
			return;
		}
		// get needed variables
		int ppb = Ampter.getPpb();
		int num_bl = Ampter.getNum_bl();
		int leftBlock = Math.max(Ampter.getViewLeft() / ppb - SMOOTH_PADDING, 0); // -2 & +2 for smooth scrolling
		int rightBlock = Math.min(Ampter.getViewRight() / ppb + SMOOTH_PADDING, num_bl - 1);
		int viewHeight = Ampter.getViewHeight();
		BufferedImage[][] specs = Ampter.getSpecs();
		// swing needs me to do this for some reason
		Graphics2D g2 = (Graphics2D) g;

		// paint the specs block by block
		for (int i = leftBlock; i <= rightBlock; i++) {
			if (specs[0][i] != null && specs[1][i] != null) {
				// draw specs with opacity determined by slider level
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) parent.getLeftSliderLevel()));
				g.drawImage(specs[0][i], i * ppb, 0, ppb, viewHeight, this);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) parent.getRightSliderLevel()));
				g.drawImage(specs[1][i], i * ppb, 0, ppb, viewHeight, this);
			}
		}
		// reset alpha level
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

		// handle playing
		boolean playing = Ampter.isPlaying();
		int headPos = Ampter.getHeadPos();
		if (playing && !wasPlaying) {
			// jump to playhead when playback starts
			if (headPos < Ampter.viewLeft || headPos > Ampter.viewRight) {
				parent.gotoPos(headPos);
			}
			wasPlaying = true;
			startTime = System.nanoTime();
		}
		// reset when playhead stops
		if (!playing && wasPlaying) {
			wasPlaying = false;
			startTime = 0;
		}

		if (playing) {
			// calculate headPos in pixels (the cursed 10E-9 is for handling nanoseconds)
			headPos += (int) ((System.nanoTime() - startTime) / 1000000000.0 * Ampter.getBl_freq() * ppb);
			// make it so playhead is always visible
			if (headPos > Ampter.viewRight) {
				parent.gotoPos(Ampter.viewRight);
			}
		}

		// draw playhead
		g2.setStroke(new BasicStroke(PLAYHEAD_STROKE_SIZE));
		g.setColor(Color.white);
		g.drawLine(headPos, 0, headPos, viewHeight);

		// draw paintbrush
		Point pos = this.getMousePosition();
		if (dragging && pos != null) {
			// blue with half alpha
			g.setColor(new Color(0, 0, 255, 128));
			int stroke = (int) (Ampter.getViewHeight() / BRUSHSTROKE_FACTOR * parent.getSizeSliderLevel());
			Polygon p = new Polygon();
			p.addPoint(dragFromX, dragFromY - stroke);
			p.addPoint(dragFromX, dragFromY + stroke);
			p.addPoint(pos.x, pos.y + stroke);
			p.addPoint(pos.x, pos.y - stroke);
			g.fillPolygon(p);
		}
	}

	@Override
	public void run() {
		// make tooltip never go away
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		while (true) {
			// redraw every frame
			repaint();
			try {
				// frame pause
				Thread.sleep(DELTA);
			} catch (InterruptedException ex) {
				// netbeans wanted me to do this
				Logger.getLogger(Realport.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(0, 0, 0));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

	// handles painting the brushstroke
    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
		if (!dragging) {
			Point pos = this.getMousePosition();
			dragFromX = pos.x;
			dragFromY = pos.y;
			dragging = true;
		}
    }//GEN-LAST:event_formMouseDragged

	// make the frequency tooltip
    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
		if (!Ampter.isAudioLoaded()) {
			return;
		}
		// write tooltip text
		Point pos = this.getMousePosition();
		if (pos != null) {
			this.setToolTipText(Ampter.freqInfo(Ampter.pixToFreq(pos.y)));
		}
    }//GEN-LAST:event_formMouseMoved

	// end of brushstroke handler
    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
		Point pos = this.getMousePosition();
		if (dragging && pos != null) {
			// call python paint
			if (pos.x < dragFromX) {
				PyLink.q.add(new Object[]{PyCalls.PAINT, pos.x, pos.y, dragFromX, dragFromY});
			} else {
				PyLink.q.add(new Object[]{PyCalls.PAINT, dragFromX, dragFromY, pos.x, pos.y});
			}
			dragging = false;
			dragFromX = 0;
			dragFromY = 0;
		}
    }//GEN-LAST:event_formMouseReleased

	// put playhead at mouse click position
    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
		Point pos = this.getMousePosition();
		if (pos != null) {
			Ampter.setHeadPos(pos.x);
		}
    }//GEN-LAST:event_formMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
