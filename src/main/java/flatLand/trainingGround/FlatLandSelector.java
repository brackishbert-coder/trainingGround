package flatLand.trainingGround;

import java.util.ArrayList;

import FlatLander.BoundingBox;
import FlatLander.FlatLandFacebook;
import flatLand.trainingGround.theStudio.Camera;

import FlatLander.FlatLander;
import XMLLEVELLOADER.FlatLanderWrper;
public class FlatLandSelector {

	private Camera theEyeInTheSky;
	private FlatLanderSelectorTokenizer flSelector;

	public FlatLandSelector(Camera theEyeInTheSky, FlatLandStructure.ViewableFlatLand flatLandLE) {
		this.theEyeInTheSky = theEyeInTheSky;
		flSelector = FlatLanderSelectorTokenizer.getInstance();
	}

	public void select(int x, int y) {
		FlatLandFacebook instance = FlatLandFacebook.getInstance();

		ArrayList<FlatLander> flatlanderFaceBook = instance.getFlatlanderFaceBook();
		boolean isSelected = false;

		while (!FlatLandFacebook.getInstance().requestToken(this)) {
		}

		for (FlatLander flatLander : flatlanderFaceBook) {
			flatLander.setDrawBB(false);
		}
		FlatLandFacebook.getInstance().releaseToken(this);
		while (!FlatLandFacebook.getInstance().requestToken(this)) {
		}
		
		
		System.err.println("X,Y: "+(x)+" , "+(y));
		
		for (FlatLander flatLander : flatlanderFaceBook) {
			
			
			flatLander.updatecurrentBB();
			BoundingBox currentflatLanderBB = flatLander.getCurrentflatLanderBB();

			boolean collidesWith = (x>=this.theEyeInTheSky.mapFromFlatLandToScreenSpaceX(currentflatLanderBB.getTopLeft().getX()) &&
					x<=this.theEyeInTheSky.mapFromFlatLandToScreenSpaceX(currentflatLanderBB.getTopRight().getX()) &&
					y>=this.theEyeInTheSky.mapFromFlatLandToScreenSpaceY(currentflatLanderBB.getTopLeft().getY()) &&
							y<= this.theEyeInTheSky.mapFromFlatLandToScreenSpaceY(currentflatLanderBB.getBottemRight().getY()));
			if (collidesWith) {
				isSelected = true;
				flatLander.setDrawBB(true);
				flSelector.set(flatLander);
				break;
			} else {
				flatLander.setDrawBB(false);
			}
		}
		FlatLandFacebook.getInstance().releaseToken(this);
		if (!isSelected) {
			flSelector.set(null);
			while (!FlatLandFacebook.getInstance().requestToken(this)) {
			}
			for (FlatLander flatLander : flatlanderFaceBook) {
				flatLander.setDrawBB(false);
			}
			FlatLandFacebook.getInstance().releaseToken(this);
		}

	}

	public void reposition(int x, int y, int posX, int posY) {
		System.err.println("x: "+x);
		System.err.println("y: "+y);
		FlatLander flatLanderTokenized = flSelector.getFlatLanderTokenized();
		
		
		if (flatLanderTokenized != null) {
		
			

			
			int diffx = x-flatLanderTokenized.getX();
			flatLanderTokenized.setX(this.theEyeInTheSky.mapFromScreenSpaceToFlatLAndX( x));
				
			int diffy = y-flatLanderTokenized.getY();
			flatLanderTokenized.setY(this.theEyeInTheSky.mapFromScreenSpaceFlatLandY(y));
			flatLanderTokenized.updatecurrentBB();
			
		}
	}
}
