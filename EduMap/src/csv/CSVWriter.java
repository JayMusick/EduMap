package csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Jeremy Gilreath
 *
 */
public class CSVWriter implements Runnable {

	private static final String[]	TYPES				= { "university", "college", "institute", "seminary", "school" };

	private static final String		STATE_URL_PREFIX	= "http://en.wikipedia.org/wiki/List_of_colleges_and_universities_in_";

	private static final Charset	CHARSET				= StandardCharsets.UTF_16;

	private static boolean[]		instOptions;

	private static boolean			writingDoc;
	private static boolean			fetchOnlineUpdates;
	private static boolean			fetchOnlineOnly;
	private static boolean			printingURLs;
	private static boolean			printingElements;
	private static boolean			writingElements;
	private static boolean			combiningAllFiles;
	private static boolean			printingInstitutions;
	private static boolean			printingCSVColumns;
	private static boolean			printingCSVRows;

	private int						stateNumber;
	private String					url;
	private String					stateFolder;
	private String					stateName;
	private String					stateAbbr;

	/**
	 * Constructor requires a thread number, folder path for file storage, and URL for parsing
	 * 
	 * @param args
	 */
	public CSVWriter(int stateNumber, String stateAbbr, String stateName, boolean[] options, boolean[] instOptions) {
		printingElements = options[0];
		writingElements = options[1];
		combiningAllFiles = options[2];
		writingDoc = options[3];
		printingInstitutions = options[4];
		printingCSVColumns = options[5];
		printingCSVRows = options[6];
		printingURLs = options[7];
		fetchOnlineOnly = options[8];
		fetchOnlineUpdates = options[9];

		setInstOptions(instOptions);
		this.stateNumber = stateNumber;
		this.stateName = stateName.replace("_", " ");
		this.stateAbbr = stateAbbr;
		url = STATE_URL_PREFIX + stateName;
		stateFolder = "./" + stateAbbr + "/";

		System.out.println("Thread " + stateNumber + " for state " + stateName + " constructed.");
	}

	@Override
	public void run() {
		System.out.println("Thread " + stateNumber + " Running.");

		final String DOCS_FOLDER = stateFolder + "docs/";
		final String ELEM_FOLDER = stateFolder + "elem/";
		final String HEAD_FOLDER = stateFolder + "head/";
		final String VALS_FOLDER = stateFolder + "vals/";
		final String ROWS_FOLDER = stateFolder + "rows/";
		final String GPS_FOLDER = stateFolder + "gps/";
		final String IP_FOLDER = stateFolder + "ip/";
		final String CSV_FOLDER = stateFolder + "csv/";

		String docFilePath = DOCS_FOLDER + "(ALL).txt";
		Util.makeFolders(DOCS_FOLDER, stateFolder);

		Document allDoc = Util.getDocument(docFilePath, url, writingDoc, fetchOnlineUpdates, fetchOnlineOnly);
		if (allDoc != null) {
			ArrayList<Element> uniqueElements = Util.removeDuplicates(getElements(allDoc), "href");
			if (printingElements) System.out.print("\n----- Unique Elements: " + uniqueElements);
			if (writingElements) {
				String eleFilePath = ELEM_FOLDER + "(ALL).txt";
				Util.makeFolders(ELEM_FOLDER);
				Util.writeElements(uniqueElements, eleFilePath);
			}

			Util.fixURLs(uniqueElements, "href", "/w/index.php?title=");
			if (printingURLs) Util.printURLs(uniqueElements, "href");

			ArrayList<Institution> institutions = createInstitutions(uniqueElements, stateAbbr, stateName);
			if (printingInstitutions) System.out.println("\n====================== " + institutions.size()
					+ " institutions:\n" + institutions.get(17));

			ArrayList<String> csvColumns = getColumns(institutions);
			if (printingCSVColumns) {
				System.out.println("\n====================== " + csvColumns.size() + " total columns:");
				for (String column : csvColumns) {
					System.out.print(column + ",");
				}
				System.out.println();
			}
			// KEEP TESTING
			// removeEmptyColumns(institutions, csvColumns);

			ArrayList<String> csvRows = getValues(institutions, csvColumns);
			if (printingCSVRows) {
				System.out.println("\n====================== " + csvRows.size() + " total rows:");
				for (String row : csvRows) {
					System.out.print(row + "\n");
				}
			}

			if (combiningAllFiles) Util.combineAllFiles("(ALL).txt", ROWS_FOLDER, HEAD_FOLDER, VALS_FOLDER, GPS_FOLDER,
					IP_FOLDER);

			writeCSV(CSV_FOLDER, csvColumns, csvRows);

			System.out.println("Thread for state " + stateNumber + " finished.");
		}
	}

	public static void setInstOptions(boolean[] instOptions) {
		CSVWriter.instOptions = instOptions;
	}

	/**
	 * Get all the elements from the Jsoup document that match a given institution type. If you want to do states other than NC, This method is
	 * where you will need to address undesired elements
	 * 
	 * @param doc
	 * @return
	 */
	private static ArrayList<Element> getElements(Document doc) {
		// get all elements without checking for duplicates
		ArrayList<Element> dupedElements = new ArrayList<Element>();
		for (String type : TYPES) {
			for (Element e : doc.getElementsContainingOwnText(type)) {
				// filter out irrelevant classes
				if (!e.hasClass("toctext") && !e.hasClass("mw-headline") && !e.hasClass("navbox-group")
						&& !e.hasClass("reference-text")) {
					// filter out irrelevant attributes
					if (!e.attr("href").contains("/wiki/Carnegie_Classification_of_Institutions_of_Higher_Education")
							&& !e.attr("href").contains("/wiki/List_of_") && !e.attr("href").contains("/wiki/Category")
							&& !e.attr("href").equals("/wiki/College")
							&& !e.attr("href").equals("/wiki/North_Carolina_Community_College_System")
							&& !e.attr("href").equals("/wiki/University_of_North_Carolina")
							&& !e.attr("dir").equals("auto") && !e.attr("dir").equals("ltr")
							&& !e.attr("rel").equals("nofollow") && !e.attr("color").equals("#FFDD00")
							&& !e.attr("class").equals("citation web") && !e.attr("style").equals("color:white")) {
						// filter out irrelevant text
						if (e.hasText() && !e.text().contains("List of") && !e.text().equals("School")) dupedElements
								.add(e);
					}
				}
			}
		}
		return dupedElements;
	}

	/**
	 * Creates an ArrayList<Institution> by taking the institution name and Wikipedia URL, and parsing the rest of its information from this page
	 * 
	 * @param elements
	 * @param stateAbbr
	 * @return
	 */
	private static ArrayList<Institution> createInstitutions(ArrayList<Element> elements, String stateAbbr,
			String stateName) {
		ArrayList<Institution> institutions = new ArrayList<Institution>();
		for (Element e : elements) {
			institutions.add(new Institution(e.text(), e.attr("href"), stateAbbr, stateName, getInstOptions()));
		}

		Iterator<Institution> instIter = institutions.iterator();
		ArrayList<String> empty = new ArrayList<String>();
		while (instIter.hasNext()) {
			Institution i = instIter.next();
			if (i.getHeaders() == null) {
				empty.add(i.toString());
				instIter.remove();
			}
		}
		return institutions;
	}

	public static boolean[] getInstOptions() {
		return instOptions;
	}

	/**
	 * Gets the valid headers across all Institutions for the CSV writer
	 * 
	 * @param institutions
	 * @return
	 */
	private static ArrayList<String> getColumns(ArrayList<Institution> institutions) {
		ArrayList<String> allColumns = new ArrayList<String>();
		ArrayList<String> columns = new ArrayList<String>();
		String column;
		for (Institution i : institutions) {
			columns = i.getHeaders();
			for (int c = 0; c < columns.size(); c++) {
				column = columns.get(c);
				if (!allColumns.contains(column)) {
					allColumns.add(column);
				}
			}
		}
		allColumns.trimToSize();
		allColumns = Util.alphabetize(allColumns);
		allColumns.remove("Name");
		allColumns.add(0, "Name");

		return allColumns;
	}

	/**
	 * Returns a comma-delimited ArrayList<String> of all institutional values
	 * 
	 * @param institutions
	 * @param columns
	 * @return
	 */
	private static ArrayList<String> getValues(ArrayList<Institution> institutions, ArrayList<String> columns) {
		ArrayList<String> allValues = new ArrayList<String>();
		ArrayList<String> theseValues = new ArrayList<String>();
		String row;
		for (Institution inst : institutions) {
			theseValues = inst.setFinalValues(columns);
			// if this institution is valid, proceed adding it to all values
			if (!theseValues.get(0).equals("INVALID_INSTITUTION")) {
				// make all values a single String and add it
				row = Util.arrayListToString(theseValues);
				allValues.add(row);
			}
		}
		allValues.trimToSize();
		return allValues;
	}

	/**
	 * Writes all data to a .csv file, usable in Microsoft Excel, OpenOffice, etc
	 * 
	 * @param folderPath
	 * @param csvColumns
	 * @param csvRows
	 */
	private static void writeCSV(String folderPath, ArrayList<String> csvColumns, ArrayList<String> csvRows) {
		Date date = new Date();
		Timestamp stamp = new Timestamp(date.getTime());
		String time = stamp.toString().replaceAll(":", ".");
		String csvFilePath = folderPath + time + ".csv";
		Util.makeFolders(folderPath);

		Path path = Paths.get(csvFilePath);
		String columns = Util.arrayListToString(csvColumns);
		System.out.println("\n====================== Writing CSV File " + csvFilePath + "!");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			writer.write(columns);

			for (String row : csvRows) {
				writer.newLine();
				writer.write(row);
			}
		} catch (IOException ioe) {
			System.err.println("File " + csvFilePath + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\t" + csvFilePath + " was written!\n");
	}

	/**
	 * Removes columns that have no values across all state institutions
	 * 
	 * @param institutions
	 * @param columns
	 */
	private static void removeEmptyColumns(ArrayList<Institution> institutions, ArrayList<String> columns) {
		ArrayList<String> theseValues = new ArrayList<String>();
		boolean removeColumn;
		String column;
		String value;
		// for every column and every institution
		for (int c = 0; c < columns.size(); c++) {
			removeColumn = true;
			column = columns.get(c);
			System.out.println("\nCurrent Column:\t" + column);// DELETE
			for (Institution inst : institutions) {
				System.out.println("Current Institution:\t" + inst.getName());// DELETE
				// check to see if that institution has this header
				int columnIndex = inst.getHeaders().indexOf(column);
				// if it doesn't have this header, move on
				if (columnIndex == -1) {
					continue;
					// otherwise,
				} else {
					// get the final values for this institution
					theseValues = inst.setFinalValues(columns);
					// if it's an invalid institution, move on
					if (theseValues.size() == 1) {
						continue;
						// otherwise,
					} else {
						// get the specific value for this column
						value = theseValues.get(columnIndex);
						// print out some info
						System.out.println("Current Value:\t" + value);// DELETE
						// if the value is valid, keep the column and move on to the next
						if (value != null && !value.isEmpty() && !value.equals(" ")) {
							removeColumn = false;
							break;
						}
					}
				}
			}
			// if no institutions had a value for this column, remove it
			if (removeColumn) {
				columns.remove(column);
				System.out.println("Removed column " + column + "!");
			}
		}
	}
}
