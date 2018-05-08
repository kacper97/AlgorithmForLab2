package eu.erasmus.intelligentSystems.search.MazeBot;

import java.util.List;

public class EvaluatedCell implements Comparable<EvaluatedCell> {
	private Cell _cell;
	private int _estimation;
	private int _cost;
	private List<Cell> _path;
	public	EvaluatedCell(Cell cell,int estimationToGoal,int costFromStart,List<Cell> path) {
		_cell = cell;
		_estimation = estimationToGoal;
		_cost = costFromStart;
		_path = path;
		_path.add(_cell);
	}
	
	public int get_evaluation() {
		return _cost + _estimation;
	}
	
	public int get_costFromStart() {
		return _cost;
	}
	
	
	public Cell get_cell() {
		return _cell;
	}
	
	public int get_row() {
		return _cell.get_row();
	}
	public int get_col() {
		return _cell.get_col();
	}
	
	public List<Cell> get_pathFromStart(){
		return _path;
	}
	@Override
	public int compareTo(EvaluatedCell o) {
		if(o.get_evaluation() != get_evaluation()) {
			return get_evaluation() - o.get_evaluation();
		}
		return get_cell().compareTo(o.get_cell());  // Secondary critrium based on rows/cols values, 
												   // doesn't really matter, just to be consistent and not 
												   // to treat two EvaluatedCells like they are the same
	}
}
