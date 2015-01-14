package minesweeper;

import java.applet.*;
import java.awt.*;

import javax.swing.*;

//**************************SETTINGS FRAME****************************
class SettingsFrame extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Choice difficulty;
    Choice song;
    Button apply;
    Applet parent;

    SettingsFrame(String title, Applet p) {
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
	apply = new Button("Apply");
	add(apply);

    }

    public boolean action(Event evtObj, Object arg) {
	if (evtObj.target instanceof Choice) {
	    return true;
	}

	if (evtObj.target instanceof Button) {
	    ((MainAppletWindow) parent).setDifficulty(difficulty.getSelectedItem());
	    return true;
	}

	return false;
    }

}
