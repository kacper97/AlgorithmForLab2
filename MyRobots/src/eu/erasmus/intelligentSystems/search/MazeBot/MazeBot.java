package eu.erasmus.intelligentSystems.search.MazeBot;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import robocode.Robot;

public class MazeBot extends Robot {
	private int _fieldSize = 30;
	private int _obstacleSeed = 20011;
	private int _agentSeed = _obstacleSeed;
	private int _numObstacles = _fieldSize * _fieldSize * 3 / 10;
	private boolean[] _closedCells;
	private Cell _currentCell;
	private Cell _goalCell;
	private List<Cell> _pathFromStartToEnd;
	private Direction _direction = Direction.Up;
	
	public MazeBot(){ 
		Random generator = new Random(_obstacleSeed);
		_closedCells = new boolean[_fieldSize*_fieldSize];
		// Injecting the same picuture of map that is generated also in RoutFinder class
		for (int currentObstacle = 0; currentObstacle < _numObstacles; currentObstacle++) {
			int initialObstacleRow = generator.nextInt(_fieldSize);
			int initialObstacleCol = generator.nextInt(_fieldSize);
			while (_closedCells[initialObstacleRow*_fieldSize+initialObstacleCol]) {
				initialObstacleRow = generator.nextInt(_fieldSize);
				initialObstacleCol = generator.nextInt(_fieldSize);
			}
			_closedCells[initialObstacleRow*_fieldSize+initialObstacleCol] = true;
		}
		// Injecting agent's position
		generator = new Random(_agentSeed);
		int i = generator.nextInt(_fieldSize);
		int j = generator.nextInt(_fieldSize);
		while (_closedCells[i*_fieldSize+j]) {
			i = generator.nextInt(_fieldSize);
			j = generator.nextInt(_fieldSize);
		}
		_currentCell = new Cell(i,j);
		_closedCells[i*_fieldSize+j] = true;
		// Generate the goal state. Doesn't need to be synced with RouteFinder Class
		i = generator.nextInt(_fieldSize);
		j = generator.nextInt(_fieldSize);
		while (_closedCells[i*_fieldSize+j]) {
			i = generator.nextInt(_fieldSize);
			j = generator.nextInt(_fieldSize);
		}
		_goalCell = new Cell(i,j); // ?Set the goal on the edge of the map? Not the best idea because it is not unusual that all cells on edges are occupied
	}
	
	public void run() {
		int tileSize = 64;
		_pathFromStartToEnd = FindRoute();
		if (_pathFromStartToEnd == null) { 
			this.setBodyColor(Color.RED);	// If goal cannot be reached, robot will not move, jsut turn red instead of the usual blue color
			return;
		}
		for(int i = 1; i < _pathFromStartToEnd.size(); i++) {
			Cell next = _pathFromStartToEnd.get(i);
			changeOrientationByNextPosition(next);
			_currentCell = next;
			this.ahead(tileSize); 
		}
	}
	
	private void changeOrientationByNextPosition(Cell nextCell) {
		Direction newDirection = whichWayToMove(nextCell.get_row(), nextCell.get_col());
		double difference = newDirection.get_degress() - _direction.get_degress();
		if (Math.abs(difference) > 180) {  // This secures that robots never makes bigger turn than 180 degrees, so his movement is more nautral
			if(difference > 0) {
				difference = difference - 360;
			}
			else {
				difference = 360 + difference;
			}
		}
		turnRight(difference);
		_direction = newDirection;
	}
	
	private Direction whichWayToMove(int nextRow, int nextCol) {
		if (nextRow != _currentCell.get_row()) {
			if (nextRow == _currentCell.get_row() - 1) {
				return Direction.Down;
			}
			else {
				return Direction.Up;
			}
		}
		else if (nextCol != _currentCell.get_col()) {
			if (nextCol == _currentCell.get_col() - 1) {
				return Direction.Left;
			}
			else {
				return Direction.Right;
			}
		}
		else { assert false; return null; }
	}
	
	// A* implementation
	// Returns path from start to goal as sequence of cells
	private List<Cell> FindRoute() {
		PriorityQueue<EvaluatedCell> queue = new PriorityQueue<>();
		int[] estimatedCells = createEstimation();
		EvaluatedCell currentCellWhileFindingRoute = new EvaluatedCell(_currentCell,0,0,new ArrayList<Cell>()); // Estimation here is not going to be taken into account since the cell 
			 																								  // will not appear in the queue
		while(currentCellWhileFindingRoute != null && currentCellWhileFindingRoute.get_cell().compareTo(_goalCell) != 0) {
			_closedCells[currentCellWhileFindingRoute.get_row()*_fieldSize+currentCellWhileFindingRoute.get_col()] = true; // After choosing we close the cell
			queue.addAll(get_neighbours(currentCellWhileFindingRoute,queue,estimatedCells));
			while(currentCellWhileFindingRoute != null && _closedCells[currentCellWhileFindingRoute.get_row()*_fieldSize+currentCellWhileFindingRoute.get_col()]) 
			{currentCellWhileFindingRoute = queue.poll();} // In case that one cell was added from two or more different paths, after the best is taken into account its unnecessary to discover paths leading to the cell by alternative paths
		}
		if (currentCellWhileFindingRoute == null) { // The goal state is not accesible so we haven't found any path
			return null;
		}
		return currentCellWhileFindingRoute.get_pathFromStart(); 
	}
	
	// Heuristic function result written into every cell, manhattan distance from the goal state
	private int[] createEstimation() {
		int[] evaluated = new int[_fieldSize*_fieldSize];
		for(int i = 0; i < _fieldSize; i++) {
			for(int j = 0; j < _fieldSize; j++) {
				if (_closedCells[i*_fieldSize + j]) continue; // Avoiding computation for obstacles
				evaluated[i*_fieldSize + j] = Math.abs(i - _goalCell.get_row()) + Math.abs(j - _goalCell.get_col()); // Manhattan distance 
			}
		}
		return evaluated;
	}
	
	private List<EvaluatedCell> get_neighbours(EvaluatedCell middle,PriorityQueue<EvaluatedCell> queue,int[] estimatedCells){
		List<EvaluatedCell> result = new ArrayList<>();
		Cell c1 = new Cell(middle.get_row(),middle.get_col() + 1);
		Cell c2 = new Cell(middle.get_row(),middle.get_col() - 1);
		Cell c3 = new Cell(middle.get_row() - 1, middle.get_col());
		Cell c4 = new Cell(middle.get_row() + 1, middle.get_col());
		addCellToListIfCorrect(result,c1,middle,estimatedCells);
		addCellToListIfCorrect(result,c2,middle,estimatedCells);
		addCellToListIfCorrect(result,c3,middle,estimatedCells);
		addCellToListIfCorrect(result,c4,middle,estimatedCells);
		return result;
	}
	
	private void addCellToListIfCorrect(List<EvaluatedCell> list, Cell c1, EvaluatedCell middle,int[] estimatedCells) {
		int cost;
		int estimation;
		if(c1.get_col() < _fieldSize &&
				c1.get_col() >= 0 &&
				c1.get_row() < _fieldSize &&
				c1.get_row() >= 0 &&
				!_closedCells[c1.get_row()*_fieldSize+c1.get_col()] ) { 
			cost = middle.get_costFromStart() + 1; // Since it's middle's neighbour and the path leads through middle it is one step further from the start than middle
			estimation = estimatedCells[c1.get_row()*_fieldSize+c1.get_col()];
			List<Cell> path = new ArrayList<Cell>(middle.get_pathFromStart());
			list.add(new EvaluatedCell(c1, estimation, cost, path)); 
			}
	}
}


