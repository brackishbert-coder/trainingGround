package FlatLander;


import java.awt.Color;

import FlatLand.Physics.TypeOfEntity;


public class FlatLanderInstance extends FlatLander {

	public FlatLanderInstance( int x, int y, String name, double startingDir,
			boolean collidable, boolean shouldPhysicsApply, TypeOfEntity entityType, Color myColor) {
		super( x, y, name, startingDir, collidable, shouldPhysicsApply, entityType, myColor);
		// TODO Auto-generated constructor stub
	}

	public void updatecurrentBB() {
		// TODO Auto-generated method stub
		
	}

	public boolean above(FlatLander flatLanderToCheckForCollisions) {
		// TODO Auto-generated method stub
		return false;
	}

	public BoundingBox getCurrentflatLanderBB() {
		// TODO Auto-generated method stub
		return null;
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
