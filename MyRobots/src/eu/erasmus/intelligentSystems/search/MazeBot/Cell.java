package eu.erasmus.intelligentSystems.search.MazeBot;

public class Cell implements Comparable<Cell> {
	private int _row;
	private int _col;
	public Cell(int row,int col) {
		_row = row;
		_col = col;
	}
	public int get_row() {
		return _row;
	}
	public int get_col() {
		return _col;
	}
	@Override
	public int compareTo(Cell o) {
		if(_row != o.get_row()) { 
			return _row - o.get_row();
		}
		else{
			return _col - o.get_col();
		}
	}
	
}
