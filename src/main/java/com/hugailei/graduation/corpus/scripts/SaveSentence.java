package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.FileUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/22
 * <p>
 * description: 存储语料库句子
 * </p>
 **/
public class SaveSentence {

    private static final String TEXT_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\bnc-sample-text";

    private static final String TYPE_FILE_PATH = "E:\\毕业论文相关\\bnc-type\\";

    private static final String TITLE_FILE_PATH = "E:\\毕业论文相关\\bnc-title\\";

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static final String CORPUS = "bnc";

    public static void main(String[] args) throws Exception{
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
        fileList = FileUtil.getFilesUnderPath(TEXT_FILE_PATH, fileList);

        Long textId = 44066L;
        Long sentenceId = 1085240L;
        for (File file : fileList) {
            System.out.println("开始分析" + file.getCanonicalPath());
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            StringBuilder text = new StringBuilder();
            int sentenceCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("line: " + line);
                List<CoreMap> sentences = StanfordParserUtil.parse(line);
                for (CoreMap sentence : sentences) {
                    text.append(sentence.toString().replace("\r", "").replace("\n", "")).append(". ");
                    sentenceCount ++;
                    int wordCount = sentence.get(CoreAnnotations.TokensAnnotation.class).size();
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO tb_sentence"
                            + "(sentence, text_id, word_count, id, corpus) "
                            + "VALUES (?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, sentence.toString());
                    preparedStatement.setLong(2, textId);
                    preparedStatement.setInt(3, wordCount);
                    preparedStatement.setLong(4, sentenceId ++);
                    preparedStatement.setString(5, CORPUS);
                    preparedStatement.execute();
                }
            }

            // 存储文章
            String title = "";
            String type = "";
//            String fileName = file.getName();
//
//            FileReader fileReader = new FileReader(new File(TITLE_FILE_PATH + fileName));
//            BufferedReader bufferedReader1 = new BufferedReader(fileReader);
//            title = bufferedReader1.readLine();
//
//            fileReader = new FileReader(new File(TYPE_FILE_PATH + fileName));
//            bufferedReader1 = new BufferedReader(fileReader);
//            type = bufferedReader1.readLine();

            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO tb_text"
                    + "(id, corpus, sentence_count, text, title, type) "
                    + "VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, textId ++);
            preparedStatement.setString(2, CORPUS);
            preparedStatement.setInt(3, sentenceCount);
            preparedStatement.setString(4, text.toString());
            preparedStatement.setString(5, title);
            preparedStatement.setString(6, type);
            preparedStatement.execute();
        }
    }
}
