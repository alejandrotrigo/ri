package ri.p2;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import ri.p2.CranEvaluator.CranQuery;
import ri.p2.CranEvaluator.CranQueryParser;

public class SearchFiles {
	
	public static void main(String[] args){
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
		List<String>listaCampos = new ArrayList<>();
		List<String>listaCamposVisual = new ArrayList<>();
		Path INDEX_PATH= null;
		StringBuilder model = new StringBuilder();
		
		for(int i=0; i<args.length;i++){
			if ("-search".equals(args[i])) {
				search = args[i+1];
				i++;
		    } else if ("-index".equals(args[i])) {
		    	indexPath = args[i+1];
		    	i++;
		    } else if ("-cut".equals(args[i])) {
		    	cut = Integer.parseInt(args[i+1]);
		    	i++;
		    } else if ("-top".equals(args[i])) {
		    	top = Integer.parseInt(args[i+1]);
			    i++;
			} else if ("-indexingmodel".equals(args[i])) {
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		model.append(args[i+1]);
		  	  		i++;
		  	  	}	
		    } else if("-queries".equals(args[i])){ //-queries int1-int2 (junto)
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		queries.add(args[i+1]);
		  	  		i++;
		  	  	}
		    }
		}
		
		
		if (queries.get(0).equals("all")){
			//Todas las queries
		}else if(queries.size() == 1){
			//Se procesa solo la query que se indica
		}else {
			//Lista de queris a procesar
		}
		
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
		queryParser.parse(pathQuery);
		List<CranQuery> cQueries = queryParser.getQueries();
		
		for (CranQuery q : cQueries){
			
		}
		

		TopDocs topDocs = null;

		try {
			topDocs = searcher.search(query, 10);
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
	}
}
