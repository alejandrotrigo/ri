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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class IndexProcessing {

	private IndexProcessing(){}
	
	
	/*
	public static List<Term> getNBestIdfTerms(String fieldU, int n, DirectoryReader indexReader){
		
		int numDocs = indexReader.maxDoc();
		List<(double, int)> termList = new ArrayList<>();
		
		for (final LeafReaderContext leaf : indexReader.leaves()) {
			// Print leaf number (starting from zero)
			System.out.println("We are in the leaf number " + leaf.ord);

			// Create an AtomicReader for each leaf
			// (using, again, Java 7 try-with-resources syntax)
			try (LeafReader leafReader = leaf.reader()) {

				// Get the fields contained in the current segment/leaf
				final Fields fields = leafReader.fields();

				//Opción 1 y 2, best n terms by idf terms
				System.out.println("Field = " + fieldU);
				final Terms terms = fields.terms(fieldU);
				final TermsEnum termsEnum = terms.iterator();
				int docFreq = 0;
				while (termsEnum.next() != null) {
					final String tt = termsEnum.term().utf8ToString();
					double idf = Math.log(numDocs/termsEnum.totalTermFreq());
					termList.add(idf);

				}
				
			}catch(IOException e){
				System.out.println(e);
			}	
		}
		return termList;
	}
	
	*/
public static List<List<String>> getNBestIdfTerms(String field, int n, DirectoryReader indexReader){
		
		int numDocs = indexReader.maxDoc();
		List<String> termList = new ArrayList<>();
		List<String> idfList = new ArrayList<>();
		List<String> order = new ArrayList<>();
		List<List<String>> res= new ArrayList<>();

		int i=1;
		
		for (final LeafReaderContext leaf : indexReader.leaves()) {
			// Print leaf number (starting from zero)
			System.out.println("We are in the leaf number " + leaf.ord);

			// Create an AtomicReader for each leaf
			// (using, again, Java 7 try-with-resources syntax)
			try (LeafReader leafReader = leaf.reader()) {

				
				final Fields fields = leafReader.fields();

					System.out.println("Field = " + field);
					final Terms terms = fields.terms(field);
					final TermsEnum termsEnum = terms.iterator();

					while ((termsEnum.next() != null) && (i<n)) {
							final String tt = termsEnum.term().utf8ToString();
							System.out.println("\t" + tt + "\ttotalFreq()=" + termsEnum.totalTermFreq() + "\tdocFreq="
									+ termsEnum.docFreq());
							double idf = Math.log(numDocs/termsEnum.totalTermFreq());
							termList.add(tt);
							idfList.add(String.valueOf(idf));
							order.add(String.valueOf(i));
							i++;

					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}
		res.add(order);
		res.add(termList);
		res.add(idfList);
		return res;
	}

	
	
	
public static String getNBestTfIdfTerms(String fieldU, int n, DirectoryReader indexReader){
		
		int numDocs = indexReader.maxDoc();
		
		for (final LeafReaderContext leaf : indexReader.leaves()) {
			// Print leaf number (starting from zero)
			System.out.println("We are in the leaf number " + leaf.ord);

			// Create an AtomicReader for each leaf
			// (using, again, Java 7 try-with-resources syntax)
			try (LeafReader leafReader = leaf.reader()) {

				// Get the fields contained in the current segment/leaf
				final Fields fields = leafReader.fields();

				//Opción 1 y 2, best n terms by idf terms
				System.out.println("Field = " + fieldU);
				final Terms terms = fields.terms(fieldU);
				final TermsEnum termsEnum = terms.iterator();
				int docFreq = 0;
				while (termsEnum.next() != null) {
					final String tt = termsEnum.term().utf8ToString();
					double idf = Math.log(numDocs/termsEnum.totalTermFreq());
					docFreq = termsEnum.docFreq();
				}
				
			}catch(IOException e){
				System.out.println(e);
			}	
		}
		return "TODO ";
	}
	
	public static void main(String[] args){
		//TODO validate arguments
		//IMPORTANT Supongo que solo se puede elegir una opción por cada vez que se llama al programa
		String usage = "-indexin indexfile " 
						+"-best_idfterms field n "
						+"-poor_idfterms field n "
						+"-best_tfidfterms field n "
						+"-poor_tfidfterms field n";
		String indexin = null;
		String fieldU = null;
		int numElements = 0;
		
		for(int i=0; i<args.length;i++){
			if("-indexin".equals(args[i])){
				indexin = args[i+1];
				i++;
			}else if("-best_idfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}else if("-poor_idfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}else if("-best_tfidfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}else if("-poor_tfidfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
			}
		}
		
		String indexFolder = indexin;

		Directory dir = null;
		DirectoryReader indexReader = null;
		DirectoryReader indexReader2 = null;

		//List<IndexableField> fields = null;
		
		try {
			dir = FSDirectory.open(Paths.get(indexFolder));
			indexReader = DirectoryReader.open(dir);
			indexReader2 = DirectoryReader.open(dir);
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		
		int numDocs =indexReader.maxDoc();
		System.out.println("NÚMERO DE DOCUMENTOS: " + numDocs);
		
		
		//Opciones 3 y 4 
		String s = getNBestIdfTerms(fieldU, numElements, indexReader);
		//System.out.println("Size of  indexReader.leaves() = " + indexReader.leaves().size());
		
		
	}
}
