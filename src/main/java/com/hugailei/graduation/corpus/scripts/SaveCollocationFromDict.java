package com.hugailei.graduation.corpus.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HU Gailei
 * @date 2019/1/21
 * <p>
 * description: 解析搭配词典，存储到数据库中
 * </p>
 **/
public class SaveCollocationFromDict {
    private static final String FILE_PATH = "D:\\MDictPC\\牛津搭配词典2.txt";
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

        FileReader fileReader = new FileReader(new File(FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = "";
        String word = null, pos = null, collocationPos = null, collocation = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equals("</>")) {
                word = null;
                pos = null;
                continue;
            }
            // 单词
//            else if (line.matches("[A-Za-z0-9\\- ,]+")) {
            else if (!line.contains("<")) {
                word = line;
                if (line.contains(",")) {
                    word = line.split(",")[0];
                }
            }
            // 词性
            else if (line.contains(">verb<") ||
                    line.contains(">noun<") ||
                    line.contains(">adj.<") ||
                    line.contains(">adv.<")) {
                Pattern pattern = Pattern.compile(">(noun)|(verb)|(adv\\.)|(adj\\.)<");
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()) {
                    pos = matcher.group().replace(">", "").replace("<", "");
                }
            }
            // 搭配词及搭配词词性
            else if (line.contains("<u>")) {
                Pattern pattern = Pattern.compile("<u>[A-Z ]+</u>");
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()) {
                    collocationPos = matcher.group()
                            .replace("<u>", "")
                            .replace("</u>", "")
                            .replaceAll(" +", "");

                    switch (collocationPos) {
                        case "VERB":
                            if (pos.equals("noun")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim() + " " + word;
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            else if (pos.equals("adv.")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = word + " " + collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim();
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();

                                    collocation = collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim() + " " + word;
                                    System.out.println(collocation);
                                    ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            break;
                        case "ADVERB":
                            if (pos.equals("adj.")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim() + " " + word;
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            else if (pos.equals("verb")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim() + " " + word;
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();

                                    collocation = word + " " + collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim();
                                    System.out.println(collocation);
                                    ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            break;
                        case "PREPOSITION":
                            if (pos.equals("verb") || pos.equals("adj.")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = word + " " + collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim();
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            break;
                        case "ADJECTIVE":
                            if (pos.equals("noun")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim() + " " + word;
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            else if (pos.equals("adv.")) {
                                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                                Matcher collocationMatcher = collocationPattern.matcher(line);
                                while (collocationMatcher.find()) {
                                    collocation = word + " " + collocationMatcher.group()
                                            .replace("<font color=black><b>", "")
                                            .replace("</b></font>", "")
                                            .trim();
                                    System.out.println(collocation);
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                            "VALUES(?, ?, ?, ?) ");
                                    ps.setString(1, word);
                                    ps.setString(2, pos);
                                    ps.setString(3, collocation);
                                    ps.setString(4, collocationPos);
                                    ps.execute();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            else if (line.contains("is used with these verbs")) {
                Pattern collocationPattern = Pattern.compile("<a href='entry://[A-Za-z]+'>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = collocationMatcher.group()
                            .replace("<a href='entry://", "")
                            .replace("'>", "")
                            .trim() + " " + word;
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                    if (pos.equals("adv.")){
                        collocation = word + " " + collocationMatcher.group()
                                .replace("<a href='entry://", "")
                                .replace("'>", "")
                                .trim();
                        System.out.println(collocation);
                        ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                "VALUES(?, ?, ?, ?) ");
                        ps.setString(1, word);
                        ps.setString(2, pos);
                        ps.setString(3, collocation);
                        ps.setString(4, collocationPos);
                        ps.execute();
                    }
                }
            }
            else if (line.contains("is used with these adjectives")) {
                Pattern collocationPattern = Pattern.compile("<a href='entry://[A-Za-z]+'>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = collocationMatcher.group()
                            .replace("<a href='entry://", "")
                            .replace("'>", "")
                            .trim() + " " + word;
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                    if (pos.equals("adv.")){
                        collocation = word + " " + collocationMatcher.group()
                                .replace("<a href='entry://", "")
                                .replace("'>", "")
                                .trim();
                        System.out.println(collocation);
                        ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                                "VALUES(?, ?, ?, ?) ");
                        ps.setString(1, word);
                        ps.setString(2, pos);
                        ps.setString(3, collocation);
                        ps.setString(4, collocationPos);
                        ps.execute();
                    }
                }
            }
            else if (line.contains("is used with these nouns as the subject")) {
                Pattern collocationPattern = Pattern.compile("<a href='entry://[A-Za-z]+'>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = collocationMatcher.group()
                            .replace("<a href='entry://", "")
                            .replace("'>", "")
                            .trim() + " " + word;
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                }
            }
            else if (line.contains("is used with these nouns as the object")) {
                Pattern collocationPattern = Pattern.compile("<a href='entry://[A-Za-z]+'>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = word + " " + collocationMatcher.group()
                            .replace("<a href='entry://", "")
                            .replace("'>", "")
                            .trim();
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                }
            }
            else if (line.contains("is used with these nouns")) {
                Pattern collocationPattern = Pattern.compile("<a href='entry://[A-Za-z]+'>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = word + " " + collocationMatcher.group()
                            .replace("<a href='entry://", "")
                            .replace("'>", "")
                            .trim();
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                }
            }

            if (line.contains(word.toUpperCase() + " + VERB")) {
                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = word + " " + collocationMatcher.group()
                            .replace("<font color=black><b>", "")
                            .replace("</b></font>", "")
                            .trim();
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                }
            }
            else if (line.contains("VERB + " + word.toUpperCase())) {
                Pattern collocationPattern = Pattern.compile("<font color=black><b>[a-zA-Z ]+</b></font>");
                Matcher collocationMatcher = collocationPattern.matcher(line);
                while (collocationMatcher.find()) {
                    collocation = collocationMatcher.group()
                            .replace("<font color=black><b>", "")
                            .replace("</b></font>", "")
                            .trim() + " " + word;
                    System.out.println(collocation);
                    PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                            "VALUES(?, ?, ?, ?) ");
                    ps.setString(1, word);
                    ps.setString(2, pos);
                    ps.setString(3, collocation);
                    ps.setString(4, collocationPos);
                    ps.execute();
                }
            }

            // 词组，直接存储
            if (line.matches("[a-zA-Z ]+") && line.contains(" ")) {
                word = line.split(" ")[0];
                collocation = line;
                System.out.println(collocation);
                PreparedStatement ps = con.prepareStatement("INSERT INTO tb_dict_collocation(word, pos, collocation, collocation_pos) " +
                        "VALUES(?, ?, ?, ?) ");
                ps.setString(1, word);
                ps.setString(2, pos);
                ps.setString(3, collocation);
                ps.setString(4, collocationPos);
                ps.execute();
            }
        }
    }
}
