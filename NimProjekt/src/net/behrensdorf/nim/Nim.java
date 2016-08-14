package net.behrensdorf.nim;

import java.util.Random;
import java.util.Stack;

public class Nim {

	private static final int MAX_TOKENS_PER_ROW = 10;
	private static final int MIN_TOKENS_PER_ROW = 2;
	private static final int DEFAULT_ROWS = 6;

	int[] rows;
	int notEmptyRows;
	int xorOverAllRows;
	NimMove lastMove;
	private Stack<NimMove> undoStack = new Stack<>();

	private static final Random rnd = new Random();

	public Nim() {
		this(DEFAULT_ROWS, MAX_TOKENS_PER_ROW);
	}

	public Nim(int rowCount, int maxTokenCount) {
		if (rowCount <= 0) {
			throw new NimException("Illegal rowcount");
		}
		if (maxTokenCount <= 0) {
			throw new NimException("Illegal token count");
		}

		rows = new int[rowCount];
		for (int r = 0; r < rowCount; r++) {
			rows[r] = MIN_TOKENS_PER_ROW + rnd.nextInt((maxTokenCount - MIN_TOKENS_PER_ROW) + 1);
			xorOverAllRows ^= rows[r];
		}
		notEmptyRows = rowCount;
	}

	// 3
	/*
	 * Das Spiel soll nicht deterministisch verlaufen. Gibt es in einer Stellung
	 * mehrere mögliche Züge, soll irgendeiner ausgewählt werden
	 * (Zufallsprinzip).
	 * 
	 * Bei Gewinnstellung soll ein optimaler Zug per Zufall gewählt werden.
	 * 
	 */
	public NimMove suggestMove() {
		if (isOver()) {
			throw new NimException("Game is over");
		}
		NimMove move = null;
		int pos = rnd.nextInt(rows.length);
		if (!isWinSituation()) {
			move = randomMove();
		} else {
			int highestOneBit = Integer.highestOneBit(xorOverAllRows);
			for (; (rows[pos] & highestOneBit) == 0; pos = ++pos % rows.length) {
			}
			move = new NimMove(pos, rows[pos] ^ xorOverAllRows);
		}
		return move;
	}

	public NimMove suggestBadMove() {
		if (isOver()) {
			throw new NimException("Game is over");
		}
		NimMove move = null;
		int pos = rnd.nextInt(rows.length);
		int startPos = pos;
		if (!isWinSituation()) {
			move = randomMove();
		} else {
			boolean badMoveFound = true;
			int highestOneBit = Integer.highestOneBit(xorOverAllRows);
			for (; (rows[pos] & highestOneBit) != 0;) {
				pos = ++pos % rows.length;
				if (pos == startPos) {
					badMoveFound = false;
					break;
				}
			}

			if (!badMoveFound) {
				move = randomMove();
			} else {
				move = new NimMove(pos, rnd.nextInt(rows[pos]));
			}
		}
		return move;
	}

	// 4
	public NimMove randomMove() {
		int row;
		int count;

		do {
			row = rnd.nextInt(rows.length);
		} while (rows[row] == 0);

		count = rnd.nextInt(rows[row]);
		return new NimMove(row, count);
	}

	// 2

	// prüfen ob Zug gültig ist
	// aktualisiere rows[]
	// aktualisiere xorOverAllRows
	// aktualisiere ggf. notEmptyRows

	// Das neue Bitmuster in xorOverAllRows erhält man
	// durch xorOverAllRows xor mit alten Reihenwert und dann
	// xorOverAllRows xor mit neuem Reihenwert

	public Nim doMove(NimMove move) throws NimException {
		if (isOver()) {
			throw new NimException("Game is over");
		} else if (!isLegalMove(move)) {
			throw new NimException("Illegal move");
		}
		int row = move.getRow();
		int count = move.getCount();

		if (count == 0) {
			notEmptyRows--;
		}

		undoStack.add(new NimMove(row, rows[row]));
		lastMove=move;

		xorOverAllRows ^= rows[row];
		xorOverAllRows ^= count;

		rows[row] = count;
		return this;
	}

	public Nim undo() {
		// nimmt den letzten Zug zurück
		NimMove move = undoStack.pop();
		int row = move.getRow();
		int oldCount = rows[row];
		int newCount = move.getCount();

		if (oldCount == 0) {
			notEmptyRows++;
		}

		xorOverAllRows ^= oldCount;
		xorOverAllRows ^= newCount;

		rows[row] = newCount;
		return this;
	}

	public Nim undo(int nMoves) {
		// nimmt die letzten n Züge zurück
		if (nMoves > undoStack.size()) {
			throw new NimException("Illegal undo count");
		}
		for (int i = 0; i < nMoves; i++) {
			undo();
		}
		return this;
	}

	public Nim reset() {
		// stellt die Ausgangssituation wieder her
		while (!undoStack.empty()) {
			undo(undoStack.size());
		}
		return this;
	}

	public NimMove getLastMove() {
		return lastMove;
	}

	public NimMove getOldValueOflastMove() {
		return undoStack.peek();
	}

	boolean isOver() {
		return notEmptyRows == 0;
	}

	// 1
	public boolean isLegalMove(NimMove move) {
		int row = move.getRow();
		int count = move.getCount();
		if (row < 0 || row >= rows.length) {
			return false;
		} else if (count < 0 || count >= rows[row]) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int r : rows) {
			sb.append(toBinaryString(r) + " " + r + "\n");
		}
		sb.append("---------------------------------\n");
		sb.append(toBinaryString(xorOverAllRows) + " " + xorOverAllRows);

		return sb.toString();
	}

	public boolean isWinSituation() {
		return xorOverAllRows != 0;
	}

	public static String toBinaryString(int zahl) {
		StringBuffer sb = new StringBuffer(32);
		for (int n = (1 << 31); n != 0; n >>>= 1) {
			sb.append((zahl & n) != 0 ? 1 : 0);
		}
		return sb.toString();
	}

	public int[] getRows() {
		int[] rowsCopy = new int[rows.length];
		System.arraycopy(this.rows, 0, rowsCopy, 0, this.rows.length);
		return rowsCopy;
	}

	public int getCount(int row) {
		int result = -1;
		if (row >= 0 && row < rows.length) {
			result = rows[row];
		}
		return result;
	}
}
