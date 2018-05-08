package eu.erasmus.intelligentSystems.search.MazeBot;

public enum Direction {
	Up(0), Down(180), Left(270), Right(90);
	
	private double _degrees;
	
	Direction(double d) {
		_degrees = d;
	}
	
	public double get_degress() {
		return _degrees;
	}
}
