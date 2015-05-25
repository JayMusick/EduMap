package csv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 * @author Jeremy Gilreath
 *
 */
public class GUI extends JApplet {
	/**
	 * 
	 */
	private static final long					serialVersionUID	= 886831141873682866L;

	private static final int					MAX_THREADS			= 8;
	private static final int					WIDTH				= 800;
	private static final int					HEIGHT				= 768;

	private static final String[]				STATES				= { "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DE",
			"FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT",
			"NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT",
			"VA", "VT", "WA", "WI", "WV", "WY"						};

	private static Hashtable<String, String>	stateTable;

	private static ArrayList<JCheckBox>			chosenStates;
	private static ArrayList<JCheckBox>			allBoxes;

	private static JFrame						frame;

	private static JPanel						mainPanel;
	private static JPanel						booleanPanel;
	private static JPanel						boxPanel;
	private static JPanel						buttonPanel;

	private static JTextArea					consoleArea;

	private static JButton						writeButton;
	private static JButton						exitButton;

	private static JScrollPane					scrollpane;

	private static JCheckBox					optWrDoc;
	private static JCheckBox					optFeOnlineUpdates;
	private static JCheckBox					optFeOnlineOnly;
	private static JCheckBox					optPrURLs;
	private static JCheckBox					optPrElements;
	private static JCheckBox					optWrElements;
	private static JCheckBox					optPrNodes;
	private static JCheckBox					optWrNodes;
	private static JCheckBox					optPrRows;
	private static JCheckBox					optWrRows;
	private static JCheckBox					optPrHeaders;
	private static JCheckBox					optWrHeaders;
	private static JCheckBox					optPrValues;
	private static JCheckBox					optWrValues;
	private static JCheckBox					optPrUTF16Values;
	private static JCheckBox					optPrGPS;
	private static JCheckBox					optWrGPS;
	private static JCheckBox					optPrIPs;
	private static JCheckBox					optWrIPs;
	private static JCheckBox					optComAllFiles;
	private static JCheckBox					optPrDescription;
	private static JCheckBox					optPrFinalValues;
	private static JCheckBox					optPrCSVColumns;
	private static JCheckBox					optPrCSVRows;
	private static JCheckBox					optPrInstitutions;
	private static JCheckBox					all;
	private static JCheckBox					none;

	private static Dimension					frameD;
	private static Dimension					mainPanelD;
	private static Dimension					booleanPanelD;
	private static Dimension					boxPanelD;
	private static Dimension					buttonPanelD;
	private static Dimension					consolePaneD;

	private static Color						otherPanelC;
	private static Color						consoleC;

	private static OutputStream					consoleStream;

	private static PrintStream					console;

	// private static Document consoleDoc;

	private static DefaultCaret					caret;

	GridLayout									boxLayout;
	GridLayout									booleanLayout;

	private static int							totalThreads		= 0;
	private static int							seconds				= 0;

	private static boolean[]					csvoptions;
	private static boolean[]					instoptions;

	/**
	 * Creates a new GUI with the specified title
	 * 
	 * @param title
	 */
	public GUI(String title) {
		initializeVariables(title);

		addComponents();

		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Initializes method and class variables
	 * 
	 * @param title
	 */
	private void initializeVariables(String title) {
		chosenStates = new ArrayList<JCheckBox>();
		allBoxes = new ArrayList<JCheckBox>(52);

		boxLayout = new GridLayout(0, 13);
		booleanLayout = new GridLayout(0, 5);

		int mainWidth = WIDTH - 24;
		int mainHeight = HEIGHT - 20;
		int buttonHeight = mainHeight / 20;
		int boxHeight = buttonHeight * 4 - 20;
		int booleanHeight = buttonHeight * 5 - 30;
		int consoleHeight = buttonHeight * 10 + 15;

		frameD = new Dimension(WIDTH, HEIGHT);
		mainPanelD = new Dimension(mainWidth, mainHeight);
		booleanPanelD = new Dimension(mainWidth, booleanHeight);
		boxPanelD = new Dimension(mainWidth, boxHeight);
		buttonPanelD = new Dimension(mainWidth, buttonHeight);
		consolePaneD = new Dimension(mainWidth, consoleHeight);

		otherPanelC = new Color(255, 175, 100);
		consoleC = new Color(255, 255, 255);

		frame = new JFrame(title);
		frame.setPreferredSize(frameD);

		mainPanel = new JPanel();
		mainPanel.setPreferredSize(mainPanelD);
		mainPanel.setBackground(otherPanelC);

		booleanPanel = new JPanel();
		booleanPanel.setPreferredSize(booleanPanelD);
		booleanPanel.setBackground(otherPanelC);
		booleanPanel.setLayout(booleanLayout);

		boxPanel = new JPanel();
		boxPanel.setPreferredSize(boxPanelD);
		boxPanel.setLayout(boxLayout);

		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(buttonPanelD);
		buttonPanel.setBackground(otherPanelC);

		consoleArea = new JTextArea();
		consoleArea.setPreferredSize(consolePaneD);
		consoleArea.setBackground(consoleC);
		consoleArea.setEditable(false);

		scrollpane = new JScrollPane(consoleArea);
		scrollpane.setPreferredSize(consolePaneD);
		scrollpane.setBackground(consoleC);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		caret = (DefaultCaret) consoleArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		optWrDoc = new JCheckBox("Writing Documents");
		optFeOnlineUpdates = new JCheckBox("Fetch Online Updates");
		optFeOnlineOnly = new JCheckBox("Fetch Online Only");
		optPrURLs = new JCheckBox("Printing URLs");
		optPrElements = new JCheckBox("Printing Elements");
		optWrElements = new JCheckBox("Writing Elements");
		optPrNodes = new JCheckBox("Printing Nodes");
		optWrNodes = new JCheckBox("Writing Nodes");
		optPrRows = new JCheckBox("Printing Rows");
		optWrRows = new JCheckBox("Writing Rows");
		optPrHeaders = new JCheckBox("Printing Headers");
		optWrHeaders = new JCheckBox("Writing Headers");
		optPrValues = new JCheckBox("Printing Values");
		optWrValues = new JCheckBox("Writing Values");
		optPrUTF16Values = new JCheckBox("Printing UTF-16 Values");
		optPrGPS = new JCheckBox("Printing GPS");
		optWrGPS = new JCheckBox("Writing GPS");
		optPrIPs = new JCheckBox("Printing IPs");
		optWrIPs = new JCheckBox("Writing IPs");
		optComAllFiles = new JCheckBox("Combining All Files");
		optPrDescription = new JCheckBox("Printing Description");
		optPrFinalValues = new JCheckBox("Printing Final Values");
		optPrCSVColumns = new JCheckBox("Printing CSV Columns");
		optPrCSVRows = new JCheckBox("Printing CSV Rows");
		optPrInstitutions = new JCheckBox("Printing Institutions");
		all = new JCheckBox("ALL");
		none = new JCheckBox("NONE");

		optPrUTF16Values.setEnabled(false);

		exitButton = new JButton("Exit");
		writeButton = new JButton("Write!");
		writeButton.setVisible(false);

		// redirectSystemStreams();
	}

	/**
	 * Adds all the components (JCheckBoxes, etc) to the panels, and the panels to the frame
	 */
	private void addComponents() {
		String state;
		JCheckBox stateBox;
		for (int i = 0; i < STATES.length; i++) {
			state = STATES[i];
			stateBox = new JCheckBox(state);
			stateBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if ((optFeOnlineOnly.isSelected() || optFeOnlineUpdates.isSelected()) && statesAreSelected()) {
						writeButton.setVisible(true);
					} else {
						writeButton.setVisible(false);
					}
				}
			});
			boxPanel.add(stateBox);
			allBoxes.add(stateBox);
		}
		allBoxes.add(all);
		allBoxes.add(none);

		optPrHeaders.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {

				if (optPrValues.isSelected() || optPrHeaders.isSelected()) {
					optPrUTF16Values.setEnabled(true);
				} else {
					optPrUTF16Values.setSelected(false);
					optPrUTF16Values.setEnabled(false);
				}
			}
		});

		optPrValues.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {

				if (optPrValues.isSelected() || optPrHeaders.isSelected()) {
					optPrUTF16Values.setEnabled(true);
				} else {
					optPrUTF16Values.setSelected(false);
					optPrUTF16Values.setEnabled(false);
				}
			}
		});

		optFeOnlineOnly.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (optFeOnlineOnly.isSelected()) {
					optFeOnlineUpdates.setEnabled(false);
					if (statesAreSelected()) writeButton.setVisible(true);
				} else {
					optFeOnlineUpdates.setEnabled(true);
					writeButton.setVisible(false);
				}
			}
		});

		optFeOnlineUpdates.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (optFeOnlineUpdates.isSelected()) {
					optFeOnlineOnly.setEnabled(false);
					if (statesAreSelected()) writeButton.setVisible(true);
				} else {
					optFeOnlineOnly.setEnabled(true);
					writeButton.setVisible(false);
				}
			}
		});

		all.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (all.isSelected()) {
					for (JCheckBox box : allBoxes) {
						if (box.getText().equals("NONE")) {
							box.setSelected(false);
						} else {
							box.setSelected(true);
						}
					}
				} else {
					none.setSelected(true);
				}
			}
		});

		none.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (none.isSelected()) {
					for (JCheckBox box : allBoxes) {
						box.setSelected(false);
					}
					writeButton.setVisible(false);
				}
			}
		});

		booleanPanel.add(optFeOnlineUpdates);
		booleanPanel.add(optFeOnlineOnly);
		booleanPanel.add(optComAllFiles);
		booleanPanel.add(optWrDoc);
		booleanPanel.add(optPrURLs);
		booleanPanel.add(optPrElements);
		booleanPanel.add(optWrElements);
		booleanPanel.add(optPrNodes);
		booleanPanel.add(optWrNodes);
		booleanPanel.add(optPrRows);
		booleanPanel.add(optWrRows);
		booleanPanel.add(optPrHeaders);
		booleanPanel.add(optWrHeaders);
		booleanPanel.add(optPrValues);
		booleanPanel.add(optWrValues);
		booleanPanel.add(optPrUTF16Values);
		booleanPanel.add(optPrGPS);
		booleanPanel.add(optWrGPS);
		booleanPanel.add(optPrIPs);
		booleanPanel.add(optWrIPs);
		booleanPanel.add(optPrDescription);
		booleanPanel.add(optPrFinalValues);
		booleanPanel.add(optPrInstitutions);
		booleanPanel.add(optPrCSVColumns);
		booleanPanel.add(optPrCSVRows);

		boxPanel.add(all);
		boxPanel.add(none);

		buttonPanel.add(writeButton);
		buttonPanel.add(exitButton);

		mainPanel.add(booleanPanel);
		mainPanel.add(boxPanel);
		mainPanel.add(buttonPanel);
		mainPanel.add(scrollpane);

		frame.add(mainPanel);

		writeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setStateBoxes(boxPanel);

				if (chosenStates.size() == 0) {
					System.out.println("Select at least one state!");
				} else {
					setoptions();

					stateTable = new Hashtable<String, String>(50);
					addTo(stateTable, "AK", "Alaska");// 10
					addTo(stateTable, "AL", "Alabama");// 68
					addTo(stateTable, "AR", "Arkansas");// 36
					addTo(stateTable, "AZ", "Arizona");// 46
					addTo(stateTable, "CA", "California");// 158
					addTo(stateTable, "CO", "Colorado");// 33
					addTo(stateTable, "CT", "Connecticut");// 38
					addTo(stateTable, "DE", "Delaware");// 11
					addTo(stateTable, "FL", "Florida"); // 97
					addTo(stateTable, "GA", "Georgia");// 97
					addTo(stateTable, "HI", "Hawaii");// 12
					addTo(stateTable, "IA", "Iowa");// 51
					addTo(stateTable, "ID", "Idaho");// 13
					addTo(stateTable, "IL", "Illinois");// 99
					addTo(stateTable, "IN", "Indiana");// 69
					addTo(stateTable, "KS", "Kansas");// 54
					addTo(stateTable, "KY", "Kentucky");// 61
					addTo(stateTable, "LA", "Louisiana");// 37
					addTo(stateTable, "MA", "Massachusetts");// 127
					addTo(stateTable, "MD", "Maryland");// 58
					addTo(stateTable, "ME", "Maine");// 32
					addTo(stateTable, "MI", "Michigan");// 92
					addTo(stateTable, "MN", "Minnesota");// 86
					addTo(stateTable, "MO", "Missouri");// 63
					addTo(stateTable, "MS", "Mississippi");// 29
					addTo(stateTable, "MT", "Montana");// 30
					addTo(stateTable, "NC", "North_Carolina");// 89
					addTo(stateTable, "ND", "North_Dakota");// 22 - EMPTY COLUMN
					addTo(stateTable, "NE", "Nebraska");// 27
					addTo(stateTable, "NH", "New_Hampshire");// 30
					addTo(stateTable, "NJ", "New_Jersey");// 56
					addTo(stateTable, "NM", "New_Mexico");// 20
					addTo(stateTable, "NV", "Nevada");// 10
					addTo(stateTable, "NY", "New_York");// 184
					addTo(stateTable, "OH", "Ohio");// 121
					addTo(stateTable, "OK", "Oklahoma");// 44
					addTo(stateTable, "OR", "Oregon");// 64
					addTo(stateTable, "PA", "Pennsylvania");// 157
					addTo(stateTable, "RI", "Rhode_Island");// 16
					addTo(stateTable, "SC", "South_Carolina");// 63
					addTo(stateTable, "SD", "South_Dakota");// 23
					addTo(stateTable, "TN", "Tennessee");// 80
					addTo(stateTable, "TX", "Texas");// 181
					addTo(stateTable, "UT", "Utah");// 32
					addTo(stateTable, "VA", "Virginia");// 104
					addTo(stateTable, "VT", "Vermont");// 28
					addTo(stateTable, "WA", "Washington");// 67
					addTo(stateTable, "WI", "Wisconsin");// 74
					addTo(stateTable, "WV", "West_Virginia");// 36
					addTo(stateTable, "WY", "Wyoming");// 10

					Hashtable<String, String> thisTable = new Hashtable<String, String>(chosenStates.size());
					for (JCheckBox state : chosenStates) {
						addTo(thisTable, state.getText(), stateTable.get(state.getText()));
					}

					totalThreads = Math.min(thisTable.size(), MAX_THREADS);
					ExecutorService executor = Executors.newFixedThreadPool(totalThreads);

					int state = 0;
					int numThreads = 0;
					Runnable worker = null;
					Map.Entry<String, String> entry;
					Set<Entry<String, String>> set = thisTable.entrySet();
					Iterator<Entry<String, String>> it = set.iterator();
					while (it.hasNext()) {
						if (numThreads < totalThreads) numThreads++;
						entry = (Entry<String, String>) it.next();
						worker = new CSVWriter(++state, entry.getKey(), entry.getValue(), csvoptions, instoptions);
						executor.execute(worker);
					}
					executor.shutdown();

					try {
						executor.awaitTermination(seconds, TimeUnit.SECONDS);
					} catch (InterruptedException ie) {
						System.err.println("Couldn't wait for ExecutorService to await termination over " + seconds
								+ " seconds!");
						ie.printStackTrace();
						System.exit(1);
					}
					System.out.println(numThreads + " Thread" + (numThreads < 2 ? "" : "s") + " Finished!");
				}
			}

			/**
			 * Adds a key:value pair to the Hashtable
			 * 
			 * @param table
			 * @param key
			 * @param value
			 */
			private void addTo(Hashtable<String, String> table, String key, String value) {
				table.put(key, value);
				if (totalThreads != MAX_THREADS) totalThreads++;
				seconds += 120;
			}

			/**
			 * Sets the program options by checking to see if JCheckBoxes are selected
			 */
			private void setoptions() {
				csvoptions = new boolean[10];

				csvoptions[0] = optPrElements.isSelected() ? true : false;
				csvoptions[1] = optWrElements.isSelected() ? true : false;
				csvoptions[2] = optComAllFiles.isSelected() ? true : false;
				csvoptions[3] = optWrDoc.isSelected() ? true : false;
				csvoptions[4] = optPrInstitutions.isSelected() ? true : false;
				csvoptions[5] = optPrCSVColumns.isSelected() ? true : false;
				csvoptions[6] = optPrCSVRows.isSelected() ? true : false;
				csvoptions[7] = optPrURLs.isSelected() ? true : false;
				csvoptions[8] = optFeOnlineOnly.isSelected() ? true : false;
				csvoptions[9] = optFeOnlineUpdates.isSelected() ? true : false;

				instoptions = new boolean[19];

				instoptions[0] = optWrElements.isSelected() ? true : false;
				instoptions[1] = optPrNodes.isSelected() ? true : false;
				instoptions[2] = optWrNodes.isSelected() ? true : false;
				instoptions[3] = optPrDescription.isSelected() ? true : false;
				instoptions[4] = optPrRows.isSelected() ? true : false;
				instoptions[5] = optPrHeaders.isSelected() ? true : false;
				instoptions[6] = optPrValues.isSelected() ? true : false;
				instoptions[7] = optWrRows.isSelected() ? true : false;
				instoptions[8] = optWrHeaders.isSelected() ? true : false;
				instoptions[9] = optWrValues.isSelected() ? true : false;
				instoptions[10] = optPrFinalValues.isSelected() ? true : false;
				instoptions[11] = optPrGPS.isSelected() ? true : false;
				instoptions[12] = optWrGPS.isSelected() ? true : false;
				instoptions[13] = optPrIPs.isSelected() ? true : false;
				instoptions[14] = optWrIPs.isSelected() ? true : false;
				instoptions[15] = optWrDoc.isSelected() ? true : false;
				instoptions[16] = optFeOnlineUpdates.isSelected() ? true : false;
				instoptions[17] = optFeOnlineOnly.isSelected() ? true : false;
				instoptions[18] = optPrUTF16Values.isSelected() ? true : false;
			}

			/**
			 * Adds to an ArrayList all selected JCheckBoxes that are components of the passed JPanels
			 * 
			 * @param panels
			 */
			private void setStateBoxes(JPanel... panels) {
				JCheckBox box;
				for (JPanel panel : panels) {
					for (Component comp : panel.getComponents()) {
						if (comp instanceof JCheckBox) {
							box = (JCheckBox) comp;
							if (box.isSelected() && !(box.getText().equals("ALL") || box.getText().equals("NONE"))) chosenStates
									.add(box);
						}
					}
				}
			}
		});
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
	}

	/**
	 * Checks to see if any states have been selected
	 * 
	 * @return
	 */
	private boolean statesAreSelected() {
		boolean selected = false;
		for (JCheckBox box : allBoxes) {
			if (box.isSelected() && !(box.getText().equals("ALL") || box.getText().equals("NONE"))) {
				selected = true;
				break;
			}
		}
		return selected;
	}

	/**
	 * Redirects the output and error streams for System to a JTextPane
	 */
	private void redirectSystemStreams() {
		consoleStream = new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				updateTextPane(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextPane(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
		console = new PrintStream(consoleStream, true);

		System.setOut(console);
		System.setErr(console);
		System.out.println("Test");
	}

	/**
	 * Updates the JTextPane with a String
	 * 
	 * @param text
	 */
	private void updateTextPane(final String text) {
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		consoleArea.append(text);// Synchronized! Don't need a thread/runnable
		// // consoleDoc = consoleArea.getDocument();
		// // try {
		// // consoleDoc.insertString(consoleDoc.getLength(), text, null);
		// // } catch (BadLocationException ble) {
		// // System.err.println("Couldn't insert the string " + text + "!");
		// // ble.printStackTrace();
		// // System.exit(1);
		// // }
		// // consoleArea.setCaretPosition(consoleDoc.getLength());
		// }
		// }).start();
	}
}
