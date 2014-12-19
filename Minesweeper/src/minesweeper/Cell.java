//*****************************Board Class****************************
package minesweeper;

import java.awt.Image;

public class Cell {
    private boolean mined;
    private boolean hidden;
    private boolean flagged;
    private int minesClose;
    private Image img;

    public boolean getMined() {
	return mined;
    }

    public boolean mined() {
	return mined;
    }

    public void setMined(boolean flag) {
	mined = flag;
    }

    public boolean getHidden() {
	return hidden;
    }

    public boolean hidden() {
	return hidden;
    }

    public void setHidden(boolean flag) {
	hidden = flag;
    }

    public void setFlagged(boolean flag) {
	flagged = flag;
    }

    public boolean flagged() {
	return flagged;
    }

    public boolean getFlagged() {
	return flagged;
    }

    public int getMinesClose() {
	return minesClose;
    }

    public void setMinesClose(int num) {
	minesClose = num;
    }

    public Image getImg() {
	return img;
    }

    public void setImg(Image i) {
	img = i;
    }
}
