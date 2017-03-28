package ri.p2;

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
	
	private static boolean optIndexes1 = false;
	private static boolean optIndexes2= false;


  private IndexFiles() {}

  /** Index all text files under a directory. */
  public static void main(String[] args) {
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
		Path INDEX_PATH= null;

		


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
				  optIndexes1=true;
		  		  indexes1.add(args[i+1]);
			    	  i++;
		  	  }
		    } else if ("-indexes2".equals(args[i])){
		    	optIndexes2=true;
		  	  indexes2 = args[i+1];
		  	  i++;
		    }
		}

		if (indexPath == null){
				if(indexes1.isEmpty()){
					if (indexes2==null){
						System.err.println("Usage: "+ usage);
						System.exit(1);
					}else
						INDEX_PATH = Paths.get(indexes2);
				}else{
					INDEX_PATH = Paths.get(indexes1.get(0));
				}
		}else {
			INDEX_PATH = Paths.get(indexPath);
		}
		
		if (!Files.isReadable(INDEX_PATH)){
			System.out.println("Document directory '" +INDEX_PATH.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
		  System.exit(1);
	 }


    Date start = new Date();
    try {
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
      
            if(optIndexes1==true){
        indexes1(indexes1,colls);

      }else if (optIndexes2==true){
    	  indexes2(indexes2,colls);
    	  
      }else{
	      IndexWriter writer = new IndexWriter(dir, iwc);
	      for (String columna:colls){
	    	  indexDocs(writer, Paths.get(columna),1);
	      }
    	  writer.close();
      }
  
   
      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }
  
 
	static void indexes1(List<String> indexes1, List<String> colls) throws IOException{
		List<IndexWriter> iws= new ArrayList<>();
	    final int numCores = Runtime.getRuntime().availableProcessors();
	    final ExecutorService executor = Executors.newFixedThreadPool(numCores);
	    Directory dir =null;
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	    IndexWriter baseIndex =new IndexWriter(FSDirectory.open(Paths.get(indexes1.get(0))),iwc);
	    indexes1.remove(0);
	    Directory[] dirArray= new Directory[indexes1.size()];
	    int i=0;

		for (String index: indexes1){
		    iwc = new IndexWriterConfig(analyzer);
			dir=FSDirectory.open(Paths.get(index));
			IndexWriter writer = new IndexWriter(dir, iwc);
			iws.add(writer);
			dirArray[i]=dir;
			i++;
		}

        for (String columna:colls){
          	 final Runnable worker = new WorkerThread(iws.get(0),columna);
             executor.execute(worker);
    		 // ThreadPool pool=new ThreadPool(columna,iws.get(0));
    		  //pool.execute();
        	  //indexDocs(iws.get(0),Paths.get(columna),1);
              iws.remove(0);
          }
	   executor.shutdown();

	   
	   /* Wait up to 1 hour to finish all the previously submitted jobs */
	   try {
	    executor.awaitTermination(1, TimeUnit.HOURS);
	   }
	   catch (final InterruptedException e) {
	    e.printStackTrace();
	    System.exit(-2);
	   }
	   System.out.println("Finished all threads");
        //Fusionamos indices
       baseIndex.addIndexes(dirArray);
       baseIndex.close();
	}
	
	
	static void indexes2(String indexes2, List<String> colls) throws IOException{
	    final int numCores = Runtime.getRuntime().availableProcessors();
	    final ExecutorService executor = Executors.newFixedThreadPool(numCores);
	    Analyzer analyzer = new StandardAnalyzer();
	    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	    IndexWriter writer =new IndexWriter(FSDirectory.open(Paths.get(indexes2)),iwc);

        for (String columna:colls){
          	 final Runnable worker = new WorkerThread2(writer,columna);
             executor.execute(worker);
    		 // ThreadPool pool=new ThreadPool(columna,iws.get(0));
    		  //pool.execute();
        	  //indexDocs(iws.get(0),Paths.get(columna),1);
          }
	   executor.shutdown();
	   /* Wait up to 1 hour to finish all the previously submitted jobs */
	   try {
	    executor.awaitTermination(1, TimeUnit.HOURS);
	   }
	   catch (final InterruptedException e) {
	    e.printStackTrace();
	    System.exit(-2);
	   }
	   System.out.println("Finished all threads");
	   writer.close();
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
  

	 public static class WorkerThread implements Runnable {
	        private final IndexWriter writer;
	        private final String  col;
	        public WorkerThread(final IndexWriter writer, final String col) {
	            this.writer = writer;
	            this.col = col;
	        }
	        @Override
	        public void run() {
	            try{
	            	IndexFiles.indexDocs(writer, Paths.get(col),1);
	    			//System.out.println(String.format("I am the thread '%s' and I am responsible for folder '%s'",
	    				//	Thread.currentThread().getName(), col));
	            	writer.close();
	            }
	            catch(Exception e){
	            	e.printStackTrace();
	            }
	        }
	    }
	 
	 public static class WorkerThread2 implements Runnable {
	        private final IndexWriter writer;
	        private final String  col;
	        public WorkerThread2(final IndexWriter writer, final String col) {
	            this.writer = writer;
	            this.col = col;
	        }
	        @Override
	        public void run() {
	            try{
	            	IndexFiles.indexDocs(writer, Paths.get(col),1);
	    			//System.out.println(String.format("I am the thread '%s' and I am responsible for folder '%s'",
	    				//	Thread.currentThread().getName(), col));
	            }
	            catch(Exception e){
	            	e.printStackTrace();
	            }
	        }
	    }





    }