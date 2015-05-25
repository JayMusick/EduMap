package csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * @author Jeremy Gilreath
 *
 */
public class Util {

	private static final Charset	CHARSET		= StandardCharsets.UTF_16;
	private static final String		ENCODING	= "UTF-16";

	/**
	 * Make the folders on disk if they do not already exist
	 * 
	 * @param folders
	 */
	public static void makeFolders(String... folders) {
		for (String path : folders) {
			File folder = new File(path);
			if (!folder.exists()) {
				boolean successful = (folder).mkdirs();
				if (!successful) {
					JOptionPane.showMessageDialog(null, "ERROR", "Directory Creation Failed.",
							JOptionPane.WARNING_MESSAGE);
					System.exit(1);
				}
			}
		}
	}

	/**
	 * Search for and remove duplicate elements in an ArrayList<Element> based on the provided attribute
	 * 
	 * @param dupedElements
	 * @param attribute
	 * @return
	 */
	public static ArrayList<Element> removeDuplicates(ArrayList<Element> dupedElements, String attribute) {
		boolean duplicate;
		ArrayList<Element> elements = new ArrayList<Element>();
		for (Element candidate : dupedElements) {
			duplicate = false;
			for (Element e : elements) {
				if (e.attr(attribute).equals(candidate.attr(attribute)) || e.text().equals(candidate.text())) {
					duplicate = true;
					break;
				}
			}
			if (!duplicate) elements.add(candidate);
		}
		return elements;
	}

	/**
	 * Determines whether to get a Jsoup document from on file or the Internet
	 * 
	 * @param docFilePath
	 * @param documentURL
	 * @return
	 */
	public static Document getDocument(String docFilePath, String documentURL, boolean writingDoc,
			boolean fetchOnlineUpdate, boolean fetchOnlineOnly) {
		Document doc = null;
		File f = new File(docFilePath);
		if (fetchOnlineOnly) {
			doc = Util.getDocumentURL(documentURL);
			if (writingDoc && doc != null) {
				Util.writeDoc(doc, docFilePath);
			}
		} else {
			if (f.exists() && !f.isDirectory()) {
				doc = Util.getDocumentFile(f);
			} else if (fetchOnlineUpdate) {
				doc = Util.getDocumentURL(documentURL);
				if (writingDoc && doc != null) {
					Util.writeDoc(doc, docFilePath);
				}
			}
		}
		return doc;
	}

	/**
	 * Create a Jsoup document from the given URL
	 * 
	 * @param url
	 * @return
	 */
	private static Document getDocumentURL(String url) {
		Document doc = null;
		System.out.println("\n====================== Connecting to " + url + "!");
		try {
			doc = Jsoup.connect(url).followRedirects(true).get();
			doc.outputSettings().charset(CHARSET);
		} catch (IOException ioe) {
			System.err.println("\tCouldn't create a document from " + url + "! Moving on...");
			// ioe.printStackTrace();
			// System.exit(1);
		}
		System.out.print(doc == null ? "\tDocument online is null!" : "\tDocument online is good!");
		return doc;
	}

	/**
	 * Create a Jsoup document from the given file
	 * 
	 * @param onDisk
	 * @return
	 */
	private static Document getDocumentFile(File onDisk) {
		Document doc = null;
		System.out.println("\n====================== Fetching file " + onDisk + "!");
		try {
			doc = Jsoup.parse(onDisk, ENCODING);
		} catch (IOException ioe) {
			System.err.println("Couldn't create a document from " + onDisk + "!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.print(doc == null ? "\tDocument on file is null!" : "\tDocument on file is good!");
		return doc;
	}

	/**
	 * Write a Jsoup document to file to be easily read
	 * 
	 * @param doc
	 * @param fileName
	 */
	private static void writeDoc(Document doc, String fileName) {
		System.out.println("\nWriting " + fileName + "...");
		try (OutputStreamWriter docStream = new OutputStreamWriter(new PrintStream(fileName), CHARSET)) {
			docStream.append(doc.toString());
		} catch (IOException ioe) {
			System.err.println("\n\tCouldn't print to file " + fileName + "!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\t" + fileName + " was written!");
	}

	/**
	 * Check to see if any of the URLs are to missing Wikipedia pages
	 * 
	 * @param elements
	 * @param attributeName
	 * @param attributeValue
	 */
	public static void fixURLs(ArrayList<Element> elements, String attributeName, String attributeValue) {
		for (Element e : elements) {
			if (e.attr(attributeName).contains(attributeValue)) {
				e.attr(attributeName, Util.fixMissingWikiURL(e.attr(attributeName)));
			}
		}
	}

	/**
	 * Fix the URLs to missing Wikipedia pages
	 * 
	 * @param badURL
	 * @return
	 */
	private static String fixMissingWikiURL(String badURL) {
		return "/wiki/" + badURL.substring(badURL.indexOf("=") + 1, badURL.indexOf("&"));
	}

	/**
	 * Print all URLs found to the console
	 * 
	 * @param urls
	 * @param attributeName
	 */
	public static void printURLs(ArrayList<Element> urls, String attributeName) {
		System.out.println("\n----- URLS Found:");
		for (Element e : urls) {
			if (!e.attr(attributeName).isEmpty()) System.out.println("\t" + e.attr(attributeName));
		}
	}

	/**
	 * Returns the first Element containing the desired attribute class from a Document
	 * 
	 * @param doc
	 * @param attributeClass
	 * @return
	 */
	public static Element getElement(Document doc, String attributeClass) {
		Element infoElem = null;
		for (Element e : doc.getAllElements()) {
			if (e.attr("class").equals(attributeClass)) {
				infoElem = e;
			}
		}
		return infoElem;
	}

	/**
	 * Write the attributes and text of a Jsoup element to file to be easily read
	 * 
	 * @param e
	 * @param fileName
	 */
	public static void writeElement(Element e, String fileName) {
		Path path = Paths.get(fileName);
		System.out.println("\n\tElement is good!" + "\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			writer.write("Element " + e + ":");
			writer.newLine();
			writer.write("\tAttributes:");
			writer.newLine();
			writer.write("\t\t" + e.attributes());
			writer.newLine();
			writer.write("\tText:");
			writer.newLine();
			writer.write("\t\t" + e.text());
			writer.newLine();
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.print("\t" + fileName + " was written!");
	}

	/**
	 * Write the attributes and text of an ArrayList<Element> to file to be easily read
	 * 
	 * @param elements
	 * @param fileName
	 */
	public static void writeElements(ArrayList<Element> elements, String fileName) {
		Path path = Paths.get(fileName);
		System.out.println("\n\tElements are good!" + "\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			for (Element e : elements) {
				writer.write("Element " + e + ":");
				writer.newLine();
				writer.write("\tAttributes:");
				writer.newLine();
				writer.write("\t\t" + e.attributes());
				writer.newLine();
				writer.write("\tText:");
				writer.newLine();
				writer.write("\t\t" + e.text());
				writer.newLine();
			}
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.print("\t" + fileName + " was written!");
	}

	/**
	 * Print the attributes and text of an ArrayList<Element> to the console to be easily read
	 * 
	 * @param elements
	 */
	public static void printElements(ArrayList<Element> elements) {
		for (Element e : elements) {
			System.out.println("----- Element " + e + ":");
			printElementAttributes(e);
			printElementText(e);
		}
	}

	/**
	 * Print the child nodes of a Jsoup element
	 * 
	 * @param e
	 */
	public static void printElementChildNodes(Element e) {
		for (int i = 0; i < e.childNodeSize(); i++) {
			System.out.println("\tChild Node " + i + ":\n\t\t" + e.childNode(i));
		}
	}

	/**
	 * Print the text of a Jsoup element
	 * 
	 * @param e
	 */
	public static void printElementText(Element e) {
		System.out.println("\tText:\t\t" + e.text());
	}

	/**
	 * Print the attributes of a Jsoup element
	 * 
	 * @param e
	 */
	public static void printElementAttributes(Element e) {
		System.out.println("\tAttributes:\t\t" + e.attributes());
	}

	/**
	 * Print the children of a Jsoup element
	 * 
	 * @param e
	 */
	public static void printElementChildren(Element e) {
		for (int i = 0; i < e.children().size(); i++) {
			System.out.println("\tChild " + i + ": \n\t\t" + e.children());
		}
	}

	/**
	 * Get all the nodes from a Jsoup element
	 * 
	 * @param e
	 * @return
	 */
	public static ArrayList<Node> getNodes(Element e) {
		ArrayList<Node> nodes = new ArrayList<Node>(e.childNodeSize());
		for (int i = 0; i < e.childNodeSize(); i++) {
			nodes.add(e.childNode(i));
		}
		return nodes;
	}

	/**
	 * Print the attributes of a Node to the console to be easily read
	 * 
	 * @param n
	 * @param count
	 */
	public static void printNode(Node n, int count) {
		System.out.println("\n----- Node " + count + ":");
		System.out.println(n);
		System.out.println("\tAttributes:");
		System.out.println("\t\t" + n.attributes());
	}

	/**
	 * Print the attributes of all Nodes to the console to be easily read
	 * 
	 * @param nodes
	 */
	public static void printNodes(ArrayList<Node> nodes) {
		int count = 0;
		for (Node n : nodes) {
			printNode(n, ++count);
		}
	}

	/**
	 * Write the attributes of a Jsoup Node to file to be easily read
	 * 
	 * @param n
	 * @param fileName
	 */
	public static void writeNode(Node n, String fileName) {
		Path path = Paths.get(fileName);
		System.out.print("\tNode is good!" + "\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			writer.write("Node 0:");
			writer.write("" + n);
			writer.newLine();
			writer.write("\tAttributes:");
			writer.newLine();
			writer.write("\t\t" + n.attributes());
			writer.newLine();
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\n\t" + fileName + " was written!");
	}

	/**
	 * Write the attributes of all Jsoup Nodes to file to be easily read
	 * 
	 * @param nodes
	 * @param fileName
	 */
	public static void writeNodes(ArrayList<Node> nodes, String fileName) {
		Path path = Paths.get(fileName);
		System.out.print("\tNodes are good!" + "\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			int count = 0;
			for (Node n : nodes) {
				writer.write("Node " + String.format("%02d", count++) + ":");
				writer.write("" + n);
				writer.newLine();
				writer.write("\tAttributes:");
				writer.newLine();
				writer.write("\t\t" + n.attributes());
				writer.newLine();
			}
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\n\t" + fileName + " was written!");
	}

	/**
	 * Splits a String into an array of Strings based on a delimiter
	 * 
	 * @param text
	 * @param delimiter
	 * @param ending
	 * @return
	 */
	public static ArrayList<String> splitCategories(String text, String delimiter, String ending) {
		String[] info = text.split(delimiter);
		ArrayList<String> infoArray = new ArrayList<String>();
		int endIndex;
		for (String s : info) {
			endIndex = s.indexOf(ending);
			if (endIndex != -1) infoArray.add(s.substring(0, endIndex));
		}
		infoArray.trimToSize();
		return infoArray;
	}

	/**
	 * Splits an ArrayList<String> into a new String[] based on beginning and ending indices for each element
	 * 
	 * @param rows
	 * @param beginning
	 * @param ending
	 * @return
	 */
	public static String[] splitRows(ArrayList<String> rows, String beginning, String ending) {
		String row;
		String[] splitRows = new String[rows.size()];
		for (int i = 0; i < rows.size(); i++) {
			row = rows.get(i);
			if (row.contains(beginning)) {
				splitRows[i] = row.substring(row.indexOf(beginning), row.indexOf(ending) + ending.length());
			} else {
				splitRows[i] = null;
			}
		}
		return splitRows;
	}

	/**
	 * Prints out data within a specific HTML tag from an Array of text rows
	 * 
	 * @param rows
	 * @param name
	 * @param intVal
	 */
	public static void printRows(String[] rows, String name, boolean intVal) {
		System.out.println("\n----- Category " + name + ":");
		for (int i = 0; i < rows.length; i++) {
			System.out.println(name.substring(0,
					name.charAt(name.length() - 1) == 's' ? name.length() - 1 : name.length())
					+ " " + String.format("%02d", i) + ":\t" + rows[i]);
			if (intVal && rows[i] != null) {
				for (char c : rows[i].toCharArray()) {
					System.out.print("char " + c + ": (int) " + (int) c + " / ");
				}
				System.out.println();
			}
		}
	}

	/**
	 * Prints out data within a specific HTML tag from an ArrayList of text rows
	 * 
	 * @param name
	 * @param intVal
	 */
	public static void printRows(ArrayList<String> rows, String name, boolean intVal) {
		System.out.println("\n----- Category " + name + ":");
		for (int i = 0; i < rows.size(); i++) {
			System.out.println(name.substring(0,
					name.charAt(name.length() - 1) == 's' ? name.length() - 1 : name.length())
					+ " " + String.format("%02d", i) + ":\t" + rows.get(i));
			if (intVal && rows.get(i) != null) {
				for (char c : rows.get(i).toCharArray()) {
					System.out.print("char " + c + ": (int) " + (int) c + " / ");
				}
				System.out.println();
			}
		}
	}

	/**
	 * Write the rows of a String[] to file, with each row starting with the type
	 * 
	 * @param rows
	 * @param fileName
	 * @param type
	 */
	public static void writeRows(String[] rows, String fileName, String type) {
		Path path = Paths.get(fileName);
		System.out.print("\t" + type + "s are good!" + "\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			for (int i = 0; i < rows.length; i++) {
				writer.write(type + " " + String.format("%02d", i) + ":\t" + rows[i]);
				writer.newLine();
			}
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\n\t" + fileName + " was written!");
	}

	/**
	 * Write the rows of an ArrayList<String> to file, with each row starting with the type
	 * 
	 * @param rows
	 * @param fileName
	 * @param type
	 */
	public static void writeRows(ArrayList<String> rows, String fileName, String type) {
		Path path = Paths.get(fileName);
		System.out.print("\t" + type + "s are good!" + "\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			for (int i = 0; i < rows.size(); i++) {
				writer.write(type + " " + String.format("%02d", i) + ":\t" + rows.get(i));
				writer.newLine();
			}
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\n\t" + fileName + " was written!");
	}

	/**
	 * Reformats the rows to contain only relevant text
	 * 
	 * @param rows
	 * @param firstLineName
	 */
	public static void reformat(String[] rows, String firstLineName, boolean isHeader) {
		String line;
		for (int i = 0; i < rows.length; i++) {
			line = rows[i];
			// The first part is always the institution name
			if (i == 0) {
				line = firstLineName;
				// The others have extraneous tags and formatting to remove
			} else {
				// if the line isn't null
				if (line != null) {
					// If the line is a magazine, remove it
					if (isHeader
							&& (line.contains("College and university rankings") || line.contains("National")
									|| line.contains("Forbes") || line.contains("U.S._News_%26_World_Report")
									|| line.contains("Global") || line.contains("Washington Monthly")
									|| line.contains("ARWU") || line.contains("QS") || line.contains("Times") || line
										.contains("Bloomberg"))) {
						line = null;
						// Otherwise, it contains useful data
					} else {
						line = extractFromTags(line);
						line = removeFormatting(line, false);
						if (line.isEmpty()) {
							line = null;
						} else {
							if (isHeader) {
								line = fixHeaders(line);
							} else {
								line = fixValues(line);
							}
							line.trim();
							// if the line is empty or several other useless values, may as well make it null
							if (line.equals("N/A") || line.equals("not available") || line.equals("~")
									|| line.isEmpty()) {
								line = null;
							} else {
								// remove unnecessary characters from the beginning and end of the line
								line = removeBoundaryCharacters(line, true);
								line = removeBoundaryCharacters(line, false);
							}
						}
					}
				}
			}
			rows[i] = line;
		}
	}

	/**
	 * Performs formatting for future CSV columns
	 * 
	 * @param line
	 * @return
	 */
	private static String fixHeaders(String line) {
		// capitalize the first letter of each word
		if (line.contains(" ") || line.contains("-")) line = capitalize(line);
		// combine formation, founded, and opened with established
		if (line.equals("Formation") || line.equals("Founded") || line.equals("Opened")) line = "Established";
		// combine area and coordinates
		if (line.equals("Area")) line = "Coordinates";
		// combine budget and endowment
		if (line.equals("Budget")) line = "Endowment";
		// combine slogan and all non-English mottos
		if (line.equals("Slogan") || (line.contains("Motto") && !line.contains("English"))) line = "Motto";
		// combine Parent School and Parent Institution
		if (line.equals("Parent School")) line = "Parent Institution";
		// combine permutations of (Campus) Location(s) and Town Or City
		if (line.equals("Town Or City") || line.contains("Location")) line = "Locations";
		// combine permutations of color/colour(s)
		if (line.contains("Color") || line.contains("Colour")) line = "Colors";
		// combine permutations of (School) Type
		if (line.contains("Type")) line = "Type";
		// combine permutations of Student(/-)Faculty Ratio
		if (line.contains("Faculty Ratio")) line = "Sudent-Faculty Ratio";
		// combine permutations of (Sports) Mascot
		if (line.contains("Mascot")) line = "Mascot";
		// combine permutations of (Official) Website
		if (line.contains("Website")) line = "Website";
		// combine permutations of (Religious) Affiliation(s), but separately
		if (line.contains("Affiliation")) line = (line.contains("Religious") ? "Religious " : "") + "Affiliations";
		// combine permutations of Vice(-/ )President(s)
		if (line.contains("Vice") && line.contains("President")) line = "Vice President";
		// combine permutations of Former Name(s)
		if (line.contains("Former Name")) line = "Former Names";
		// combine permutations of Founder(s)
		if (line.contains("Founder")) line = "Founders";
		return line;
	}

	/**
	 * Performs formatting for future CSV values
	 * 
	 * @param line
	 * @return
	 */
	private static String fixValues(String line) {
		// replace multiple whitespace characters with a single space
		line = line.replaceAll("\\s+", " ");
		// if it contains m2/km2, superscript the 2
		if (line.contains("m2")) line = line.replaceAll("m2", "m" + String.valueOf((char) 178));
		// if the line didn't insert a space between 'US' and "$", or did insert a space after "$"
		if (line.contains("$")) line = line.replaceAll("US\\$", "US \\$").replaceAll("\\$ ", "\\$");
		// if it contains GPS coordinates, remove them
		if (line.contains("Coordinates:")) line = removeAfterFirst(line, "Coordinates:");
		if (line.contains(String.valueOf((char) 176))) line = removeAfterFirst(line, String.valueOf((char) 176));
		// if the line contains a space, fix improper punctuation
		if (line.contains(" ")) {
			line = line.replaceAll(" \\+", "+").replaceAll("\\( ", "(").replaceAll(" \\)", ")").replaceAll(" ,", ",");
			// and if the line contains poorly formatted date data for the 'established' header, fix that too
			if (line.contains(" days ago)")) line = removeAfterLast(line, "(");
		}
		// if the line contains an underscore but is not a URL, make it a space
		if (!(line.contains("http") || line.contains("www.") || line.contains(".edu"))) line = line
				.replaceAll("_", " ");
		return line;
	}

	/**
	 * returns an alphabetized an ArrayList<String> of the ArrayList<String> passed to it
	 * 
	 * @param headers
	 * @return
	 */
	public static ArrayList<String> alphabetize(ArrayList<String> headers) {
		ArrayList<String> alphabetized = new ArrayList<String>(headers.size());
		for (String header : headers) {
			alphabetized.add(header);
		}
		Collections.sort(alphabetized);
		return alphabetized;
	}

	/**
	 * Capitalizes the first letter in every word contained in a String
	 * 
	 * @param line
	 */
	private static String capitalize(String line) {
		char[] lineArray = line.toCharArray();
		StringBuilder newHeader = new StringBuilder();

		boolean capNext = false;
		for (int i = 0; i < lineArray.length; i++) {
			if (i == 0 || capNext) {
				newHeader.append(String.valueOf(lineArray[i]).toUpperCase());
				capNext = false;
			} else {
				if (lineArray[i] == ' ' || lineArray[i] == '-') {
					capNext = true;
				}
				newHeader.append(String.valueOf(lineArray[i]));
			}
		}
		return newHeader.toString();
	}

	/**
	 * Removes any remaining unnecessary characters from the end of a String
	 * 
	 * @param line
	 * @return
	 */
	public static String removeBoundaryCharacters(String line, boolean fromTheEnd) {
		if (line != null) {
			boolean keepChecking = true;
			int position;
			while (keepChecking && line != null) {
				position = fromTheEnd ? line.length() - 1 : 0;
				if (line.charAt(position) == ',' || line.charAt(position) == ';' || line.charAt(position) == ':'
						|| line.charAt(position) == '-' || line.charAt(position) == ' '
						|| line.charAt(position) == '\\' || line.charAt(position) == '/'
						|| line.charAt(position) == (char) 134 || line.charAt(position) == (char) 8224) {
					if (line.length() == 1) {
						line = null;
					} else {
						line.trim();
						line = fromTheEnd ? line.substring(0, position) : line.substring(1);
						line.trim();
					}
				} else {
					keepChecking = false;
				}
			}
		}
		return line;
	}

	/**
	 * Returns a substring of the original line without anything after the first index of the passed sequence
	 * 
	 * @param line
	 * @param toRemove
	 * @return
	 */
	private static String removeAfterFirst(String line, String toRemove) {
		return line.substring(0, line.indexOf(toRemove));
	}

	/**
	 * Returns a substring of the original line without anything after the last index of the passed sequence
	 * 
	 * @param line
	 * @param toRemove
	 * @return
	 */
	public static String removeAfterLast(String line, String toRemove) {
		return line.substring(0, line.lastIndexOf(toRemove));
	}

	/**
	 * Replaces HTML/Wikipedia-specific characters and formatting with actual UTF-16 values
	 * 
	 * @param info
	 * @return
	 */
	private static String removeFormatting(String info, boolean printEachStep) {
		if (printEachStep) System.out.println("After Formatting Step 00: " + info);
		String formatted = info.replaceAll(String.valueOf("\n"), "").replaceAll(String.valueOf("\r"), "");
		if (printEachStep) System.out.println("After Formatting Step 01: " + formatted);
		// if it contains HTML-specific characters, replace them with their actual UTF-16 values
		if (formatted.contains("&")) formatted = formatted.replaceAll("&nbsp;", " ").replaceAll("&amp;", "&")
				.replaceAll("&quot;", "\"").replaceAll("&sup2;", "2").replaceAll("&deg;", String.valueOf((char) 176))
				.replaceAll("&frac12;", String.valueOf((char) 189)).replaceAll("&gt;", ">").replaceAll("&lt;", "<")
				.replaceAll("&oacute;", String.valueOf((char) 243)).replaceAll("&AElig;", String.valueOf((char) 198))
				.replaceAll("&eacute;", String.valueOf((char) 233)).replaceAll("&reg;", String.valueOf((char) 174))
				.replaceAll("&ograve;", String.valueOf((char) 242)).replaceAll("&iacute;", String.valueOf((char) 237));
		if (printEachStep) System.out.println("After Formatting Step 02: " + formatted);
		// remove several undesirable UTF-16 characters
		formatted = formatted.replaceAll("\\" + String.valueOf((char) 63), "'")
				.replaceAll(String.valueOf((char) 65533), "").replaceAll(String.valueOf((char) 9608), "")
				.replaceAll(String.valueOf((char) 8226), "").replaceAll(">", "")
				.replaceAll(String.valueOf((char) 8212), "-");
		if (printEachStep) System.out.println("After Formatting Step 03: " + formatted);
		// if the string contains an open bracket, remove wikipedia-specific formatting
		if (formatted.contains("[")) {
			// System.out.println("It contains a bracket!");
			for (int i = 1; i < 31; i++) {
				formatted = formatted.replaceAll("\\[" + i + "]", "");
			}
			formatted = formatted.replaceAll("\\[citation needed]", "").replaceAll("\\[dead link]", "")
					.replaceAll("\\[update]", "").replaceAll("\\[]", "");
		}
		if (printEachStep) System.out.println("After Formatting Step 04: " + formatted);
		return formatted.trim();
	}

	/**
	 * Returns the substring within the given bounds, if it exists
	 * 
	 * @param text
	 * @param bounds
	 * @return
	 */
	public static String removeOutside(String text, String bounds) {
		if (text.contains(" / ")) {
			return text.substring(text.indexOf("bounds" + 3, text.lastIndexOf("bounds")));
		} else {
			return text;
		}
	}

	/**
	 * Returns all text placed within HTML tags
	 * 
	 * @param row
	 * @return
	 */
	private static String extractFromTags(String row) {
		boolean copying = false;
		String extracted = "";
		for (char c : row.toCharArray()) {
			if (c == '>') copying = true;
			if (c == '<') copying = false;

			if (copying) {
				extracted += c;
			}
		}
		return extracted;
	}

	/**
	 * Turns an ArrayList<String> into a comma-delimited String
	 * 
	 * @param list
	 * @return
	 */
	public static String arrayListToString(ArrayList<String> list) {
		StringBuilder sb = new StringBuilder();
		for (int v = 0; v < list.size(); v++) {
			if (v != 0) sb.append(",");
			sb.append("\"" + list.get(v).replaceAll("\"", "''") + "\"");
		}
		return sb.toString();
	}

	/**
	 * Prints the attribute type and its value to the console
	 * 
	 * @param value
	 * @param title
	 */
	public static void printString(String value, String title) {
		System.out.println("\n----- " + title + " :\n\t" + value);
	}

	/**
	 * Writes a String and its title to file
	 * 
	 * @param data
	 * @param title
	 * @param fileName
	 */
	public static void writeString(String data, String title, String fileName) {
		Path path = Paths.get(fileName);
		System.out.print("\n\t" + title + (title.substring(title.length() - 2).equals("ss") ? " is" : " are")
				+ " good!\nWriting " + fileName + "...");
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			writer.write(data);
		} catch (IOException ioe) {
			System.err.println("File " + fileName + " couldn't be read/written to!");
			ioe.printStackTrace();
			System.exit(1);
		}
		System.out.println("\n\t" + fileName + " was written!");
	}

	/**
	 * Combines all text files in the folder directory into a single file (fileName)
	 * 
	 * @param fileName
	 * @param folders
	 */
	public static void combineAllFiles(String fileName, String... folders) {
		String line;
		File dir;
		File output;
		File[] allFiles;
		boolean created;
		boolean successful;
		for (String folder : folders) {
			successful = false;
			dir = new File(folder);
			output = new File(folder + fileName);
			if (output.exists()) output.delete();
			if (!dir.exists()) dir.mkdir();
			System.out.println("\n====================== Combining all files in " + folder + "...");
			try {
				created = (output).createNewFile();
				if (created && dir.isDirectory()) {
					allFiles = dir.listFiles();
					try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder + fileName, true)))) {
						for (File f : allFiles) {
							try (BufferedReader br = new BufferedReader(new FileReader(f))) {
								while ((line = br.readLine()) != null) {
									out.println(line);
								}
								successful = true;
							} catch (IOException ioe) {
								System.err.println("\tCannot read from file " + f + "!");
								ioe.printStackTrace();
								System.exit(1);
							}
						}
					} catch (IOException ioe) {
						System.err.println("\tCannot read from file " + fileName + "!");
						ioe.printStackTrace();
						System.exit(1);
					}
				} else {
					System.err.println("\tFile " + output + " could not be created!");
				}
			} catch (IOException ioe) {
				System.err.println("\tCannot create file " + output + "!");
				ioe.printStackTrace();
				System.exit(1);
			}
			if (successful) System.out.println("\tCombined all files in " + folder + " into " + fileName + "!");
		}
	}

}
