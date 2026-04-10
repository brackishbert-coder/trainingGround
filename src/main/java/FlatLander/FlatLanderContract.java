package FlatLander;

import java.awt.Color;


import Constructs.Construct;
import Constructs.Point;
import FlatLand.Physics.TypeOfEntity;



public interface FlatLanderContract {

	Color getColor();

	int getX();

	int getY();

	void update();
	void updatecurrentBB();
	

	void pushtoMemory(Point point);

	Construct popMemory();

	Construct peekMemory();

	String getName();

	void setMoveX(int moveX);

	void setMoveY(int moveY);

	void moveX();

	void moveY();

	void setX(int i);
	void setY(int i);
	int getMoveY();
	int getMoveX();
	boolean shouldPhysicsApply();
	TypeOfEntity getEntityType();
	int getPreviousX();
	int getPreviousY();
	void setPreviousX(int previousX);

	void setPreviousY(int previousY);
	void changeMoveYBy(int i);
	void changeMoveXBy(int i);
	boolean above(FlatLander flatLanderToCheckForCollisions);
	FlatLanderClassification getClassification();
	void setClassification(FlatLanderClassification classification);
	BoundingBox getCurrentflatLanderBB();
	void setCurrentflatLanderBB(BoundingBox currentflatLanderBB);
	boolean isDrawBB();
	void setDrawBB(boolean drawBB);

	void update(String key, boolean gameMode);
}
