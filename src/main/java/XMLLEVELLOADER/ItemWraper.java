package XMLLEVELLOADER;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import Box.Box.Box;
import Box.Box.PromptObserver;
import Box.GameSpaceInterpreter.SandBox;
import Constructs.Point;
import FlatLand.Physics.Collidable;
import FlatLand.Physics.CollisionSide;
import FlatLand.Physics.TypeOfEntity;
import FlatLander.BoundingBox;
import FlatLander.FlatLander;
import ITM.A_ITM;
import flatLand.trainingGround.Sprites.ObserverPrompt;
import flatLand.trainingGround.Sprites.Sprites;
import flatLand.trainingGround.Sprites.TerminalSprite;
import theStart.thePeople.FlatLanderFaceBook;

public class ItemWraper extends A_ITM implements Collidable{
	protected Sprites sprite = null;
	protected BoundingBox previousflatLanderBB = new BoundingBox();
	private BoundingBox currentflatLanderBB = new BoundingBox();
	private boolean drawBB = false;
	
	private ByteArrayOutputStream baos;
	private SandBox box;
	XMLLEVELLOADER.FlatLanderWrper terminal = null;
	String terminalPath = "/home/wes/git/TrainingGround/TG/trainingGround/res/charmap-oldschool_white_sew.png";
	
	
	public ItemWraper( int x, int y, String name, double dir, boolean collidiable, boolean shouldPhysics,
			TypeOfEntity entityType, Color color2) {
		super( x, y, name, dir, collidiable, shouldPhysics, entityType, color2);
		// TODO Auto-generated constructor stub
	}



	public boolean above(FlatLander flatLanderToCheckForCollisions) {
		if (getCurrentflatLanderBB().getBottemLeft().getY() < flatLanderToCheckForCollisions.getCurrentflatLanderBB()
				.getTopLeft().getY()
				&& flatLanderToCheckForCollisions.getCurrentflatLanderBB().getTopLeft().getY()
						- getCurrentflatLanderBB().getBottemLeft().getY() == 1
				&& ((getCurrentflatLanderBB().getBottemLeft().getX() >= flatLanderToCheckForCollisions
						.getCurrentflatLanderBB().getTopLeft().getX()
						&& getCurrentflatLanderBB().getBottemLeft().getX() <= flatLanderToCheckForCollisions
								.getCurrentflatLanderBB().getTopRight().getX())
						|| (getCurrentflatLanderBB().getBottemRight().getX() >= flatLanderToCheckForCollisions
								.getCurrentflatLanderBB().getTopLeft().getX()
								&& getCurrentflatLanderBB().getBottemRight().getX() <= flatLanderToCheckForCollisions
										.getCurrentflatLanderBB().getTopRight().getX()))) {
			return true;
		}
		return false;
	}

	public BoundingBox getCurrentBoundingBox() {

		return this.getCurrentflatLanderBB();
	}
	
	private BoundingBox getPreviousBoundingBox() {

		return this.previousflatLanderBB;
	}

	
	public BoundingBox getCurrentflatLanderBB() {
		return currentflatLanderBB;
	}


	public void setCurrentflatLanderBB(BoundingBox currentflatLanderBB) {
		this.currentflatLanderBB = currentflatLanderBB;
	}
	public boolean isDrawBB() {
		return drawBB;
	}

	public void setDrawBB(boolean drawBB) {
		this.drawBB = drawBB;
	}
	public int collidesFrom(FlatLander flatLanderToCheckForCollisprotectedions) {

		int xdirection = previousX - (x + moveX);
		int ydirection = previousY - (y + moveY);
		if (xdirection < 0) {
			// moving right
			if (ydirection < 0) {
				// falling
				return 1;
			} else if (ydirection > 0) {
				// rising
				return 3;
			} else {
				// notMoving
				return 4;
			}

		} else if (xdirection > 0) {
			if (ydirection < 0) {
				// fallingkeyboardHandler
				return 1;
			} else if (ydirection > 0) {
				// rising
				return 3;
			} else {
				// notMoving
				return 2;
			}
		} else {
			if (ydirection < 0) {
				// falling
				return 1;
			} else if (ydirection > 0) {
				// rising
				return 3;
			} else {
				// notMoving
				return 0;
			}
		}
	}

	public int passesThroughSide(FlatLander flatLanderToCheckForCollisions) {
		int predictedX = flatLanderToCheckForCollisions.getX();
		int predictedY = flatLanderToCheckForCollisions.getY();

		return 0;
	}

	public boolean passesThrough(FlatLanderWrper flatLanderToCheckForCollisions) {
		return previousflatLanderBB.passesThrough(getCurrentflatLanderBB(),
				flatLanderToCheckForCollisions.getCurrentBoundingBox());
	}

	public Boolean collidesWith(FlatLanderWrper flatLandercollide) {
		if (collidable && flatLandercollide.collidable) {
			return this.getCurrentflatLanderBB().collidesWith(flatLandercollide.getCurrentBoundingBox());
		}
		return false;
	}

	public boolean passesThrough(PlayerWrper flatLanderToCheckForCollisions) {
		return previousflatLanderBB.passesThrough(getCurrentflatLanderBB(),
				flatLanderToCheckForCollisions.getCurrentBoundingBox());
	}

	public Boolean collidesWith(PlayerWrper flatLandercollide) {
		if (collidable && flatLandercollide.collidable) {
			return this.getCurrentflatLanderBB().collidesWith(flatLandercollide.getCurrentBoundingBox());

		}
		return false;
	}

	public Boolean collidesWith(int x, int y) {

		return this.getCurrentflatLanderBB().collidesWith(x, y);
	}
	@Override
	public void update() {
		super.update();
		sprite.updateState();
		previousflatLanderBB = new BoundingBox(getCurrentflatLanderBB());
		getCurrentflatLanderBB().updateBoundingBox(new Point(getX(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY() + this.sprite.getHeight()),
				new Point(getX(), getY() + this.sprite.getHeight()));
	}

	public void updatecurrentBB() {
		getCurrentflatLanderBB().updateBoundingBox(new Point(getX(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY() ),
				new Point(getX() + this.sprite.getWidth() , getY() + this.sprite.getHeight() ),
				new Point(getX(), getY() + this.sprite.getHeight() ));

	}
	public void setSprite(Sprites sprite) {
		this.sprite = sprite;
		getCurrentflatLanderBB().setBB(new Point(getX(), getY()), new Point(getX() + this.sprite.getWidth(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY() + this.sprite.getHeight()),
				new Point(getX(), getY() + this.sprite.getHeight()));
		previousflatLanderBB = new BoundingBox(getCurrentflatLanderBB());
	}

	public void draw(Graphics graphics) {
		BufferedImage update = sprite.update(this);
		graphics.drawImage(update, this.x, this.y, null);
	}

	@Override
	public void update(String key, boolean gameMode) {
		sprite.update(key, gameMode,false);
		
	}



	public void buildTerminal() {
		baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		System.setOut(ps);

		box = new SandBox(baos,FlatLanderFaceBook.getFlatlanderFaceBook(), null, null, null);
		TerminalSprite sprite2 = new TerminalSprite(terminalPath, 24, 6,200);
		PromptObserver promptOb = new PromptObserver(sprite2);
		ObserverPrompt obvPrompt = new ObserverPrompt(box);
		sprite2.addObserver(obvPrompt);

		box.addObserver(promptOb);

		terminal = new XMLLEVELLOADER.FlatLanderWrper(50, 10, 0, "terminal", 1.2, true, true, TypeOfEntity.TERRAIN, Color.BLACK);
		terminal.setSprite(sprite2);
		
	}


	@Override
	public boolean collidesWith(Collidable other) {
		if (collidable) {
			return this.getCurrentflatLanderBB().collidesWith(other.getCurrentBoundingBox());

		}
		return false;
	}

	@Override
	public boolean passesThrough(Collidable other) {
		return previousflatLanderBB.passesThrough(getCurrentflatLanderBB(),
				other.getCurrentBoundingBox());
	}

	@Override
	public CollisionSide collidesFrom(Collidable other) {

	    BoundingBox a = getCurrentflatLanderBB();
	    BoundingBox b = other.getCurrentBoundingBox();

	    int ax = a.x + a.width / 2;
	    int ay = a.y + a.height / 2;
	    int bx = b.x + b.width / 2;
	    int by = b.y + b.height / 2;

	    int dx = ax - bx;
	    int dy = ay - by;

	    int overlapX = (a.width / 2 + b.width / 2) - Math.abs(dx);
	    int overlapY = (a.height / 2 + b.height / 2) - Math.abs(dy);

	    if (overlapX < overlapY) {
	        return dx > 0 ? CollisionSide.RIGHT : CollisionSide.LEFT;
	    } else {
	        return dy > 0 ? CollisionSide.BOTTOM : CollisionSide.TOP;
	    }
	}

}
