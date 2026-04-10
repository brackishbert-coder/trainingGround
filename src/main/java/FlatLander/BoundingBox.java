package FlatLander;

import Constructs.Point;

public class BoundingBox {
	private Point topLeft;
	private Point topRight;
	private Point bottemRight;
	private Point bottemLeft;
	public int x;
	public int width;
	public int height;
	public int y;

	public BoundingBox(BoundingBox BB) {
		setTopLeft(BB.getTopLeft());
		setTopRight(BB.getTopRight());
		setBottemRight(BB.getBottemRight());
		setBottemLeft(BB.getBottemLeft());
	}

	public BoundingBox() {
	}

	public void setBB(Point tl, Point tr, Point br, Point bl) {
		setTopLeft(tl);
		setTopRight(tr);
		setBottemRight(br);
		setBottemLeft(bl);
		x=tl.getX();
		y=tl.getY();
		width=Math.abs(tr.getX()-tl.getY());
		height=Math.abs(bl.getY()-tl.getY());
	}

	public boolean collidesWith(BoundingBox boundingBox) {
		if (checkPointInBB(boundingBox, getTopLeft(),getBottemLeft(),getTopRight(),getBottemRight())) {
			return true;
		}

		return false;

	}
	public boolean collidesWith(int x,int y) {
		if (checkPointInBB(x,y, getTopLeft(),getBottemLeft(),getTopRight(),getBottemRight())) {
			return true;
		}
		
		return false;
		
	}


	private boolean checkPointInBB(int x,int y, Point topLeft, Point bottemLeft, Point topRight, Point bottemRight) {
		if (topLeft.getX()  <= x && topRight.getX() >= x) {
			if (topLeft.getY()<=y&&bottemLeft.getY()>=y) {
				return true;
			}
		}

		
		return false;
	}
	
	private boolean checkPointInBB(BoundingBox boundingBox, Point topLeft, Point bottemLeft, Point topRight, Point bottemRight) {
		if (topLeft.getX()  >= boundingBox.getTopLeft().getX()&& topRight.getX() <= boundingBox.getTopRight().getX()) {
			if (topLeft.getY()>=boundingBox.getTopLeft().getY()&&bottemLeft.getY()<=boundingBox.getBottemLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;// TODO Auto-generated method stub
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getTopLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getBottemLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}
		}
		
		if(topLeft.getX()  < boundingBox.getTopLeft().getX()&& (topRight.getX() > boundingBox.getTopLeft().getX()&&topRight.getX() < boundingBox.getTopRight().getX())) {
			//left side
			if (topLeft.getY()>=boundingBox.getTopLeft().getY()&&bottemLeft.getY()<=boundingBox.getBottemLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getTopLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getBottemLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}
		}
		if((topLeft.getX()  < boundingBox.getTopRight().getX()&&topLeft.getX()> boundingBox.getTopLeft().getX())&& topRight.getX() > boundingBox.getTopRight().getX()) {
			//right side
			if (topLeft.getY()>=boundingBox.getTopLeft().getY()&&bottemLeft.getY()<=boundingBox.getBottemLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getTopLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getBottemLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}
		}
		if(topLeft.getX()  < boundingBox.getTopLeft().getX()&& topRight.getX() > boundingBox.getTopRight().getX()) {
			//right side
			if (topLeft.getY()>=boundingBox.getTopLeft().getY()&&bottemLeft.getY()<=boundingBox.getBottemLeft().getY()) {
				return true;// TODO Auto-generated method stub
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getTopLeft().getY()&& bottemLeft.getY()>=boundingBox.getTopLeft().getY()) {
				return true;
			}else if(topLeft.getY()<= boundingBox.getBottemLeft().getY()&& bottemLeft.getY()>=boundingBox.getBottemLeft().getY()) {
				return true;
			}
		}
		
		return false;
	}

	
	
	
	
	
	
	
	
	public Point getTopLeft() {
		return topLeft;
	}

	public void setTopLeft(Point topLeft) {
		this.topLeft = topLeft;
	}

	public Point getTopRight() {
		return topRight;
	}

	public void setTopRight(Point topRight) {
		this.topRight = topRight;
	}

	public Point getBottemRight() {
		return bottemRight;
	}

	public void setBottemRight(Point bottemRight) {
		this.bottemRight = bottemRight;
	}
	// TODO Auto-generated method stub
	public Point getBottemLeft() {
		return bottemLeft;
	}

	public void setBottemLeft(Point bottemLeft) {
		this.bottemLeft = bottemLeft;
	}

	public void updateBoundingBox(Point point, Point point2, Point point3, Point point4) {
		setTopLeft(point);
		setTopRight(point2);
		setBottemRight(point3);
		setBottemLeft(point4);
	}

	public boolean passesThrough(BoundingBox currentflatLanderBB, BoundingBox toCheckIfPassedThrough) {

		// right to left// TODO Auto-generated method stub
		if (getTopLeft().getX() >= toCheckIfPassedThrough.getTopLeft().getX()
				&& getTopRight().getX() >= toCheckIfPassedThrough.getTopRight().getX()
				&& getBottemLeft().getX() >= toCheckIfPassedThrough.getBottemLeft().getX()
				&& getBottemRight().getX() >= toCheckIfPassedThrough.getBottemRight().getX()) {
			if (currentflatLanderBB.getTopLeft().getX() <= toCheckIfPassedThrough.getTopLeft().getX()
					&& currentflatLanderBB.getTopRight().getX() <= toCheckIfPassedThrough.getTopRight().getX()
					&& currentflatLanderBB.getBottemLeft().getX() <= toCheckIfPassedThrough.getBottemLeft().getX()
					&& currentflatLanderBB.getBottemRight().getX() <= toCheckIfPassedThrough.getBottemRight().getX()) {
				return true;
			}
		}
		// left to right
		if (getTopLeft().getX() <= toCheckIfPassedThrough.getTopLeft().getX()
				&& getTopRight().getX() <= toCheckIfPassedThrough.getTopRight().getX()
				&& getBottemLeft().getX() <= toCheckIfPassedThrough.getBottemLeft().getX()
				&& getBottemRight().getX() <= toCheckIfPassedThrough.getBottemRight().getX()) {
			if (currentflatLanderBB.getTopLeft().getX() >= toCheckIfPassedThrough.getTopLeft().getX()
					&& currentflatLanderBB.getTopRight().getX() >= toCheckIfPassedThrough.getTopRight().getX()
					&& currentflatLanderBB.getBottemLeft().getX() >= toCheckIfPassedThrough.getBottemLeft().getX()
					&& currentflatLanderBB.getBottemRight().getX() >= toCheckIfPassedThrough.getBottemRight().getX()) {
				return true;
			}
		}
		// top to bottem
		if (getTopLeft().getY() >= toCheckIfPassedThrough.getTopLeft().getY()
				&& getTopRight().getY() >= toCheckIfPassedThrough.getTopRight().getY()
				&& getBottemLeft().getY() >= toCheckIfPassedThrough.getBottemLeft().getY()
				&& getBottemRight().getY() >= toCheckIfPassedThrough.getBottemRight().getY()) {
			if (currentflatLanderBB.getTopLeft().getY() <= toCheckIfPassedThrough.getTopLeft().getY()
					&& currentflatLanderBB.getTopRight().getY() <= toCheckIfPassedThrough.getTopRight().getY()
					&& currentflatLanderBB.getBottemLeft().getY() <= toCheckIfPassedThrough.getBottemLeft().getY()
					&& currentflatLanderBB.getBottemRight().getY() <= toCheckIfPassedThrough.getBottemRight().getY()) {

				if ((currentflatLanderBB.getTopLeft().getX() >= toCheckIfPassedThrough.getTopLeft().getX()
						|| currentflatLanderBB.getTopRight().getX() >= toCheckIfPassedThrough.getTopLeft().getX())
						&& (currentflatLanderBB.getTopLeft().getX() <= toCheckIfPassedThrough.getTopRight().getX()
								|| currentflatLanderBB.getTopRight().getX() <= toCheckIfPassedThrough.getTopRight()
										.getX())) {

					return true;
				}
			}
		}
		// bottem to top
		if (getTopLeft().getY() <= toCheckIfPassedThrough.getTopLeft().getY()
				&& getTopRight().getY() <= toCheckIfPassedThrough.getTopRight().getY()
				&& getBottemLeft().getY() <= toCheckIfPassedThrough.getBottemLeft().getY()
				&& getBottemRight().getY() <= toCheckIfPassedThrough.getBottemRight().getY()) {
			if (currentflatLanderBB.getTopLeft().getY() >= toCheckIfPassedThrough.getTopLeft().getY()
					&& currentflatLanderBB.getTopRight().getY() >= toCheckIfPassedThrough.getTopRight().getY()
					&& currentflatLanderBB.getBottemLeft().getY() >= toCheckIfPassedThrough.getBottemLeft().getY()
					&& currentflatLanderBB.getBottemRight().getY() >= toCheckIfPassedThrough.getBottemRight().getY()) {
				if ((currentflatLanderBB.getTopLeft().getX() >= toCheckIfPassedThrough.getTopLeft().getX()
						|| currentflatLanderBB.getTopRight().getX() >= toCheckIfPassedThrough.getTopLeft().getX())
						&& (currentflatLanderBB.getTopLeft().getX() <= toCheckIfPassedThrough.getTopRight().getX()
								|| currentflatLanderBB.getTopRight().getX() <= toCheckIfPassedThrough.getTopRight()
										.getX())) {

					return true;
				}
			}
		}

		return false;

	}

	public boolean contains(int x, int y) {
		if(getTopLeft().getX()<=x && x<=getTopRight().getX() && getTopRight().getY()<=y && getBottemLeft().getY()>=y)
			return true;
		return false;
	}

}
