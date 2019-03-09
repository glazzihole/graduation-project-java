package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2019/2/3
 * <p>
 * description: 验证难度评级公式
 * </p>
 **/
public class CheckSentenceRank {
    public static String INPUT_FILE_PATH = "E:\\毕业论文相关\\作文\\阅读\\阅读理解\\训练集\\6.txt";
    public static String OUTPUT_FILE_PATH = "E:\\毕业论文相关\\作文\\阅读\\结果统计\\R\\6.txt";
    public static int RIGHT_LEVEL = 6;
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

        FileReader fileReader = new FileReader(new File(INPUT_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
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
                }
                int avgWordLength = totalWordLength / wordCount;
                double R = -0.017 * sentenceLength + 0.143 * avgWordLength
                        +0.184 * level1WordCount + 0.19 * level2WordCount
                        +0.245 * level3WordCount + 0.197 * level4WordCount
                        +0.156 * level5WordCount + 0.186 * level6WordCount;
                System.out.println(R);
                fileWriter.write(R + "\t" + RIGHT_LEVEL + "\r\n");
                fileWriter.flush();
            } // for (CoreMap sentence : result)
        }
    }
}
