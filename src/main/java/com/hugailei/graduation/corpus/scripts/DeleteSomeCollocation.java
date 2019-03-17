package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HU Gailei
 * @date 2019/3/9
 * <p>
 * description: bug数据处理
 * </p>
 **/
public class DeleteSomeCollocation {
    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }
        String[] lines = {"Studies of plants have now identified a new class of regulatory molecules called oligosaccharins.",};

        // 读取数据库，存储各级词汇
        Set<String> rank1WordSet = new HashSet<>();
        Set<String> rank2WordSet = new HashSet<>();
        Set<String> rank3WordSet = new HashSet<>();
        Set<String> rank4WordSet = new HashSet<>();
        Set<String> rank5WordSet = new HashSet<>();
        Set<String> rank6WordSet = new HashSet<>();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_rank_word where rank_num = 1");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            rank1WordSet.add(rs.getString("word"));
        }
        ps = con.prepareStatement("SELECT * FROM tb_rank_word where rank_num = 2");
        rs = ps.executeQuery();
        while (rs.next()) {
            rank2WordSet.add(rs.getString("word"));
        }
        ps = con.prepareStatement("SELECT * FROM tb_rank_word where rank_num = 3");
        rs = ps.executeQuery();
        while (rs.next()) {
            rank3WordSet.add(rs.getString("word"));
        }
        ps = con.prepareStatement("SELECT * FROM tb_rank_word where rank_num = 4");
        rs = ps.executeQuery();
        while (rs.next()) {
            rank4WordSet.add(rs.getString("word"));
        }
        ps = con.prepareStatement("SELECT * FROM tb_rank_word where rank_num = 5");
        rs = ps.executeQuery();
        while (rs.next()) {
            rank5WordSet.add(rs.getString("word"));
        }
        ps = con.prepareStatement("SELECT * FROM tb_rank_word where rank_num = 6");
        rs = ps.executeQuery();
        while (rs.next()) {
            rank6WordSet.add(rs.getString("word"));
        }
       for (String line : lines) {
           List<CoreMap> result = StanfordParserUtil.parse(line);
           for (CoreMap sentence : result) {
               // 计算句子长度
               int clauseCount = 0;
               // 计算平均词长及各级单词数
               int totalWordLength = 0;
               int wordCount = 0;
               int level1WordCount = 0, level2WordCount = 0, level3WordCount = 0;
               int level4WordCount = 0, level5WordCount = 0, level6WordCount = 0;

               // 专有名词数量
               int properNounCount = 0;
               // 名词数量，动词数量，代词数量
               int nounCount = 0, verbCount = 0, pronounCount = 0, adjCount = 0, advCount = 0, prepCount = 0;
               Set<String> wordSet = new HashSet<>();
               for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                   String word = token.get(CoreAnnotations.TextAnnotation.class);
                   wordCount ++;
                   totalWordLength += word.length();

                   String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                   boolean found = false;
                   if (rank1WordSet.contains(lemma)) {
                       level1WordCount ++;
                       found = true;
                   }
                   if (!found) {
                      if (rank2WordSet.contains(lemma)) {
                           level2WordCount ++;
                           found = true;
                       }
                   }
                   if (!found) {
                      if (rank3WordSet.contains(lemma)) {
                          level3WordCount ++;
                          found = true;
                      }
                   }
                   if (!found) {
                      if (rank4WordSet.contains(lemma)) {
                          level4WordCount ++;
                          found = true;
                      }
                   }
                   if (!found) {
                       if (rank5WordSet.contains(lemma)) {
                           level5WordCount ++;
                           found = true;
                       }
                   }
                   if (!found) {
                       if (rank6WordSet.contains(lemma)) {
                           level6WordCount ++;
                       }
                   }

                   // 计算专有名词数量
                   String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                   if (!ner.equals("O")) {
                       properNounCount++;
                   }

                   // 计算各词性词汇数量
                   String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                   if (pos.matches("NN.*")) {
                       nounCount ++;
                   }
                   else if (pos.matches("VB.*")) {
                       verbCount ++;
                   }
                   else if (pos.matches("RB.*")) {
                       advCount ++;
                   }
                   else if (pos.matches("JJ.*")) {
                       adjCount ++;
                   }
                   else if (pos.equals("PRP")) {
                       pronounCount ++;
                   }
                   else if (pos.equals("IN") || pos.equals("RP")) {
                       prepCount ++;
                   }

                   wordSet.add(word.toLowerCase());

               } //  for (CoreLabel token )

               // 计算从句个数及最大从句长度
               int maxClauseLength = 0;
               Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
               TregexMatcher matcher = TregexPattern.compile("SBAR | S").matcher(tree);
               while (matcher.findNextMatchingNode()) {
                   clauseCount ++;
                   int clauseLength = matcher.getMatch().getLeaves().size();
                   if (clauseLength >= maxClauseLength) {
                       maxClauseLength = clauseLength;
                   }
               }

               // 计算句法树深度
               int treeDepth = tree.depth();

               // 计算各短语的数量
               int nounPhraseCount = 0, verbPhraseCount = 0, adjPhraseCount = 0, advPhraseCount = 0, prepPhraseCount = 0;
               matcher = TregexPattern.compile("NP").matcher(tree);
               while (matcher.findNextMatchingNode()) {
                   nounPhraseCount ++;
               }
               matcher = TregexPattern.compile("PP").matcher(tree);
               while (matcher.findNextMatchingNode()) {
                   prepPhraseCount ++;
               }
               matcher = TregexPattern.compile("VP").matcher(tree);
               while (matcher.findNextMatchingNode()) {
                   verbPhraseCount ++;
               }
               matcher = TregexPattern.compile("ADJP").matcher(tree);
               while (matcher.findNextMatchingNode()) {
                   adjPhraseCount ++;
               }
               matcher = TregexPattern.compile("ADVP").matcher(tree);
               while (matcher.findNextMatchingNode()) {
                   advPhraseCount ++;
               }

               // 计算类符/形符比
               int typeCount = wordSet.size();
               double ratio = Double.valueOf(String.valueOf(typeCount)) / Double.valueOf(String.valueOf(wordCount));

               // 计算平均词长
               int avgWordLength = totalWordLength / wordCount;

               // 写入到文件
               double out = level1WordCount*1 +
                       level2WordCount*3 +
                       level3WordCount*5 +
                       level4WordCount*6 +
                       level5WordCount*10 +
                       level6WordCount*15 +
                       wordCount +
                       ratio +
                       avgWordLength +
                       clauseCount*10 +
                       properNounCount +
                       nounCount +
                       verbCount +
                       adjCount +
                       advCount +
                       pronounCount +
                       prepCount +
                       nounPhraseCount +
                       verbPhraseCount +
                       adjPhraseCount +
                       advCount +
                       prepPhraseCount +
                       treeDepth +
                       maxClauseLength * 2;
               System.out.println(out);
           } // for (CoreMap sentence : result)
       }
    }
}
