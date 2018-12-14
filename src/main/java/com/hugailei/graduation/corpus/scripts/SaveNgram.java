/**
 * 
 */
package com.hugailei.graduation.corpus.scripts;

import com.google.common.collect.Lists;
import com.hugailei.graduation.corpus.domain.Ngram;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.TextPattern;
import nl.inl.blacklab.search.grouping.HitGroup;
import nl.inl.blacklab.search.grouping.HitGroups;
import nl.inl.blacklab.search.grouping.HitProperty;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.util.BlsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HU Gailei
 * @date 2018/10/27
 * <p>
 * description: ngram提取并存储
 * </p>
 **/
public class SaveNgram {
    private static String[] CORPUS_NAME_ARRAY = { "bnc", "chinadaily"};

    private static Searcher SEARCHER = null;

    private static String CORPUS_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\indexData\\";

    
    private static String REGEX = "[(\\-llb\\-)(\\-lrb\\-)(\\-rrb\\-)(\\-rlb\\-)"
            + "`/~!@#$%^&*\\(\\)+=|{}':;,\\[\\]\\.<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

    //数据库主机地址
    private static final String DB_HOST = "192.168.99.100";

    //数据库端口
    private static final String DB_PORT = "3307";

    //数据库名称
    private static final String DB_NAME="corpus";

    //数据库表名称
    private static final String COLL_NAME="tb_ngram";

    //数据库用户名
    private static final String USER_NAME="root";

    //数据库用户密码
    private static final String USER_PASSWORD="123456";

    public static void main( String[] args  ) throws Exception {
        
        for( String corpusName : CORPUS_NAME_ARRAY) {
            SEARCHER = Searcher.open(new File(CORPUS_PATH + corpusName));
            // 获取所有大小写不敏感的词表
            System.out.println( "正在获取语料库" + corpusName + "中所有的单词……" );
            List<String> lowercasedWordList = getAllWordsInCorpus(SEARCHER, "word", false);
            System.out.println( "正在获取语料库" + corpusName + "中所有的Ngram并存入数据库" );
            getNgramList(corpusName, lowercasedWordList, "word");
            System.out.println( "操作完成！" );
        }
    }
    
    /**
     * 获取语料库中的所有信息
     * 
     * @param type
     *            "word"-查所有单词；"lemma"-查所有原型
     * @param sensitive
     *            是否大小写敏感
     * @return
     * @throws IOException
     */
    private static List<String> getAllWordsInCorpus(Searcher searcher, String type, boolean sensitive) throws IOException {
        IndexReader reader = searcher.getIndexReader();
        Fields fields = MultiFields.getFields(reader);
        String field = sensitive ? "contents%" + type + "@s" : "contents%" + type + "@i";
        
        Terms terms = fields.terms(field);
        List<String> allWordsList = Lists.newArrayList();
        if (terms != null) {
            TermsEnum iterator = terms.iterator();
            BytesRef thisTerm;
            while ((thisTerm = iterator.next()) != null) {
                if (StringUtils.isNotBlank(thisTerm.utf8ToString())) {
                    // 测试用途，生产代码需删除
                    if (Pattern.matches("[A-Za-z]+", thisTerm.utf8ToString())) {
                        allWordsList.add(thisTerm.utf8ToString());
                    }
                }
            }
        }
        return allWordsList;
    }
    
    /**
     * 从索引库中获取Ngram列表
     * @param wordList
     *             索引库中的所有单词（不区分大小写）
     * @param groupBy
     *             分组类型，可选“word”，“lemma”
     * @return
     * @throws BlsException
     * @throws IOException 
     * @throws CorruptIndexException 
     */
    private static List<Ngram> getNgramList(String corpusName, List<String> wordList, String groupBy ) throws Exception{
        //连接本地数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url, USER_NAME, USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }
        List<Ngram> NgramList = Lists.newArrayList();
        String groupStr = "hit:" + groupBy + ":i";

        //遍历单词列表
        for(String word : wordList) {
            //提取2~5的Ngram
            String patt = "\""+word+"\""+"[]{1,4}";
            //根据patt和分组条件（groupStr）得到hitgroups
            TextPattern pattern;
            pattern = BlsUtils.parsePatt(SEARCHER, patt, "corpusql");
            Hits hits= SEARCHER.find( pattern );
            HitProperty groupProp = HitProperty.deserialize(hits, groupStr);
            HitGroups groups = hits.groupedBy(groupProp);
            //遍历hitgroups,得到该单词的所有ngram和频率
            for (HitGroup group: groups) {
                if( group.size() > 2) {
                    String ngramStr = group.getIdentity().toString();
                    Pattern p = Pattern.compile(REGEX);
                    Matcher matcher = p.matcher(ngramStr);
                    if( !matcher.find() ) {
                        int nValue = ngramStr.split( " +" ).length;
                        int freq = group.size();
                        System.out.println(ngramStr);
                        // 存入数据库
                        try {
                            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                                    + "(ngram_str, n_value, freq, corpus) "
                                    + " VALUES (?, ?, ?, ?)");
                            preparedStatement.setString(1, ngramStr.toLowerCase());
                            preparedStatement.setInt(2, nValue);
                            preparedStatement.setInt(3, freq);
                            preparedStatement.setString(4, corpusName);
                            preparedStatement.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("出错啦，不过我还能挺住");
                        }
                    }
                }
            }// for (HitGroup group: groups)
        }// for(String word : wordList)
        return NgramList;
    }

}
