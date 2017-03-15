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

public class IndexFiles {



  private IndexFiles() {}

  /** Index all text files under a directory. */
  public static void main(String[] args) {
			  String usage = "-openmode openmode (append, create, create_or_append) "
						+"-index pathname "
						+"-coll pathname (*.sgm files) "
						+"-colls pathname_1 ... pathname_n "
						+"-indexes1 pathname_0 pathname_1 ... pathname_n "
						+"-indexes2 pathname_0 ";
		String openMode = "append";
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
		      i++;
		    } else if ("-colls".equals(args[i])) {
		  	  while (args[i+1].charAt(0) != '-'){
		  		  colls.add(args[i+1]);
			    	  i++;
		  	  }
		    } else if ("-indexes1".equals(args[i])){
		  	  while (args[i+1].charAt(0) != '-'){
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

      if (true) { //if openMode.equals("create") {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else { //else if openMode.equals("create_or_append")
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      } /*else if openMode.equals("append"){
				iwc.setOpenMode(OpenMode.APPEND):
			}else { System.out.println("Error")}*/
      // Optional: for better indexing performance, if you
      // are indexing many documents, increase the RAM
      // buffer.  But if you do this, increase the max heap
      // size to the JVM (eg add -Xmx512m or -Xmx1g):
      //
      // iwc.setRAMBufferSizeMB(256.0);

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocs(writer, Paths.get(coll));

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

  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
          } catch (IOException ignore) {
            // don't index files that can't be read.
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
    }
  }

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


  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
		/*Comprobar si el nombre del archivo es reut2-xxx.sgm
		String nArchivo = file.toString().substring(6,9);
		int archivon = Integer.parseInt(nArchivo);

		if (nArchivo > 100 || nArchivo <0){
			System.out.println("ERROR, archivo no pertenece").
		} */
    try (InputStream stream = Files.newInputStream(file)) {
      // make a new, empty document


      BufferedReader contents = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

      StringBuffer buff= new StringBuffer();

      buff.append(contents);

      Document doc = new Document();


  	List<List<String>> articles = Reuters21578Parser.parseString(buff);



  	StringBuilder titles=new StringBuilder();


  	for(List<String> sdoc : articles){

  	//añadir los campos del doc

  		titles.append(sdoc.get(1));
        System.out.println("TITULO " + sdoc.get(1));



  		//doc.add(new TextField("TITLE", sdoc.get(2),Field.Store.YES));

  		//dateline

  		//date

  		//doc.add(new TextField("BODY", sdoc.get(4),Field.Store.YES));


  	}

		doc.add(new TextField("TITLES", titles.toString(),Field.Store.YES));


  //añadir otros campos

        Field pathField = new StringField("path", file.toString(), Field.Store.YES);
        doc.add(pathField);



        // Add the last modified date of the file a field named "modified".
        // Use a LongPoint that is indexed (i.e. efficiently filterable with
        // PointRangeQuery).  This indexes to milli-second resolution, which
        // is often too fine.  You could instead create a number based on
        // year/month/day/hour/minutes/seconds, down the resolution you require.
        // For example the long value 2011021714 would mean
        // February 17, 2011, 2-3 PM.
        doc.add(new LongPoint("modified", lastModified));

        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.
        doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

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
  }
