package nl.inl.blacklab.core.search.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import nl.inl.blacklab.core.search.Hits;
import nl.inl.blacklab.core.search.Searcher;
import nl.inl.blacklab.core.search.grouping.HitGroup;
import nl.inl.blacklab.core.search.grouping.HitGroups;
import nl.inl.blacklab.core.search.grouping.HitPropValue;
import nl.inl.blacklab.core.search.grouping.HitProperty;


public class VocabularyCount {

    
    public static void main(String[] args) throws Exception {
        
        String indexPath = "D:\\someProject\\corpus1\\data\\corpus\\brown";
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));    
        String field = "contents%lemma@s";

        Map<String, Integer> map = getAllWords(dir,field);
//        System.out.println( map.size() );
        getWordWithPosFreq(new File(indexPath),map);
   
      }  

    public static Map<String, Integer> getAllWords(Directory dir, String field) throws Exception {
        IndexReader reader = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(reader);    
        Terms terms = fields.terms(field);
        TermsEnum iterator = terms.iterator();
        BytesRef thisTerm = null;
        Map<String, Integer> map = new HashMap<String, Integer>();    
        
        while((thisTerm=iterator.next()) != null) {
//            System.out.println( thisTerm.utf8ToString() + "\t" + iterator.totalTermFreq() );
            map.put(thisTerm.utf8ToString(), (int) iterator.totalTermFreq());
        }    
//        System.out.println( map.size() );
//        for(Entry<String, Integer> entry : map.entrySet()){  
//            System.out.println("Key = "+entry.getKey()+",value="+entry.getValue());  
//        }         
        return map;       
    }
    
    @SuppressWarnings(value = { "" })
    private static void getWordWithPosFreq(File indexDir, Map<String, Integer> map ) throws IOException, ParseException {   
        Searcher searcher = Searcher.open( indexDir );  
        
        String field = "contents%word@s";
        String queryStr = "Singing";
        Term term = new Term(field,queryStr);     
        SpanQuery query = new SpanTermQuery(term);  
        
        Hits hits = Hits.fromSpanQuery( searcher, BLSpanQuery.wrap( query ) );
        
        HitGroups hitGroups = null;
        HitGroup hitGroup = null;
        
        String group = "hit:lemma:s";
        String viewGroup = "cws:lemma:s:singing";
        
        HitPropValue viewGroupVal = null;
        HitProperty groupProp = null;
        
        groupProp = HitProperty.deserialize( hits, group );
        viewGroupVal = HitPropValue.deserialize(hits, viewGroup);
        
        hitGroups = hits.groupedBy(groupProp);
        
        int size = 0;
        if(hitGroups.getTotalResults()!=0) {
            hitGroup = hitGroups.getGroup(viewGroupVal);
            
            size = hitGroup.size();          
        }
             
        System.out.println( size );  
        
    }
}
