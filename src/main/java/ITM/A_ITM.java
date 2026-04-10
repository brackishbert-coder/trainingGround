package ITM;

import java.awt.Color;

import Constructs.Construct;
import Constructs.Point;
import DEG.DegradesToken;
import DES.DestructionToken;
import FlatLand.Physics.TypeOfEntity;
import FlatLander.BoundingBox;
import FlatLander.FlatLander;
import FlatLander.FlatLanderClassification;
import PER.Perminant;
import PER.PerminantToken;
import PRG.ProgToken;
import SP_SUD.SpontaniousDestruToken;
import SP_SUD.SpontaniousSpawnToken;
import UPGRD.UPGRADABLE;

public abstract class A_ITM extends FlatLander implements ITM {


	

	public A_ITM(int x,int y,String name,double dir,boolean collidiable,boolean shouldPhysics,TypeOfEntity entityType,Color color) {
		super(x,y,name,dir,collidiable,shouldPhysics,entityType,color);
		long seed = 384903839;
		DegradesToken DT = new DegradesToken(seed);
		DestructionToken DesT = new DestructionToken(seed);
		PerminantToken P = new PerminantToken(seed);
		ProgToken Prog = new ProgToken(seed);
		SpontaniousSpawnToken SpontSpaw = new SpontaniousSpawnToken(seed);
		SpontaniousDestruToken SpontDest = new SpontaniousDestruToken(seed);
		UPGRADABLE uggToken = new UPGRADABLE(seed); 
	}

















	


}
