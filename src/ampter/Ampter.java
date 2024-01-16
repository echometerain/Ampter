/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ampter;

import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;

/**
 *
 * @author hhwl
 */
public class Ampter extends javax.swing.JFrame {

	// notenames for tooltip
	static String[] noteNames = {"A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab"};
	static stockPlugin[] stocks = new stockPlugin[12];
	static BufferedImage[][] specs; // spectrograms array [channel][block]
	static boolean playing = false;
	static boolean audioLoaded = false;
	static boolean ef_selected = false; // effect selected
	static boolean fullCircle = false; // if all the spectrograms have been calculated
	static int headPos = 0; // playhead position (in pixels)
	static int bl_size = 0; // block size (in audio frames)
	static int bl_freq = 0; // blocks per second
	static int sample_rate = 0;
	static int num_frames = 0;
	static int num_bl = 0; // total number of blocks
	Realport realport1 = new Realport(this);
	JViewport vPort; // viewable area of scrollpane (viewport1)
	static int viewLeft = 0; // scrollpane left
	static int viewRight = 0; // scrollpane right
	static int realWidth = 0; // actual width of the edit region (realport1)
	static int viewHeight = 0; // scrollpane height
	static int ppb = 100; // pixels per block

	// getter setters spam
	public double getLeftSliderLevel() {
		// slider max is 100
		return leftSlider.getValue() / 100.0;
	}

	public double getRightSliderLevel() {
		return rightSlider.getValue() / 100.0;
	}

	public static boolean isFullCircle() {
		return fullCircle;
	}

	public static void setFullCircle(boolean fullCircle) {
		Ampter.fullCircle = fullCircle;
	}

	public static int getPpb() {
		return ppb;
	}

	public static void setAudioLoaded(boolean audioLoaded) {
		Ampter.audioLoaded = audioLoaded;
	}

	public static boolean isAudioLoaded() {
		return audioLoaded;
	}

	public static int getViewLeft() {
		return viewLeft;
	}

	public static int getViewRight() {
		return viewRight;
	}

	public static int getViewHeight() {
		return viewHeight;
	}

	public static void setSpecs(BufferedImage[][] specs) {
		Ampter.specs = specs;
	}

	public static int getHeadPos() {
		return headPos;
	}

	public static void setHeadPos(int headPos) {
		Ampter.headPos = headPos;
	}

	public static int getBl_size() {
		return bl_size;
	}

	public static void setBl_size(int bl_size) {
		Ampter.bl_size = bl_size;
	}

	public static int getSample_rate() {
		return sample_rate;
	}

	public static void setSample_rate(int sample_rate) {
		Ampter.sample_rate = sample_rate;
	}

	public static int getNum_frames() {
		return num_frames;
	}

	public static void setNum_frames(int num_frames) {
		Ampter.num_frames = num_frames;
	}

	public static int getNum_bl() {
		return num_bl;
	}

	public static void setNum_bl(int num_bl) {
		Ampter.num_bl = num_bl;
	}

	public static boolean getPlaying() {
		return playing;
	}

	public static BufferedImage[][] getSpecs() {
		return specs;
	}

	public static boolean isPlaying() {
		return playing;
	}

	public static void setPlaying(boolean playing) {
		Ampter.playing = playing;
	}

	public static int getBl_freq() {
		return bl_freq;
	}

	public static void setBl_freq(int bl_freq) {
		Ampter.bl_freq = bl_freq;
	}

	public double getSizeSliderLevel() {
		return sizeSlider.getValue() / 10.0;
	}

	public static boolean isEf_selected() {
		return ef_selected;
	}

	// get frequency info for tooltip
	public static String freqInfo(double freq) {
		double A4 = 440; // standard western tuning
		double position = Math.log(freq / A4) / Math.log(2) + 4; // frequency to pitch scale
		int octave = (int) Math.floor(position); // octave number starting on A
		int octaveC = (int) Math.floor(position - 0.25); // starting on C (A->C is 1/4 of an octave)
		double note = 12 * (position - octave); // decimal note value
		if (note >= 2.5 && note < 3.0) { // prevents having both C3 & C4
			octaveC++;
		}

		int roundNote = (int) Math.round(note); // rounded note value
		double cents = Math.round((note - roundNote) * 100); // get cents
		String centString = cents >= 0 ? " +" + cents : " " + cents;

		return "" + Math.round(freq) + "Hz\n" + noteNames[roundNote % 12] + octaveC + centString + " cents";
	}

	// convert pixel coordinates to frequency
	public static double pixToFreq(int pix) {
		// mel scale position of the top of the window
		double nyquest_mel = 2595.0 * Math.log10(1.0 + (sample_rate / 2.0) / 700.0);
		double loc_mel = (viewHeight - pix) / ((double) viewHeight) * nyquest_mel;
		// inverse mel formula https://en.wikipedia.org/wiki/Mel_scale
		return 700.0 * (Math.pow(10.0, loc_mel / 2595.0) - 1.0);
	}

	/**
	 * Creates new form Ampter
	 */
	// create thread for jep (jep python can't multithread)
	Thread pyThread = new Thread(new PyLink(this));
	Thread realThread; // create thread for realport

	public Ampter() {
		// prevents weird locale error
		Locale.setDefault(new Locale("en", "US"));
		// set modern look
		try {
			UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
		// start python
		pyThread.start();
		// set ampter logo
		this.setIconImage(new ImageIcon("./assets/ampterIcon.png").getImage());
		initComponents();

		vPort = viewport1.getViewport();
		// netbeans wants me to write it like this
		vPort.addChangeListener(this::viewportChangePerformed);
		// adding realport manually cause the gui builder crashed lol
		viewport1.add(realport1);
		viewport1.setViewportView(realport1);
		realThread = new Thread(realport1);
		realThread.start();

		// not in the gui builder for some reason
		stockList.addListSelectionListener(this::listValueChanged);
		// write every stock plugin into the array
		stockPlugin.init(stocks);
	}

	// get new positions for viewable region when dimensions change
	public void viewportChangePerformed(ChangeEvent evt) {
		Rectangle rect = vPort.getViewRect();
		viewLeft = rect.x;
		viewRight = viewLeft + rect.width;
		realWidth = num_bl * ppb;
		viewHeight = rect.height;
		realport1.setPreferredSize(new Dimension(realWidth, viewHeight));
		viewport1.revalidate();
//        System.out.println("" + viewLeft + " " + viewRight + " " + realWidth + " " + viewHeight);
	}

	// select stock effect
	public void listValueChanged(ListSelectionEvent evt) {
		// choosing vst sets index to -1
		if (stockList.getSelectedIndex() == -1 || evt.getValueIsAdjusting() == true) {
			return;
		}
		VSTUIButton.setEnabled(false);
		int selected = stockList.getSelectedIndex();
//		System.out.println(selected);
		stocks[selected].buildPanel(stockOptions);
		stocks[selected].givePy();
		ef_selected = true;

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        chooseFiles = new javax.swing.JFileChooser();
        leftPanel = new javax.swing.JPanel();
        loadAudio = new javax.swing.JButton();
        effectsLabel = new javax.swing.JLabel();
        chooseStock = new javax.swing.JScrollPane();
        stockList = new javax.swing.JList<>();
        stockScroll = new javax.swing.JScrollPane();
        stockOptions = new javax.swing.JPanel();
        loadVSTButton = new javax.swing.JButton();
        VSTUIButton = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        optionsLabel = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();
        sizeSlider = new javax.swing.JSlider();
        leftLabel = new javax.swing.JLabel();
        leftSlider = new javax.swing.JSlider();
        rightLabel = new javax.swing.JLabel();
        rightSlider = new javax.swing.JSlider();
        shiftBack = new javax.swing.JButton();
        playPause = new javax.swing.JButton();
        shiftForward = new javax.swing.JButton();
        stopReset = new javax.swing.JButton();
        saveAudio = new javax.swing.JButton();
        viewport1 = new javax.swing.JScrollPane();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Ampter");
        setBackground(new java.awt.Color(0, 0, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setPreferredSize(new java.awt.Dimension(1000, 800));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        leftPanel.setBackground(new java.awt.Color(153, 153, 153));
        leftPanel.setPreferredSize(new java.awt.Dimension(225, 700));

        loadAudio.setBackground(new java.awt.Color(0, 102, 153));
        loadAudio.setText("Load Audio");
        loadAudio.setToolTipText("");
        loadAudio.setFocusable(false);
        loadAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadAudioActionPerformed(evt);
            }
        });

        effectsLabel.setForeground(new java.awt.Color(0, 0, 0));
        effectsLabel.setText("Effects");

        chooseStock.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chooseStock.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        stockList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Gain", "Bitcrush", "Chorus", "Clipping", "Compressor", "MP3Compressor", "Delay", "Distortion", "Reverb", "PitchShift", "Phaser", "NoiseGate" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        stockList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        stockList.setEnabled(false);
        stockList.setFocusable(false);
        chooseStock.setViewportView(stockList);

        stockScroll.setPreferredSize(new java.awt.Dimension(175, 175));

        stockOptions.setMaximumSize(new java.awt.Dimension(175, 32767));
        stockOptions.setMinimumSize(new java.awt.Dimension(175, 10));
        stockOptions.setPreferredSize(new java.awt.Dimension(150, 100));
        stockOptions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        stockScroll.setViewportView(stockOptions);

        loadVSTButton.setText("Load VST");
        loadVSTButton.setEnabled(false);
        loadVSTButton.setFocusable(false);
        loadVSTButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadVSTButtonActionPerformed(evt);
            }
        });

        VSTUIButton.setText("Open VST UI");
        VSTUIButton.setEnabled(false);
        VSTUIButton.setFocusable(false);
        VSTUIButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VSTUIButtonActionPerformed(evt);
            }
        });

        optionsLabel.setForeground(new java.awt.Color(0, 0, 0));
        optionsLabel.setText("Options");

        sizeLabel.setForeground(new java.awt.Color(0, 0, 0));
        sizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        sizeLabel.setText("Size");

        sizeSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        sizeSlider.setEnabled(false);
        sizeSlider.setFocusable(false);

        leftLabel.setForeground(new java.awt.Color(0, 0, 0));
        leftLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        leftLabel.setText("L");

        leftSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        leftSlider.setValue(100);
        leftSlider.setEnabled(false);
        leftSlider.setFocusable(false);

        rightLabel.setForeground(new java.awt.Color(0, 0, 0));
        rightLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rightLabel.setText("R");

        rightSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        rightSlider.setValue(100);
        rightSlider.setEnabled(false);
        rightSlider.setFocusable(false);

        shiftBack.setText("<<");
        shiftBack.setEnabled(false);
        shiftBack.setFocusable(false);
        shiftBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        shiftBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shiftBackActionPerformed(evt);
            }
        });

        playPause.setText(">||");
        playPause.setEnabled(false);
        playPause.setFocusable(false);
        playPause.setMargin(new java.awt.Insets(0, 0, 0, 0));
        playPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playPauseActionPerformed(evt);
            }
        });

        shiftForward.setEnabled(false);
        shiftForward.setFocusable(false);
        shiftForward.setLabel(">>");
        shiftForward.setMargin(new java.awt.Insets(0, 0, 0, 0));
        shiftForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shiftForwardActionPerformed(evt);
            }
        });

        stopReset.setText("X");
        stopReset.setEnabled(false);
        stopReset.setFocusable(false);
        stopReset.setMargin(new java.awt.Insets(0, 0, 0, 0));
        stopReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopResetActionPerformed(evt);
            }
        });

        saveAudio.setBackground(new java.awt.Color(0, 102, 153));
        saveAudio.setText("Save Audio");
        saveAudio.setToolTipText("");
        saveAudio.setEnabled(false);
        saveAudio.setFocusable(false);
        saveAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAudioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveAudio)
                    .addComponent(loadAudio)
                    .addComponent(optionsLabel)
                    .addComponent(effectsLabel)
                    .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(leftPanelLayout.createSequentialGroup()
                            .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(sizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(sizeLabel))
                            .addGap(37, 37, 37)
                            .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(leftSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(leftLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(rightSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(rightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(loadVSTButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(VSTUIButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(leftPanelLayout.createSequentialGroup()
                        .addComponent(shiftBack, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(playPause, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(shiftForward, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopReset, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chooseStock, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stockScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(loadAudio)
                .addGap(11, 11, 11)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playPause, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shiftForward, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopReset, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shiftBack, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(effectsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chooseStock, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stockScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(loadVSTButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(VSTUIButton)
                .addGap(18, 18, 18)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sizeLabel)
                    .addComponent(leftLabel)
                    .addComponent(rightLabel))
                .addGap(9, 9, 9)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(leftSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(rightSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(sizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveAudio)
                .addContainerGap(53, Short.MAX_VALUE))
        );

        getContentPane().add(leftPanel, java.awt.BorderLayout.WEST);

        viewport1.setBackground(new java.awt.Color(0, 0, 0));
        viewport1.setPreferredSize(new java.awt.Dimension(1000, 750));
        viewport1.setViewportView(null);
        getContentPane().add(viewport1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadVSTButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadVSTButtonActionPerformed
		// give vst file to python
		chooseFiles.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = chooseFiles.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		PyLink.q.add(new Object[]{PyCalls.METHOD, "set_vst_effect", chooseFiles.getSelectedFile().getPath()});

		VSTUIButton.setEnabled(true);
		// disable stock effect
		stockList.setSelectedIndex(-1);
		stockList.setSelectedValue(null, false);
		stockList.validate();
		ef_selected = true;
    }//GEN-LAST:event_loadVSTButtonActionPerformed

    private void VSTUIButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VSTUIButtonActionPerformed
		// open vst ui
		PyLink.q.add(new Object[]{PyCalls.METHOD, "open_vst_ui"});
    }//GEN-LAST:event_VSTUIButtonActionPerformed

    private void loadAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadAudioActionPerformed
		// give audio file to python
		chooseFiles.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = chooseFiles.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		PyLink.q.add(new Object[]{PyCalls.LOAD_AUDIO, chooseFiles.getSelectedFile().getPath(), this});

		// enable sliders and buttons
		stockList.setEnabled(true);
		loadVSTButton.setEnabled(true);
		sizeSlider.setEnabled(true);
		leftSlider.setEnabled(true);
		rightSlider.setEnabled(true);
		shiftBack.setEnabled(true);
		playPause.setEnabled(true);
		shiftForward.setEnabled(true);
		stopReset.setEnabled(true);
		saveAudio.setEnabled(true);
    }//GEN-LAST:event_loadAudioActionPerformed

    private void playPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playPauseActionPerformed
		// play and pause
		if (playing) {
			playing = false;
		} else {
			playing = true;
			PyLink.q.add(new Object[]{PyCalls.METHOD, "play", (int) (((double) headPos) / ppb * bl_size)});
		}
    }//GEN-LAST:event_playPauseActionPerformed

    private void shiftForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shiftForwardActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_shiftForwardActionPerformed

    private void stopResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopResetActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_stopResetActionPerformed

    private void shiftBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shiftBackActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_shiftBackActionPerformed

    private void saveAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAudioActionPerformed
		// give save file location to python
		int returnVal = chooseFiles.showSaveDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		PyLink.q.add(new Object[]{PyCalls.METHOD, "save_song", chooseFiles.getSelectedFile().toString()});
    }//GEN-LAST:event_saveAudioActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
		// play audio with space
		switch (evt.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				// hacky but works
				playPauseActionPerformed(null);
				break;
		}
    }//GEN-LAST:event_formKeyPressed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(() -> {
			new Ampter().setVisible(true);
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton VSTUIButton;
    private javax.swing.JFileChooser chooseFiles;
    private javax.swing.JScrollPane chooseStock;
    private javax.swing.JLabel effectsLabel;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JSlider leftSlider;
    private javax.swing.JButton loadAudio;
    private javax.swing.JButton loadVSTButton;
    private javax.swing.JLabel optionsLabel;
    private javax.swing.JButton playPause;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JSlider rightSlider;
    private javax.swing.JButton saveAudio;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton shiftBack;
    private javax.swing.JButton shiftForward;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JSlider sizeSlider;
    private javax.swing.JList<String> stockList;
    private javax.swing.JPanel stockOptions;
    private javax.swing.JScrollPane stockScroll;
    private javax.swing.JButton stopReset;
    private javax.swing.JScrollPane viewport1;
    // End of variables declaration//GEN-END:variables
}
