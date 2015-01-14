package minesweeper;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

public class MainAppletWindow extends Applet implements MouseListener {
    private static final long serialVersionUID = -4868516420381977551L;

    private int blownX, blownY;
    private int currentImage = 0;
    private int sleepTime = 100; // milliseconds to sleep
    private int totalImages = 25;
    private int exploded[];
    private boolean doAnimation, newGame = true;
    private BufferedImage explosion1;
    private Image img[], explosion[];
    private GameBoard board;
    private SettingsFrame fr;
    private MediaTracker mTracker;
    private Timer myTimer;

    public void init() {
	fr = new SettingsFrame("Settings", this);
	fr.setVisible(true);
	fr.setSize(400, 100);
	img = new Image[15];
	doAnimation = false;
	addMouseListener(this);

	myTimer = new Timer(true);
	myTimer.schedule(new TimerTask() {
	    public void run() {
		repaint();
	    }
	}, 0, 100);
	mTracker = new MediaTracker(this);

	// loads pics into the img[] array
	for (int i = 0; i < img.length; i++)
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

	// create the default game board
	board = new GameBoard();

	resize(board.getColumnLength() * 25, board.getRowLength() * 25);

	// System.setProperty("sun.awt.enableExtraMouseButtons", "true");
    }

    /**
     * Use this to set the difficulty of a new game.
     * 
     * @param diff
     *            Pass in a string for the difficulty. "Easy" will create a board with 9 columns, 9 rows, and 10 mines;
     *            "Hard" will create a board with 16 columns, 30 rows, and 99 mines; any other string will create a game
     *            board with 16 columns, 16 rows, and 40 mines.
     */
    public void setDifficulty(String diff) {
	board = new GameBoard(diff);
	newGame = true;
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

	newGame = false;

	if (!board.getGameOver() && !board.winner()) {// clicking will only have an effect if there's still a game
	    if (e.getButton() == 1) {

		// THIS BLOCK KEPT HERE IN CASE DEBUGGING IS NEEDED IN THE FUTURE
		// // shows info in the status-- at the bottom of the applet window
		// showStatus("x:" + x + " y:" + y + " xBlock:" + col + " yBlock:" + row + " Mines left: "
		// + board.minesLeft());

		// reveals the block which was clicked on
		if (!board.reveal(col, row)) {
		    setImages(row, col);
		    startAnim();
		    repaint();
		} else {
		    setImages(row, col);
		    repaint();
		}
	    }

	    if (e.getButton() > 1 && board.getHidden(row, col))
		board.toggleFlag(row, col);

	    setImages(row, col);
	    repaint();
	}
    }

    private void setImages(int row, int col) {

	// shows info in the status-- at the bottom of the applet window
	if (board.getGameOver())
	    showStatus("Percentage Completed: " + board.getPercentFinished() + "%");
	else
	    showStatus(" xBlock:" + col + " yBlock:" + row + " Mines left: " + board.minesLeft());

	/*-
	 * All image possibilities:
	 * 1. Hidden, not flagged
	 * 2. Hidden, flagged
	 * 3. Revealed
	 * 	a. 0 mines close
	 * 	b. 1 mine close
	 * 	c. 2 mines close
	 * 	d. 3 mines close
	 * 	e. 4 mines close
	 * 	f. 5 mines close
	 * 	g. 6 mines close
	 * 	h. 7 mines close
	 * 	i. 8 mines close
	 * 4. Game over && mined && not flagged
	 * 5. Game over && flagged && not mined
	 * 6. Winner && mined
	 * 7. Game over && mined && exploded
	 */
	if (!board.getGameOver()) {
	    for (int i = 0; i < board.getRowLength(); i++) {
		for (int j = 0; j < board.getColumnLength(); j++) {
		    if (board.getHidden(i, j)) {// 1 and 2: hidden
			if (board.getFlagged(i, j))// 2. hidden and flagged
			    board.setImage(i, j, img[11]);
			else if (board.winner() && board.getMined(i, j))// 6. winner and mined
			    board.setImage(i, j, img[11]);
			else
			    board.setImage(i, j, img[10]);// 1. Hidden and not flagged
		    } else
			board.setImage(i, j, img[board.getMinesClose(i, j)]);// 3. Revealed (set mines close)
		}
	    }
	}

	// Game Over
	else {
	    // get the location of the exploded mine
	    exploded = board.getExploded();

	    for (int i = 0; i < board.getRowLength(); i++)
		for (int j = 0; j < board.getColumnLength(); j++) {

		    // 4. mined and not flagged
		    if (board.getMined(i, j) && !board.getFlagged(i, j)) {

			// 7. Mined and exploded
			if (i == exploded[0] && j == exploded[1])
			    board.setImage(i, j, img[14]);

			// 4. mined and not flagged
			else
			    board.setImage(i, j, img[9]);
		    }

		    // 5. flagged and not mined
		    if (board.getFlagged(i, j) && !board.getMined(i, j))
			board.setImage(i, j, img[12]);
		}

	}
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void startAnim() {
	exploded = board.getExploded();// this gets the row and column of the exploded mine
	doAnimation = true;
	currentImage = 0;
	blownY = exploded[0] * 25 - 20;
	blownX = exploded[1] * 25 - 20;
    }

    // override update to eliminate flicker
    public void update(Graphics g) {
	paint(g);
    }

    // ******************paint method*********************
    public void paint(Graphics g) {

	for (int i = 0; i < board.getRowLength(); i++)
	    for (int j = 0; j < board.getColumnLength(); j++) {
		if (newGame)
		    g.drawImage(img[10], j * 25, i * 25, 25, 25, this);
		else
		    g.drawImage(board.getImage(i, j), j * 25, i * 25, 25, 25, this);

	    }

	if (board.winner()) {
	    g.setFont(new Font("Arial", Font.BOLD, 40));
	    g.setColor(Color.blue);
	    g.drawString("YOU WIN!!!", 20, 50);
	    showStatus("Percentage Completed: " + board.getPercentFinished() + "%");
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
		}

		// //play sound
		// boom.play();

		if (board.getRowLength() < 10) {
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
