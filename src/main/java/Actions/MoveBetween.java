package Actions;

import FlatLander.FlatLander;

public class MoveBetween extends Actions {

	private int moveByx;
	private int moveByy;
	private int xLow;
	private int xHigh;
	private int yLow;
	private int yHigh;
	private ActionStatus actionStatus;
	boolean xdirect= false;
	boolean ydirect = false;



	public void act() {
		actionStatus = ActionStatus.STARTED;
		if(actor.getX()<=xLow) {
			xdirect = true;
		}
		if(actor.getX()>=xHigh) {
			xdirect = false;
		}
		
		if(actor.getY()<=yLow) {
			ydirect=true;
		}
		
		if(actor.getY()>=yHigh) {
			ydirect = false;
		}
		if(xdirect) {
			actor.changeMoveXBy(moveByx);
			
		}else {
			actor.changeMoveXBy(-moveByx);
		}
		if(ydirect) {
			actor.changeMoveYBy(moveByy);
		}else {
			actor.changeMoveYBy(-moveByy);
		}
		//physics.applyPhysics();
		actionStatus = ActionStatus.COMPLETE;
		
		
		
		
	}

	
	
	public MoveBetween(FlatLander actor,int moveByx,int moveByy,int xLow,int xHigh, int yLow, int yHigh) {
		super(actor);
		this.moveByx = moveByx;
		this.moveByy = moveByy;
		this.xLow = xLow;
		this.xHigh = xHigh;
		this.yLow = yLow;
		this.yHigh = yHigh;
		this.actionStatus = ActionStatus.UNSTARTED;
		
	}
	
	
	
	
	public ActionStatus getActionStatus() {

		return actionStatus;
	}

}
