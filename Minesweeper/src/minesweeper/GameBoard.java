/**
 * @author Stephen Wagner
 */

package minesweeper;

import java.awt.*;
import java.util.*;

public class GameBoard {
    private static int EASY = 9, MEDIUM = 16, HARD = 30;
    private int totMines = 0, totFlags = 0;
    private boolean firstClick = true, gameOver = false;
    private Image[] img;
    private Cell board[][];
    private int exploded[] = null;

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
     *            must be either "Easy" or "Hard" -- any other String will initialize it as medium.
     */
    public GameBoard(String difficulty) {

	switch (difficulty) {
	case "Easy":
	    newGame(EASY, EASY);
	    totMines = 10;
	    break;
	case "Hard":
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
	for (int i = 0; i < board.length; i++) {
	    for (int j = 0; j < board[0].length; j++) {
		board[i][j] = new Cell();
		board[i][j].setHidden(true); // initializes every space as hidden
		board[i][j].setMined(false); // initializes no mines on the board
		board[i][j].setMinesClose(0); // because no mines, minesClose = 0
	    }
	}
    }

    // private void newGame(int numRows, int numCols, Image[] imgs) {
    // img = imgs;
    //
    // board = new Cell[numRows][numCols];
    // for (int i = 0; i < board.length; i++) {
    // for (int j = 0; j < board[0].length; j++) {
    // board[i][j] = new Cell();
    // board[i][j].setHidden(true); // initializes every space as hidden
    // board[i][j].setMined(false); // initializes no mines on the board
    // board[i][j].setMinesClose(0); // because no mines, minesClose = 0
    // board[i][j].setImg(img[10]); // initial image
    // }
    // }
    // }

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
	    totFlags++;
	} else {
	    board[row][col].setFlagged(false);
	    totFlags--;
	}
    }

    public void firstClick(int col, int row) {
	int numY, numX, mineCount = 0;
	Random rand = new Random();

	// distribute mines
	while (mineCount < totMines) {
	    numY = rand.nextInt(board.length); // create a random distribution
	    numX = rand.nextInt(board[0].length);

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
		    // board[i][j].setImg(img[board[i][j].getMinesClose()]);
		}
		// else
		// board[i][j].setImg(img[9]);
		// if (board[i][j].flagged() && !board[i][j].mined())
		// board[i][j].setImg(img[12]);
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
     * @return TRUE if the cell is clear, FALSE only if the cell has a mine in it.
     */
    public boolean reveal(int col, int row) {
	/*-
	 * All the possibilities of tiles to reveal:
	 * 0. it is not hidden
	 * 1. it is hidden, it has 0 mines close to it
	 * 2. it is hidden, it has one of more mines close to it
	 * 3. it is hidden, it has a mine in it
	 * 4. it is not hidden, it has a number of mines close to it,
	 * 	it has the same number of flags close to it.
	 */

	if (outOfBounds(col, row))
	    return true;

	// 0. it is not hidden
	if (!board[row][col].hidden)
	    return true;

	// 1. it is hidden, it is not mined, it has 0 mines close to it,
	// recursively reveal all others like it
	if (!board[row][col].mined && board[row][col].minesClose == 0) {
	    board[row][col].setHidden(false);
	    reveal(col - 1, row - 1); // above and left
	    reveal(col, row - 1); // above
	    reveal(col + 1, row - 1); // above and right
	    reveal(col - 1, row); // left
	    reveal(col + 1, row); // right
	    reveal(col - 1, row + 1); // below and left
	    reveal(col, row + 1); // below
	    reveal(col + 1, row + 1); // below and right
	    return true;
	}

	// 2. it is hidden, it has one or more mines close to it
	if (board[row][col].minesClose > 0) {
	    board[row][col].setHidden(false);
	    // repaint(xCoordinate, yCoordinate, 25, 25);
	    return true;
	}

	// 3. it is hidden, it has a mine in it
	if (board[row][col].mined && !board[row][col].flagged) {
	    gameOver(col, row);
	    return false;
	}

	return true;
    }

    /**
     * This is used when the user clicks on a space which has a number on it AND the immediate surrounding cells have
     * the same number of flags on them as the number on the cell which was clicked on. In other words, use only if
     * GameBoard.sameNumberOfFlags(int row, int col) returns TRUE for the cell which the user clicked on.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     */
    public void speedyOpener(int row, int col) {
	// 4. it is not hidden, it has a number of mines close to it, it has the same number of flags close to it
	for (int i = row - 1; i <= row + 1; i++) {
	    for (int j = col - 1; j <= col + 1; j++) {
		if (!outOfBounds(j, i))
		    if (!board[i][j].flagged())
			reveal(j, i);
	    }
	}
    }

    /**
     * Use this method when the user clicks on a cell which is already cleared (GameBoard.getHidden(row, col) returns
     * false) and the cell has a number on it greater than 0 (GameBoard.getMinesClose(row, col) returns > 0). Example of
     * how to use it in a click event:
     * 
     * <pre>
     * if (!board.getHidden(ySq, xSq) &amp;&amp; board.getMinesClose(ySq, xSq) &gt; 0 &amp;&amp; board.sameNumberOfFlags(ySq, xSq)) {
     *     board.speedyOpener(ySq, xSq);
     * } else
     *     board.reveal(xSq, ySq);
     * </pre>
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @return True only if the cell which was clicked on has the same number of flags immediately surrounding it as it
     *         has mines immediately surrounding it.
     */
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

	if (revealed == board.length * board[0].length - totMines) {
	    makeAllMinesFlagged();
	    totFlags = totMines;
	}

	return revealed == board.length * board[0].length - totMines && !gameOver;
    }

    private void makeAllMinesFlagged() {
	// for(Cell[] array: board)
	// for(Cell c: array){
	// if(c.mined)
	// c.setFlagged(true);
	// }

	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[i].length; j++) {
		if (board[i][j].mined())
		    board[i][j].setFlagged(true);
	    }
    }

    public void gameOver(int col, int row) {
	gameOver = true;

	// give the coordinates of the exploded cell
	exploded = new int[2];
	exploded[0] = row;
	exploded[1] = col;

	// for (int i = 0; i < board.length; i++)
	// for (int j = 0; j < board[i].length; j++) {
	// if (i == row && j == col)
	// board[i][j].setImg(img[9]);
	// if (board[i][j].flagged && !board[i][j].mined)
	// board[i][j].setImg(img[11]);
	// if (board[i][j].mined && !board[i][j].flagged)
	// board[i][j].setHidden(false);
	// }
    }

    /**
     * Returns the coordinates of the exploded mine. Will return null when there is no exploded mine.
     * 
     * @return exploded[0] = row, exploded[1] = col; as in gameBoard[row][col];
     */
    public int[] getExploded() {
	return exploded;
    }

    public boolean getFirstClick() {
	return firstClick;
    }

    public int getRowLength() {
	return board.length;
    }

    public int getColumnLength() {
	return board[0].length;
    }

    public boolean getFlagged(int row, int col) {
	return board[row][col].flagged;
    }

    public boolean getGameOver() {
	return gameOver;
    }

    public boolean getHidden(int row, int col) {
	return board[row][col].hidden;
    }

    public boolean getMined(int row, int col) {
	return board[row][col].mined;
    }

    public int getMinesClose(int row, int col) {
	return board[row][col].minesClose;
    }

    public Image getImage(int row, int col) {
	return board[row][col].getImg();
    }

    public void setHidden(int row, int col, Boolean flag) {
	board[row][col].setHidden(flag);
    }

    public void setImage(int row, int col, Image img) {
	board[row][col].setImg(img);
    }

    public int minesLeft() {
	return totMines - totFlags;
    }
}
