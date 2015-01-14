package minesweeper;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

//**************************SETTINGS FRAME****************************
class SettingsFrame extends JFrame implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Choice difficulty;
    private Choice song;
    private Button applyButton, hintButton;
    private MainAppletWindow parent;

    SettingsFrame(String title, MainAppletWindow p) {
	super(title);
	parent = p;

	setLayout(new FlowLayout(FlowLayout.LEFT));

	// adding choices to the difficulty list
	difficulty = new Choice();
	difficulty.addItem("Easy");
	difficulty.addItem("Medium");
	difficulty.addItem("Hard");
	add(difficulty);

	// adding choices to the song selection
	song = new Choice();
	song.addItem("Defusing");
	song.addItem("Epic");
	song.addItem("Distracting");
	add(song);

	// add button to apply settings changes
	applyButton = new Button("Apply");
	applyButton.addActionListener(this);
	add(applyButton);

	// add hint button
	hintButton = new Button("Hint");
	hintButton.addActionListener(this);
	add(hintButton);

	this.setBounds(0, 0, 1000, 500);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == applyButton) {
	    parent.setDifficulty(difficulty.getSelectedItem());
	}

	if (e.getSource() == hintButton) {
	    parent.hint();
	}
    }

    // public boolean action(Event evtObj, Object arg) {
    // if (evtObj.target instanceof Choice) {
    // return true;
    // }
    //
    // if (evtObj.target instanceof Button) {
    // return true;
    // }
    //
    // return false;
    // }

}
