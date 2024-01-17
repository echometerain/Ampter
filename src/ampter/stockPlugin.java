/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ampter;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JSpinner.*;
import java.awt.event.*;

/**
 *
 * @author hhwl
 */
public class stockPlugin {

	int index;
	Ampter source;
	HashMap<String, double[]> settings = new HashMap<>(); // [min, value, max]

	public stockPlugin(int index, Ampter source) {
		this.index = index;
		this.source = source;
	}

	public void changeVal(String st, int val) {
		settings.get(st)[1] = val;
	}

	public void setSettings(HashMap<String, double[]> settings) {
		this.settings = settings;
	}

	public HashMap<String, double[]> getSettings() {
		return settings;
	}

	public void givePy() {
		HashMap<String, Object> temp = new HashMap<>();
		for (String e : settings.keySet()) {
			temp.put(e, settings.get(e)[1]);
		}
		PyLink.q.add(new Object[]{PyCalls.METHOD, "set_stock_effect", index, temp});
	}

	public void buildPanel(JPanel p) {
		p.removeAll();
		for (String e : settings.keySet()) {
			JLabel name = new javax.swing.JLabel();
			name.setMaximumSize(new java.awt.Dimension(80, 20));
			name.setMinimumSize(new java.awt.Dimension(80, 20));
			name.setPreferredSize(new java.awt.Dimension(80, 20));
			name.setText(e);
			name.setToolTipText(e);
			p.add(name);

			JSpinner chooser = new javax.swing.JSpinner();
			double[] arr = settings.get(e);
			SpinnerNumberModel model = new SpinnerNumberModel(arr[1], arr[0], arr[2], 0.5);
			chooser.setModel(model);
			chooser.setEditor(new CustomNumberEditor(chooser, "0.0"));
			chooser.setMaximumSize(new java.awt.Dimension(80, 20));
			chooser.setMinimumSize(new java.awt.Dimension(80, 20));
			chooser.setPreferredSize(new java.awt.Dimension(80, 20));
			chooser.addChangeListener(new valueChange(this, e));
			chooser.setFocusable(false);
			p.add(chooser);
		}
		p.revalidate();
		p.repaint();
	}

	// based on https://stackoverflow.com/questions/15277349/jspinner-does-not-transfer-focus-when-pressing-enter
	class CustomNumberEditor extends NumberEditor implements KeyListener {

		private final JFormattedTextField textField;

		public CustomNumberEditor(JSpinner spinner, String formatString) {
			super(spinner, formatString);
			textField = getTextField();
			textField.addKeyListener(this);
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					source.requestFocus();
					break;
				case KeyEvent.VK_SPACE:
					source.requestFocus();
					source.playPauseHandle();
					break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

	public class valueChange implements ChangeListener {

		stockPlugin stock;
		String settingName;

		public valueChange(stockPlugin stock, String settingName) {
			this.stock = stock;
			this.settingName = settingName;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			double[] arr = stock.getSettings().get(settingName);
			arr[1] = (double) (((JSpinner) e.getSource()).getValue());
			stock.givePy();
		}
	}

	public static void init(stockPlugin[] stocks, Ampter source) {
		double dMax = Double.MAX_VALUE;
		double dMin = -Double.MAX_VALUE;
		for (int i = 0; i < 12; i++) {
			stocks[i] = new stockPlugin(i, source);
		}

		// write [min, value, max] according to https://spotify.github.io/pedalboard/reference/pedalboard.html
		HashMap<String, double[]> gain = new HashMap<>();
		gain.put("gain_db", new double[]{0, 1, dMax});
		HashMap<String, double[]> bitcrush = new HashMap<>();
		bitcrush.put("bit_depth", new double[]{0, 1, 32});
		HashMap<String, double[]> chorus = new HashMap<>();
		chorus.put("rate_hz", new double[]{0, 1, 100});
		chorus.put("depth", new double[]{0, 0.25, 1});
		chorus.put("centre_delay_ms", new double[]{0, 1, dMax});
		chorus.put("feedback", new double[]{0, 0, 1});
		chorus.put("mix", new double[]{0, 0.5, 1});
		HashMap<String, double[]> clipping = new HashMap<>();
		clipping.put("threshold_db", new double[]{dMin, -6, dMax});
		HashMap<String, double[]> compressor = new HashMap<>();
		compressor.put("threshold_db", new double[]{dMin, -6, dMax});
		compressor.put("ratio", new double[]{dMin, 1, dMax});
		compressor.put("attack_ms", new double[]{dMin, 1, dMax});
		compressor.put("release_ms", new double[]{dMin, 100, dMax});
		HashMap<String, double[]> MP3Compressor = new HashMap<>();
		MP3Compressor.put("vbr_quality", new double[]{0, 2, 10});
		HashMap<String, double[]> delay = new HashMap<>();
		delay.put("delay_seconds", new double[]{dMin, 0.5, dMax});
		delay.put("feedback", new double[]{0, 0, 1});
		delay.put("mix", new double[]{0, 0.5, 1});
		HashMap<String, double[]> distortion = new HashMap<>();
		distortion.put("drive_db", new double[]{dMin, 25, dMax});
		HashMap<String, double[]> reverb = new HashMap<>();
		reverb.put("room_size", new double[]{0, 0.5, 1});
		reverb.put("damping", new double[]{0, 0.5, 1});
		reverb.put("wet_level", new double[]{0, 0.33, 1});
		reverb.put("dry_level", new double[]{0, 0.4, 1});
		reverb.put("width", new double[]{0, 1, 1});
		reverb.put("freeze_mode", new double[]{0, 0, 1});
		HashMap<String, double[]> pitchShift = new HashMap<>();
		pitchShift.put("semitones", new double[]{dMin, 0, dMax});
		HashMap<String, double[]> phaser = new HashMap<>();
		phaser.put("rate_hz", new double[]{dMin, 1, dMax});
		phaser.put("depth", new double[]{0, 0.5, 1});
		phaser.put("centre_frequency_hz", new double[]{0, 1300, dMax}); // shouldn't be above nyquest but who cares
		phaser.put("feedback", new double[]{0, 0, 1});
		phaser.put("mix", new double[]{0, 0.5, 1});
		HashMap<String, double[]> noiseGate = new HashMap<>();
		noiseGate.put("threshold_db", new double[]{dMin, -100, dMax});
		noiseGate.put("ratio", new double[]{0, 10, dMax});
		noiseGate.put("attack_ms", new double[]{dMin, 1, dMax});
		noiseGate.put("release_ms", new double[]{dMin, 100, dMax});

		// no enums cause I'm lazy
		stocks[0].setSettings(gain);
		stocks[1].setSettings(bitcrush);
		stocks[2].setSettings(chorus);
		stocks[3].setSettings(clipping);
		stocks[4].setSettings(compressor);
		stocks[5].setSettings(MP3Compressor);
		stocks[6].setSettings(delay);
		stocks[7].setSettings(distortion);
		stocks[8].setSettings(reverb);
		stocks[9].setSettings(pitchShift);
		stocks[10].setSettings(phaser);
		stocks[11].setSettings(noiseGate);

	}
}
