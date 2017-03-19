package udc.ri.p1ri;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexProcessing {
	
	private static boolean optBestIdf = false;
	private static boolean optWorstIdf = false;
	private static boolean optBestTfIdf = false;
	private static boolean optWorstTfIdf= false;

	private IndexProcessing(){}
	

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

					while ((termsEnum.next() != null) && (i<=n)) {
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



public static List<List<String>> getNWorstIdfTerms(String field, int n, DirectoryReader indexReader){
	
	int numDocs = indexReader.maxDoc();
	List<String> termList = new ArrayList<>();
	List<String> idfList = new ArrayList<>();
	List<String> order = new ArrayList<>();
	List<List<String>> res= new ArrayList<>();

	int i=1;
	
	for (final LeafReaderContext leaf : indexReader.leaves()) {
		// Print leaf number (starting from zero)
		//System.out.println("We are in the leaf number " + leaf.ord);

		// Create an AtomicReader for each leaf
		// (using, again, Java 7 try-with-resources syntax)
		try (LeafReader leafReader = leaf.reader()) {

			
			final Fields fields = leafReader.fields();

				System.out.println("Field = " + field);
				final Terms terms = fields.terms(field);
				final TermsEnum termsEnum = terms.iterator();

				while ((termsEnum.next() != null) && (i<=n)) {
						final String tt = termsEnum.term().utf8ToString();
						System.out.println("\t" + tt + "\ttotalFreq()=" + termsEnum.totalTermFreq() + 
								"\tdocFreq=" + termsEnum.docFreq());
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


public static void printIdfTerms(List<List<String>> terms, int n){
	System.out.println("N \tTerm \t idf" );
	int i=0;
	while (i<n){
	System.out.println(terms.get(0).get(i)+"\t"+terms.get(1).get(i)+"\t "+terms.get(2).get(i));
	i++;
	}
}

public static void printTfIdfTerms(List<List<String>> terms, int n){
	System.out.println("N \tTerm \t idf  tf" );
	int i=0;
	while (i<n){
	System.out.println(terms.get(0).get(i)+"\t"+terms.get(1).get(i)+"\t "
	+terms.get(2).get(i)+"\t"+terms.get(3).get(i));
	i++;
	}
}


public static List<List<String>> getNBestTfIdfTerms(String field, int n, DirectoryReader indexReader){

    int numDocs = indexReader.maxDoc();
    List<String> termList = new ArrayList<>();
    List<String> idfList = new ArrayList<>();
    List<String> order = new ArrayList<>();
    List<List<String>> res= new ArrayList<>();
    List<String> tfidf = new ArrayList<>();
    int frecuencia;
    
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

                            while ((termsEnum.next() != null) && (i<=n)) {
                                            final String tt = termsEnum.term().utf8ToString();
                                            System.out.println("\t" + tt + "\ttotalFreq()=" + termsEnum.totalTermFreq() + "\tdocFreq="
                                                            + termsEnum.docFreq());
                                            double idf = Math.log(numDocs/termsEnum.totalTermFreq());
                                            termList.add(tt);
                                            idfList.add(String.valueOf(idf));
                                            order.add(String.valueOf(i));
                                            frecuencia = termsEnum.docFreq();
                                            if (frecuencia == 0){
                                            	tfidf.add(String.valueOf(0));
                                            }else{
                                                    double tfidfV = (1 + Math.log(frecuencia)) * idf ;
                                                    tfidf.add(String.valueOf(tfidfV));
                                            }
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
    res.add(tfidf);
    return res;
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
				optBestIdf=true;
			}else if("-poor_idfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
				optWorstIdf=true;
			}else if("-best_tfidfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
				optBestTfIdf=true;
			}else if("-poor_tfidfterms".equals(args[i])){
				fieldU = args[i+1];
				numElements = Integer.parseInt(args[i+2]);
				i+=2;
				optWorstTfIdf=true;
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

		if (optBestIdf){
			List<List<String>> bestidf= getNBestIdfTerms(fieldU, numElements, indexReader);
			printIdfTerms(bestidf,numElements);
		}
		if (optWorstIdf){
			List<List<String>> worstidf= getNWorstIdfTerms(fieldU, numElements, indexReader);
			printIdfTerms(worstidf,numElements);
		}
		
		if (optBestTfIdf){
			List<List<String>> besttfidf= getNBestTfIdfTerms(fieldU, numElements, indexReader);
			printTfIdfTerms(besttfidf,numElements);
		}
		/*
		if (optWorstTfIdf){
			List<List<String>> worstidf= getNWorstIdfTerms(fieldU, numElements, indexReader);
			printIdfTerms(worstidf,numElements);
		}
		*/


		
	}
}



/*
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
