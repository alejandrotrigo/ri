package ri.p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class SearchFiles {
	
	public static void main(String[] args) throws Exception{
		String usage = "-search default | jm | dir"
				+"-indexin pathname "
				+"-cut n "
				+"-top n "
				+"-queries all | int1 | int1 - int2 "
				+"-fieldsproc lista-campos"
				+"-fieldvisual lista-campos"
				+"-rf1 tq td ndr ('ndr = numero de documentos relevantes')"
				+"-rf2 ndr"
				+"-prfjm nd nw"
				+"-prfdir nd nw"
				+"-explain";
		String search = "default";
		String indexPath = null;
		int cut = 0;
		int top = 0;
		List<String>queries = new ArrayList<>();
		List<String>listaCamposVisual = new ArrayList<>();
		Path INDEX_PATH= null;
		StringBuilder model = new StringBuilder();
		String[] fieldsproc = new String[2];
		
		for(int i=0; i<args.length;i++){
			if ("-search".equals(args[i])) {
				search = args[i+1];
				i++;
		    } else if ("-indexin".equals(args[i])) {
		    	indexPath = args[i+1];
		    	i++;
		    } else if ("-cut".equals(args[i])) {
		    	cut = Integer.parseInt(args[i+1]);
		    	i++;
		    } else if ("-top".equals(args[i])) {
		    	top = Integer.parseInt(args[i+1]);
			    i++;
			} else if("-queries".equals(args[i])){ //-queries int1-int2 (junto)
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		queries.add(args[i+1]);
		  	  		i++;
		  	  	}
		    } else if ("-fieldsproc".equals(args[i])) {
		    	int j = 0;
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		fieldsproc[j] = args[i+1];
		  	  		j++;i++;
		  	  		
		  	  	}	
		    } else if("-fieldvisual".equals(args[i])){ //-queries int1-int2 (junto)
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		listaCamposVisual.add(args[i+1]);
		  	  		i++;
		  	  	}
		    }
		} 
		
		
		
		/*if (queries.get(0).equals("all")){
			//Todas las queries
		}else if(queries.size() == 1){
			//Se procesa solo la query que se indica
		}else {
			//Lista de queris a procesar
		}*/
		
		// Path de las queris hardcoded
		String pathQuery = "/home/alejandro/Escritorio/ing/3º/RI/P2/cran.qry";
		IndexReader reader = null;
		Directory dir = null;
		IndexSearcher searcher = null;
		CranQueryParser queryParser = new CranQueryParser();
		Query query = null;
		
		try {
			dir = FSDirectory.open(Paths.get(indexPath));
			reader = DirectoryReader.open(dir);

		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}

			searcher = new IndexSearcher(reader);
			queryParser = new CranQueryParser();
			queryParser.parse(pathQuery);
		
		
		List<CranQuery> cQueries = queryParser.getQueries();
		StringBuilder queryArray = new StringBuilder();
		System.out.println("ASDASDASDAS "+cQueries.get(1).getDocumentID());

		for (CranQuery q : cQueries){
			queryArray.append(q.getQuery());
		}
		

		TopDocs topDocs = null;

		/*try {
			topDocs = searcher.search(query, 10);
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}*/
	}
	
}

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


public List<CranQuery> getQueries() {
	return this.documents;
}
}
