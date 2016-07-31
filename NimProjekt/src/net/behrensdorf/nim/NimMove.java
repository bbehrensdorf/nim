package net.behrensdorf.nim;

public class NimMove {
	int row;
	int count;

	NimMove(int row, int count) {
		this.row = row;
		this.count = count;
	}

	int getRow() {
		return row;
	}

	int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "NimMove [row=" + row + ", count=" + count + "]";
	}
}