package minesweeper;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class GameBoard {
    int easy = 9;
    int med = 16;
    int hardX = 16;
    int hardY = 30;
    int totRows, totCols, r = 0, c = 0, xCoordinate = -25, yCoordinate = -25, totMines, blownX,
	    blownY;
    int currentImage = 0;
    int sleepTime = 100; // milliseconds to sleep
    int totalImages = 25;
    boolean click1, gamePlay, doAnimation;
    BufferedImage explosion1;
    Image img[], explosion[];
    Cell board[][];

    
    public void initializeBoard() {
	gamePlay = true;

	board = new Cell[totRows][totCols];
	for (int i = 0; i < totRows; i++) {
	    for (int j = 0; j < totCols; j++) {
		board[i][j] = new Cell();
		board[i][j].setHidden(true); // initializes every space to be
					     // hidden (all spaces are
					     // unrevealed)
		board[i][j].setMined(false); // initializes no mines in the
					     // minefield
		board[i][j].setMinesClose(0); // initializes no mines close to
					      // any space on the board
		board[i][j].setImg(img[10]);
	    }
	}
    }

    public void setDifficulty(String diff) {
	click1 = true;
	r = c = 0;

	switch (diff) {
	case "Easy":
	    totRows = totCols = easy;
	    totMines = 10;
	    break;
	case "Hard":
	    totRows = hardY;
	    totMines = 99;
	    totCols = hardX;
	    break;
	default:
	    totRows = totCols = med;
	    totMines = 40;
	}

	initializeBoard();

    }

}
