package theStart.thePeople;

import theStart.theStuff.ClassOfFlatLander;

public enum FlatlanderType {
	OneToOne, OneToMany, ManyToOne, ManyToMany;

	
	
	public static FlatlanderType ofType(int inputType) {
		switch(inputType) {
		case 0:
			return FlatlanderType.OneToOne;
		case 1:
			return FlatlanderType.OneToMany;
		case 2:
			return FlatlanderType.ManyToOne;
		case 3:
			return FlatlanderType.ManyToMany;default:
			return FlatlanderType.ManyToOne;
		
		}
		
	}
	
	
}



