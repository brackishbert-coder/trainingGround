package FlatLandStructure;

import java.awt.Color;
import java.util.ArrayList;
import FlatLander.FlatLandFacebook;
import FlatLander.FlatLander;
import View.Observable;
import View.Observer;
import View.UpdateCycle;

public class ViewableFlatLand extends BaseFlatLand implements Observable, UpdateCycle {

	private   int time;
	private ArrayList<Observer> tim = new ArrayList<Observer>();

	private Color flatLandColor = Color.BLACK;
	private Color voidColor = Color.BLACK;

	public ViewableFlatLand(int flatLandWidth, int flatLandHeight, boolean b) {
		super(Color.class, flatLandWidth, flatLandHeight, b);

		setTime(0);
		clearFlatLand();

	}

	public Color getColor(FlatLander flatLander) {

		int heightPos;
		int widthPos;
		if (getIsWraped()) {
			heightPos = getYBoundedAndWraped(flatLander);
			widthPos = getXBoundedAndWraped(flatLander);
		} else {

			heightPos = getYUnWraped(flatLander);
			widthPos = getXUnWraped(flatLander);

		}
		Color color3 = (Color) getFlatLandAt(widthPos, heightPos);

		return color3;
	}

	public Color getColor(int widthPos, int heightPos) {

		if (getIsWraped()) {
			heightPos = getYBoundedAndWraped(heightPos);
			widthPos = getXBoundedAndWraped(widthPos);
		} else {

			heightPos = getYUnWraped(heightPos);
			widthPos = getXUnWraped(widthPos);

		}
		Color color3 = (Color) getFlatLandAt(widthPos, heightPos);

		return color3;
	}

	public void setColor(FlatLander flatLander, Color color) {

		int heightPos;
		int widthPos;
		if (getIsWraped()) {
			heightPos = getYBoundedAndWraped(flatLander);
			widthPos = getXBoundedAndWraped(flatLander);
		} else {

			heightPos = getYUnWraped(flatLander);
			widthPos = getXUnWraped(flatLander);

		}
		setFlatLandAt(widthPos, heightPos, color);
	}

	public void clearFlatLand() {

		clearFlatland();

	}

	public void update() {

		ArrayList<FlatLander> bookOfFlatLanders = FlatLandFacebook.getInstance().getFlatlanderFaceBook();
		while (!FlatLandFacebook.getInstance().requestToken(this)) {
		}

		for (FlatLander flatLander : bookOfFlatLanders) {

			flatLander.setPreviousX(flatLander.getX());
			flatLander.setPreviousY(flatLander.getY());

		}
		FlatLandFacebook.getInstance().releaseToken(this);

		while (!FlatLandFacebook.getInstance().requestToken(this)) {
			System.out.println("hey");
		}
		for (FlatLander flatLander : bookOfFlatLanders) {
			flatLander.update();
			
			flatLander.setMoveX(0);
			flatLander.setMoveY(0);
			
		}
		FlatLandFacebook.getInstance().releaseToken(this);
		setTime(time + 1);
	}

	public  int getTime() {
		return time;
	}

	public void setTime(int time) {
		notify("Global Time: ");
		this.time = time;
	}

	public void attach(Observer obi) {
		tim.add(obi);

	}

	public void detach(Observer obi) {
		tim.remove(obi);
	}

	public void notify(String message) {
		for (Observer timmys : tim) {
			timmys.update(message);
		}

	}

	public Color getFlatLandColor() {
		return flatLandColor;
	}

	public void setFlatLandColor(Color flatLandColor) {
		this.flatLandColor = flatLandColor;

	}

	public Color getVoidColor() {

		return voidColor;
	}

}
