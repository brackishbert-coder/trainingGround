package XMLLEVELLOADER;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import Actions.ActionStack;
import Actions.ActionsInterface;
import Actions.NoAction;
import Box.Box.PromptObserver;
import Box.GameSpaceInterpreter.SandBox;
import Constructs.Point;
import FlatLand.Physics.Collidable;
import FlatLand.Physics.CollisionSide;
import FlatLand.Physics.TypeOfEntity;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.BoundingBox;
import FlatLander.FlatLander;
import dialogManagement.DialogManager;
import flatLand.trainingGround.EventHandler;
import flatLand.trainingGround.Sprites.ObserverPrompt;
import flatLand.trainingGround.Sprites.Sprites;
import flatLand.trainingGround.Sprites.TerminalSprite;
import theStart.thePeople.FlatLanderFaceBook;
import theStart.theView.TheControls.GameScreen;
import userInput.PlayerKeybordHandler;

public class FlatLanderWrper extends FlatLander implements Terminal ,Collidable{

	protected Sprites sprite = null;
	protected BoundingBox previousflatLanderBB = new BoundingBox();
	private BoundingBox currentflatLanderBB = new BoundingBox();
	private boolean drawBB = false;

	protected ActionsInterface preferedAction;
	protected ActionStack actions;
	
	private ByteArrayOutputStream baos;
	private SandBox box;
	XMLLEVELLOADER.FlatLanderWrper terminal = null;
	String terminalPath = "/home/wes/gitworkspace/trainingGround/res/charmap-oldschool_white_sew.png";
	private userInput.PlayerKeybordHandler keyboardHandler;

	public FlatLanderWrper(int x, int y, int z, String name, double startingDir,
			boolean collidable, boolean shouldPhysicsApply, TypeOfEntity entityType, Color myColor) {
		super(x, y, name, startingDir, collidable, shouldPhysicsApply, entityType, myColor);
		this.setPreferedAction(new NoAction(this));
		this.setActionStack(new ActionStack(new ArrayList<ActionsInterface>()));

		// TODO Auto-generated constructor stub
	}

	public Sprites getSprite() {
		return sprite;
	}

	public void setSprite(Sprites sprite) {
		this.sprite = sprite;
		getCurrentflatLanderBB().setBB(new Point(getX(), getY()), new Point(getX() + this.sprite.getWidth(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY() + this.sprite.getHeight()),
				new Point(getX(), getY() + this.sprite.getHeight()));
		previousflatLanderBB = new BoundingBox(getCurrentflatLanderBB());
	}

	@Override
	public void update() {
		int act = actions.act();
		if (act == -1)
			doPreferedActions();
		
		if (terminal != null) {
			terminal.x = this.x;
			terminal.y = this.y - 10;
			try {
				box.runPrompt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		previousflatLanderBB = new BoundingBox(getCurrentflatLanderBB());
		currentflatLanderBB.updateBoundingBox(new Point(getX(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY()),
				new Point(getX() + this.sprite.getWidth(), getY() + this.sprite.getHeight()),
				new Point(getX(), getY() + this.sprite.getHeight()));
		super.update();
	}

	public void updatecurrentBB() {
		currentflatLanderBB.updateBoundingBox(new Point(getX() + moveX, getY() + moveY),
				new Point(getX() + this.sprite.getWidth() + moveX, getY() + moveY),
				new Point(getX() + this.sprite.getWidth() + moveX, getY() + this.sprite.getHeight() + moveY),
				new Point(getX() + moveX, getY() + this.sprite.getHeight() + moveY));

	}

	public boolean above(FlatLander flatLanderToCheckForCollisions) {
		if (currentflatLanderBB.getBottemLeft().getY() < flatLanderToCheckForCollisions.getCurrentflatLanderBB()
				.getTopLeft().getY()
				&& flatLanderToCheckForCollisions.getCurrentflatLanderBB().getTopLeft().getY()
						- currentflatLanderBB.getBottemLeft().getY() == 1
				&& ((currentflatLanderBB.getBottemLeft().getX() >= flatLanderToCheckForCollisions
						.getCurrentflatLanderBB().getTopLeft().getX()
						&& currentflatLanderBB.getBottemLeft().getX() <= flatLanderToCheckForCollisions
								.getCurrentflatLanderBB().getTopRight().getX())
						|| (currentflatLanderBB.getBottemRight().getX() >= flatLanderToCheckForCollisions
								.getCurrentflatLanderBB().getTopLeft().getX()
								&& currentflatLanderBB.getBottemRight().getX() <= flatLanderToCheckForCollisions
										.getCurrentflatLanderBB().getTopRight().getX()))) {
			return true;
		}
		return false;
	}

	public BoundingBox getCurrentBoundingBox() {

		return currentflatLanderBB;
	}

	@SuppressWarnings("unused")
	private BoundingBox getPreviousBoundingBox() {

		return this.previousflatLanderBB;
	}


	@SuppressWarnings("unused")
	public int passesThroughSide(FlatLander flatLanderToCheckForCollisions) {
		int predictedX = flatLanderToCheckForCollisions.getX();
		int predictedY = flatLanderToCheckForCollisions.getY();

		return 0;
	}

	public boolean passesThrough(FlatLanderWrper flatLanderToCheckForCollisions) {
		return previousflatLanderBB.passesThrough(getCurrentflatLanderBB(),
				flatLanderToCheckForCollisions.getCurrentBoundingBox());
	}

	public boolean passesThrough(PlayerWrper flatLanderToCheckForCollisions) {
		return previousflatLanderBB.passesThrough(getCurrentflatLanderBB(),
				flatLanderToCheckForCollisions.getCurrentBoundingBox());
	}

	public boolean collidesWith(FlatLanderWrper flatLandercollide) {
		if (collidable && flatLandercollide.collidable) {
			return currentflatLanderBB.collidesWith(flatLandercollide.getCurrentBoundingBox());

		}
		return false;
	}

	public Boolean collidesWith(int x, int y) {

		return currentflatLanderBB.collidesWith(x, y);
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

	protected void doPreferedActions() {
		preferedAction.act();
	}

	public void setPreferedAction(ActionsInterface preferedAction) {
		this.preferedAction = preferedAction;
	}

	public void setActionStack(ActionStack actions) {
		this.actions = actions;
	}

	public void addToActionStack(ActionsInterface itemToAddToBeginning) {
		this.actions.addActiontoBeginning(itemToAddToBeginning);
	}

	public ActionStack getActionStack() {
		// TODO Auto-generated method stub
		return actions;
	}

	public boolean collidesWith(PlayerWrper flatLandercollide) {
		if (collidable && flatLandercollide.collidable) {
			return this.getCurrentflatLanderBB().collidesWith(flatLandercollide.getCurrentBoundingBox());

		}
		return false;
	}

	@Override
	public void update(String key, boolean gameMode) {
		sprite.update(key, gameMode,false);
	}

	@Override
	public void setKeyBindingsForTerminal(JPanel panel) {
		// TODO terminalAuto-generated method stub

	}

	
	public void buildTerminal(ViewableFlatLand flatLand2, EventHandler events) {
		baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		System.setOut(ps);
		

		FlatLanderFaceBook.getInstance();
		box = new SandBox(baos, FlatLanderFaceBook.getFlatlanderFaceBook(),
				FlatLanderFaceBook.getInstance(),flatLand2, events);
		TerminalSprite sprite2 = new TerminalSprite(terminalPath, 24, 6,200);
		PromptObserver promptOb = new PromptObserver(sprite2);
		ObserverPrompt obvPrompt = new ObserverPrompt(box);
		sprite2.addObserver(obvPrompt);

		box.addObserver(promptOb);
		//promptOb.notify("WHATS UP = whats up");
		terminal = new FlatLanderWrper(50, 10, 0, "terminal", 1.2, true, true, TypeOfEntity.TERRAIN, Color.BLACK);
		terminal.setSprite(sprite2);
		
		DialogManager.getInstance().add(promptOb, obvPrompt, terminal);

	}
	


	public void buildKeyBoardHandler(GameScreen panel) {
		keyboardHandler = new PlayerKeybordHandler(this);
		keyboardHandler.buildKeyBindings(panel);
	}

	public FlatLanderWrper getTerminal() {
		// TODO Auto-generated method stub
		return terminal;
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
