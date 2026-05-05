package Island;

import java.util.ArrayList;

import XMLLEVELLOADER.FlatLanderWrper;

public class Island {

	ArrayList<Feature> interactable = new ArrayList<Feature>(); 
	ArrayList<Feature> noninteractable = new ArrayList<Feature>(); 
	FlatLanderWrper islandFlatlander = null;
	
	public Island(FlatLanderWrper fl) {
		islandFlatlander = fl;
	}
	
	public void display() {}
	
	
	
}
