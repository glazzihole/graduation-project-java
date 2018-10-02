package nl.inl.blacklab.core.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.json.JSONObject;

import nl.inl.blacklab.core.search.Hits;
import nl.inl.blacklab.core.search.Searcher;
import nl.inl.blacklab.core.search.grouping.HitGroup;
import nl.inl.blacklab.core.search.grouping.HitGroups;
import nl.inl.blacklab.core.search.grouping.HitProperty;
import nl.inl.blacklab.core.search.lucene.BLSpanQuery;


public class VocabularyCount {

    private static String corpusName = "brown";
    private static String indexPath = "D:\\someProject\\corpus1\\data\\corpus\\" + corpusName;
    private static Searcher searcher;
    
    public static void main(String[] args) throws Exception {
        long startTime=System.currentTimeMillis();
        
        String corpus = corpusName;
        
        searcher = Searcher.open( new File (indexPath) ); 
       
        List<String> allWordsList_s = getAllWordsInCorpus("word",true);  
        List<String> allWordsList_i = getAllWordsInCorpus("word",false);  
        
//        saveAllWordsPosAndSize( allWordsList_s,"D:\\someProject\\corpus1\\data\\corpus\\1.txt",true );
//        Map<String,JSONObject> allWordsPosAndSize = getAllWordsPosAndSize(allWordsList_s,true);    
//        JSONObject testJsonObject = allWordsPosAndSize.get( "singing" );
        
//        
//        saveAllWordsLemmaPosAndSize(allWordsList_s,"D:\\someProject\\corpus1\\data\\corpus\\3.txt",true);
        Map<String,JSONObject> allWordsLemmaPosAndSize = getAllWordsLemmaPosAndSize(allWordsList_s,true);
        JSONObject testJsonObject = allWordsLemmaPosAndSize.get( "my" );
        
        Map<String,JSONObject> wordsLemmaPosAndSize = getAllWordsLemmaPosAndSize(allWordsList_i,false);
        testJsonObject = wordsLemmaPosAndSize.get( "my" );
        
        //组成表2
        Map<String,JSONObject> form2 = getForm2(allWordsLemmaPosAndSize,wordsLemmaPosAndSize,corpus);
        testJsonObject = form2.get( "my_my" );
        
        System.out.println( form2 );
        System.out.println( testJsonObject );
        
        //组成表1
//        List<String> allLemmaList_s = getAllWordsInCorpus("lemma",true);
//        Map<String,JSONObject> form1 = getForm1(allLemmaList_s,allWordsLemmaPosAndSize,corpus);
//        testJsonObject = form1.get( "mêlée" );
//        System.out.println( form1 );
//        System.out.println( testJsonObject );        
        
        long endTime=System.currentTimeMillis();
        float excTime=(float)(endTime-startTime)/1000;
        System.out.println("操作完成！执行时间："+excTime+"s");
        
    }  

    //获取语料库中的所有信息  type:"word"-查所有单词；"lemma"-查所有原型
    private static List<String> getAllWordsInCorpus(String type,boolean sensitive) throws IOException{
        
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open( dir );
        Fields fields = MultiFields.getFields( reader );
        String field = null;
        switch(type) {
            case "word":
                field =  sensitive?"contents%word@s":"contents%word@i";
                break;
            case "lemma":
                field =  sensitive?"contents%lemma@s":"contents%lemma@i";
                break;
        }      
        
        Terms terms = fields.terms( field );
        TermsEnum iterator = terms.iterator();
        BytesRef thisTerm = null;
        
        List<String> allWordsList = new ArrayList<String>();  
        
        while((thisTerm=iterator.next()) != null ) {          
            if( thisTerm.utf8ToString()!=null && !("").equals(thisTerm.utf8ToString()) ) {
                allWordsList.add(thisTerm.utf8ToString());
            }
        }         
        return allWordsList;
    }
    
    private static HitGroups getHitGroups(Searcher searcher,String field,String queryStr,String group) throws CorruptIndexException, IOException {
        
        Term term = new Term(field,queryStr);     
        SpanQuery query = new SpanTermQuery(term);
        Hits hits = Hits.fromSpanQuery( searcher, BLSpanQuery.wrap( query ) );
        HitProperty groupProp = HitProperty.deserialize( hits, group );
        
        return hits.groupedBy(groupProp);
    }
    
//    private static HitGroup getHitGroup(Hits hits,HitGroups hitGroups,String viewGroup) {
//        viewGroupVal = null;       
//        viewGroupVal = HitPropValue.deserialize(hits, viewGroup);
//        
//        return hitGroups.getGroup(viewGroupVal);
//    }
    
    //获取所有单词的词性及对应词性的词频，并存入本地文件。文件内容为json格式字符串    
    private static void saveAllWordsPosAndSize(List<String> allWordsList,String filePath,boolean sensitive) throws CorruptIndexException, IOException {
        String field = sensitive?"contents%word@s":"contents%word@i";
        String group = sensitive?"hit:pos:s":"hit:pos:i";
        String word_cm = null;
        
        HitGroups hitGroups;
        
        File f = new File(filePath);  
        if (!f.exists()) {          
            f.createNewFile(); 
        } 
        FileWriter fw = new FileWriter(filePath,true);
        fw.write( "{" );
        int listSize = allWordsList.size();
        int groupSize;
        List<HitGroup> hitGroupList;
        for(int i=0; i<listSize; i++){ 
            word_cm = allWordsList.get( i );
            hitGroups = getHitGroups(searcher,field,word_cm,group);                    
            hitGroupList = hitGroups.getGroups();            
            groupSize = hitGroupList.size();      
            fw.write("\"" + word_cm + "\":{");
            
            for(int j=0; j<groupSize; j++) {
                fw.write("\"" + hitGroupList.get( j ).getIdentity().toString() + "\":" +
                        hitGroupList.get( j ).size() +
                        ","
                        );
            }  
            
            fw.write("},");
            hitGroups = null;
            hitGroupList = null;
        }
        fw.write( "}" );
        fw.close();
    }
    
    //获取所有单词的词性及对应词性的词频
    @SuppressWarnings( "null" )
    private static Map<String,JSONObject> getAllWordsPosAndSize(List<String> allWordsList, boolean sensitive) throws CorruptIndexException, IOException {
        String field = sensitive?"contents%word@s":"contents%word@i";
        String group = sensitive?"hit:pos:s":"hit:pos:i";
        String word_cm = null;
        
        HitGroups hitGroups;
        Map<String,JSONObject> AllWordsPosAndSize = new HashMap<String, JSONObject>(); 
        
        JSONObject jsonObjectTemp = new JSONObject(); 
        int listSize = allWordsList.size();
        int groupSize;
        List<HitGroup> hitGroupList;
        for(int i=0; i<listSize; i++){ 
            word_cm = allWordsList.get( i );
            hitGroups = getHitGroups(searcher,field,word_cm,group);                    
            hitGroupList = hitGroups.getGroups();            
            groupSize = hitGroupList.size();                
        
            for(int j=0; j<groupSize; j++) {
                jsonObjectTemp.put(hitGroupList.get( j ).getIdentity().toString(), hitGroupList.get( j ).size());               
            }
            AllWordsPosAndSize.put(word_cm, jsonObjectTemp);
            
            hitGroups = null;
            hitGroupList = null;
            jsonObjectTemp = new JSONObject();
            
        }        
        return AllWordsPosAndSize;
    }
    
    //获取所有单词的词性和对应的原型，以及对应词性+原型的词频，并存储在本地文件中，文件内容为json格式的字符串
    private static void saveAllWordsLemmaPosAndSize(List<String> allWordsList,String filePath,boolean sensitive) throws CorruptIndexException, IOException {
        String field = sensitive?"contents%word@s":"contents%word@i";
        String group = sensitive?"hit:lemma:s,hit:pos:s":"hit:lemma:i,hit:pos:i";
        String word_m = null;
        
        HitGroups hitGroups;
        
        File f = new File(filePath);  
        if (!f.exists()) {          
            f.createNewFile(); 
        } 
        FileWriter fw = new FileWriter(filePath,true);
        fw.write( "{" );
        
        int listSize = allWordsList.size();
        int groupSize;
        List<HitGroup> hitGroupList;
        Map<String,JSONObject> lemmaPosAndFreq = new HashMap<String, JSONObject>();
        JSONObject posAndFreq = new JSONObject();
        String[] lemmaAndPos = null;
        String lemma = null;
        String pos = null;
        int freq = 0;
        Iterator it;
        for(int i=0; i<listSize; i++){ //循环单词列表
//        for(int i=0; i<1; i++){ //测试用
            word_m = allWordsList.get( i );
            hitGroups = getHitGroups(searcher,field,word_m,group);                    
            hitGroupList = hitGroups.getGroups();            
            groupSize = hitGroupList.size();      
            fw.write("\"" + word_m + "\":{");
            
            for(int j=0; j<groupSize; j++) {//循环原型-词性 groups
                lemmaAndPos = (hitGroupList.get( j ).getIdentity().toString()).split( " / " );
                lemma = lemmaAndPos[0].trim();
                pos = lemmaAndPos[1].trim();
                freq = hitGroupList.get( j ).size();
                if(lemmaPosAndFreq.containsKey( lemma )) {
                    posAndFreq = lemmaPosAndFreq.get( lemma );
                    posAndFreq.put( pos, freq );
                }
                else {
                    posAndFreq.put( pos, freq );
                    lemmaPosAndFreq.put( lemma, posAndFreq );
                }

                posAndFreq = new JSONObject();               
            }
            
            //写入文件
            for(Entry<String,JSONObject> entry : lemmaPosAndFreq.entrySet()) {
                lemma = entry.getKey();
                fw.write("\"" + lemma + "\":{");
                posAndFreq = entry.getValue();
                it = posAndFreq.keys();
                while(it.hasNext()) {
                    pos = (String)it.next();
                    fw.write("\"" + pos + "\":" + posAndFreq.getInt( pos ) + ",");
                }
                fw.write("},");
                posAndFreq = new JSONObject();
            }
            
            fw.write("},");
            hitGroups = null;
            hitGroupList = null;
            lemmaPosAndFreq = new HashMap<String, JSONObject>() ;
            it = null;
        }//循环单词列表
        fw.write( "}" );
        fw.close();
    }
  
    
    //获取所有单词的词性和对应的原型,以及对应词性+原型的词频
    private static Map<String,JSONObject> getAllWordsLemmaPosAndSize(List<String> allWordsList,boolean sensitive) throws CorruptIndexException, IOException {
        String field = sensitive?"contents%word@s":"contents%word@i";
        String group = sensitive?"hit:lemma:s,hit:pos:s":"hit:lemma:i,hit:pos:i";
        String word_m = null;
        
        HitGroups hitGroups;
        
        Map<String,JSONObject> wordsLemmaPosAndSize = new HashMap<String,JSONObject>();
        int listSize = allWordsList.size();
        int groupSize;
        List<HitGroup> hitGroupList;
        Map<String,JSONObject> lemmaPosAndFreq_map = new HashMap<String, JSONObject>();
        JSONObject posAndFreq = new JSONObject();
        JSONObject lemmaPosAndFreq_jb = new JSONObject();
        String[] lemmaAndPos = null;
        String lemma = null;
        String pos = null;
        int freq = 0;
     
        for(int i=0; i<listSize; i++){ //循环单词列表
//        for(int i=0; i<1; i++){ //测试用
            word_m = allWordsList.get( i );
            hitGroups = getHitGroups(searcher,field,word_m,group);                    
            hitGroupList = hitGroups.getGroups();            
            groupSize = hitGroupList.size();      
           
            for(int j=0; j<groupSize; j++) {//循环原型-词性 groups
                lemmaAndPos = (hitGroupList.get( j ).getIdentity().toString()).split( " / " );
                lemma = lemmaAndPos[0].trim();
                pos = lemmaAndPos[1].trim();
                freq = hitGroupList.get( j ).size();
                if(lemmaPosAndFreq_map.containsKey( lemma )) {
                    posAndFreq = lemmaPosAndFreq_map.get( lemma );
                    posAndFreq.put( pos, freq );
                }
                else {
                    posAndFreq.put( pos, freq );
                    lemmaPosAndFreq_map.put( lemma, posAndFreq );
                }

                posAndFreq = new JSONObject();               
            }
            
            for(Entry<String,JSONObject> entry : lemmaPosAndFreq_map.entrySet()) {
                lemma = entry.getKey();
                posAndFreq = entry.getValue();
                lemmaPosAndFreq_jb.put( lemma, posAndFreq );
                posAndFreq = new JSONObject();
            }
            wordsLemmaPosAndSize.put( word_m, lemmaPosAndFreq_jb );
            
            hitGroups = null;
            hitGroupList = null;
            lemmaPosAndFreq_map = new HashMap<String, JSONObject>() ;
            lemmaPosAndFreq_jb = new JSONObject();
        }//循环单词列表
        return wordsLemmaPosAndSize;

    }
    
    //得到表2
    private static Map<String,JSONObject> getForm2(Map<String,JSONObject> allWordsLemmaPosAndSize,Map<String,JSONObject> wordsLemmaPosAndSize,String corpusName) {
        Map<String,JSONObject> mapOfForm2 = new HashMap<String, JSONObject>();
        
        //mapOfForm2所需变量
        JSONObject jsonObjectOfWordLemma = new JSONObject();
        JSONObject jsonObjectOfCorpus = new JSONObject();
        JSONObject jsonObjectOfPos = new JSONObject();        
        
        //遍历wordsLemmaPosAndSize,生成jsonObjectOfForm2的架构所需变量
        String lemma,word_lemma,pos_name;
        int tf = 0,tf_wp=0;
        JSONObject lemmaPosSize = new JSONObject(); 
        JSONObject posSize = new JSONObject(); 
        Iterator itForlemmaPosSize;
        Iterator itForPosSize;
        //遍历wordsLemmaPosAndSize,生成jsonObjectOfForm2的架构
        for(Entry<String, JSONObject> entry1:wordsLemmaPosAndSize.entrySet()) {
            word_lemma = entry1.getKey();
            lemmaPosSize = entry1.getValue();//获取单词的所有原型，词性和相应原型+词性的词频
            itForlemmaPosSize = lemmaPosSize.keys();
            while(itForlemmaPosSize.hasNext()) {//遍历所有原型
                lemma = (String)itForlemmaPosSize.next();
                word_lemma = word_lemma + "_" + lemma;//(String)it.next()为单词原型
                posSize = lemmaPosSize.getJSONObject( lemma );//获取指定原型的词性和对应词频
                itForPosSize = posSize.keys();
                jsonObjectOfCorpus.put( "tf", tf );
                while(itForPosSize.hasNext()) {//遍历指定原型下的所有词性
                    pos_name = (String) itForPosSize.next();
                    tf_wp = posSize.getInt( pos_name );   
                    tf = tf + tf_wp;
                    jsonObjectOfPos.put( "tf_wp", tf_wp );
                    jsonObjectOfCorpus.put( pos_name, jsonObjectOfPos );
                   
                    tf_wp = 0;
                    pos_name = null;
                    jsonObjectOfPos = new JSONObject();
                } //遍历指定原型下的所有词性 
              
                jsonObjectOfCorpus.put( "tf", tf );
                jsonObjectOfWordLemma.put( corpusName, jsonObjectOfCorpus );
                mapOfForm2.put( word_lemma, jsonObjectOfWordLemma );
                
                tf=0;
                itForPosSize = null;
                word_lemma = entry1.getKey();
                jsonObjectOfCorpus = new JSONObject();
                jsonObjectOfWordLemma = new JSONObject();
                
            }//遍历所有原型
            
            lemmaPosSize = new JSONObject();
            posSize = new JSONObject();
            itForlemmaPosSize = null;

        }// for循环 entry1
        
        //遍历allWordsLemmaPosAndSize，填充form2的架构
        String word_pcm;
        int tf_wpcm;
        String mapKey;
        for(Entry<String, JSONObject> entry2:allWordsLemmaPosAndSize.entrySet()) {
            word_pcm = entry2.getKey();
            lemmaPosSize = entry2.getValue();
            itForlemmaPosSize = lemmaPosSize.keys();
            while(itForlemmaPosSize.hasNext()) {//遍历所有原型
                lemma = (String) itForlemmaPosSize.next();
                mapKey = word_pcm.toLowerCase() + "_" + lemma;
                jsonObjectOfWordLemma = mapOfForm2.get( mapKey );
                jsonObjectOfCorpus = jsonObjectOfWordLemma.getJSONObject( corpusName );
                posSize = lemmaPosSize.getJSONObject( lemma );//获取某原型下的所有词性和词频
                
                itForPosSize = posSize.keys();
                while(itForPosSize.hasNext()) {//遍历所有词性
                    pos_name = (String) itForPosSize.next();
                    tf_wpcm = posSize.getInt( pos_name );
                    
                    //将该大小写敏感，形态敏感，带词性的单词及词频放入form2框架中
                    jsonObjectOfPos = jsonObjectOfCorpus.getJSONObject( pos_name );
                    jsonObjectOfPos.put( word_pcm, tf_wpcm );
                    
                    pos_name = null;
                    tf_wpcm = 0;
                    jsonObjectOfPos = new JSONObject();
                }//遍历所有词性
                
                lemma = null;
                mapKey = null;
                jsonObjectOfWordLemma = new JSONObject();
                jsonObjectOfCorpus = new JSONObject();
                posSize = new JSONObject();
                itForPosSize = null;
                
            }//遍历所有原型
            
            word_pcm = null;
            lemmaPosSize = new JSONObject();
            itForlemmaPosSize = null;

        }//for循环 entry2
        
        return mapOfForm2;
    }
    
    private static Map<String, JSONObject> getForm1( List<String> allLemmaList_s, Map<String, JSONObject> allWordsLemmaPosAndSize, String corpusName ) {
        Map<String, JSONObject> mapOfForm1 = new HashMap<String, JSONObject>();
        JSONObject jsonObjectOfLemma = new JSONObject();
        JSONObject jsonObjectOfCorpus = new JSONObject();
        JSONObject jsonObjectOfPos = new JSONObject();
        JSONObject jsonObjectOfLemma_pc = new JSONObject();
        //先生成表1框架
        int lemmaListSize = allLemmaList_s.size();
        String lemma = null;
        for(int i=0; i<lemmaListSize; i++) {
            lemma = allLemmaList_s.get( i ).toLowerCase();
            jsonObjectOfCorpus.put("tf",0);
            jsonObjectOfLemma.put( corpusName, jsonObjectOfCorpus );
            mapOfForm1.put( lemma, jsonObjectOfLemma );
        }
        
        jsonObjectOfLemma = new JSONObject();
        jsonObjectOfCorpus = new JSONObject();
       
        //遍历allWordsLemmaPosAndSize,填充表1框架
        String lemma_pc=null,lemma_pcm = null;
        JSONObject lemmaPosSize = new JSONObject(),posSize = new JSONObject();
        Iterator itForLemmaPosSize,itForPosSize;
        String posName = null;
        int tf=0,tf_sp=0,tf_spc=0,tf_spcm = 0;
        for(Entry<String,JSONObject> entry : allWordsLemmaPosAndSize.entrySet()) {
            lemma_pcm = entry.getKey();
            lemmaPosSize = entry.getValue();
            itForLemmaPosSize = lemmaPosSize.keys();
            while( itForLemmaPosSize.hasNext() ) {//遍历所有原型
                lemma_pc = (String) itForLemmaPosSize.next();
                posSize = lemmaPosSize.getJSONObject( lemma_pc );
                itForPosSize = posSize.keys();
                
                jsonObjectOfLemma = mapOfForm1.get( lemma_pc.toLowerCase() );
                System.out.println(lemma_pc+"//"+lemma_pc.toLowerCase());
                while( itForPosSize.hasNext() ) {//遍历所有词性,获取对应词频
                    posName = (String) itForPosSize.next();
                    tf_spcm = posSize.getInt( posName );
                    
                    //插入到form1的框架中
                    jsonObjectOfCorpus = jsonObjectOfLemma.getJSONObject( corpusName );
                    tf = jsonObjectOfCorpus.getInt( "tf" );
                    if(jsonObjectOfCorpus.isNull( posName )) {//没有这个词性
                        tf_sp = 0;
                    }
                    else {//有这个词性
                        jsonObjectOfPos = jsonObjectOfCorpus.getJSONObject( posName );
                        tf_sp = jsonObjectOfPos.getInt( "tf_sp" );
                        if(jsonObjectOfPos.isNull( lemma_pc )) {//没有这个lemma_pc
                            
                        }
                        else {
                            jsonObjectOfLemma_pc = jsonObjectOfPos.getJSONObject( lemma_pc );
                            tf_spc = jsonObjectOfLemma_pc.getInt( "tf_spc" );
                        }
                    }
                    tf_spc = 0;
                    tf_spc = tf_spc + tf_spcm;
                    jsonObjectOfLemma_pc.put( "tf_spc", tf_spc );
                    jsonObjectOfLemma_pc.put( lemma_pcm, tf_spcm );
                    jsonObjectOfPos.put( lemma_pc, jsonObjectOfLemma_pc );
                    tf_sp = tf_sp + tf_spc;
                    jsonObjectOfPos.put("tf_sp",tf_sp);
                    tf = tf + tf_sp;
                    jsonObjectOfPos.put( "tf", tf );
                    jsonObjectOfCorpus.put( posName, jsonObjectOfPos);
                    
                    tf = 0;
                    tf_spc = 0;
                    jsonObjectOfLemma_pc = new JSONObject();
                    jsonObjectOfPos = new JSONObject();
                    tf_sp = 0;
                    jsonObjectOfCorpus = new JSONObject();
                    posName = null;
                    tf_spcm = 0;                  
                    
                }//遍历所有词性,获取对应词频
                
                mapOfForm1.put( lemma, jsonObjectOfLemma );
                jsonObjectOfLemma = new JSONObject();
                lemma = null;
                posSize = new JSONObject();
                itForPosSize = null;              
               
            }//遍历所有原型
            
            lemma_pcm = null;
            lemmaPosSize = new JSONObject();
            itForLemmaPosSize = null;
            
        }//for循环
        return mapOfForm1;
    }
   
}
