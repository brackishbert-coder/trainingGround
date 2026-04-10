package PER;

import java.awt.Color;

import Constructs.Construct;
import Constructs.Point;
import FlatLand.Physics.TypeOfEntity;
import FlatLander.BoundingBox;
import FlatLander.FlatLander;
import FlatLander.FlatLanderClassification;

public abstract class A_Perminant implements Perminant{

	public A_Perminant() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatecurrentBB() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pushtoMemory(Point point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Construct popMemory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Construct peekMemory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMoveX(int moveX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMoveY(int moveY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveX() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveY() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setX(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setY(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMoveY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMoveX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean shouldPhysicsApply() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TypeOfEntity getEntityType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPreviousX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPreviousY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPreviousX(int previousX) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPreviousY(int previousY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeMoveYBy(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeMoveXBy(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean above(FlatLander flatLanderToCheckForCollisions) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FlatLanderClassification getClassification() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClassification(FlatLanderClassification classification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BoundingBox getCurrentflatLanderBB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCurrentflatLanderBB(BoundingBox currentflatLanderBB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDrawBB() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDrawBB(boolean drawBB) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(String key, boolean gameMode) {
		// TODO Auto-generated method stub
		
	}

}
