package Constructs;

public class Point implements Construct {
	private int x;
	private int y;

	public Point(int pointx, int pointy) {
		x = pointx;
		y = pointy;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void addX(int moveX) {
		x=x+moveX;
	}

	public void addY(int moveY) {
	y=y+moveY;
	}
}
