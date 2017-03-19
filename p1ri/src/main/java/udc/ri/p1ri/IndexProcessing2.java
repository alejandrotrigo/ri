package udc.ri.p1ri;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.search.Query;



public class IndexProcessing2 {
	
	
	public static void main(String[] args) {
		String index=args[2];
		String field=args[3];
		String term= args[4];
		
		doDelDocsTerm(index,field,term);
		
}

	
	public static void doDelDocsTerm (String index,String field, String term){
		

	IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
	config.setOpenMode(OpenMode.APPEND);

	IndexWriter writer = null;

	try {
		writer = new IndexWriter(FSDirectory.open(Paths.get(index)), config);
	} catch (CorruptIndexException e1) {
		System.out.println("Graceful message: exception " + e1);
		e1.printStackTrace();
	} catch (LockObtainFailedException e1) {
		System.out.println("Graceful message: exception " + e1);
		e1.printStackTrace();
	} catch (IOException e1) {
		System.out.println("Graceful message: exception " + e1);
		e1.printStackTrace();
	}

	try {
		writer.deleteDocuments(new Term(field, term));
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	try {
		writer.forceMergeDeletes();
		// Forces merging of all segments that have deleted documents.
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

	try {
		writer.commit();
		writer.close();
	} catch (CorruptIndexException e) {
		System.out.println("Graceful message: exception " + e);
		e.printStackTrace();
	} catch (IOException e) {
		System.out.println("Graceful message: exception " + e);
		e.printStackTrace();
	}

}
	
	/*
	public void doDelDocsQuery (String squery, String index){

		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(OpenMode.APPEND);

		IndexWriter writer = null;

		try {
			writer = new IndexWriter(FSDirectory.open(Paths.get(index)), config);
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (LockObtainFailedException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}

		try {
		    Analyzer analyzer = new StandardAnalyzer();
			Query query= QueryParser.parse(squery);
			writer.deleteDocuments(query);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			writer.forceMergeDeletes();
			// Forces merging of all segments that have deleted documents.
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			writer.commit();
			writer.close();
		} catch (CorruptIndexException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		}

	}
	
	*/
}
