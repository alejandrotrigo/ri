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
import java.util.List;

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
	  
	  //TODO:validate arguments
	  
	  //TODO indexes1 indexes2
			  String usage = "-openmode openmode (append, create, create_or_append) "
						+"-index pathname "
						+"-coll pathname (*.sgm files) "
						+"-colls pathname_1 ... pathname_n "
						+"-indexes1 pathname_0 pathname_1 ... pathname_n "
						+"-indexes2 pathname_0 ";
		String openMode = "create";
		String indexPath = null;
		String coll = null;
		String indexes2 = null;
		ArrayList<String> colls = new ArrayList<>();
		ArrayList<String> indexes1 = new ArrayList<>();
		
		
		


		for(int i=0; i<args.length;i++){
			if ("-openmode".equals(args[i])) {
		      openMode = args[i+1];
		      i++;
		    } else if ("-index".equals(args[i])) {
		      indexPath = args[i+1];
		      i++;
		    } else if ("-coll".equals(args[i])) {
		      coll = args[i+1];
		      colls.add(coll);
		      i++;
		    } else if ("-colls".equals(args[i])) {
		  	  while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  		  colls.add(args[i+1]);
			    	  i++;
		  	  }
		    } else if ("-indexes1".equals(args[i])){
			  while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  		  indexes1.add(args[i+1]);
			    	  i++;
		  	  }
		    } else if ("-indexes2".equals(args[i])){
		  	  indexes2 = args[i+1];
		  	  i++;
		    }
		}

		if (indexPath == null){
			System.err.println("Usage: "+ usage);
			System.exit(1);
		}

		final Path INDEX_PATH = Paths.get(indexPath);
		if (!Files.isReadable(INDEX_PATH)){
			System.out.println("Document directory '" +INDEX_PATH.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
		  System.exit(1);
		}


    Date start = new Date();
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
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
      // Optional: for better indexing performance, if you
      // are indexing many documents, increase the RAM
      // buffer.  But if you do this, increase the max heap
      // size to the JVM (eg add -Xmx512m or -Xmx1g):
      //
      // iwc.setRAMBufferSizeMB(256.0);
      
      //TODO:separar index de indexes1 de indexes2
      IndexWriter writer = new IndexWriter(dir, iwc);
      List<IndexWriter> writers= createIws(indexes1,iwc);
      
      for (String columna:colls){
    	  if (indexPath != null){
    		  indexDocs(writers.get(0), Paths.get(columna),1);
    		  writers.remove(0);
    	  }else{
    		  Path indice=null;
    	   	   int a= new ThreadPool(columna,indice,writer).execute();
    	  }
      }
      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here.  This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      // writer.forceMerge(1);

      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }
  
  
	static List<IndexWriter> createIws(List<String> indexes1,IndexWriterConfig iwc) throws IOException{
		
		List<IndexWriter> iws= new ArrayList<>();
	    Directory dir =null;

		
		for (String index: indexes1){
			
			dir=FSDirectory.open(Paths.get(index));
			IndexWriter writer = new IndexWriter(dir, iwc);
			iws.add(writer);
		}
		
		
		return iws;
	}
  


  static void indexDocs(final IndexWriter writer, Path path, long threadnum) throws IOException {
    if (Files.isDirectory(path)) {
    	
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        	//TODO:comprobacion nombre
      		//Comprobar si el nombre del archivo es reut2-xxx.sgm
        	/*
      		int nArchivo = file.toString().length();
      		String numero= file.toString().substring((nArchivo-8), (nArchivo-5));
      		System.out.println(numero);
      		int archivon = Integer.parseInt(numero);

      		if ((archivon > 999) || (archivon <0)){
      			System.out.println("ERROR, archivo no pertenece");
      		}*/
    	    try (InputStream stream = Files.newInputStream(file)) {
    	        // make a new, empty document

              BufferedReader contents = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

              StringBuffer buff= new StringBuffer();
              String line;
              
              while((line= contents.readLine()) != null)
            	  buff.append(line).append("\n");
              buff.append(System.getProperty("line.separator"));
              
              buff.append(contents);

          		List<List<String>> articles = Reuters21578Parser.parseString(buff);

          		int i=1;
          	  	for(List<String> sdoc : articles){
          	  		
          	  		indexDoc(writer, file, sdoc,i);
          	  		i++;
          	  	}
          } catch (IOException ignore) {
            // don't index files that can't be read.
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      //indexDoc(writer, path, sdoc);
    }
  }


  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, List<String> sdoc,int docn) throws IOException {

	  
	  	Document doc = new Document();
    
  		doc.add(new TextField("TITLE", sdoc.get(0),Field.Store.YES));
  		doc.add(new TextField("BODY", sdoc.get(1),Field.Store.YES));
  		doc.add(new TextField("TOPICS", sdoc.get(2),Field.Store.YES));
  		doc.add(new TextField("DATELINE", sdoc.get(3),Field.Store.YES));
  		doc.add(new TextField("DOCNUMBER", String.valueOf(docn),Field.Store.YES));
  		SimpleDateFormat date= new SimpleDateFormat("dd-MMM-YYYY hh:mm:ss.SS");
  		try {
			Date now= date.parse(sdoc.get(4));
	  		doc.add(new StringField("DATE", now.toString(),Field.Store.YES));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



  		
  //añadir otros campos
        Field pathField = new StringField("path", file.toString(), Field.Store.YES);
        doc.add(pathField);

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
          // New index, so we just add the document (no old document can be there):
          System.out.println("adding " + file);
          writer.addDocument(doc);
        } else {
          // Existing index (an old copy of this document may have been indexed) so
          // we use updateDocument instead to replace the old one matching the exact
          // path, if present:
          System.out.println("updating " + file);
          writer.updateDocument(new Term("path", file.toString()), doc);
        }
  	}




    }
