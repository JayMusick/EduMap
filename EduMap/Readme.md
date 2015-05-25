#EduMap
A multi-threaded web scraper written in Java with the Jsoup API. This program dynamically finds the Wikipedia page for every educational institution in an arbitrary number of U.S. states and, if a page exists for an institution, aggregates various data from it and writes it all institutional data to a .csv file viewable in Microsoft Excel or OpenOffice Calc.

In order for an institution to have its data parsed, it must have a page on Wikipedia; a list of these institutions for a given state can be found at: http://en.wikipedia.org/wiki/List_of_colleges_and_universities_in_ + (name of state).

Default states are: CA, FL, LA, NY, NC, ND. These give a good demonstration of the program's capabilities, and reveal many different data points for each institution. For example, at least one California institution contains information in all of the following categories:

Name, ABA Profile, Academic Staff, Admin. Staff, Affiliations, Alumni Newsletter, Athletics, Bar Pass Rate, Board Secretary, Campus, Campuses In, Chairman, Chancellor, Colors, Coordinates, Dean, Divisions, Doctoral Students, Endowment, Enrollment, Established, Faculty, Former Names, Genre Headquarters, IP Address, Industry, Locations, Mascot, Motto, Motto In English, Nickname, Parent, Postgraduates, President, Provost, Religious Affiliations, Sports, Students, Type, Undergraduates, Website.

v1.53 changes: Minor Update

	1) hashtable correctly sized to 50 elements rather than 52
	
upcoming features:

	1) redirect System.out and System.err to JTextArea (currently very slow, EDT blocked?)
	2) fix missing 'Official Website' URLs by possibly fetching them from a search engine
	3) implement ability to search for institutions using GPS coordinates and radius
	4) remove several instances of empty columns being retained in .csv
	5) investigate searching for institutions by city name
	6) put institutions on a real map using GPS coords