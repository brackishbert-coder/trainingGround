package Notes;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import Logging.LOG;

public class Notes extends JFrame
		implements DocumentListener{

	private JTextField entry;
	private JLabel jLabel1;
	private JScrollPane jScrollPane1;
	private JLabel status;
	private JTextArea textArea;

	final static Color HILIT_COLOR = Color.LIGHT_GRAY;
	final static Color ERROR_COLOR = Color.PINK;
	final static String CANCEL_ACTION = "cancel-search";

	final Color entryBg;
	final Highlighter hilit;
	final Highlighter.HighlightPainter painter;

	public Notes() {
		initComponents();
		try {
			FileInputStream in = new FileInputStream("Notes/content.txt");
		
			textArea.read(new InputStreamReader(in), null);
		} catch (IOException e) {
			e.printStackTrace();
			LOG.LOG.println(e.getMessage());
		}

		hilit = new DefaultHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
		textArea.setHighlighter(hilit);

		entryBg = entry.getBackground();
		entry.getDocument().addDocumentListener(this);

		InputMap im = entry.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = entry.getActionMap();
		im.put(KeyStroke.getKeyStroke("ESCAPE"), CANCEL_ACTION);
		am.put(CANCEL_ACTION, new CancelAction());
		
		this.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	try {
	        		
					OutputStream out= new FileOutputStream("Notes/content.txt");
					String notes = textArea.getText();
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
					outputStreamWriter.write(notes);
					outputStreamWriter.flush();
					outputStreamWriter.close();
					out.close();
				} catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
					LOG.LOG.println(e1.getMessage());
				} catch (IOException e1) {
					
					e1.printStackTrace();
					LOG.LOG.println(e1.getMessage());
				}
	        }
	    });
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	private void initComponents() {
		entry = new JTextField();
		textArea = new JTextArea();
		status = new JLabel();
		jLabel1 = new JLabel();


		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("thoughts and ideraz");

		textArea.setColumns(20);
		textArea.setLineWrap(true);
		textArea.setRows(5);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(true);
		jScrollPane1 = new JScrollPane(textArea);

		jLabel1.setText("Enter text to search:");

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

//Create a parallel group for the horizontal axis
		ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

//Create a sequential and a parallel groups
		SequentialGroup h1 = layout.createSequentialGroup();
		ParallelGroup h2 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);

//Add a container gap to the sequential group h1
		h1.addContainerGap();

//Add a scroll pane and a label to the parallel group h2
		h2.addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);
		h2.addComponent(status, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);

//Create a sequential group h3
		SequentialGroup h3 = layout.createSequentialGroup();
		h3.addComponent(jLabel1);
		h3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		h3.addComponent(entry, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE);

//Add the group h3 to the group h2
		h2.addGroup(h3);
//Add the group h2 to the group h1
		h1.addGroup(h2);

		h1.addContainerGap();

//Add the group h1 to the hGroup
		hGroup.addGroup(GroupLayout.Alignment.TRAILING, h1);
//Create the horizontal group
		layout.setHorizontalGroup(hGroup);

//Create a parallel group for the vertical axis
		ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
//Create a sequential group v1
		SequentialGroup v1 = layout.createSequentialGroup();
//Add a container gap to the sequential group v1
		v1.addContainerGap();
//Create a parallel group v2
		ParallelGroup v2 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(jLabel1);
		v2.addComponent(entry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
//Add the group v2 tp the group v1
		v1.addGroup(v2);
		v1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		v1.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE);
		v1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		v1.addComponent(status);
		v1.addContainerGap();

//Add the group v1 to the group vGroup
		vGroup.addGroup(v1);
//Create the vertical group
		layout.setVerticalGroup(vGroup);
		pack();
	}

	public void search() {
		hilit.removeAllHighlights();

		String s = entry.getText();
		if (s.length() <= 0) {
			message("Nothing to search");
			return;
		}

		String content = textArea.getText();
		int index = content.indexOf(s, 0);
		if (index >= 0) { // match found
			try {
				int end = index + s.length();
				hilit.addHighlight(index, end, painter);
				textArea.setCaretPosition(end);
				entry.setBackground(entryBg);
				message("'" + s + "' found. Press ESC to end search");
			} catch (BadLocationException e) {
				e.printStackTrace();
				LOG.LOG.println(e.getMessage());
			}
		} else {
			entry.setBackground(ERROR_COLOR);
			message("'" + s + "' not found. Press ESC to start a new search");
		}
	}

	void message(String msg) {
		status.setText(msg);
	}

// DocumentListener methods

	public void insertUpdate(DocumentEvent ev) {
		search();
	}

	public void removeUpdate(DocumentEvent ev) {
		search();
	}

	public void changedUpdate(DocumentEvent ev) {
	}

	class CancelAction extends AbstractAction {
		public void actionPerformed(ActionEvent ev) {
			hilit.removeAllHighlights();
			entry.setText("");
			entry.setBackground(entryBg);
		}
	}

	public void windowActivated(WindowEvent arg0) {
		System.out.println("HEY");
	}

	public void windowClosed(WindowEvent arg0) {
		System.out.println("HEY");
	}

	public void windowClosing(WindowEvent arg0) {
		System.out.println("HEY");
	}

	
	public void windowDeactivated(WindowEvent arg0) {
		System.out.println("HEY");
	}

	
	public void windowDeiconified(WindowEvent arg0) {
		System.out.println("HEY");
	}

	
	public void windowIconified(WindowEvent arg0) {
		System.out.println("HEY");
	}

	public void windowOpened(WindowEvent arg0) {
		System.out.println("HEY");
	}

	/// window listner methods


}
