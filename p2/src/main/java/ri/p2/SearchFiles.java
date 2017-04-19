package ri.p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class SearchFiles {
	
	static int gtop= 1;
	
	public static void main(String[] args) throws Exception{
		String usage = "-search default | jm l| dir m"
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
		String indexPath = null;
		int cut = 0;
		int top = 0;
		List<String>search= new ArrayList<>();
		List<String>queries = new ArrayList<>();
		List<String>fieldsvisual = new ArrayList<>();
		List<String> fieldsproc = new ArrayList<>();
		
		for(int i=0; i<args.length;i++){
			if ("-search".equals(args[i])) {
			  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		search.add(args[i+1]);
		  	  		i++;
		  	  	}
		    } else if ("-indexin".equals(args[i])) {
		    	indexPath = args[i+1];
		    	i++;
		    } else if ("-cut".equals(args[i])) {
		    	cut = Integer.parseInt(args[i+1]);
		    	i++;
		    } else if ("-top".equals(args[i])) {
		    	top = Integer.parseInt(args[i+1]);
		    	gtop=top;
			    i++;
			} else if("-queries".equals(args[i])){ //-queries int1-int2 (junto)
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		queries.add(args[i+1]);
		  	  		i++;
		  	  	}
		    } else if ("-fieldsproc".equals(args[i])) {
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		if(args[i+1].equals("W")){
		  	  			fieldsproc.add("BODY");
		  	  		}
		  	  			else{fieldsproc.add("TITLE");}
			  	  		i++;
		  	  	}	
		    } else if("-fieldsvisual".equals(args[i])){
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
			  	  	switch (args[i+1]) {
				  	  	case "I":
				  	  		fieldsvisual.add("ID");
				  	  		break;
						case "T":
							fieldsvisual.add("TITLE");
							break;
						case "A":
							fieldsvisual.add("AUTHORS");
							break;
						case "B":
							fieldsvisual.add("DATE");
							break;
						case "W":
							fieldsvisual.add("BODY");
							break;
						default:
							// Unknown field. Ignore it.
							break;
			  	  	}
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
		//String pathQuery = "/home/alejandro/Escritorio/ing/3º/RI/P2/cran.qry";

		String pathQuery = "/home/ruben/Desktop/cranquerys/cran.qry";
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
			

    		if (search.get(0).equals("default")) {
    			Similarity bM25=new BM25Similarity();
    			searcher.setSimilarity(bM25);
    		}else if (search.get(0).equals("jm")){
    			float lambda= Float.valueOf(search.get(1));
    			Similarity jmS=new LMJelinekMercerSimilarity(lambda);
    			searcher.setSimilarity(jmS);
    		}else if (search.get(0).equals("dir")){
    			float mu= Float.valueOf(search.get(1));
    			Similarity dirS=new LMDirichletSimilarity(mu);
    			searcher.setSimilarity(dirS);
    		}else { 
    			System.out.println("Error, se toma default por defecto");
    	    }
		
		
		
			
			List<CranQuery> cQueries = queryParser.getQueries();
			List<CranQuery> queriesProc= new ArrayList<>();
			if(queries.get(0).equals("all")){
				queriesProc=cQueries;
			}else if(queries.size()==1){
				queriesProc.add(cQueries.get(Integer.parseInt(queries.get(0))));
			}else if(queries.size()==2){
				queriesProc=cQueries.subList(Integer.parseInt(queries.get(0)),Integer.parseInt(queries.get(1))+1);
			}
			
		StandardAnalyzer analyzer=new StandardAnalyzer();
		CranRelevances relevances = new CranRelevances();
		relevances.parse("/home/ruben/Desktop/cranquerys/cranqrel");

		List<CranDocument> docs = null;

		for (CranQuery q : queriesProc) {
				query = doMultiQuery(q,fieldsproc, analyzer);
				docs = doSearch(searcher, query,reader,fieldsvisual,top);
				updateRelevants(docs, q.getDocumentID(), relevances);
				metrics(docs, q.getDocumentID(), relevances);
			}


	}


	
	
	
	private static void metrics(List<CranDocument> docs, int queryId,
			CranRelevances relevances) {
		// Calculate required metrics
		List<Integer> relevant = relevances.RelevancesOf(queryId);
		float p10 = calculatePN(docs, relevant, 10);
		float p20 = calculatePN(docs, relevant, 20);
		float r10 = calculateRN(docs, relevant, 10);
		float r20 = calculateRN(docs, relevant, 20);
		float map = calculateMAP(docs, relevant);

		// Single Query res
		System.out.println("Metrics for query:  " + queryId);
		System.out.printf("   P@10 = %f; P@20 = %f\n", p10, p20);
		System.out.printf("   R@10 = %f; R@20 = %f\n", r10, r20);
		//TODO AP
		System.out.println();
		System.out.println("\n");

		// TODO all querys averages
		System.out.printf("   MAP  = %f\n", map);

	}

	private static float calculatePN(List<CranDocument> docs, List<Integer> rels,
			int N) {
		int max = Math.min(N, docs.size());
		int docId = -1;
		int relevantDocs = 0;
		for (int i = 0; i < max; i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int r : rels) {
				if (docId == r)
					relevantDocs++;
			}
		}

		if (docs.size() > 0)
			return (float) relevantDocs / docs.size();
		return 0.0f;
	}

	private static float calculateRN(List<CranDocument> docs, List<Integer> rels,
			int N) {
		int max = Math.min(N, docs.size());
		int docId = -1;
		int relevantDocs = 0;
		for (int i = 0; i < max; i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int r : rels) {
				if (docId == r)
					relevantDocs++;
			}
		}

		if (rels.size() > 0)
			return (float) relevantDocs / rels.size();
		return 0.0f;
	}

	private static float calculateMAP(List<CranDocument> docs, List<Integer> rels) {
		int max = Math.min(gtop, docs.size());
		int docId = -1;
		int relevantDocs = 0;
		float precision = 0.0f;
		for (int i = 0; i < max; i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (Integer r : rels) {
				if (docId == r) {
					relevantDocs++;
					if (docs.size() > 0)
						//precision += (float) relevantDocs / docs.size();
						System.out.println(relevantDocs + "   " + (i+1) + "   " + gtop + "  " + rels.size());
						precision += (float) relevantDocs / (i + 1);
				}
			}
		}

		if (rels.size() > 0)
			return precision / rels.size();
			//return precision / optTopN;
		return 0.0f;
	}
	
	public static List<CranDocument> doSearch(IndexSearcher searcher, Query query, IndexReader reader,List<String> fieldsvisual,Integer top) {
		List<CranDocument> docs = new ArrayList<CranDocument>();
		
		TopDocs topDocs=null;	

		
		try {
			topDocs = searcher.search(query, top);
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		
		System.out.println(
				"\n" + topDocs.totalHits + " results for query \"" + query.toString() + "\" showing for the first " + top
						+ " documents\n");

		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < Math.min(top, topDocs.totalHits); i++) {
			try {
				
				Document d = searcher.doc(hits[i].doc);
				//TODO ojo al +1 en el .doc
				System.out.println(getVisuals(reader.document(topDocs.scoreDocs[i].doc),fieldsvisual)+"SCORE: " + topDocs.scoreDocs[i].score+"\n");
				docs.add(new CranDocument(Integer.parseInt(d.get("ID")), hits[i].score));

			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}

		}

		return docs;
	}
	
	public static String getVisuals(Document d, List<String> fieldsvisual){
		String visuals="";

		for (String f:fieldsvisual){
			visuals=visuals+(f+": "+d.get(f)+"\n");
		}
		return visuals;
	}
	


	
	public static Query doMultiQuery(CranQuery query,List<String> fieldsproc, Analyzer analyzer) {
		List<String> queryW= new ArrayList<String>();
		List<String> fields= fieldsproc;
		Query q = null;
		queryW.add(query.getQuery());
		try {
			if (fields.size()==1){
				q= MultiFieldQueryParser.parse(queryW.toArray(new String[queryW.size()]),
						fields.toArray((new String[fields.size()])),analyzer);
			}else{
				queryW.add(queryW.get(0));
				q= MultiFieldQueryParser.parse(queryW.toArray(new String[queryW.size()]),
						fields.toArray((new String[fields.size()])),analyzer);
				}
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			e.printStackTrace();
		}
		return q;
	}

	
	private static void updateRelevants(List<CranDocument> docs, int queryId,
			CranRelevances rels) {
		int docId = 0;
		List<Integer> relevantDocs = rels.RelevancesOf(queryId);
		for (int i = 0; i < docs.size(); i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int r : relevantDocs) {
				if (docId == r)
					docs.get(i).setRelevance(true);
			}
		}
	}
	
	
	

	//FIN searchFILES TODO
}


class CranRelevances {
	
	HashMap<Integer, List<Integer>> relevances = new HashMap<Integer, List<Integer>>();

	private int finishedDocId= -1;
	private int activeDocId = -1;
	private int activeRelevance = -1;
	private boolean newDocument = false;

	public void parse(String FileName) throws FileNotFoundException,
			IOException, ParseException {
		File file = new File(FileName);
		if (file.exists() && file.isFile()) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				List<Integer> rel = new ArrayList<>();
				for (String line; (line = br.readLine()) != null;) {
					processLine(line);
					if (!newDocument){
						rel.add(activeRelevance);
					}else{
						if (!rel.isEmpty()){
							relevances.put(finishedDocId, rel);
							rel= new ArrayList<>();
							}
						
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
		
		
		int docId= 0;
		int docRel= 0;
		int i= 1;
		int j=0;
		

		try {
			while(line.charAt(i) != ' '){
				i++;
			}
			docId=Integer.parseInt(line.substring(0, i));
			i++;
			j=i;
			while(line.charAt(i) != ' '){
				i++;
			}
			docRel=Integer.parseInt(line.substring(j,i));

			newDocument = true;	
			if (activeDocId==docId){
				newDocument= false;
			}else{
				
				finishedDocId=activeDocId;
				activeDocId=docId;
				
			}
			activeRelevance = docRel;
			
		} catch (Exception e) {
			throw e;
		}
		

	}

	public List<Integer> RelevancesOf(Integer docID) {
		List<Integer> rel = relevances.get(docID);
		if (rel == null)
			rel = new ArrayList<Integer>();
		return rel;
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
	} else if (line.startsWith(".W")){
			activeField=".W";
	}else{
		if(activeField!=null){
			activeDocument.addQuery(line);
		}
	}
}


public List<CranQuery> getQueries() {
	return this.documents;
}
}

