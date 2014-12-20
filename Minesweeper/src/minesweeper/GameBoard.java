package minesweeper;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class GameBoard {
    static int EASY = 9, MEDIUM = 16, HARD = 30;
    int totRows, totCols, r = 0, c = 0, xCoordinate = -25, yCoordinate = -25, totMines, blownX, blownY;
    int currentImage = 0;
    int sleepTime = 100; // milliseconds to sleep
    int totalImages = 25;
    boolean firstClick, gamePlay, doAnimation;
    BufferedImage explosion1;
    Image img[], explosion[];
    Cell board[][];

    /**
     * Creates a new game board with a medium difficulty, i.e. 16 rows and 16 columns.
     */
    public GameBoard() {
	totMines = 40;
	newGame(MEDIUM, MEDIUM);
    }

    /**
     * Creates a new minesweeper game board with a specified difficulty
     * 
     * @param difficulty
     *            must be either "easy" or "hard" -- any other String will initialize it as medium.
     */
    public GameBoard(String difficulty) {

	switch (difficulty) {
	case "easy":
	    newGame(EASY, EASY);
	    totMines = 10;
	    break;
	case "hard":
	    newGame(HARD, MEDIUM);
	    totMines = 99;
	    break;
	default:
	    newGame(MEDIUM, MEDIUM);
	    totMines = 40;
	}

    }

    /**
     * Create a custom sized minesweeper game board by specifying the number of rows and columns you want, and the total
     * number of mines.
     * 
     * @param row
     *            Total number of rows you want in your new board.
     * @param col
     *            Total number of columns you want in your new board.
     * @param mines
     *            Total number of mines you want in your new board.
     */
    public GameBoard(int row, int col, int mines) {
	totMines = mines;
	newGame(row, col);
    }

    private void newGame(int numRows, int numCols) {
	board = new Cell[numRows][numCols];
	for (int i = 0; i < totRows; i++) {
	    for (int j = 0; j < totCols; j++) {
		board[i][j] = new Cell();
		board[i][j].setHidden(true); // initializes every space as hidden
		board[i][j].setMined(false); // initializes no mines on the board
		board[i][j].setMinesClose(0); // because no mines, minesClose = 0
		board[i][j].setImg(img[10]); // initial image
	    }
	}
    }

    public void initializeBoard() {
	gamePlay = true;

	board = new Cell[totRows][totCols];
	for (int i = 0; i < totRows; i++) {
	    for (int j = 0; j < totCols; j++) {
		board[i][j] = new Cell();
		board[i][j].setHidden(true); // initializes every space as hidden
		board[i][j].setMined(false); // initializes no mines on the board
		board[i][j].setMinesClose(0); // because no mines, minesClose = 0
		board[i][j].setImg(img[10]); // initial image
	    }
	}
    }

    /**
     * Flags a cell. If already flagged, it will un-flag the cell. You must pass in the row and column of the cell which
     * you would like to toggle, represented by a double-dimensional array: array[row][col].
     * 
     * @param row
     *            The row location.
     * @param col
     *            The column location.
     */
    public void toggleFlag(int row, int col) {
	if (!board[row][col].flagged()) {
	    board[row][col].setFlagged(true);
	    // repaint();
	} else {
	    board[row][col].setFlagged(false);
	    // repaint();
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

	firstClick = false;
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

    /**
     * When a mine is selected to be revealed (clicked on in a game), use this function to reveal it. You must pass in
     * the row and column of the cell to be revealed like a double-dimensional array: array[row][column]. This will
     * recursively open spaces that are empty with no mines around it.
     * 
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     */
    public void reveal(int col, int row) {
	/*
	 * all of the possibilities of tiles to reveal: 1. it is hidden, it has 0 mines close to it 2. it is hidden, it
	 * has one or more mines close to it 3. it is hidden, it has a mine in it 4. it is not hidden, it has a number
	 * of mines close to it, it has the same number of flags close to it
	 */

	if (outOfBounds(col, row))
	    return;

	xCoordinate = (col) * 25;
	yCoordinate = (row) * 25;

	// 1. it is hidden, it is not mined, it has 0 mines close to it,
	// recursively reveal all others like it
	if (board[row][col].hidden() && !board[row][col].mined() && board[row][col].getMinesClose() == 0) {
	    board[row][col].setHidden(false);
	    reveal(col - 1, row - 1); // above and left
	    reveal(col, row - 1); // above
	    reveal(col + 1, row - 1); // above and right
	    reveal(col - 1, row); // left
	    reveal(col + 1, row); // right
	    reveal(col - 1, row + 1); // below and left
	    reveal(col, row + 1); // below
	    reveal(col + 1, row + 1); // below and right
	    // repaint(xCoordinate, yCoordinate, 25, 25);
	}

	// 2. it is hidden, it has one or more mines close to it
	if (board[row][col].hidden() && board[row][col].getMinesClose() > 0) {
	    board[row][col].setHidden(false);
	    // repaint(xCoordinate, yCoordinate, 25, 25);
	    return;
	}

	// 3. it is hidden, it has a mine in it
	if (board[row][col].mined && !board[row][col].flagged) {
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

}
