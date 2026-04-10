package flatLand.trainingGround;

public class GameEvent {

	
	private int x =0 ; 
	private int y = 0;
	private String name;
	private String type;
	
	public GameEvent(Integer xpos, Integer ypos, String name, String type) {
		this.x = xpos;
		this.y = ypos;
		this.type = type;
		this.setName(name);
	}

	public int getX() {
		// TODO Auto-generated method stub
		return x;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEventWithinDistsnce(int x, int y, int distance) {
	
			int dist = (int) Math.sqrt( Math.pow(x-this.getX(),2) + Math.pow( y-this.getY() ,2));
			if (dist <= distance) {
				return true;
			}
	
		return false;
	}

	public String getType() {
		return type;
	}

}
