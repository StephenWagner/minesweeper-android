package minesweeper;

/*Stephen Wagner
 * 14 May 2014
 * D4
 * Coach
 * Final Project
 */

import java.applet.Applet;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.MediaTracker;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

//**************************SETTINGS FRAME****************************
class settings extends Frame {
    Choice difficulty;
    Choice song;
    Button apply;
    mineSweeperAttempt parent;

    settings(String title, mineSweeperAttempt p) {
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
	    // repaint();
	    return true;
	}

	if (evtObj.target instanceof Button) {
	    parent.setDifficulty(difficulty.getSelectedItem());
	    // repaint();
	    return true;
	}

	return false;
    }

}

// ********************************MAIN APPLET**************************
public class mineSweeperAttempt extends Applet implements MouseListener {
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
    settings fr;
    MediaTracker mTracker;
    Timer myTimer;

    public void init() {
	fr = new settings("Settings", this);
	fr.setVisible(true);
	fr.setSize(400, 100);
	img = new Image[13];
	click1 = true;
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

	totRows = totCols = med; // starts the game off in medium difficulty

	// loads pics into the img[] array
	for (int i = 0; i <= 12; i++)
	    img[i] = getImage(getCodeBase(), "pics/" + i + ".png");

	try // load the splice sheet into a buffered image
	{
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

	initializeBoard(); // initializes the array, this may also need to be
			   // done if a new game is created

	resize(totCols * 25, totRows * 25);

	// System.setProperty("sun.awt.enableExtraMouseButtons", "true");

    }

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

	resize(totCols * 25, totRows * 25);

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
	    if (gamePlay) {
		showStatus("x:" + x + " y:" + y + " xBlock:" + col + " yBlock:" + row);

		if (click1)
		    firstClick(col, row);
		else if (!board[row][col].hidden() && board[row][col].getMinesClose() > 0
			&& sameNumberOfFlags(row, col))
		    speedyOpener(row, col);
		else
		    reveal(col, row);
	    }
	if (e.getButton() > 1 && board[row][col].hidden())
	    toggleFlag(row, col);

    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void toggleFlag(int row, int col) {
	if (!board[row][col].flagged()) {
	    board[row][col].setFlagged(true);
	    repaint();
	} else {
	    board[row][col].setFlagged(false);
	    repaint();
	}
    }

    public void firstClick(int col, int row) {
	int numY, numX, mineCount = 0;
	Random rand = new Random();

	// distribute mines
	while (mineCount < totMines) {
	    numY = rand.nextInt(totRows); // create a random distribution
	    numX = rand.nextInt(totCols);

	    if (!board[numY][numX].mined() && checkPlacement(col, row, numX, numY)) {
		board[numY][numX].setMined(true);
		mineCount++;
	    }
	}

	// distribute numbers and images
	for (int i = 0; i < board.length; i++) {
	    for (int j = 0; j < board[i].length; j++) {
		if (!board[i][j].mined()) {
		    board[i][j].setMinesClose(minesClose(j, i));
		    board[i][j].setImg(img[board[i][j].getMinesClose()]);
		} else
		    board[i][j].setImg(img[9]);
		if (board[i][j].flagged() && !board[i][j].mined())
		    board[i][j].setImg(img[12]);
	    }
	}

	reveal(col, row);

	click1 = false;
    }

    public int minesClose(int col, int row) {
	int count = 0;

	for (int i = row - 1; i <= row + 1; i++) {
	    for (int j = col - 1; j <= col + 1; j++) {
		if (!outOfBounds(j, i)) {
		    if (board[i][j].getMined())
			count++;
		}
	    }
	}
	return count;
    }

    public boolean checkPlacement(int x, int y, int numX, int numY) {
	for (int i = y - 1; i <= y + 1; i++) {
	    for (int j = x - 1; j <= x + 1; j++) {
		if (numY == i & numX == j)
		    return false;
	    }
	}

	return true;
    }

    public boolean outOfBounds(int col, int row) {
	return col < 0 || row < 0 || col >= board[0].length || row >= board.length;
    }

    /*
     * all of the possibilities of tiles to reveal: 1. it is hidden, it has 0
     * mines close to it 2. it is hidden, it has one or more mines close to it
     * 3. it is hidden, it has a mine in it 4. it is not hidden, it has a number
     * of mines close to it, it has the same number of flags close to it
     */
    public void reveal(int col, int row) {
	if (outOfBounds(col, row))
	    return;

	xCoordinate = (col) * 25;
	yCoordinate = (row) * 25;

	// 1. it is hidden, it is not mined, it has 0 mines close to it,
	// recursively reveal all others like it
	if (board[row][col].hidden() && !board[row][col].mined()
		&& board[row][col].getMinesClose() == 0) {
	    board[row][col].setHidden(false);
	    reveal(col - 1, row - 1);
	    reveal(col, row - 1);
	    reveal(col + 1, row - 1);
	    reveal(col - 1, row);
	    reveal(col + 1, row);
	    reveal(col - 1, row + 1);
	    reveal(col, row + 1);
	    reveal(col + 1, row + 1);
	    repaint(xCoordinate, yCoordinate, 25, 25);
	}

	// 2. it is hidden, it has one or more mines close to it
	if (board[row][col].hidden() && board[row][col].getMinesClose() > 0) {
	    board[row][col].setHidden(false);
	    repaint(xCoordinate, yCoordinate, 25, 25);
	    return;
	}

	// 3. it is hidden, it has a mine in it
	if (board[row][col].mined() && !board[row][col].flagged()) {
	    gameOver(col, row);
	}

    }

    public void speedyOpener(int row, int col) {
	// 4. it is not hidden, it has a number of mines close to it, it has the
	// same number of flags close to it
	for (int i = row - 1; i <= row + 1; i++) {
	    for (int j = col - 1; j <= col + 1; j++) {
		if (!outOfBounds(j, i))
		    if (!board[i][j].flagged())
			reveal(j, i);
	    }
	}
	// if (!board[row][col].hidden() && board[row][col].getMinesClose()>0 &&
	// sameNumberOfFlags(row, col))
	// {
	//
	// }
    }

    public boolean sameNumberOfFlags(int row, int col) {
	int count = 0;

	for (int i = row - 1; i <= row + 1; i++) {
	    for (int j = col - 1; j <= col + 1; j++) {
		if (!outOfBounds(j, i))
		    if (board[i][j].flagged())
			count++;
	    }
	}

	return count == board[row][col].getMinesClose();
    }

    public boolean winner() {
	int revealed = 0;

	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[i].length; j++) {
		if (!board[i][j].hidden())
		    revealed++;
	    }

	if (revealed == totRows * totCols - totMines) {
	    makeAllMinesFlagged();
	}

	return revealed == totRows * totCols - totMines;
    }

    private void makeAllMinesFlagged() {
	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[i].length; j++) {
		if (board[i][j].mined())
		    board[i][j].setFlagged(true);
	    }
    }

    public void gameOver(int col, int row) {
	gamePlay = false;

	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[i].length; j++) {
		if (i == row && j == col)
		    board[i][j].setImg(explosion[1]);
		if (board[i][j].flagged() && !board[i][j].mined())
		    board[i][j].setImg(img[11]);
		if (board[i][j].mined())
		    board[i][j].setHidden(false);
	    }

	startAnim(row, col);
	// //from the earth animation-- no idea why it's here, really
	// public void start(Graphics g)
	// {
	// g.drawImage(explosion[0],(x+10)*25-19, (y+10)*25-19, 64, 64, this);
	// currentImage = 1;
	// }

	// repaint((x+10)*25-19, (y+10)*25-19, 64, 64);
	// some sort of end of game behavior??? or just a "Start new game"
	// button on the main applet?
    }

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
	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[i].length; j++) {
		if (board[i][j].hidden() && !board[i][j].flagged())
		    g.drawImage(img[10], j * 25, i * 25, 25, 25, this);
		else if (board[i][j].hidden() && board[i][j].flagged())
		    g.drawImage(img[11], j * 25, i * 25, 25, 25, this);
		else
		    g.drawImage(board[i][j].getImg(), j * 25, i * 25, 25, 25, this);
	    }

	if (winner()) {
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
