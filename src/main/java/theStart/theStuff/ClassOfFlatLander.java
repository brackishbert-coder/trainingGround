package theStart.theStuff;

public enum ClassOfFlatLander {
	InputNuron, PyramidalNuron, MultipolarNuron, BipolarNuron, PurkinjeNuron,OutputNuron;

	
	
	public static ClassOfFlatLander ofType(int inputType) {
		switch(inputType) {
		case 0:
			return ClassOfFlatLander.InputNuron;
		case 1:
			return ClassOfFlatLander.PyramidalNuron;
		case 2:
			return ClassOfFlatLander.MultipolarNuron;
		case 3:
			return ClassOfFlatLander.BipolarNuron;
		case 4:
			return ClassOfFlatLander.PurkinjeNuron;
		case 5:
			return ClassOfFlatLander.OutputNuron;
		default:
			return ClassOfFlatLander.InputNuron;
		
		}
		
	}
	
	
}

