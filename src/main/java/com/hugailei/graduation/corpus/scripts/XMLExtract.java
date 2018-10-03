package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/9/22
 * <p>
 * description: 解析XML，抽取指定节点的内容并输出
 * </p>
 **/
public class XMLExtract {
    private static final String XML_FILES_PATH = "C:\\Users\\GAILEI\\Desktop\\2553\\2553\\download\\Texts";
    private static final String XML_EXTRACT_SCRIPT_PATH = "D:\\xsltproc\\bin\\xsltproc.exe";
    private static final String XML_STYLE_SHEET_PATH = "D:\\xsltproc\\stylesheet\\justTheWords.xsl";
    private static final String RESULT_FILE_PATH = "E:\\毕业论文相关\\bnc-sample-text\\";
    public static void main(String[] args) {
        List<File> allFile = new ArrayList<>();
        FileUtil.getFilesUnderPath(XML_FILES_PATH, allFile);

        //对于每个文件，提取主题及标题，输出到分别输出到指定文件夹
        for (File file : allFile) {
            System.out.println(file.getName() + "提取开始");
            getText(file.getAbsolutePath());

        }
        System.out.println("提取完成");
    }

    /**
     * 提取指定节点的内容
     *
     * @param filePath
     */
    private static void getText(String filePath) {
        String fileName = new File(filePath).getName().replace("xml", "txt");
        try {
            Runtime.getRuntime().exec("cmd.exe /c "
                    + XML_EXTRACT_SCRIPT_PATH + " "
                    + XML_STYLE_SHEET_PATH + " "
                    + filePath + " >"
                    + RESULT_FILE_PATH + fileName);
        } catch (IOException e) {
            System.out.println("提取出错：" + e.getStackTrace());
        }
    }
}
