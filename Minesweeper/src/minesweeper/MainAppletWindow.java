package minesweeper;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

public class MainAppletWindow extends Applet implements MouseListener {
    /**
     * 
     */
    private static final long serialVersionUID = -4868516420381977551L;

    int totRows, totCols, r = 0, c = 0, xCoordinate = -25, yCoordinate = -25, totMines, blownX, blownY;
    int currentImage = 0;
    int sleepTime = 100; // milliseconds to sleep
    int totalImages = 25;
    boolean firstClick, gamePlay, doAnimation;
    BufferedImage explosion1;
    Image img[], explosion[];
    GameBoard board;
    SettingsFrame fr;
    MediaTracker mTracker;
    Timer myTimer;

    public void init() {
	fr = new SettingsFrame("Settings", this);
	fr.setVisible(true);
	fr.setSize(400, 100);
	img = new Image[13];
	firstClick = true;
	totMines = 40;
	doAnimation = false;
	addMouseListener(this);

	myTimer = new Timer(true);
	myTimer.schedule(new TimerTask() {
	    public void run() {
		repaint();
	    }
	}, 0, 100);
	mTracker = new MediaTracker(this);

	// totRows = totCols = med; // starts the game off in medium difficulty

	// loads pics into the img[] array
	for (int i = 0; i <= 12; i++)
	    img[i] = getImage(getCodeBase(), "pics/" + i + ".png");

	try {
	    // load the splice sheet into a buffered image
	    explosion1 = ImageIO.read(new File("pics/explosion25.png"));
	}

	// catch exceptions to loading the buffered image
	catch (IOException e) {
	    e.printStackTrace();
	}

	// explosion[] image array initiated
	explosion = new Image[25];

	// timer for the explosion
	myTimer = new Timer(true);
	myTimer.schedule(new TimerTask() {
	    public void run() {
		repaint();
	    }
	}, 0, sleepTime);

	// loads explosion image array (and puts it into a media tracker???)
	for (int i = 0, count = 0; i < 5; i++) {
	    for (int j = 0; j < 5; j++) {
		explosion[count] = explosion1.getSubimage(j * 64, i * 64, 64, 64);
		mTracker.addImage(explosion[count], count);
		count++;
	    }
	}

	try {
	    mTracker.waitForID(0);
	}

	catch (InterruptedException e) {
	}

	board = new GameBoard(img);

	resize(board.getColumnLength() * 25, board.getRowLength() * 25);

	// System.setProperty("sun.awt.enableExtraMouseButtons", "true");

    }

    public void setDifficulty(String diff) {
	board = new GameBoard(diff, img);
	resize(board.getColumnLength() * 25, board.getRowLength() * 25);
	repaint();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
	int x = e.getX();
	int y = e.getY();
	int col = (x) / 25;
	int row = (y) / 25;

	if (e.getButton() == 1)
	    // if (gamePlay) {
	    showStatus("x:" + x + " y:" + y + " xBlock:" + col + " yBlock:" + row);

	if (board.getFirstClick()) {
	    board.firstClick(col, row);
	} else if (!board.getHidden(row, col) && board.getMinesClose(row, col) > 0 && board.sameNumberOfFlags(row, col)) {
	    board.speedyOpener(row, col);
	} else
	    board.reveal(col, row);
	// }
	if (e.getButton() > 1 && board.getHidden(row, col))
	    board.toggleFlag(row, col);

	repaint();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    // public void gameOver(int col, int row) {
    // gamePlay = false;
    //
    // for (int i = 0; i < board.length; i++)
    // for (int j = 0; j < board[i].length; j++) {
    // if (i == row && j == col)
    // board[i][j].setImg(explosion[1]);
    // if (board[i][j].flagged() && !board[i][j].mined())
    // board[i][j].setImg(img[11]);
    // if (board[i][j].mined())
    // board[i][j].setHidden(false);
    // }
    //
    // startAnim(row, col);
    // // //from the earth animation-- no idea why it's here, really
    // // public void start(Graphics g)
    // // {
    // // g.drawImage(explosion[0],(x+10)*25-19, (y+10)*25-19, 64, 64, this);
    // // currentImage = 1;
    // // }
    //
    // // repaint((x+10)*25-19, (y+10)*25-19, 64, 64);
    // // some sort of end of game behavior??? or just a "Start new game"
    // // button on the main applet?
    // }

    public void startAnim(int row, int col) {
	doAnimation = true;
	currentImage = 0;
	blownY = row * 25 - 20;
	blownX = col * 25 - 20;
	// repaint(col*25,row*25);
    }

    // override update to eliminate flicker
    public void update(Graphics g) {
	paint(g);
    }

    // ******************paint method*********************
    // TODO set up repaint so that I can just repaint one block at a time
    // TODO if gameOver, it should show the mines, paint the flags over the
    // correctly flagged mines, and some sort of X over an incorrectly flagged
    // tile
    public void paint(Graphics g) {
	for (int i = 0; i < board.getRowLength(); i++)
	    for (int j = 0; j < board.getColumnLength(); j++) {
		if (board.getHidden(i, j) && !board.getFlagged(i, j))
		    g.drawImage(img[10], j * 25, i * 25, 25, 25, this);
		else if (board.getHidden(i, j) && board.getFlagged(i, j))
		    g.drawImage(img[11], j * 25, i * 25, 25, 25, this);
		else
		    g.drawImage(board.getImage(i, j), j * 25, i * 25, 25, 25, this);
	    }

	if (board.winner()) {
	    gamePlay = false;
	    g.setFont(new Font("Arial", Font.BOLD, 40));
	    g.setColor(Color.blue);
	    g.drawString("YOU WIN!!!", 20, 50);
	}

	if (mTracker.checkID(currentImage, true)) {
	    if (doAnimation) {
		g.drawImage(explosion[currentImage], blownX, blownY, 64, 64, this);
		if (currentImage == 0)
		    explosion[totalImages - 1].flush();
		else
		    explosion[currentImage - 1].flush();

		currentImage = ++currentImage % totalImages;

		if (currentImage == 0) {
		    doAnimation = false;
		    // super.update(g);
		}

		// //play sound
		// boom.play();

		if (totRows < 10) {
		    // display a large "Game Over" on the screen
		    g.setFont(new Font("TimesRoman", Font.BOLD, 40));
		    g.setColor(Color.red);
		    g.drawString("Game Over", 10, 100);
		} else {
		    // display a large "Game Over" on the screen
		    g.setFont(new Font("TimesRoman", Font.BOLD, 40));
		    g.setColor(Color.red);
		    g.drawString("Game Over", 100, 200);
		}
	    }
	}
    }

}
