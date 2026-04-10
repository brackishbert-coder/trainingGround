package Player;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Constructs.Point;
import FlatLand.Physics.TypeOfEntity;
import FlatLander.BoundingBox;
import FlatLander.FlatLander;

public class Player extends FlatLander{

	

	private PlayerState playerState;
	private BoundingBox previousflatLanderBB;
	
	



	public Player(Color myColor, int x, int y, String name, double startingDir,
			boolean collidable) {
		super( x, y, name, startingDir, collidable,true,TypeOfEntity.PLAYER, myColor);
		
		
		playerState = PlayerState.STILL;
	}

	
	
	@Override
	public void update() {
		
	moveX();
		moveY();
		time++;
		
	}






	public PlayerState state() {
		
		return playerState;
	}



	public void setState(PlayerState state) {
		playerState = state;
	}



	public int getPreviousX() {
		return previousX;
	}



	public int getPreviousY() {
		return previousY;
	}



	public void updatecurrentBB() {
		// TODO Auto-generated method stub
		
	}



	public BoundingBox getCurrentflatLanderBB() {
		// TODO Auto-generated method stub
		return null;
	}



	public boolean above(FlatLander flatLanderToCheckForCollisions) {
		// TODO Auto-generated method stub
		return false;
	}



	public void setCurrentflatLanderBB(BoundingBox currentflatLanderBB) {
		// TODO Auto-generated method stub
		
	}



	public boolean isDrawBB() {
		// TODO Auto-generated method stub
		return false;
	}



	public void setDrawBB(boolean drawBB) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void update(String key, boolean gameMode) {
		// TODO Auto-generated method stub
		
	}





	


	
	
	
}
