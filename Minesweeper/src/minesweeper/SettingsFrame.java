package minesweeper;

import java.applet.*;
import java.awt.*;

//**************************SETTINGS FRAME****************************
class SettingsFrame extends Frame {
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

    // hide window when terminated by user
    public boolean handleEvent(Event evtObj) {
	if (evtObj.id == Event.WINDOW_DESTROY) {
	    setVisible(false);
	    return true;
	}
	return super.handleEvent(evtObj);
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
