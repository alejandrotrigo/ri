package ri.p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class CranEvaluator {
	

	
	class CranQuery {

		private int documentID = -1; // .I
		private List<String> query = null; // .W


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

		public CranQuery(int id) {
			documentID = id;
		}

		public void addQuery(String line) {
			if (query == null)
				query = new ArrayList<String>();
			query.add(line.trim());
		}

		public int getDocumentID() {
			return documentID;
		}

		public String getQuery() {
			return BuildStringFromList(query).trim();
		}
	}

	class CranQueryParser {

		private CranQuery activeDocument = null;
		private List<CranQuery> documents = null;
		private String activeField = "";
		private boolean newDocument = false;

		public void parse(String FileName) throws FileNotFoundException,
				IOException, ParseException {
			File file = new File(FileName);
			if (file.exists() && file.isFile()) {
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					documents = new ArrayList<CranQuery>();
					for (String line; (line = br.readLine()) != null;) {
						// process the line.
						processLine(line);
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
	
	private void processLine(String line) throws ParseException {
		if (line.isEmpty())
			return;

		if (line.startsWith(".I")) {
			int documentID = -1;
			try {
				documentID = Integer.parseInt(line.substring(3, line.length()));
			} catch (Exception e) {
				throw e;
			}
			activeDocument = new CranQuery(documentID);
			activeField = "";
			newDocument = true;
		} else {
			if (activeDocument != null) {
				if (line.startsWith(".W")) {
						activeField = line;
				}
			} else {
					if (!activeField.isEmpty()) {
							activeDocument.addQuery(line);	
						}
					}
				}
			}
	
	
	public List<CranQuery> GetQueries() {
		return this.documents;
	}
}

}
