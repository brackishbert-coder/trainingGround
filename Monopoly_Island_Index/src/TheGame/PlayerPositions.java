package TheGame;

import java.util.ArrayList;

import TheGame.PlayerPositions.Position;

public class PlayerPositions {

	private int starty;
	private int startx;
	private int startt;

	ArrayList<Position> positions= new ArrayList<>(); 
private int[] dimensionsBeyondTime;
	public PlayerPositions(Status status,int x,int y,int startt,int ...n) {
		this.startx = x;
		this.starty = y;
		this.startt = startt;
		positions.add(new Position(status,x,y,startt,n));
	}

	public class Position {

		Status status=Status.GO;
		private int t;
		private int[] n;
		private Status stat;
		private int x;
		private int y;

		public Position(Status status,int x,int y, int t,int ...n ) {
			this.stat = status;
			this.x = x;
			this.y = y;
			this.setT(t);
			dimensionsBeyondTime = new int[n.length];
			for (int i = 0; i < n.length; i++) {
				dimensionsBeyondTime[i]=n[i];
			}
		}
		
		
		public int[] getAllDimensionsBeyondTime(){
			return dimensionsBeyondTime;
		}
		
		
		public int getDimensionLbetweenTandN(int indextplus1){
			return dimensionsBeyondTime[indextplus1];
		}
		
		


		public int getT() {
			return t;
		}

		public void setT(int t) {
			this.t = t;
		}


		public Status getStatus() {
			return stat;
		}


		public void setStatus(Status stat) {
			this.stat = stat;
		}


		public int getX() {
			return x;
		}


		public void setX(int x) {
			this.x = x;
		}


		public int getY() {
			return y;
		}


		public void setY(int y) {
			this.y = y;
		}
		
	}

	public int getCurrentXPosition() {
		return positions.get(positions.size()-1).getX();
		
	}

	public int getCurrentYPosition() {
	
	return positions.get(positions.size()-1).getY();
	}

	
	
	
	
}
