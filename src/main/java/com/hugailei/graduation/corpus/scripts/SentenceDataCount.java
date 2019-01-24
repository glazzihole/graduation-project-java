package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.FileUtil;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/1/21
 * <p>
 * description: 句子难度各项指标统计
 * </p>
 **/
public class SentenceDataCount {
    private static final String ARTICLE_FILE_PATH = "";
    private static final String OUTPUT_FILE_PATH = "";
    private static final int LEVEL = 0;
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

        FileWriter fileWriter = new FileWriter(new File(OUTPUT_FILE_PATH));

        List<File> fileList = new ArrayList<>();
        fileList = FileUtil.getFilesUnderPath(ARTICLE_FILE_PATH, fileList);
        for (File file : fileList) {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                List<CoreMap> result = StanfordParserUtil.parse(line);
                for (CoreMap sentence : result) {
                    // 计算句子长度
                    int sentenceLength = sentence.toString().split(" ").length;
                    int clauseCount = 0;
                    // 计算平均词长及各级单词数
                    int totalWordLength = 0;
                    int wordCount = 0;
                    int level1WordCount = 0, level2WordCount = 0, level3WordCount = 0;
                    int level4WordCount = 0, level5WordCount = 0, level6WordCount = 0;

                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        wordCount ++;
                        totalWordLength += word.length();

                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_rank_word WHERE word = ? " +
                                "and rankNum = ?");
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
                                    "and rankNum = ?");
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
                                    "and rankNum = ?");
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
                                    "and rankNum = ?");
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
                                    "and rankNum = ?");
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
                                    "and rankNum = ?");
                            ps.setString(1, lemma);
                            ps.setInt(2, 6);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                level6WordCount ++;
                                break;
                            }
                        }
                    }

                    // 计算从句个数
                    Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                    TregexMatcher soThatClauseMatcher = TregexPattern.compile("SBAR | S").matcher(tree);
                    while (soThatClauseMatcher.findNextMatchingNode()) {
                        clauseCount ++;
                    }
                    int avgWordLength = totalWordLength / wordCount;

                    // 写入到文件 句长，平均词长，从句数，四级单词数，六级单词数，专4单词数，专8单词数，托福雅思单词数，GRE/GMAT单词数
                    String out = sentenceLength + "\t" +
                                avgWordLength + "\t" +
                                clauseCount + "\t" +
                                level1WordCount + "\t" +
                                level2WordCount + "\t" +
                                level3WordCount + "\t" +
                                level4WordCount + "\t" +
                                level5WordCount + "\t" +
                                level6WordCount;
                    fileWriter.write(out);
                    fileWriter.flush();
                } // for (CoreMap sentence : result)
            } // while line
        } // for File
    }
}
