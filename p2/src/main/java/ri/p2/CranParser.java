package ri.p2;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class CranParser {
	
	private CranDocument activeDocument = null;
	private List<CranDocument> documents = null;
	private String activeField = "";
	private boolean newDocument = false;

	public void parse(String FileName) throws FileNotFoundException,
			IOException, ParseException {
		File file = new File(FileName);
		if (file.exists() && file.isFile()) {

			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				documents = new ArrayList<CranDocument>();
				for (String line; (line = br.readLine()) != null;) {
					ProcessLine(line);
					if (newDocument) {
						documents.add(activeDocument);
						newDocument = false;
					}
				}
			}
		} else {
			throw new FileNotFoundException(FileName);
		}
	}

	private void ProcessLine(String line) throws ParseException {
		if (line.isEmpty())
			return;

		if (line.startsWith(".I")) {
			// Create a new document
			int documentID = -1;
			try {
				documentID = Integer.parseInt(line.substring(3, line.length()));
			} catch (Exception e) {
				throw e;
			}
			activeDocument = new CranDocument(documentID);
			activeField = "";
			newDocument = true;
		} else {
				if (activeDocument != null) {
				if (line.startsWith(".")) {
					switch (line) {
					case ".T":
					case ".A":
					case ".B":
					case ".W":
						activeField = line;
						break;
					default:
						activeField = "";
						break;
					}
				} else {
					if (!activeField.isEmpty()) {
						switch (activeField) {
						case ".T":
							activeDocument.AddTitle(line);
							break;
						case ".A":
							activeDocument.AddAuthors(line);
							break;
						case ".B":
							activeDocument.AddDate(line);
							break;
						case ".W":
							activeDocument.AddBody(line);
							break;
						default:
							// Unknown field. Ignore it.
							break;
						}
					}
				}
			}
		}
	}

	public List<CranDocument> GetDocuments() {
		return this.documents;
	}


}



class CranDocument {

	private int documentID = -1;
	private List<String> title = null;
	private List<String> body= null;
	private List<String> date = null;
	private List<String> authors = null;


	private String BuildStringFromList(List<String> list) {
		StringBuilder sb = new StringBuilder();
		if (list != null)
			for (String s : list) {
				sb.append(s);
				if (!s.endsWith(" "))
					sb.append(" ");
			}
		return sb.toString();
	}

	public CranDocument(int id) {
		documentID = id;
	}

	public void AddTitle(String line) {
		if (title == null)
			title = new ArrayList<String>();
		title.add(line.trim());
	}

	public void AddBody(String line) {
		if (body == null)
			body = new ArrayList<String>();
		body.add(line.trim());
	}

	public void AddDate(String line) throws ParseException {
		if (date == null)
			date = new ArrayList<String>();
		date.add(line.trim());
	}

	public void AddAuthors(String line) {
		if (authors == null)
			authors = new ArrayList<String>();
		authors.add(line.trim());
	}

	public int GetDocumentID() {
		return documentID;
	}

	public String GetTitle() {
		return BuildStringFromList(title).trim();
	}

	public String GetBody() {
		return BuildStringFromList(body).trim();
	}

	public String GetDate() {
		return BuildStringFromList(date).trim();
	}

	public List<String> GetAuthors() {
		return authors;
	}

}

		
