package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.utils.FileUtil;
import com.hugailei.graduation.corpus.utils.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/9/22
 * <p>
 * description: 脚本程序，对语料库进行标注，并将标注结果存放至mysql数据库。
 * </p>
 **/
public class SaveParserResult {
    //文件路径
    private static final String FILE_PATH = "E:\\毕业论文相关\\bnc-text";

    //数据库主机地址
    private static final String DB_HOST = "192.168.99.100";

    //数据库端口
    private static final String DB_PORT = "3307";

    //数据库名称
    private static final String DB_NAME="corpus";

    //数据库表名称
    private static final String TEXT_COLL_NAME="tb_text";

    //数据库表名称
    private static final String SENTENCE_COLL_NAME="tb_sentence";

    //数据库表名称
    private static final String WORD_COLL_NAME="tb_word";

    //数据库表名称
    private static final String POS_COLL_NAME="tb_pos";

    //数据库表名称
    private static final String LEMMA_COLL_NAME="tb_lemma";

    //数据库用户名
    private static final String USER_NAME="root";

    //数据库用户密码
    private static final String USER_PASSWORD="123456";

    //语料库名称
    private static final String CORPUS_NAME="bnc";

    public static void main(String[] args) throws Exception {
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        //读取语料
        List<File> fileList = new ArrayList<>();
        fileList = FileUtil.getFilesUnderPath(FILE_PATH, fileList);
        Long textId = 1L;
        Long sentenceId = 1L;
        for (File file : fileList) {
            System.out.println("开始存储" + file.getCanonicalPath());
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            StringBuilder text = new StringBuilder();
            StringBuilder sentence;
            int textSentenceCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("line: " + line);
                text.append(line.replace("\r", "").replace("\n",""));
                // 获取标注结果
                List<CoreMap> result = StanfordParserUtil.parse(line);
                for(CoreMap sentenceCoreMap : result) {
                    sentence = new StringBuilder();
                    for (CoreLabel token : sentenceCoreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                        // 单词
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        // 词性
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        // 原型
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                        // 存储单词表, 无则插入，有则更新
                        PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM " + WORD_COLL_NAME
                                + " WHERE word=? and pos=? and lemma=? and corpus=?");
                        preparedStatement.setString(1, word.toLowerCase());
                        preparedStatement.setString(2, pos);
                        preparedStatement.setString(3, lemma.toLowerCase());
                        preparedStatement.setString(4, CORPUS_NAME);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            Long id = resultSet.getLong("id");
                            int freq = resultSet.getInt("freq") + 1;
                            // 更新频率
                            preparedStatement = con.prepareStatement("UPDATE " + WORD_COLL_NAME
                                    + " SET freq=? WHERE id=?");
                            preparedStatement.setInt(1, freq);
                            preparedStatement.setLong(2, id);
                            preparedStatement.execute();

                            // 更新例句
                            String sentenceIds = resultSet.getString("sentence_ids") + sentenceId + " ";
                            preparedStatement = con.prepareStatement("UPDATE " + WORD_COLL_NAME
                                    + " SET sentence_ids=? WHERE id=?");
                            preparedStatement.setString(1, sentenceIds);
                            preparedStatement.setLong(2, id);
                            preparedStatement.execute();
                        } else {
                            String sentenceIds = sentenceId + " ";
                            preparedStatement = con.prepareStatement("INSERT INTO " + WORD_COLL_NAME
                                    + "(word, pos, lemma, freq, sentence_ids, corpus) "
                                    + " VALUES (?, ?, ?, ?, ?, ?)");
                            preparedStatement.setString(1, word.toLowerCase());
                            preparedStatement.setString(2, pos);
                            preparedStatement.setString(3, lemma.toLowerCase());
                            preparedStatement.setInt(4, 1);
                            preparedStatement.setString(5, sentenceIds);
                            preparedStatement.setString(6, CORPUS_NAME);
                            preparedStatement.execute();
                        }

                        // 存储词性，无则插入，有则更新
                        preparedStatement = con.prepareStatement("SELECT * FROM " + POS_COLL_NAME
                                + " WHERE pos=? and corpus=?");
                        preparedStatement.setString(1, pos);
                        preparedStatement.setString(2, CORPUS_NAME);
                        resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            Long id = resultSet.getLong("id");
                            int freq = resultSet.getInt("freq") + 1;
                            // 更新频率
                            preparedStatement = con.prepareStatement("UPDATE " + POS_COLL_NAME
                                    + " SET freq=? WHERE id=?");
                            preparedStatement.setInt(1, freq);
                            preparedStatement.setLong(2, id);
                            preparedStatement.execute();
                        } else {
                            preparedStatement = con.prepareStatement("INSERT INTO " + POS_COLL_NAME + "(pos, freq, corpus) " +
                                    " VALUES (?, ?, ?)");
                            preparedStatement.setString(1, pos);
                            preparedStatement.setInt(2, 1);
                            preparedStatement.setString(3, CORPUS_NAME);
                            preparedStatement.execute();
                        }

                        // 存储原型，无则插入，有则更新
                        preparedStatement = con.prepareStatement("SELECT * FROM " + LEMMA_COLL_NAME
                                + " WHERE lemma=? and corpus=?");
                        preparedStatement.setString(1, lemma.toLowerCase());
                        preparedStatement.setString(2, CORPUS_NAME);
                        resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            int freq = resultSet.getInt("freq") + 1;
                            Long id = resultSet.getLong("id");
                            // 更新频率
                            preparedStatement = con.prepareStatement("UPDATE " + LEMMA_COLL_NAME
                                    + " SET freq=? WHERE id=?");
                            preparedStatement.setInt(1, freq);
                            preparedStatement.setLong(2, id);
                            preparedStatement.execute();
                        } else {
                            preparedStatement = con.prepareStatement("INSERT INTO " + LEMMA_COLL_NAME + "(lemma, freq, corpus) " +
                                    " VALUES (?, ?, ?)");
                            preparedStatement.setString(1, lemma.toLowerCase());
                            preparedStatement.setInt(2, 1);
                            preparedStatement.setString(3, CORPUS_NAME);
                            preparedStatement.execute();
                        }

                        sentence.append(word).append(" ");
                    }
                    // 存储句子
                    int sentenceWordCount = line.replaceAll("\\p{P}", " ").split(" +").length;
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + SENTENCE_COLL_NAME
                            + "(id, sentence, text_id, word_count) VALUES (?, ?, ?, ?)");
                    preparedStatement.setLong(1, sentenceId);
                    preparedStatement.setString(2, sentence.toString());
                    preparedStatement.setLong(3, textId);
                    preparedStatement.setInt(4, sentenceWordCount);
                    preparedStatement.execute();
                    sentenceId ++;

                    textSentenceCount++;
                }
            } // while line
            //存储文章
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + TEXT_COLL_NAME
                    + "(id, text, sentence_count, corpus) "
                    + "VALUES (?, ?, ?, ?)");
            preparedStatement.setLong(1, textId);
            preparedStatement.setString(2, text.toString());
            preparedStatement.setInt(3, textSentenceCount);
            preparedStatement.setString(4, CORPUS_NAME);
            preparedStatement.execute();
            textId++;
        } // for file

        con.close();
    }
}
