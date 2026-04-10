package environment;

import java.awt.Color;

import Constructs.Construct;
import Constructs.Point;
import FlatLand.Physics.Physics;
import FlatLand.Physics.TypeOfEntity;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.BoundingBox;
import FlatLander.FlatLander;
import FlatLander.FlatLanderClassification;
import dialogManagement.DialogManager;
import flatLand.trainingGround.EventHandler;
import flatLand.trainingGround.theStudio.Camera;

public class Environment  {



	private ViewableFlatLand viewableFlatLand;
	private Physics phy;
	private Camera cam;
	private DialogManager dmman;
	private EventHandler eh;

	public Environment(ViewableFlatLand viewableFlatLand,Physics phy,Camera cam,DialogManager dmman,EventHandler eh) {
		this.viewableFlatLand = viewableFlatLand;
		this.phy = phy;
		this.cam = cam;
		this.dmman = dmman;
		this.eh = eh;
		this.viewableFlatLand.setIsWraped(true);
	}

}
