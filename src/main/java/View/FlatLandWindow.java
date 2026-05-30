package View;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import FlatLandStructure.ViewableFlatLand;

public class FlatLandWindow extends JFrame {

	private static JTextArea txtrWidth;
	private static JTextArea txtrHeight;
	private static JTextArea txtrTotalNumberOfPixels;
	private static JTextArea txtrCurrentFlatlandTime;
	private static JButton rollTheDice;
	private JPanel panel;
	private static Panel displaysStatsAboutRunningScenario;
	private static ViewableFlatLand currentFlatLand = null;
	private static int height;
	private static int width;
	private static long FPS = 0;
	private static HashMap<String, JTextArea> factsaboutanexperimentthatisrunning = new HashMap<String, JTextArea>();
	private boolean Close = false;
	private static ArrayList<Observer> tim = new ArrayList<Observer>();
	private static JTextArea txtrUpdate;
	private static JTextArea txtrPic;
	private static JTextArea txtrOther;
	private static JTextArea txtrFPS;
	private static GeneralFieldObserver updateObserver;
	private static GeneralFieldObserver picObserver;
	private static GeneralFieldObserver otherObserver;
	private static JButton random;
	private static JButton random1;
	private static JButton random2;
	private static JButton random3;
	private static JButton random4;
	private static JButton random5;
	private static JButton random6;
	private static JButton random7;
	private static JSlider slide1;
	private static JSlider slide2;
	private static JSlider slide3;
	private static JSlider slide4;
	private static JSlider slide5;
	private static JSlider slide6;
	private static JSlider slide7;
	private static JSlider slide8;
	private static JCheckBox jCheckBox;
	private static JCheckBox jCheckBox1;
	private static JCheckBox jCheckBox2;
	private static JCheckBox jCheckBox3;
	private static JCheckBox jCheckBox4;
	private static JCheckBox jCheckBox5;
	private static JCheckBox jCheckBox6;
	private static JCheckBox jCheckBox7;
	private static JTextField jTextField;
	private static JTextField jTextField1;
	private static JTextField jTextField2;
	private static JTextField jTextField3;
	private static boolean toggle0;
	private static boolean toggle1;
	private static JTextArea xinflatland;
	private static JTextArea yinflatland;
	private static GeneralFieldObserver xObserver;
	private static GeneralFieldObserver yObserver;

	public FlatLandWindow(String windowName, ViewableFlatLand flatland, JComponent panel2, int height, int width) {

		this.height = height;
		this.width = width;
		JTextField textField = new JTextField(20);
		textField.setVisible(true);
		setupWindowListner();

		this.add(textField);
		currentFlatLand = flatland;
		setupPanel(panel2);

		this.setSize(height, width);

		this.add(panel);

		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				try {
					System.err.println("closing");
					PrintWriter output = new PrintWriter(new FileOutputStream("filename.txt"));
					String string = "hello";

					String bytes = txtrWidth.getText();
					String name = "";

					int bytez = slide1.getValue();

					name = "slide1: ";
					output.print(name);
					output.print(slide1.getValue());
					output.print("\n");

					bytez = slide2.getValue();
					name = "slide2: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");
					bytez = slide3.getValue();
					name = "slide3: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");
					bytez = slide4.getValue();
					name = "slide4: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");

					bytez = slide5.getValue();
					name = "slide5: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");
					bytez = slide6.getValue();
					name = "slide6: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");
					bytez = slide7.getValue();
					name = "slide7: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");

					bytez = slide8.getValue();
					name = "slide8: ";
					output.print(name);
					output.print(bytez);
					output.print("\n");
					boolean selected = jCheckBox.isSelected();
					name = "jCheckBox: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");

					selected = jCheckBox1.isSelected();
					name = "jCheckBox1: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");

					selected = jCheckBox2.isSelected();
					name = "jCheckBox2: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");

					selected = jCheckBox3.isSelected();
					name = "jCheckBox3: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");
					selected = jCheckBox4.isSelected();
					name = "jCheckBox4: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");

					selected = jCheckBox5.isSelected();
					name = "jCheckBox5: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);
					output.print("\n");

					selected = jCheckBox6.isSelected();
					name = "jCheckBox6: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");
					selected = jCheckBox7.isSelected();
					name = "jCheckBox7: ";
					output.print(name);
					if (selected)
						output.print(1);
					else
						output.print(0);

					output.print("\n");

					bytes = jTextField.getText();
					name = "jTextField: ";
					output.print(name);
					output.print(bytes);
					output.print("\n");
					bytes = jTextField1.getText();
					name = "jTextField1: ";
					output.print(name);
					output.print(bytes);
					output.print("\n");
					bytes = jTextField2.getText();
					name = "jTextField2: ";
					output.print(name);
					output.print(bytes);
					output.print("\n");
					bytes = jTextField3.getText();
					name = "jTextField3: ";
					output.print(name);
					output.print(bytes);
					output.print("\n");

					;
					output.flush();
					output.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		panel.setFocusable(true);
		panel.requestFocusInWindow();

		this.pack();
		this.setVisible(true);

		this.repaint();
	}

	public boolean isClose() {
		return Close;
	}

	private void setupWindowListner() {
		WindowListener windowListener2 = new WindowListener() {

			public void windowOpened(WindowEvent e) {

			}

			public void windowClosing(WindowEvent e) {
				Close = true;

			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

		};
		//
//		ZERO_regester_a_fact_or_statistic_about___();
//		ONE_add_regestered_facts_added_to_info_panel();
//		TWO_write_statistics_to_a_text_box_single_line(getCurrentFlatLand());

		this.addWindowListener(windowListener2);
	}

	public void refresh() {
		this.repaint();
	}

	public void setupPanel(JComponent panel2) {
		panel = new JPanel(new BorderLayout());

		displaysStatsAboutRunningScenario = new Panel();

		displaysStatsAboutRunningScenario.setLayout(new GridLayout(8, 1, 0, 0));

		panel.add(displaysStatsAboutRunningScenario, BorderLayout.SOUTH);

		panel.add(panel2, BorderLayout.WEST);

		ZERO_regester_a_fact_or_statistic_about___();
		ONE_add_regestered_facts_added_to_info_panel();
		TWO_write_statistics_to_a_text_box_single_line(getCurrentFlatLand());

	}

	private static void ZERO_regester_a_fact_or_statistic_about___() {
		txtrWidth = new JTextArea();
		txtrHeight = new JTextArea();
		txtrTotalNumberOfPixels = new JTextArea();
		txtrCurrentFlatlandTime = new JTextArea();
		txtrFPS = new JTextArea();
		txtrUpdate = new JTextArea();
		txtrPic = new JTextArea();
		txtrOther = new JTextArea();
		rollTheDice = new JButton();
		xinflatland = new JTextArea();
		yinflatland = new JTextArea();
		HashMap<String, JComponent> componentSettingsMap = new HashMap<String, JComponent>();
		random = new JButton();
		toggle0 = false;
		
		toggle1 = false;
		random1 = new JButton();
		
		random2 = new JButton();
		random3 = new JButton();
		random4 = new JButton();
		random5 = new JButton();
		random6 = new JButton();
		random7 = new JButton();
		slide1 = new JSlider();
		componentSettingsMap.put("slide1", slide1);
		slide1.setMaximum(255);
		slide1.setMinimum(0);
		slide1.setValue(0);
		slide2 = new JSlider();
		componentSettingsMap.put("slide2", slide2);
		slide2.setMaximum(255);
		slide2.setMinimum(0);
		slide2.setValue(0);

		slide3 = new JSlider();
		componentSettingsMap.put("slide3", slide3);
		slide3.setMaximum(255);
		slide3.setMinimum(0);
		slide3.setValue(0);

		slide4 = new JSlider();
		componentSettingsMap.put("slide4", slide4);
		slide4.setMaximum(255);
		slide4.setMinimum(0);
		slide4.setValue(0);

		slide5 = new JSlider();
		componentSettingsMap.put("slide5", slide5);
		slide5.setMaximum(23);
		slide5.setMinimum(0);
		slide5.setValue(0);

		slide6 = new JSlider();
		componentSettingsMap.put("slide6", slide6);
		slide6.setMaximum(23);
		slide6.setMinimum(0);
		slide6.setValue(0);

		slide7 = new JSlider();
		componentSettingsMap.put("slide7", slide7);
		slide7.setMaximum(23);
		slide7.setMinimum(0);
		slide7.setValue(0);

		slide8 = new JSlider();
		componentSettingsMap.put("slide8", slide8);
		slide8.setMaximum(23);
		slide8.setMinimum(0);
		slide8.setValue(0);

		jCheckBox = new JCheckBox();
		componentSettingsMap.put("jCheckBox", jCheckBox);
		jCheckBox1 = new JCheckBox();
		componentSettingsMap.put("jCheckBox1", jCheckBox1);
		jCheckBox2 = new JCheckBox();
		componentSettingsMap.put("jCheckBox2", jCheckBox2);
		jCheckBox3 = new JCheckBox();
		componentSettingsMap.put("jCheckBox3", jCheckBox3);
		jCheckBox4 = new JCheckBox();
		componentSettingsMap.put("jCheckBox4", jCheckBox4);
		jCheckBox5 = new JCheckBox();
		componentSettingsMap.put("jCheckBox5", jCheckBox5);
		jCheckBox6 = new JCheckBox();
		componentSettingsMap.put("jCheckBox6", jCheckBox6);
		jCheckBox7 = new JCheckBox();

		componentSettingsMap.put("jCheckBox7", jCheckBox7);
		jTextField = new JTextField();
		componentSettingsMap.put("jTextField", jTextField);
		jTextField1 = new JTextField();
		componentSettingsMap.put("jTextField1", jTextField1);
		jTextField2 = new JTextField();
		componentSettingsMap.put("jTextField2", jTextField2);
		jTextField3 = new JTextField();
		componentSettingsMap.put("jTextField3", jTextField3);
		Scanner in = null;
		try {
			in = new Scanner(new FileInputStream("/home/wes/gitworkspace/ScreenIntegration/ScreenIntegration/filename.txt"));

			while (in.hasNextLine()) {
				String next = in.nextLine();
				String[] split = next.split(":");
				String name = split[0];
				String[] split2 = split[1].split(" ");
				String value = "";
				if (split2.length != 0) {
					value = split2[1];
				}
				JComponent jComponent = componentSettingsMap.get(name);
				if (value.length() > 0) {
					Integer valueOf = Integer.valueOf(value);
					if (jComponent instanceof JSlider) {
						((JSlider) jComponent).setValue(valueOf);
					} else if (jComponent instanceof JCheckBox) {

						if (valueOf == 0)
							((JCheckBox) jComponent).setSelected(false);
						else
							((JCheckBox) jComponent).setSelected(true);

					} else if (jComponent instanceof JTextField) {

						
					}

				}

				System.out.println(name + " and " + value);
			}

			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jTextField3.setVisible(true);
		

		

		

		

		

		
		
		
		
		

		getCurrentFlatLand().attach(new TimeObserver(getCurrentFlatLand(), txtrCurrentFlatlandTime));
		attach(new FPSObserver(txtrFPS));
		updateObserver = new GeneralFieldObserver("Update: ", txtrUpdate);
		picObserver = new GeneralFieldObserver("PicAndDevelop: ", txtrPic);

		otherObserver = new GeneralFieldObserver("Other: ", txtrOther);
		xObserver = new GeneralFieldObserver("X: ", xinflatland);
		yObserver = new GeneralFieldObserver("Y: ", yinflatland);
	}

	private static void ONE_add_regestered_facts_added_to_info_panel() {
		factsaboutanexperimentthatisrunning.put("height", txtrHeight);
		factsaboutanexperimentthatisrunning.put("width", txtrWidth);
		factsaboutanexperimentthatisrunning.put("number_of_pixels", txtrTotalNumberOfPixels);
		factsaboutanexperimentthatisrunning.put("global_time", txtrCurrentFlatlandTime);
		factsaboutanexperimentthatisrunning.put("FPS", txtrFPS);
		factsaboutanexperimentthatisrunning.put("Update", txtrUpdate);
		factsaboutanexperimentthatisrunning.put("PicAndDevelop", txtrPic);
		factsaboutanexperimentthatisrunning.put("Other", txtrOther);
		factsaboutanexperimentthatisrunning.put("X in flatlad", xinflatland);
		factsaboutanexperimentthatisrunning.put("Y in flatlad", yinflatland);

		displaysStatsAboutRunningScenario.add(random);
		displaysStatsAboutRunningScenario.add(random1);
		displaysStatsAboutRunningScenario.add(random2);
		displaysStatsAboutRunningScenario.add(random3);
		displaysStatsAboutRunningScenario.add(random4);
		displaysStatsAboutRunningScenario.add(random5);
		displaysStatsAboutRunningScenario.add(random6);
		displaysStatsAboutRunningScenario.add(random7);
		displaysStatsAboutRunningScenario.add(slide1);
		displaysStatsAboutRunningScenario.add(slide2);
		displaysStatsAboutRunningScenario.add(slide3);
		displaysStatsAboutRunningScenario.add(slide4);
		displaysStatsAboutRunningScenario.add(slide5);

		displaysStatsAboutRunningScenario.add(slide6);
		displaysStatsAboutRunningScenario.add(slide7);
		displaysStatsAboutRunningScenario.add(slide8);

		displaysStatsAboutRunningScenario.add(jTextField);
		displaysStatsAboutRunningScenario.add(jTextField1);
		displaysStatsAboutRunningScenario.add(jTextField2);
		displaysStatsAboutRunningScenario.add(jTextField3);

		displaysStatsAboutRunningScenario.add(jCheckBox);
		displaysStatsAboutRunningScenario.add(jCheckBox1);
		displaysStatsAboutRunningScenario.add(jCheckBox2);
		displaysStatsAboutRunningScenario.add(jCheckBox3);
		displaysStatsAboutRunningScenario.add(jCheckBox4);
		displaysStatsAboutRunningScenario.add(jCheckBox5);
		displaysStatsAboutRunningScenario.add(jCheckBox6);
		displaysStatsAboutRunningScenario.add(jCheckBox7);

		displaysStatsAboutRunningScenario.add(txtrHeight);
		displaysStatsAboutRunningScenario.add(txtrWidth);
		displaysStatsAboutRunningScenario.add(txtrTotalNumberOfPixels);
		displaysStatsAboutRunningScenario.add(txtrCurrentFlatlandTime);
		displaysStatsAboutRunningScenario.add(txtrFPS);
		displaysStatsAboutRunningScenario.add(txtrUpdate);
		displaysStatsAboutRunningScenario.add(txtrPic);
		displaysStatsAboutRunningScenario.add(txtrOther);
		displaysStatsAboutRunningScenario.add(rollTheDice);
		displaysStatsAboutRunningScenario.add(xinflatland);
		displaysStatsAboutRunningScenario.add(yinflatland);

	}

	private static void TWO_write_statistics_to_a_text_box_single_line(ViewableFlatLand current) {

		txtrHeight.setText("height: " + height);
		txtrWidth.setText("width: " + width);
		txtrTotalNumberOfPixels.setText("total pixel count: " + (height * width));
		txtrCurrentFlatlandTime.setText("Global Time: " + current.getTime());
		txtrFPS.setText("FPS: " + FPS);
		System.out.println("height: " + height);
		System.out.println("width: " + width);
		System.out.println("total pixel count: " + (height * width));
		System.out.println("FPS: " + FPS);
	}

	public static void attach(Observer obi) {
		tim.add(obi);

	}

	public static void detach(Observer obi) {
		tim.remove(obi);
	}

	public void notify(String message) {
		for (Observer timmys : tim) {
			timmys.update(message);
		}

	}

	public void notifyUpdate(String message) {
		updateObserver.update(message);

	}

	public void notifyPic(String message) {
		picObserver.update(message);

	}

	public void notifyOther(String message) {
		otherObserver.update(message);

	}

	public void notifyX(String message) {
		xObserver.update(message);

	}

	public void notifyY(String message) {
		yObserver.update(message);

	}

	public JPanel getPanel() {
		return panel;
	}

	public static ViewableFlatLand getCurrentFlatLand() {
		return currentFlatLand;
	}

	public void setflatLand(ViewableFlatLand flatLand) {
		currentFlatLand = flatLand;
	}

	public void setPanel(JComponent panel2) {
		panel=(JPanel) panel2; 
		this.add(panel);
	}
	

}
