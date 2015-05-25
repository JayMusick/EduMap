package csv;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * @author Jeremy Gilreath
 *
 */
public class Institution {
	private static final String		INST_URL_PREFIX	= "http://en.wikipedia.org";
	private static final String[]	DOMAINS			= { ".gov", ".mil", ".us", ".edu", ".org", ".net", ".com" };

	private static boolean			writingDoc;
	private static boolean			fetchOnlineUpdates;
	private static boolean			fetchOnlineOnly;
	private static boolean			writingElements;
	private static boolean			printingNodes;
	private static boolean			writingNodes;
	private static boolean			printingDescription;
	private static boolean			printingRows;
	private static boolean			printingHeaders;
	private static boolean			printingValues;
	private static boolean			printingUTF16Values;
	private static boolean			writingRows;
	private static boolean			writingHeaders;
	private static boolean			writingValues;
	private static boolean			printingFinalValues;
	private static boolean			printingGPS;
	private static boolean			writingGPS;
	private static boolean			printingIPs;
	private static boolean			writingIPs;

	private ArrayList<String>		theseHeaders, theseValues, finalValues;

	private StringBuilder			description;

	private String					name;
	private String					gpsLocation;
	private String					ipAddress;
	private String					stateName;
	private String					stateAbbr;

	/**
	 * Constructor for each Institution with the specified name, URL, state abbreviation/name, and options
	 * 
	 * @param instName
	 * @param instURL
	 * @param folderPath
	 */
	public Institution(String instName, String instURL, String stateAbbr, String stateName, boolean[] options) {
		writingElements = options[0];
		printingNodes = options[1];
		writingNodes = options[2];
		printingDescription = options[3];
		printingRows = options[4];
		printingHeaders = options[5];
		printingValues = options[6];
		writingRows = options[7];
		writingHeaders = options[8];
		writingValues = options[9];
		printingFinalValues = options[10];
		printingGPS = options[11];
		writingGPS = options[12];
		printingIPs = options[13];
		writingIPs = options[14];
		writingDoc = options[15];
		fetchOnlineUpdates = options[16];
		fetchOnlineOnly = options[17];
		printingUTF16Values = options[18];

		String stateFolder = "./" + stateAbbr + "/";

		// Replace the '?' (int val 65533), '-' (int val 8211), and '/' that appears due to formatting issues into '-' (int val 45)
		name = instName.replace((char) 65533, '-').replace((char) 8211, '-').replace("/", "-");
		if (name.length() > 100) {
			name = name.substring(0, 100);
		}
		this.stateName = stateName;
		this.stateAbbr = stateAbbr;
		description = new StringBuilder();

		final String DOCS_FOLDER = stateFolder + "docs/";
		String docsFilePath = DOCS_FOLDER + name + ".txt";
		Document instDoc = Util.getDocument(docsFilePath, INST_URL_PREFIX + instURL, writingDoc, fetchOnlineUpdates,
				fetchOnlineOnly);
		if (instDoc != null) {

			Element infoElem = Util.getElement(instDoc, "infobox vcard");
			if (infoElem != null) {
				if (writingElements) {
					final String ELEM_FOLDER = stateFolder + "elem/";
					String elemFilePath = ELEM_FOLDER + name + ".txt";
					Util.makeFolders(ELEM_FOLDER);
					Util.writeElement(infoElem, elemFilePath);
				}

				Element gpsElem = Util.getElement(instDoc, "geo-dec");
				if (gpsElem != null) {
					gpsLocation = Util.removeOutside(gpsElem.text(), " //");
				} else {
					gpsLocation = " ";
				}
				if (printingGPS) Util.printString(gpsLocation, "Coordinates");
				if (writingGPS) {
					final String GPS_FOLDER = stateFolder + "gps/";
					String gpsFilePath = GPS_FOLDER + name + ".txt";
					Util.makeFolders(GPS_FOLDER);
					Util.writeString(gpsLocation, "Coordinates", gpsFilePath);
				}

				Node infoNode = getNode(infoElem);
				if (printingNodes) Util.printNode(infoNode, 0);
				if (writingNodes) {
					final String NODE_FOLDER = stateFolder + "node/";
					String nodeFilePath = NODE_FOLDER + name + ".txt";
					Util.makeFolders(NODE_FOLDER);
					Util.writeNode(infoNode, nodeFilePath);
				}

				String infoStr = infoNode.toString().replace("\n", "");
				if (printingDescription) System.out.println("----- Description for " + name + ":\n\t" + infoStr);

				ArrayList<String> categoryRows = Util.splitCategories(infoStr, "<tr>", "</tr>");
				if (printingRows) Util.printRows(categoryRows, "Rows", false);
				if (writingRows) {
					final String ROWS_FOLDER = stateFolder + "rows/";
					String rowsFilePath = ROWS_FOLDER + name + ".txt";
					Util.makeFolders(ROWS_FOLDER);
					Util.writeRows(categoryRows, rowsFilePath, "Row");
				}

				String[] categoryHeaders = Util.splitRows(categoryRows, "<th", "</th>");
				Util.reformat(categoryHeaders, "Name", true);
				if (printingHeaders) Util.printRows(categoryHeaders, "Headers", printingUTF16Values);
				if (writingHeaders) {
					final String HEAD_FOLDER = stateFolder + "head/";
					String headFilePath = HEAD_FOLDER + name + ".txt";
					Util.makeFolders(HEAD_FOLDER);
					Util.writeRows(categoryHeaders, headFilePath, "Header");
				}

				String[] categoryValues = Util.splitRows(categoryRows, "<td", "</td>");
				Util.reformat(categoryValues, name, false);
				if (printingValues) Util.printRows(categoryValues, "Values", printingUTF16Values);
				if (writingValues) {
					final String VALS_FOLDER = stateFolder + "vals/";
					String valsFilePath = VALS_FOLDER + name + ".txt";
					Util.makeFolders(VALS_FOLDER);
					Util.writeRows(categoryValues, valsFilePath, "Value");
				}

				ipAddress = getIP(categoryHeaders, categoryValues);
				if (printingIPs) Util.printString(ipAddress, "IP Address");
				if (writingIPs) {
					final String IP_FOLDER = stateFolder + "ip/";
					String ipFilePath = IP_FOLDER + name + ".txt";
					Util.makeFolders(IP_FOLDER);
					Util.writeString(ipAddress, "IP Address", ipFilePath);
				}
				updateDescription(categoryHeaders, categoryValues);
				initCategories(categoryHeaders, categoryValues);
			}
		}
	}

	/**
	 * Returns the non-empty Node contained with the Element
	 * 
	 * @param e
	 * @return
	 */
	private static Node getNode(Element e) {
		Node infoNode = null;
		for (int i = 0; i < e.childNodeSize(); i++) {
			if (!e.childNode(i).toString().equals(" ")) infoNode = e.childNode(i);
		}
		return infoNode;
	}

	/**
	 * Gets the IP Address for the Institution's website
	 * 
	 * @param headers
	 * @param values
	 * @return
	 */
	private String getIP(String[] headers, String[] values) {
		String url = null;
		for (int i = 0; i < headers.length; i++) {
			if (headers[i] != null && headers[i].equals("Website")) {
				url = values[i];
				break;
			}
		}

		String instIP = " ";
		if (url != null) {
			// replace spaces with proper formatting for URLs
			url = url.replaceAll(" ", "%20");
			// if the url contains http(s), remove all the http(s):// from the url
			if (url.contains("http") || url.contains("Http")) url = url.substring(url.indexOf("://") + 3);
			if (!url.startsWith("www.")) url = "www." + url;
			// check to see if it has a domain
			boolean domainFound = false;
			for (String domain : DOMAINS) {
				if (url.contains(domain)) {
					domainFound = true;
					url = url.substring(0, url.indexOf(domain) + domain.length());
					break;
				}
			}
			// if a domain wasn't found, default to .edu
			if (!domainFound) {
				url = Util.removeBoundaryCharacters(url, true);
				url += ".edu";
			}
			// attempt to connect
			try {
				instIP = InetAddress.getByName(url).getHostAddress();
			} catch (UnknownHostException uhe) {
				System.err.println("Could not connect to host " + url + "!");
				// uhe.printStackTrace();
				// System.exit(1);
			}
		}
		return instIP;
	}

	/**
	 * Updates the Institution description based on parsed headers and values
	 * 
	 * @param headers
	 * @param values
	 */
	private void updateDescription(String[] headers, String[] values) {
		description.append(name);
		// determine the length of the longest header
		int longest = 0;
		for (int i = 0; i < headers.length; i++) {
			if (headers[i] != null && headers[i].length() > longest) longest = headers[i].length();
		}
		// for each header
		for (int i = 0; i < headers.length; i++) {
			// add a new line with the current header name
			if (headers[i] != null) {
				description.append("\n" + headers[i] + ":");
				// add spaces up to the length of the longest header
				for (int j = headers[i].length(); j < longest; j++) {
					description.append(" ");
				}
				// add the rest of the line
				description.append("\t" + values[i]);
			}
		}
	}

	/**
	 * Puts all non-null, non-empty combinations of headers and values into their respective ArrayLists
	 * 
	 * @param headers
	 * @param values
	 */
	private void initCategories(String[] headers, String[] values) {
		boolean validInstitution = true;
		theseHeaders = new ArrayList<String>();
		theseValues = new ArrayList<String>();

		boolean checkingBoundaries;
		String header;
		String value;
		for (int i = 0; i < headers.length; i++) {
			header = headers[i];
			value = values[i];
			if (header != null && value != null && !value.isEmpty()) {
				// If we are checking the Location values
				if (header.equals("Locations")) {
					// and the value contains the state name/abbreviation, or the Name does
					if (values[0].contains(stateName) || value.contains(stateName) || value.contains(stateAbbr)) {
						// check to see if it contains any remnants from the beginning of GPS coordinates
						checkingBoundaries = true;
						while (checkingBoundaries) {
							// remove unnecessary characters from the beginning and end of the line
							value = Util.removeBoundaryCharacters(value, true);
							value = Util.removeBoundaryCharacters(value, false);
							// if it does contain remnants, remove them before adding the value to allVaues
							if (Character.isDigit(value.charAt(value.length() - 1))) {
								if (value.length() == 1) {
									value = null;
									break;
								} else {
									value = Util.removeAfterLast(value, " ");
								}
							} else {
								// otherwise, simply add it if it is not empty or null
								if (value != null && !value.isEmpty()) {
									theseHeaders.add(header);
									theseValues.add(value);
									checkingBoundaries = false;
								}
							}
						}
						// otherwise, insert a String to notify CSVWriter to remove this out-of-state/un-verifiable institution entirely
					} else {
						theseValues = new ArrayList<String>(1);
						theseValues.add("INVALID_INSTITUTION");
						validInstitution = false;
						break;
					}
					// otherwise, simply add it
				} else {
					theseHeaders.add(header);
					theseValues.add(value);
				}
			}
		}

		if (validInstitution) {
			alphabetize(theseHeaders, theseValues);

			insertColumn("Coordinates");
			insertValue("Coordinates", gpsLocation);

			insertColumn("IP Address");
			insertValue("IP Address", ipAddress);

			theseHeaders.trimToSize();
			theseValues.trimToSize();
		}
	}

	/**
	 * Alphabetizes the header names and sorts the values accordingly
	 * 
	 * @param headers
	 * @param values
	 */
	private void alphabetize(ArrayList<String> headers, ArrayList<String> values) {
		ArrayList<String> alphaHeaders = Util.alphabetize(headers);
		alphaHeaders.remove("Name");
		alphaHeaders.add(0, "Name");

		String header;
		ArrayList<String> alphaValues = new ArrayList<String>(values.size());
		for (int i = 0; i < alphaHeaders.size(); i++) {
			header = alphaHeaders.get(i);
			alphaValues.add(values.get(headers.indexOf(header)));
		}

		headers = alphaHeaders;
		values = alphaValues;
	}

	/**
	 * Inserts a new column into the list
	 * 
	 * @param newColumn
	 */
	private void insertColumn(String newColumn) {
		// if it already contains the header, ignore it
		if (theseHeaders.contains(newColumn)) {
			System.err.println("allHeaders already contains the column '" + newColumn + "'! Moving on...");
			// otherwise, add the new header
		} else {
			theseHeaders.add(newColumn);
		}
	}

	/**
	 * Inserts a new value for this column
	 * 
	 * @param column
	 * @param value
	 */
	private void insertValue(String column, String value) {
		if (theseValues.size() < theseHeaders.size()) {
			theseValues.add(value);
		} else {
			theseValues.set(theseHeaders.indexOf(column), value);
		}
	}

	/**
	 * Creates the final values for this institution to be used by the CSV Writer
	 * 
	 * @param finalHeaders
	 * @return
	 */
	public ArrayList<String> setFinalValues(ArrayList<String> finalHeaders) {
		finalValues = new ArrayList<String>(finalHeaders.size());
		String thisValue;
		// for every header in the final list
		for (String thatHeader : finalHeaders) {
			// if that header is contained by this institution
			if (theseHeaders.contains(thatHeader)) {
				thisValue = theseValues.get(theseHeaders.indexOf(thatHeader));
				// insert the value of that header into that position in final values
				finalValues.add(thisValue);
				// if this value told us the institution is invalid, stop adding values
				if (thisValue.equals("INVALID_INSTITUTION")) break;
			} else {
				finalValues.add(" ");
			}
		}
		finalValues.trimToSize();
		if (printingFinalValues) System.out.println("\nFINAL VALUES: " + finalValues);
		return finalValues;
	}

	/**
	 * @return
	 */
	public ArrayList<String> getHeaders() {
		return theseHeaders;
	}

	/**
	 * @return
	 */
	public ArrayList<String> getValues() {
		return theseValues;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return description.toString();
	}
}
