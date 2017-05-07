package ri.p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class SearchFiles {
	
	static int gtop= 1;
	
	public static void main(String[] args) throws Exception{
		String usage = "-search default | jm l| dir m"
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
		String indexPath = null;
		int cut = 0;
		int top = 0;
		int rf2=-1;
		boolean explain=false;
		List<String>rf1=new ArrayList<>();
		List<String>search= new ArrayList<>();
		List<String>queries = new ArrayList<>();
		List<String>fieldsvisual = new ArrayList<>();
		List<String> fieldsproc = new ArrayList<>();
		List<String> prfjm=new ArrayList<>();
		List<String> prfdir=new ArrayList<>();

		
		for(int i=0; i<args.length;i++){
			if ("-search".equals(args[i])) {
			  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		search.add(args[i+1]);
		  	  		i++;
		  	  	}
		    } else if ("-indexin".equals(args[i])) {
		    	indexPath = args[i+1];
		    	i++;
		    } else if ("-cut".equals(args[i])) {
		    	cut = Integer.parseInt(args[i+1]);
		    	i++;
		    } else if ("-top".equals(args[i])) {
		    	top = Integer.parseInt(args[i+1]);
		    	gtop=top;
			    i++;
			} else if("-queries".equals(args[i])){ //-queries int1-int2 (junto)
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		queries.add(args[i+1]);
		  	  		i++;
		  	  	}
		    } else if ("-fieldsproc".equals(args[i])) {
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		if(args[i+1].equals("W")){
		  	  			fieldsproc.add("BODY");
		  	  		}
		  	  			else{fieldsproc.add("TITLE");}
			  	  		i++;
		  	  	}	
		    } else if("-fieldsvisual".equals(args[i])){
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
			  	  	switch (args[i+1]) {
				  	  	case "I":
				  	  		fieldsvisual.add("ID");
				  	  		break;
						case "T":
							fieldsvisual.add("TITLE");
							break;
						case "A":
							fieldsvisual.add("AUTHORS");
							break;
						case "B":
							fieldsvisual.add("DATE");
							break;
						case "W":
							fieldsvisual.add("BODY");
							break;
						default:
							// Unknown field. Ignore it.
							break;
			  	  	}
		  	  	i++;
		  	  	}
		    }else if("-rf1".equals(args[i])){
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		rf1.add(args[i+1]);
		  	  		i++;
		  	  	}
		    }else if ("-rf2".equals(args[i])) {
			    	rf2 = Integer.parseInt(args[i+1]);
				    i++;
		  	}else if("-prfjm".equals(args[i])){
			  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
			  	  		prfjm.add(args[i+1]);
			  	  		i++;
			  	  	}
			}else if("-prfdir".equals(args[i])){
		  	  	while (((i+1) < args.length) && (args[i+1].charAt(0) != '-')){
		  	  		prfdir.add(args[i+1]);
		  	  		i++;
		  	  	}
		    }else if ("-explain".equals(args[i])) {
		    	explain = true;
			    i++;
		    }	 
			} 
		
		
		
		/*if (queries.get(0).equals("all")){
			//Todas las queries
		}else if(queries.size() == 1){
			//Se procesa solo la query que se indica
		}else {
			//Lista de queris a procesar
		}*/
		
		// Path de las queris hardcoded
		//String pathQuery = "/home/alejandro/Escritorio/ing/3º/RI/P2/cran.qry";

		String pathQuery = "/home/ruben/Desktop/cranquerys/cran.qry";
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

			List<TermData> totalTermData =initializeTermData(reader);
			try{
				reader = DirectoryReader.open(dir);
					
			}catch (IOException e1) {
				System.out.println("Graceful message: exception " + e1);
				e1.printStackTrace();
			}
			searcher = new IndexSearcher(reader);
			queryParser = new CranQueryParser();
			queryParser.parse(pathQuery);
			
			if (search.get(0).equals("default")) {
				Similarity bM25=new BM25Similarity();
				searcher.setSimilarity(bM25);
			}else if (search.get(0).equals("jm")){
				float lambda= Float.valueOf(search.get(1));
				Similarity jmS=new LMJelinekMercerSimilarity(lambda);
				searcher.setSimilarity(jmS);
			}else if (search.get(0).equals("dir")){
				float mu= Float.valueOf(search.get(1));
				Similarity dirS=new LMDirichletSimilarity(mu);
				searcher.setSimilarity(dirS);
			}else { 
				System.out.println("Error, se toma default por defecto");
		    }
		
		
		
			
			List<CranQuery> cQueries = queryParser.getQueries();
			List<CranQuery> queriesProc= new ArrayList<>();
			if(queries.get(0).equals("all")){
				queriesProc=cQueries;
			}else if(queries.size()==1){
				queriesProc.add(cQueries.get((Integer.parseInt(queries.get(0))-1)));
			}else if(queries.size()==2){
				queriesProc=cQueries.subList(Integer.parseInt(queries.get(0))-1,Integer.parseInt(queries.get(1)));
			}
			
		StandardAnalyzer analyzer=new StandardAnalyzer();
		CranRelevances relevances = new CranRelevances();
		relevances.parse("/home/ruben/Desktop/cranquerys/cranqrel");

		List<CranDocument> docs = null;
		List<Float> allAp = new ArrayList<>();

		for (CranQuery q : queriesProc) {
				List<Integer> rels=relevances.RelevancesOf(q.getDocumentID());
				float ap = 0.0f;
				query = doMultiQuery(q,fieldsproc, analyzer);
				docs = doSearch(searcher, query,reader,fieldsvisual,top,rels,q);
				updateRelevants(docs, q.getDocumentID(), relevances);
				ap = metrics(docs, q.getDocumentID(), relevances);
				allAp.add(ap);
				if (!rf1.isEmpty()){
					String rf1Res;
					CranQuery nq=null;
					rf1Res=doRf1(searcher,q,relevances,totalTermData,rf1,explain);
					nq=executeRf1(rf1Res,q);
					query = doMultiQuery(nq,fieldsproc, analyzer);
					doSearch(searcher, query,reader,fieldsvisual,top,rels,q);
				}
				if (rf2!=-1){
					CranQuery nq=null;
					nq=executeRf2(q,relevances,rf2,searcher);
					query = doMultiQuery(nq,fieldsproc, analyzer);
					doSearch(searcher, query,reader,fieldsvisual,top,rels,q);
					
				}
				if (!prfjm.isEmpty()){
					CranQuery dirjm=null;
					q.getQuery();
					dirjm=doPrf(q,prfjm,queriesProc,totalTermData,relevances,
							docs,"jm",reader,Float.valueOf(search.get(1)),explain);
					query = doMultiQuery(dirjm,fieldsproc, analyzer);
					doSearch(searcher, query,reader,fieldsvisual,top,rels,q);

				}
				if (!prfdir.isEmpty()){
					CranQuery dirq=null;
					q.getQuery();
					dirq=doPrf(q,prfdir,queriesProc,totalTermData,relevances,
							docs,"dir",reader,Float.valueOf(search.get(1)),explain);
					query = doMultiQuery(dirq,fieldsproc, analyzer);
					doSearch(searcher, query,reader,fieldsvisual,top,rels,q);

				}
				
				
				
		}
		float map = calculateMAP(allAp, cut);
		System.out.printf("MAP = %f\n", map);
		



	}	
	
	
	private static float metrics(List<CranDocument> docs, int queryId,
			CranRelevances relevances) {
		// Calculate required metrics
		List<Integer> relevant = relevances.RelevancesOf(queryId);
		float p10 = calculatePN(docs, relevant, 10);
		float p20 = calculatePN(docs, relevant, 20);
		float r10 = calculateRN(docs, relevant, 10);
		float r20 = calculateRN(docs, relevant, 20);
		float ap = calculateAP(docs, relevant);
		// Single Query res
		System.out.println("Metrics for query:  " + queryId);
		System.out.printf("   P@10 = %f; P@20 = %f\n", p10, p20);
		System.out.printf("   R@10 = %f; R@20 = %f\n", r10, r20);
		System.out.printf("   AP for query %d = %f\n", queryId, ap);
		System.out.println();
		return ap;

	}
	
	private static boolean isRelevant(CranDocument doc, List<Integer> rels){
		if (rels.contains(doc.getDocumentID())){
			return true;
		}else{
			return false;
		}
	}

	private static float calculatePN(List<CranDocument> docs, List<Integer> rels,
			int N) {
		int max = Math.min(N, docs.size());
		int docId = -1;
		int relevantDocs = 0;
		for (int i = 0; i < max; i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int r : rels) {
				if (docId == r)
					relevantDocs++;
			}
		}

		if (docs.size() > 0)
			return (float) relevantDocs / docs.size();
		return 0.0f;
	}

	private static float calculateRN(List<CranDocument> docs, List<Integer> rels,
			int N) {
		int max = Math.min(N, docs.size());
		int docId = -1;
		int relevantDocs = 0;
		for (int i = 0; i < max; i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int r : rels) {
				if (docId == r)
					relevantDocs++;
			}
		}

		if (rels.size() > 0)
			return (float) relevantDocs / rels.size();
		return 0.0f;
	}
	
	private static float calculateAP(List<CranDocument> docs, List<Integer> rels) {
		float countDocs=0;
		float countRels=0; 
		float ap = 0.0f;
		
		for(CranDocument doc : docs){
			int docId = doc.getDocumentID();
				if (rels.contains(docId)){
					countDocs++;
					countRels++;
					ap =ap+ countRels/countDocs;
				}else{
					countDocs++;
					//ap = countRels/countDocs;
				}
		}
		
		return (ap/countRels);
	}

	private static float calculateMAP(List<Float> allAp, int cut) {
		float sumAp = 0.0f;
		int i=0;
		
		if (allAp.size()>0){
			if (cut < allAp.size()){
				for (i=0; i<cut; i++){
					sumAp+=allAp.get(i);
				}
			}else{
				for(Float ap : allAp){
					sumAp+=ap;
				}
			}
		}
		
		return sumAp/cut;
	}
	
	public static List<CranDocument> doSearch(IndexSearcher searcher, Query query, IndexReader reader,List<String> fieldsvisual,Integer top,List<Integer> rels,CranQuery originalQuery) {
		List<CranDocument> docs = new ArrayList<CranDocument>();
		
		TopDocs topDocs=null;	

		
		try {
			topDocs = searcher.search(query, top);
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		
		System.out.println(
				"\n" + topDocs.totalHits + " results for query \"" + query.toString() + "\" showing for the first " + top
						+ " documents\n");

		ScoreDoc[] hits = topDocs.scoreDocs;
		Document d=null;
			for (int i = 0; i < Math.min(top, topDocs.totalHits); i++) {		
			try {	
				originalQuery.addDocs(topDocs);
				 d = searcher.doc(hits[i].doc);
				CranDocument cDoc = new CranDocument(Integer.parseInt(d.get("ID")), hits[i].score);
				cDoc.addBody(d.get("BODY"));
				docs.add(cDoc);
				if (isRelevant(cDoc, rels)){
					System.out.println(getVisuals(reader.document(topDocs.scoreDocs[i].doc),fieldsvisual)+"SCORE: " + topDocs.scoreDocs[i].score);
					System.out.println("RELEVANT\n");
				}else{
					System.out.println(getVisuals(reader.document(topDocs.scoreDocs[i].doc),fieldsvisual)+"SCORE: " + topDocs.scoreDocs[i].score+"\n");
				}

			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}

		}

		return docs;
	}
	
	public static String getVisuals(Document d, List<String> fieldsvisual){
		String visuals="";

		for (String f:fieldsvisual){
			visuals=visuals+(f+": "+d.get(f)+"\n");
		}
		return visuals;
	}
	


	
	public static Query doMultiQuery(CranQuery query,List<String> fieldsproc, Analyzer analyzer) {
		List<String> queryW= new ArrayList<String>();
		List<String> fields= fieldsproc;
		Query q = null;
		queryW.add(query.getQuery());
		try {
			if (fields.size()==1){
				q= MultiFieldQueryParser.parse(queryW.toArray(new String[queryW.size()]),
						fields.toArray((new String[fields.size()])),analyzer);
			}else{
				queryW.add(queryW.get(0));
				q= MultiFieldQueryParser.parse(queryW.toArray(new String[queryW.size()]),
						fields.toArray((new String[fields.size()])),analyzer);
				}
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			e.printStackTrace();
		}
		return q;
	}

	
	private static void updateRelevants(List<CranDocument> docs, int queryId,
			CranRelevances rels) {
		int docId = 0;
		List<Integer> relevantDocs = rels.RelevancesOf(queryId);
		for (int i = 0; i < docs.size(); i++) {
			try {
				docId = docs.get(i).getDocumentID();
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int r : relevantDocs) {
				if (docId == r)
					docs.get(i).setRelevance(true);
			}
		}
	}


	public static List<TermData> initializeTermData(IndexReader indexReader){
        List<TermData> tdList=new ArrayList<>();

                
        for (final LeafReaderContext leaf : indexReader.leaves()) {
                try (LeafReader leafReader = leaf.reader()) {

                		String field= "BODY";
                        int numDocs = indexReader.maxDoc();
                        final Fields fields = leafReader.fields();
                        final Terms terms = fields.terms(field);
                        final TermsEnum termsEnum = terms.iterator();
                        PostingsEnum postings = null;
                        int tf=0;
                        int docFreq=0;
                        String termino=null;
                        
                            
                        

                        while (termsEnum.next() != null) {
                            postings = termsEnum.postings(postings, PostingsEnum.FREQS);
                        	
                        	while(postings.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                                tf = postings.freq();
                                docFreq = termsEnum.docFreq();
                                termino = termsEnum.term().utf8ToString();
                                TermData td = new TermData(termino, docFreq, numDocs, tf, postings.docID());
                                tdList.add(td);
                                
                            }
                        }
                        return tdList;


                } catch (IOException e) {
                        e.printStackTrace();
                
                }
                return null;
        }
		return null;
                
}
	
	
	public static String doRf1(IndexSearcher searcher,CranQuery q,CranRelevances qRelevances,
			List<TermData> totalTermData ,List<String> rf1,boolean explain){		
		
		
		List<Integer> relevants=new ArrayList<>();
		List<List<TermData>> finalTerms=new ArrayList<>();
		List<String> docsBody=new ArrayList<>();
		String s="";
		int r=0;
		List<TermData> tqTerms= new ArrayList<>();

		if(!rf1.isEmpty()){

				String sTerms=q.getQuery();
				List<String> stringTerms=Arrays.asList(sTerms.split(" "));
				Iterator<String> iter= stringTerms.iterator();
				TermData a=null;
				String b=null;
				while (iter.hasNext()){
					b=iter.next();
					Iterator<TermData> iter2= totalTermData.iterator();	
					while (iter2.hasNext()){
						a=(TermData)iter2.next();
						if ((a.termino.equals(b)) && (!tqTerms.contains(a.getTermino()))){
							tqTerms.add(a);
							break;
						}
					}
				Collections.sort(tqTerms,new Comparator<TermData>(){
					@Override
					public int compare(TermData o1, TermData o2) {
						return String.valueOf(o1.idf).compareTo(String.valueOf(o2.idf));
					}
					
				});
				}
			relevants=qRelevances.RelevancesOf(q.getDocumentID());
			relevants=relevants.subList(0, Integer.parseInt(rf1.get(2)));
			Iterator<Integer> iterRelevants=relevants.iterator();

			while(iterRelevants.hasNext()){
				r=iterRelevants.next();
				try {
					docsBody.add((searcher.doc(r)).get("BODY"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
			List<String> l=new ArrayList<>();
			for(String d:docsBody){
				List<TermData> docTerms=new ArrayList<>();
				l=Arrays.asList(d.split(" "));
				for (String st:l){
					Iterator<TermData> termIter=totalTermData.iterator();
					TermData term=null;
					while(termIter.hasNext()){
						term=termIter.next();
						if ((st.equals(term.termino))&& (!docTerms.contains(term))){
							docTerms.add(term);
							break;
						}
						
					}
					Collections.sort(docTerms,new Comparator<TermData>(){
						@Override
						public int compare(TermData o1, TermData o2) {
							return String.valueOf(o1.tfidf).compareTo(String.valueOf(o2.tfidf));
						}
						
					});
					
				}
				finalTerms.add(docTerms.subList(0, Integer.parseInt(rf1.get(1))));

			}
			TermData td=null;
			for (List<TermData> dt:finalTerms){
				Iterator<TermData> dIter=dt.iterator();
				while(dIter.hasNext()){
					td=dIter.next();
					s=s+" "+td.termino;
					if(explain){
						System.out.println("Término de doc:" + td.getTermino()+" idf: " +
					td.getIdf()+" tf: "+td.getTf());
						
					}
				}
			}
			
		tqTerms=tqTerms.subList(0, Integer.valueOf(rf1.get(0)));
		for(TermData tq:tqTerms){
			s=s+" "+tq.termino;
			if(explain){
				System.out.println("Término de la query: " +tq.getTermino()+ " idf: " + tq.idf);
			}
		}
		return s;
			
	}
	
	
	private static CranQuery executeRf1(String s,CranQuery q){
		
		String sq=q.getQuery();
		
		sq=sq+s;
		
		CranQuery finalQuery=new CranQuery(3000);
		finalQuery.addQuery(sq);
		
		return finalQuery;
		
	}
	
	private static CranQuery executeRf2(CranQuery q,CranRelevances qRelevances,int ndr,IndexSearcher searcher){
		
		List<Integer> relevants=new ArrayList<>();
		
		relevants=qRelevances.RelevancesOf(q.getDocumentID());
		relevants=relevants.subList(0,ndr);
		Iterator<Integer> iterRelevants=relevants.iterator();
		int r;
		String titles="";
		while(iterRelevants.hasNext()){
			r=iterRelevants.next();
			try {
				titles=titles+" "+(searcher.doc(r)).get("TITLE");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		String sq=q.getQuery();
		
		sq=sq+titles;
		
		CranQuery finalQuery=new CranQuery(3001);
		finalQuery.addQuery(sq);
		
		return finalQuery;
		
		
		
	}
	
	private static CranQuery doPrf(CranQuery actualQ,List<String> prf,List<CranQuery> queriesProc,
			List<TermData> totalTermData, CranRelevances rels,List<CranDocument> docs,String mode,
			IndexReader reader,float param,boolean explain) throws IOException{
		int nd=Integer.valueOf(prf.get(0));
		int nw=Integer.valueOf(prf.get(1));
		float pD=1 / (float)nd;
		float pwD=0;
		float pwR=0;
		float pqD=0;
		float D= 0;
		List<TermData> pwrList=new ArrayList<>();
		CranQuery finalQuery=null;
		List<CranDocument> pwrSet=docs.subList(0,nd);
		List<Integer> pwrSetId=new ArrayList<>();
		String aq=actualQ.getQuery();
		String[] aqArray=aq.split(" ");
		List<String>aqList=Arrays.asList(aqArray);
		String s="";
		for (CranDocument d:pwrSet){
			pwrSetId.add(d.getDocumentID());
		}
		
			D=0;
			pwD=0;
			pwR=0;
			pqD=0;
			
			for(ScoreDoc Sdoc: actualQ.getTopDocs().scoreDocs){
				pqD+=Math.log(Sdoc.score);
				
			}
			
			for (TermData td:totalTermData){
				if(pwrSetId.contains(td.getDocId())){
					D+=td.getTf();					
					Term term=new Term("BODY",td.getTermino());
					if(mode.equals("dir")){
							pwD=(float) (td.getTf() + param * (reader.totalTermFreq(term)/reader.getSumTotalTermFreq("BODY")) / D + param);
					}else{
						pwD=(float) ((1-param)*(td.getTf()/D) + param*reader.totalTermFreq(term)/reader.getSumTotalTermFreq("BODY")
								/ D + param);

					}
					pwR=pD*pwD*pqD;
					td.setpwR(pwR);
					if(explain){
						td.setpD(pD);
						td.setpwD(pwD);
						td.setpqD(pqD);
					}
					pwrList.add(td);


				}
				
				Collections.sort(pwrList,new Comparator<TermData>(){
					@Override
					public int compare(TermData o1, TermData o2) {
						return String.valueOf(o1.pwR).compareTo(String.valueOf(o2.pwR));
					}
					
				});
				
				
				
				
				
			}

			for (int i=0;i<pwrList.size();i++){
				s=pwrList.get(i).getTermino();
				if(!aqList.contains(s)){
					aq=aq+" " + s;
					if(explain){
						System.out.println("Término:"+s+ " P(D): "+pwrList.get(i).getpD()+"P(w|D): "+ 
					pwrList.get(i).getpwD()+ " P(qi|D): "+pwrList.get(i).getpqD());
					}
				}
				if (i>=(nw-1)){
					break;
				}
			}
							
			
			finalQuery=new CranQuery(3001);
			finalQuery.addQuery(aq);

			
		return finalQuery;

		
	}
	

}

class TermData{
	
	
	String termino;
	int docId;
	int df;
	double idf;
	double tf;
	double tfidf;
	float pwR;
	float pD;
	float pwD;
	float pqD;
	
	public TermData(String term, int docFreq,int numDocs, double termf,int docId){
		this.termino=term;
		if (tf ==0){
			this.tf=0;
		}else if(tf >=1){
			this.tf= 1+Math.log(tf);
		}
		this.docId=docId;
		this.tf=termf;
		this.df=docFreq;
		this.idf=Math.log(numDocs/docFreq);
		this.tfidf= this.idf * this.tf;
	}
	
	
	public double getIdf(){
		return this.idf;
	}
	
	public String getTermino(){
		return this.termino;
	}
	
	public int getDf(){
		return this.df;
	}
	
	public int getDocId(){
		return this.docId;
	}
	
	public double getTf(){
		return this.tf;
	}
	
	public double getTfdf(){
		return this.tfidf;
	}
	public float getpwR(){
		return this.pwR;
	}
	public float getpD(){
		return this.pD;
	}
	public float getpwD(){
		return this.pwD;
	}
	public float getpqD(){
		return this.pqD;
	}
	public void setpwR(float pwR){
		this.pwR=pwR;
	}
	public void setpD(float pD){
		this.pD=pD;
	}
	public void setpwD(float pwD){
		this.pwD=pwD;
	}
	public void setpqD(float pqD){
		this.pqD=pqD;
	}

}


class CranRelevances {
	
	HashMap<Integer, List<Integer>> relevances = new HashMap<Integer, List<Integer>>();

	private int finishedDocId= -1;
	private int activeDocId = -1;
	private int activeRelevance = -1;
	private boolean newDocument = false;

	public void parse(String FileName) throws FileNotFoundException,
			IOException, ParseException {
		File file = new File(FileName);
		if (file.exists() && file.isFile()) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				List<Integer> rel = new ArrayList<>();
				for (String line; (line = br.readLine()) != null;) {
					processLine(line);
					if (!newDocument){
						rel.add(activeRelevance);
					}else{
						if (!rel.isEmpty()){
							relevances.put(finishedDocId, rel);
							rel= new ArrayList<>();
							}
						
					}
				}
			}
		} else {
			throw new FileNotFoundException(FileName);
		}
	}
	
	private void processLine(String line) throws ParseException {
		
		
		
		if (line.isEmpty())
		    return;
		
		
		int docId= 0;
		int docRel= 0;
		int i= 1;
		int j=0;
		

		try {
			while(line.charAt(i) != ' '){
				i++;
			}
			docId=Integer.parseInt(line.substring(0, i));
			i++;
			j=i;
			while(line.charAt(i) != ' '){
				i++;
			}
			docRel=Integer.parseInt(line.substring(j,i));

			newDocument = true;	
			if (activeDocId==docId){
				newDocument= false;
			}else{
				
				finishedDocId=activeDocId;
				activeDocId=docId;
				
			}
			activeRelevance = docRel;
			
		} catch (Exception e) {
			throw e;
		}
		

	}

	public List<Integer> RelevancesOf(Integer docID) {
		List<Integer> rel = relevances.get(docID);
		if (rel == null)
			rel = new ArrayList<Integer>();
		return rel;
	}
}

class CranQuery {

	private int documentID = -1; // .I
	private List<String> query = null; // .W
	private TopDocs tDocs=null;


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

	public CranQuery(int id) {
		documentID = id;
	}

	public void addQuery(String line) {
		if (query == null)
			query = new ArrayList<String>();
		query.add(line.trim());
	}
	public void addDocs(TopDocs docs){
		this.tDocs=docs;
	}
	

	public int getDocumentID() {
		return documentID;
	}

	public String getQuery() {
		return BuildStringFromList(query).trim();
	}
	
	public TopDocs getTopDocs(){
		return tDocs;
	}
}

class CranQueryParser {

	private CranQuery activeQuery = null;
	private List<CranQuery> queries = null;
	private String activeField = "";
	private boolean newQuery = false;
	private int queryId=1;
	
	

	public void parse(String FileName) throws FileNotFoundException,
			IOException, ParseException {
		File file = new File(FileName);
		if (file.exists() && file.isFile()) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				queries = new ArrayList<CranQuery>();
				for (String line; (line = br.readLine()) != null;) {
					processLine(line,queryId);
					if (newQuery) {
						queries.add(activeQuery);
						newQuery = false;
						queryId++;
					}
				}
			}
		} else {
			throw new FileNotFoundException(FileName);
		}
	}

private void processLine(String line,int queryId) throws ParseException {
	if (line.isEmpty())
		return;
	if (line.startsWith(".I")) {
		int id = queryId;
		activeQuery = new CranQuery(id);
		activeField = "";
		newQuery = true;
	} else if (line.startsWith(".W")){
			activeField=".W";
	}else{
		if(activeField!=null){
			activeQuery.addQuery(line);
		}
	}
}


public List<CranQuery> getQueries() {
	return this.queries;
}
}

