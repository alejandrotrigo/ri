package ri.p2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

public class IndexFiles {
	

  private IndexFiles() {}

  /** Index all text files under a directory. */
  public static void main(String[] args) {
		String usage = "-openmode openmode (append, create, create_or_append) "
						+"-index pathname "
						+"-coll pathname (*.sgm files) "
						+"-indexingmodel (default | jm lambda | dir mu)";
		String openMode = "create";
		String indexPath = null;
		String coll = null;
		String indexingmodel = "default";
		Path INDEX_PATH= null;
		StringBuilder model = new StringBuilder();

		


		for(int i=0; i<args.length;i++){
			if ("-openmode".equals(args[i])) {
		      openMode = args[i+1];
		      i++;
		    } else if ("-index".equals(args[i])) {
		      indexPath = args[i+1];
		      i++;
		    } else if ("-coll".equals(args[i])) {
		      coll = args[i+1];
		      i++;
		    } else if ("-indexingmodel".equals(args[i])) {
		  	  while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  		  model.append(args[i]);
		  		  i++;
		  	  }
		    } 
		}
		
		indexingmodel = model.toString();

		if (indexPath == null){
			System.err.println("Usage: "+ usage);
			System.exit(1);
		}else{
			INDEX_PATH = Paths.get(indexPath);
		}
		
		if (!Files.isReadable(INDEX_PATH)){
			System.out.println("Document directory '" +INDEX_PATH.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
		  System.exit(1);
		}


    	try {
    	    Date start = new Date();
    		System.out.println("Indexing to directory '" + INDEX_PATH + "'...");

    		Directory dir = FSDirectory.open(INDEX_PATH);
    		Analyzer analyzer = new StandardAnalyzer();
    		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

    		if (openMode.equals("create")) {
    			iwc.setOpenMode(OpenMode.CREATE);
    		}else if (openMode.equals("create_or_append")){
    			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
    		}else if (openMode.equals("append")){
    			iwc.setOpenMode(OpenMode.APPEND);
    		}else { 
    			System.out.println("Error, se toma create por defecto");
    	    }
    		
    		
    		/*if (indexingmodel.equals("default")) {
    			iwc.setOpenMode(OpenMode.CREATE);
    		}else if (indexingmodel.equals("jm lambda")){
    			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
    		}else if (indexingmodel.equals("dir mu")){
    			iwc.setOpenMode(OpenMode.APPEND);
    		}else { 
    			System.out.println("Error, se toma create por defecto");
    	    }*/
    		
           
    		IndexWriter writer = new IndexWriter(dir, iwc);
    		indexDocs(writer, Paths.get(coll));
    		writer.close();
  
   
    		Date end = new Date();
    		System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    	} catch (IOException e) {
    		System.out.println(" caught a " + e.getClass() +
    				"\n with message: " + e.getMessage());
    	}
  }
  


  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
	  if (Files.isDirectory(path)) {
    	
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        	
        	try{
          		CranParser parser = new CranParser();
          		parser.parse(file.toString());
          		List<CranDocument> documents = parser.getDocuments();

          	  	for(CranDocument doc : documents){
          	  		indexDoc(writer, file, doc);
          	  	}
        	}catch(FileNotFoundException e){
        		e.printStackTrace();
        	}catch(ParseException e1){
        		e1.printStackTrace();
        	}
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      //indexDoc(writer, path, sdoc);
    }
  }


  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, CranDocument cDoc) throws IOException {

	  
	  	Document doc = new Document();
    
	  	doc.add(new TextField("ID", String.valueOf(cDoc.getDocumentID()),Field.Store.YES));
  		doc.add(new TextField("TITLE", cDoc.getTitle(),Field.Store.YES));
  		doc.add(new TextField("AUTHORS", cDoc.getAuthors(),Field.Store.YES));
  		doc.add(new TextField("BODY", cDoc.getBody(),Field.Store.YES));
  		doc.add(new TextField("DATE", cDoc.getDate(),Field.Store.YES));


        Field pathField = new StringField("path", file.toString(), Field.Store.YES);
        doc.add(pathField);

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
          // New index, so we just add the document (no old document can be there):
      //  System.out.println("adding " + file);
          writer.addDocument(doc);
        } else {
          // Existing index (an old copy of this document may have been indexed) so
          // we use updateDocument instead to replace the old one matching the exact
          // path, if present:
         // System.out.println("updating " + file);
          writer.updateDocument(new Term("path", file.toString()), doc);
        }
  	}
  





    }