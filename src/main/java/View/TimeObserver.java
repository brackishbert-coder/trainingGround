package View;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import FlatLandStructure.ViewableFlatLand;



public class TimeObserver implements Observer {

	
	
	private JTextArea timeField=new JTextArea();
	private JTextField timeArea=new JTextField();
	private ViewableFlatLand flatland;


	public TimeObserver(ViewableFlatLand flatland,JTextArea updateField){
		this.flatland = flatland;
		this.timeField = updateField;
		
	}
	public TimeObserver(ViewableFlatLand flatland,JTextField updateField){
		this.flatland = flatland;
		this.timeArea = updateField;
		
	}
	
	
	public void update(String message) {
		timeField.setText(message+flatland.getTime());
		timeArea.setText(message+flatland.getTime());
	}



}
