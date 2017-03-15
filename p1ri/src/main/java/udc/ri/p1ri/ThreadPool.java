package udc.ri.p1ri;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.IndexWriter;


public class ThreadPool {
	
	private String col;
	private Path indice;
	private IndexWriter writer;
	
	public ThreadPool(String col,Path indice, IndexWriter writer){
		this.writer=writer;
		this.col=col;
		this.indice=indice;
	}

	/**
	 * This Runnable takes a folder and prints its path.
	 */
	public static class WorkerThread implements Runnable {

		private final Path folder;
		private Path indice;
		private String col;
		private IndexWriter writer;

		public WorkerThread(final Path folder ) {
			this.folder = folder;
		}

		/**
		 * This is the work that the current thread will do when processed by
		 * the pool. In this case, it will only print some information.
		 */
		@Override
		public void run() {
			
			try {
				IndexFiles.indexDocs(writer,Paths.get(col),Thread.currentThread().getId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(String.format("I am the thread '%s' and I am responsible for folder '%s'",
					Thread.currentThread().getName(), folder));
		}

	}

	public int execute() {
	

		/*
		 * Create a ExecutorService (ThreadPool is a subclass of
		 * ExecutorService) with so many thread as cores in my machine. This can
		 * be tuned according to the resources needed by the threads.
		 */
		final int numCores = Runtime.getRuntime().availableProcessors();
		final ExecutorService executor = Executors.newFixedThreadPool(numCores);

		/*
		 * We use Java 7 NIO.2 methods for input/output management. More info
		 * in: http://docs.oracle.com/javase/tutorial/essential/io/fileio.html
		 *
		 * We also use Java 7 try-with-resources syntax. More info in:
		 * https://docs.oracle.com/javase/tutorial/essential/exceptions/
		 * tryResourceClose.html
		 */
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(col))) {

			/* We process each subfolder in a new thread. */
			for (final Path path : directoryStream) {
				if (Files.isDirectory(path)) {
					final Runnable worker = new WorkerThread(path);
					/*
					 * Send the thread to the ThreadPool. It will be processed
					 * eventually.
					 */
					executor.execute(worker);
				}
			}

		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		/*
		 * Close the ThreadPool; no more jobs will be accepted, but all the
		 * previously submitted jobs will be processed.
		 */
		executor.shutdown();

		/* Wait up to 1 hour to finish all the previously submitted jobs */
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
			return 0;
		} catch (final InterruptedException e) {
			e.printStackTrace();
			System.exit(-2);
		}

		System.out.println("Finished all threads");
		return 0;


	}

}