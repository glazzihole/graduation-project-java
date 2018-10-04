package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.Word;
import com.hugailei.graduation.corpus.utils.FileUtil;
import com.hugailei.graduation.corpus.utils.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
public class SaveParserResult2 {
    //文件路径
    private static final String FILE_PATH = "E:\\毕业论文相关\\bnc-sample-text";

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
        Connection con = DriverManager.getConnection(url, USER_NAME, USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        //读取语料
        List<File> fileList = new ArrayList<>();
        fileList = FileUtil.getFilesUnderPath(FILE_PATH, fileList);
        Long textId = 1L;
        Long sentenceId = 1L;
        HashMap<String, Word> wordLemmaPos2Word = new HashMap<>();
        for (File file : fileList) {
            System.out.println("开始分析" + file.getCanonicalPath());
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            StringBuilder text = new StringBuilder();
            StringBuilder sentence;
            int textSentenceCount = 0;
            while ((line = bufferedReader.readLine()) != null && line != "") {
                System.out.println("line: " + line);
                text.append(line.replace("\r", "").replace("\n", ""));
                // 获取标注结果
                List<CoreMap> result = StanfordParserUtil.parse(line);
                for (CoreMap sentenceCoreMap : result) {
                    sentence = new StringBuilder();
                    for (CoreLabel token : sentenceCoreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                        // 单词
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        // 词性
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        // 原型
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                        if (word.toLowerCase().matches("[\\p{P}0-9]")) {
                            continue;
                        }

                        //存储单词
                        String key = word.toLowerCase() + "_" + lemma.toLowerCase() + "_" + pos;
                        if (wordLemmaPos2Word.containsKey(key)) {
                            Word newWord = wordLemmaPos2Word.get(key);
                            int newFreq = newWord.getFreq() + 1;
                            String newSentenceIds = newWord.getSentenceIds() + sentenceId + ",";
                            newWord.setFreq(newFreq);
                            newWord.setSentenceIds(newSentenceIds);
                            wordLemmaPos2Word.put(key, newWord);
                        } else {
                            Word newWord = new Word(null,
                                                    word.toLowerCase(),
                                                    pos,
                                                    lemma.toLowerCase(),
                                                    1,
                                                    sentenceId+",",
                                                    CORPUS_NAME
                                                );
                            wordLemmaPos2Word.put(key, newWord);
                        }
                    } //for token
                } // for sentence
                sentenceId++;
            } //while line
        }//for file

        System.out.println("数据统计完成，现在开始存入数据库");
        for (Map.Entry<String, Word> entry : wordLemmaPos2Word.entrySet()) {
            Word word = entry.getValue();
            System.out.println("WORD: " + word.toString());
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + WORD_COLL_NAME
                    + "(word, pos, lemma, freq, sentence_ids, corpus) "
                    + " VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, word.getWord());
            preparedStatement.setString(2, word.getPos());
            preparedStatement.setString(3, word.getLemma());
            preparedStatement.setInt(4, word.getFreq());
            preparedStatement.setString(5, word.getSentenceIds());
            preparedStatement.setString(6, word.getCorpus());
            preparedStatement.execute();
        }
    }
}
