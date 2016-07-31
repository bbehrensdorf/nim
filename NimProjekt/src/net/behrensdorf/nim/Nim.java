package net.behrensdorf.nim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Nim {

	private static final int MAX_TOKENS_PER_ROW = 10;
	private static final int MIN_TOKENS_PER_ROW = 2;
	private static final int DEFAULT_ROWS = 6;

	int[] rows;
	int notEmptyRows;
	int xorOverAllRows;

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
		if (!isWinSituation()) {
			move = randomMove();
		} else {
			List<Integer> winMoves = new ArrayList<>();
			int highestOneBit = Integer.highestOneBit(xorOverAllRows);
			for (int i = 0; i < rows.length; i++) {
				if ((rows[i] & highestOneBit) != 0) {
					winMoves.add(i);
				}
			}

			int row = winMoves.get(rnd.nextInt(winMoves.size()));
			int count = rows[row];
			count ^= xorOverAllRows;
			move = new NimMove(row, count);
		}
		return move;
	}

	public NimMove suggestBadMove() {
		if (isOver()) {
			throw new NimException("Game is over");
		}
		NimMove move = null;
		if (!isWinSituation()) {
			move = randomMove();
		} else {
			List<Integer> looseMoves = new ArrayList<>();
			int highestOneBit = Integer.highestOneBit(xorOverAllRows);
			for (int i = 0; i < rows.length; i++) {
				if ((rows[i] > 0) && ((rows[i] & highestOneBit) == 0)) {
					looseMoves.add(i);
				}
			}
			if (looseMoves.size() == 0) {
				move = randomMove();
			} else {
				int row = looseMoves.get(rnd.nextInt(looseMoves.size()));
				int count = rows[row];
				count = rnd.nextInt(count);
				move = new NimMove(row, count);
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
	// aktualisiere ggf. norEmptyRows

	// Das neue Bitmuster in xorOverAllRows erhält man
	// durch

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

		xorOverAllRows ^= rows[row];
		xorOverAllRows ^= count;

		rows[row] = count;

		return this;
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
