package View;

import javax.swing.JTextArea;

public class GeneralFieldObserver implements Observer {
	private JTextArea Field;
	private String fieldTitle;


	public GeneralFieldObserver(String fieldTitle,JTextArea updateField) {
		this.fieldTitle = fieldTitle;
		this.Field = updateField;
		
	}

	public void update(String message) {
		Field.setText(fieldTitle+message);
	}

}
