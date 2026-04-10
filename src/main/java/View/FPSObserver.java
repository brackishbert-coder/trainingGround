package View;

import javax.swing.JTextArea;

public class FPSObserver implements Observer {

	
	private JTextArea FPSField;


	public FPSObserver(JTextArea updateField) {
		this.FPSField = updateField;
		
	}
	
	
	public void update(String message) {
		FPSField.setText("FPS: "+message);
	}

}
