package FlatLandStructure;

import java.awt.Color;
import java.lang.reflect.Array;
import FlatLander.FlatLander;





public class BaseFlatLand<T> {

	public int width;
	public int height;
	private Boolean isWraped;
	private T[][] theFlatLandVector;
	private Class<T> cls;
	
	public BaseFlatLand(Class<T> cls,int flatLandWidth, int flatLandHeight, Boolean isWraped) {
		this.cls = cls;
		this.width = flatLandWidth;
		this.height = flatLandHeight;
		this.isWraped = isWraped;
		this.theFlatLandVector = (T[][])Array.newInstance(cls, width, height);	
		
		
		
	}
	

	

	
	
	

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}


	
	
	public Boolean getIsWraped() {
		return isWraped;
	}

	public void setIsWraped(Boolean isWraped) {
		this.isWraped = isWraped;
	}

	
	public int getYBoundedAndWraped(FlatLander flatLander) {
		int y = flatLander.getY();
		int flatLandHeight = getHeight() - 1;
		if (y < 0) {
			int temp = Math.abs(y);
			if (temp > flatLandHeight) {
				temp = temp % flatLandHeight;
				y = flatLandHeight - temp;
			} else {
				y = flatLandHeight - temp;
			}

		} else if (y >= flatLandHeight) {
			y = y % flatLandHeight;
		}

		return y;
	}

	public int getYBoundedAndWraped(int ypos) {
		int y = ypos;
		int flatLandHeight = getHeight() - 1;
		if (y < 0) {
			int temp = Math.abs(y);
			if (temp > flatLandHeight) {
				temp = temp % flatLandHeight;
				y = flatLandHeight - temp;
			} else {
				y = flatLandHeight - temp;
			}

		} else if (y >= flatLandHeight) {
			y = y % flatLandHeight;
		}

		return y;
	}

	public int getYUnWraped(FlatLander flatLander) {
		int y = flatLander.getY();
		int flatLandHeight = getHeight() - 1;
		if (y < 0) {
			y = 0;
		} else if (y >= flatLandHeight) {
			y = flatLandHeight;
		}
		return y;
	}

	public int getYUnWraped(int ypos) {
		int y = ypos;
		int flatLandHeight = getHeight() - 1;
		if (y < 0) {
			y = 0;
		} else if (y >= flatLandHeight) {
			y = flatLandHeight;
		}
		return y;
	}

	public int getXBoundedAndWraped(FlatLander flatLander) {
		int x = flatLander.getX();
		int flatlandWidth = getWidth() - 1;
		if (x < 0) {
			int temp = Math.abs(x);
			if (temp > flatlandWidth) {
				temp = temp % flatlandWidth;
				x = flatlandWidth - temp;
			} else {
				x = flatlandWidth - temp;
			}
		} else if (x >= flatlandWidth) {
			x = x % flatlandWidth;
		}

		return x;
	}

	public int getXBoundedAndWraped(int xpos) {
		int x = xpos;
		int flatlandWidth = getWidth() - 1;
		if (x < 0) {
			int temp = Math.abs(x);
			if (temp > flatlandWidth) {
				temp = temp % flatlandWidth;
				x = flatlandWidth - temp;
			} else {
				x = flatlandWidth - temp;
			}
		} else if (x >= flatlandWidth) {
			x = x % flatlandWidth;
		}

		return x;
	}

	public int getXUnWraped(int xpos) {
		int x = xpos;
		int flatlandWidth = getWidth() - 1;
		if (x < 0) {
			x = 0;
		} else if (x >= flatlandWidth) {
			x = flatlandWidth;
		}
		return x;
	}

	public int getXUnWraped(FlatLander flatLander) {
		int x = flatLander.getX();
		int flatlandWidth = getWidth() - 1;
		if (x < 0) {
			x = 0;
		} else if (x >= flatlandWidth) {
			x = flatlandWidth;
		}
		return x;
	}



	public void setFlatLandAt(int xpos, int ypos, T object) {
		theFlatLandVector[xpos][ypos]=object;
	}
	
	public void clearFlatland() {
		
		this.theFlatLandVector = (T[][])Array.newInstance(cls, width, height);
		
		
	}

	
	
	public T getFlatLandAt(int xpos,int ypos) {
		return theFlatLandVector[xpos][ypos];
	}
	
	
	
}
