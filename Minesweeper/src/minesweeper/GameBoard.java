/**
 * @author Stephen Wagner
 */

package minesweeper;

import java.awt.*;
import java.util.*;

public class GameBoard {
    private static int EASY = 9, MEDIUM = 16, HARD = 30;
    private int totMines = 0, totFlags = 0, canHintNum = 3;
    private boolean firstClick = true, gameOver = false, speedyOpenOK = true;
    private Cell board[][];
    private int exploded[] = null;

    /**
     * Creates a new game board with a medium difficulty, i.e. 16 rows, 16 columns and 40 total mines.
     */
    public GameBoard() {
	totMines = 40;
	newGame(MEDIUM, MEDIUM);
    }

    /**
     * Creates a new minesweeper game board with a specified difficulty. Easy = 9 rows, 9 columns, 10 mines. Medium = 16
     * rows, 16 columns, 40 mines. Hard = 30 rows, 16 columns, 99 mines.
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
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
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

    /**
     * Flags a cell. If already flagged, it will un-flag the cell. You must pass in the row and column of the cell which
     * you would like to toggle, represented by a double-dimensional array: array[row][col].
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * */
    public void toggleFlag(int row, int col) {
	if (!board[row][col].flagged()) {
	    board[row][col].setFlagged(true);
	    totFlags++;
	} else {
	    board[row][col].setFlagged(false);
	    totFlags--;
	}
    }

    private void firstClick(int col, int row) {
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

	// distribute numbers
	for (int i = 0; i < board.length; i++) {
	    for (int j = 0; j < board[i].length; j++) {
		if (!board[i][j].mined()) {
		    board[i][j].setMinesClose(minesClose(j, i));
		}
	    }
	}

	firstClick = false;

	reveal(col, row);

    }

    private int minesClose(int col, int row) {
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

    private boolean checkPlacement(int x, int y, int numX, int numY) {
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

	if (firstClick)
	    firstClick(col, row);

	// 4. it is not hidden, it has a number of mines close to it, it has the same number of flags close to it.
	if (speedyOpenOK && !board[row][col].hidden && board[row][col].minesClose > 0 && sameNumberOfFlags(row, col)) {
	    speedyOpenOK = false;
	    if (speedyOpener(row, col)) {
		speedyOpenOK = true;
		return true;
	    } else {
		speedyOpenOK = true;
		return false;
	    }
	}

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
     * GameBoard.sameNumberOfFlags(int row, int col) returns TRUE for the cell which the user clicked on. This is
     * already implemented in the GameBoard.reveal(row, col) method.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     */
    public boolean speedyOpener(int row, int col) {
	// 4. it is not hidden, it has a number of mines close to it, it has the same number of flags close to it
	for (int i = row - 1; i <= row + 1; i++) {
	    for (int j = col - 1; j <= col + 1; j++) {
		if (!outOfBounds(j, i))
		    if (!board[i][j].flagged())
			if (!reveal(j, i))
			    return false;
	    }
	}

	return true;
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

    /**
     * Use this method to determine if the player has won the game. If the player has won, it will make every mined cell
     * flagged.
     * 
     * @return True if all non-mined cells have been revealed.
     */
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
	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[i].length; j++) {
		if (board[i][j].mined())
		    board[i][j].setFlagged(true);
	    }
    }

    private void gameOver(int col, int row) {
	gameOver = true;

	// set the coordinates of the exploded cell to what was passed in
	exploded = new int[2];
	exploded[0] = row;
	exploded[1] = col;

    }

    /**
     * Returns the coordinates of the exploded mine. Will return null when there is no exploded mine. Used as in
     * array[row][column], where row = y-position, column = x-position.
     * 
     * @return exploded[0] = row, exploded[1] = column. This is 0-based.
     */
    public int[] getExploded() {
	return exploded;
    }

    /**
     * Find out if you have done the first reveal yet.
     * 
     * @return TRUE if the game has no revealed cells yet.
     */
    public boolean getFirstClick() {
	return firstClick;
    }

    /**
     * Will return the row length of the board, as in array[row][column], where the row length is the y-axis.
     * 
     * @return The number of cells in the row, i.e. board[row][column].
     */
    public int getRowLength() {
	return board.length;
    }

    /**
     * Will return the column length of the board, as in array[row][column], where the column length is the x-axis.
     * 
     * @return The number of cells in the column, i.e. board[row][column].
     */
    public int getColumnLength() {
	return board[0].length;
    }

    /**
     * Find out if a specific cell is flagged.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @return TRUE if the cell is flagged, FALSE if the cell is not flagged.
     */
    public boolean getFlagged(int row, int col) {
	return board[row][col].flagged;
    }

    /**
     * Find out if the player has lost the game.
     * 
     * @return TRUE only if the game has been lost (if a cell has been revealed which contained a mine).
     */
    public boolean getGameOver() {
	return gameOver;
    }

    /**
     * Find out if a cell is hidden or not.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @return TRUE if a cell is hidden (unrevealed), FALSE if revealed.
     */
    public boolean getHidden(int row, int col) {
	return board[row][col].hidden;
    }

    /**
     * Find out if a specific cell contains a mine.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @return TRUE only if the specified cell contains a mine.
     */
    public boolean getMined(int row, int col) {
	return board[row][col].mined;
    }

    /**
     * Find out how many mines a specific cell has close to it.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @return An int of the number of mines which surround your specified cell.
     */
    public int getMinesClose(int row, int col) {
	return board[row][col].minesClose;
    }

    /**
     * Find out what image is stored in a specific cell.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @return A reference to the image stored in the specified cell.
     */
    public Image getImage(int row, int col) {
	return board[row][col].getImg();
    }

    /**
     * Make a specific cell hidden or revealed.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @param flag
     *            True to make the cell hidden. False to make the cell revealed.
     */
    public void setHidden(int row, int col, Boolean flag) {
	board[row][col].setHidden(flag);
    }

    /**
     * Set the image associated with a specified cell.
     * 
     * @param row
     *            Row position, as in array[row][column]. This is the y-position cell on a screen (zero-based).
     * @param col
     *            Column position, as in array[row][column]. This is the x-position cell on a screen (zero-based).
     * @param img
     *            Reference to the image which will be stored in the cell.
     */
    public void setImage(int row, int col, Image img) {
	board[row][col].setImg(img);
    }

    /**
     * Find out how many mines are left to be found. This returns the total number of mines in the board less the number
     * of flagged cells. It does not adjust for incorrectly flagged cells.
     * 
     * @return An int which gives the total mines less the number of flagged cells.
     */
    public int minesLeft() {
	return totMines - totFlags;
    }

    public float getPercentFinished() {
	int totalCells, clearedCells;
	float percentFinished;

	totalCells = board.length * board[0].length;
	clearedCells = totalClearedCells();
	percentFinished = (clearedCells * 100) / totalCells;

	return percentFinished;
    }

    /**
     * Gives a hint. If a hint cannot be given, it will return the same row and col that was passed in.
     * 
     * @param row
     * @param col
     * @return An int array with the row being hint[0] and the column being hint[1].
     */
    public int[] getHint(int row, int col) {
	int hint[] = new int[2];
	hint[0] = row;
	hint[1] = col;
	int lowestMinesClose = 9;
	Boolean changed = false;

	if (!board[row][col].getHidden() && canHintNum > 0) {
	    // checks every existing cell that surrounds the passed in cell
	    for (int i = row - 1; i <= row + 1; i++) {
		for (int j = col - 1; j <= col + 1; j++) {
		    if (!outOfBounds(j, i)) {// makes sure it's not out of bounds before doing a comparison
			// if cell is hidden, not mined, and the
			if (board[i][j].hidden && !board[i][j].mined()
				&& board[i][j].getMinesClose() <= lowestMinesClose) {
			    lowestMinesClose = board[i][j].getMinesClose();
			    hint[0] = i;
			    hint[1] = j;
			    changed = true;
			}
		    }
		}
	    }
	}

	if (changed)
	    canHintNum--;

	return hint;
    }

    public Boolean canHint() {
	return canHintNum > 0;
    }

    public void setNumberOfHints(int totalNumberOfHints) {
	if (totalNumberOfHints < 6) {
	    this.canHintNum = totalNumberOfHints;
	}
    }

    public int getNumberOfHintsLeft() {
	return canHintNum;
    }

    private int totalClearedCells() {
	int totalClearedCells = 0;

	for (int i = 0; i < board.length; i++)
	    for (int j = 0; j < board[0].length; j++)
		if (correctlyCompletedCell(i, j))
		    totalClearedCells++;

	return totalClearedCells;
    }

    private Boolean correctlyCompletedCell(int row, int col) {
	Boolean correctlyCompleted;

	/*-
	 * A cell has been correctly done if it is:
	 * 1. flagged and mined
	 * 2. a. revealed and not mined
	 */
	correctlyCompleted = !board[row][col].hidden() && !board[row][col].mined() || board[row][col].flagged()
		&& board[row][col].mined();
	return correctlyCompleted;
    }
}
