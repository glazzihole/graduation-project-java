package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/28
 * <p>
 * description: 计算文章中每类句型的数量
 * </p>
 **/
public class CountSententenceTypeInText {
    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static final int TOPIC = 4;

    private static final String CORPUS_NAME = "bnc";

    private static final String OUTPUT_FILE_PATH = "E:\\sentence-type-count-topic" + CORPUS_NAME + "_" +TOPIC + ".txt";

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }
        float topic1SentenceCount = 0, topic2SentenceCount = 0, topic3SentenceCount = 0, topic4SentenceCount = 0;
        float type1Count = 0, type2Count = 0, type3Count = 0, type4Count = 0, type5Count = 0;
        float type6Count = 0, type7Count = 0, type8Count = 0, type9Count = 0, type10Count = 0, type11Count = 0;
        PreparedStatement preparedStatement = con.prepareStatement("SELECT sentence FROM tb_sentence where topic = " + TOPIC + " " +
                "AND corpus = '" + CORPUS_NAME + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                try {
                    String sentence = resultSet.getString("sentence");
                    System.out.println(sentence);
                    switch (TOPIC) {
                        case 1:
                            topic1SentenceCount ++;
                            break;
                        case 2:
                            topic2SentenceCount ++;
                            break;
                        case 3:
                            topic3SentenceCount ++;
                            break;
                        case 4:
                            topic4SentenceCount ++;
                            break;
                        default:
                            break;
                    }
                    List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
                    List<SentencePattern> sentencePatternList = SentencePatternUtil.findAllClauseType(coreMapList.get(0));
                    if (sentencePatternList != null) {
                        for (SentencePattern sentencePattern : sentencePatternList) {
                            int type = sentencePattern.getType();
                            System.out.println(type + " " + sentence);
                            switch (type) {
                                case 1:
                                    type1Count ++;
                                    break;
                                case 2:
                                    type2Count ++;
                                    break;
                                case 3:
                                    type3Count ++;
                                    break;
                                case 4:
                                    type4Count ++;
                                    break;
                                case 5:
                                    type5Count ++;
                                    break;
                                case 6:
                                    type6Count ++;
                                    break;
                                case 7:
                                    type7Count ++;
                                    break;
                                case 8:
                                    type8Count ++;
                                    break;
                                case 9:
                                    type9Count ++;
                                    break;
                                case 10:
                                    type10Count ++;
                                    break;
                                case 11:
                                    type11Count ++;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            } // while


        System.out.println("分析完成，开始存入文件");
        FileWriter fileWriter = new FileWriter(new File(OUTPUT_FILE_PATH));
        float sentenceCount = 0;
        switch (TOPIC) {
            case 1:
                sentenceCount = topic1SentenceCount;
                break;
            case 2:
                sentenceCount = topic2SentenceCount ++;
                break;
            case 3:
                sentenceCount = topic3SentenceCount ++;
                break;
            case 4:
                sentenceCount = topic4SentenceCount ++;
                break;
            default:
                break;
        }
        fileWriter.write(
                sentenceCount + "\t" +
                type1Count + "\t" +
                type2Count + "\t" +
                type3Count + "\t" +
                type4Count + "\t" +
                type5Count + "\t" +
                type6Count + "\t" +
                type7Count + "\t" +
                type8Count + "\t" +
                type9Count + "\t" +
                type10Count + "\t" +
                type11Count + "\t" +
                type1Count / sentenceCount + "\t" +
                type2Count / sentenceCount  + "\t" +
                type3Count / sentenceCount  + "\t" +
                type4Count / sentenceCount  + "\t" +
                type5Count / sentenceCount  + "\t" +
                type6Count / sentenceCount  + "\t" +
                type7Count / sentenceCount  + "\t" +
                type8Count / sentenceCount  + "\t" +
                type9Count / sentenceCount  + "\t" +
                type10Count / sentenceCount  + "\t" +
                type11Count / sentenceCount
        );
        fileWriter.flush();
        System.out.println("处理完成");
    }
}
