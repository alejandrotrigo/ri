package udc.ri.p1ri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexProcessing {

	private IndexProcessing(){}
	
	public static void main(String[] args){
		//TODO validate arguments
		//IMPORTANT Supongo que solo se puede elegir una opción por cada vez que se llama al programa
		String usage = "-indexin indexfile " 
						+"-best_idfterms field n "
						+"-poor_idfterms field n "
						+"-best_tfidfterms field n "
						+"-poor_tfidfterms field n";
		String indexin = null;
		String field;
		int numElements;
		
		for(int i=0; i<args.length;i++){
			if("-indexin".equals(args[i])){
				indexin = args[i+1];
				i++;
			}else if("-best_idfterms".equals(args[i])){
				field = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}else if("-poor_idfterms".equals(args[i])){
				field = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}else if("-best_tfidfterms".equals(args[i])){
				field = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}else if("-poor_tfidfterms".equals(args[i])){
				field = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}
		}
		
		//Check if the given path is correct
		if(indexin == null){
			System.out.println("Usage: "+usage);
			System.exit(1);
		}
		final Path PROCESSING_INDEX_PATH = Paths.get(indexin);
		if (!Files.isReadable(PROCESSING_INDEX_PATH)){
			System.out.println("Document directory '" +PROCESSING_INDEX_PATH.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
		  System.exit(1);
		}
		
		
		
	}
}
