package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HU Gailei
 * @date 2019/3/6
 * <p>
 * description: 对文件中的句子进行筛选，只选择符合条件的句子作为当前等级的例句
 * </p>
 **/
public class ClassifySentence {
    private static String INPUT_FILE_PATH = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\2.txt";
    private static String OUTPUT_FILE_PATH1 = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\1.txt";
    private static String OUTPUT_FILE_PATH2 = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\2.txt";
    private static String OUTPUT_FILE_PATH3 = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\3.txt";
    private static String OUTPUT_FILE_PATH4 = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\4.txt";
    private static String OUTPUT_FILE_PATH5 = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\5.txt";
    private static String OUTPUT_FILE_PATH6 = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\手动分级后的\\6.txt";
    private static final String DB_HOST = "192.168.99.100";
    private static final String DB_PORT = "3307";
    private static final String DB_NAME="corpus";
    private static final String USER_NAME="root";
    private static final String USER_PASSWORD="123456";

    public static void main(String[] args) throws Exception{
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }
        FileReader fileReader = new FileReader(new File(INPUT_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        FileWriter fileWriter1 = new FileWriter(OUTPUT_FILE_PATH1, true);
        FileWriter fileWriter2 = new FileWriter(OUTPUT_FILE_PATH2, true);
        FileWriter fileWriter3 = new FileWriter(OUTPUT_FILE_PATH3, true);
        FileWriter fileWriter4 = new FileWriter(OUTPUT_FILE_PATH4, true);
        FileWriter fileWriter5 = new FileWriter(OUTPUT_FILE_PATH5, true);
        FileWriter fileWriter6 = new FileWriter(OUTPUT_FILE_PATH6, true);
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
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
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                            "and rank_num = ?");
                    ps.setString(1, lemma);
                    ps.setInt(2, 1);
                    ResultSet rs = ps.executeQuery();
                    boolean found = false;
                    while (rs.next()) {
                        level1WordCount ++;
                        found = true;
                        break;
                    }
                    if (!found) {
                        ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                                "and rank_num = ?");
                        ps.setString(1, lemma);
                        ps.setInt(2, 2);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            level2WordCount ++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                                "and rank_num = ?");
                        ps.setString(1, lemma);
                        ps.setInt(2, 3);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            level3WordCount ++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                                "and rank_num = ?");
                        ps.setString(1, lemma);
                        ps.setInt(2, 4);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            level4WordCount ++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                                "and rank_num = ?");
                        ps.setString(1, lemma);
                        ps.setInt(2, 5);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            level5WordCount ++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                                "and rank_num = ?");
                        ps.setString(1, lemma);
                        ps.setInt(2, 6);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            level6WordCount ++;
                            break;
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
                double ratio = typeCount / wordCount;

                // 计算平均词长
                int avgWordLength = totalWordLength / wordCount;

                // 分数
                double total = level1WordCount*1 +
                        level2WordCount*2 +
                        level3WordCount*3 +
                        level4WordCount*4 +
                        level5WordCount*5 +
                        level6WordCount*6 +
                        wordCount +
                        ratio +
                        avgWordLength +
                        clauseCount +
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
                        maxClauseLength;
                System.out.println(total);

                if (total >= 185) {
                    fileWriter6.append(sentence.toString() + "\r\n");
                    fileWriter6.flush();
                }
                else if (total >= 160) {
                    fileWriter5.append(sentence.toString() + "\r\n");
                    fileWriter5.flush();
                }
                else if (total >= 145) {
                    fileWriter4.append(sentence.toString() + "\r\n");
                    fileWriter4.flush();
                }
                else if (total >= 125) {
                    fileWriter3.append(sentence.toString() + "\r\n");
                    fileWriter3.flush();
                }
                else if (total >= 110) {
                    fileWriter2.append(sentence.toString() + "\r\n");
                    fileWriter2.flush();
                }
                else {
                    fileWriter1.append(sentence.toString() + "\r\n");
                    fileWriter1.flush();
                }
            } // for (CoreMap sentence : result)
        }
    }
}
