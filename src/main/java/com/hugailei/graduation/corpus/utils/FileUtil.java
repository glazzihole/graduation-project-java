package com.hugailei.graduation.corpus.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/9/9
 * <p>
 * description: 文件相关的工具类
 * </p>
 **/
@Slf4j
public class FileUtil {

    /**
     * 获取指定路径下的所有文件
     *
     * @param path
     * @param fileList
     * @return
     */
    public static List<File> getFilesUnderPath(String path, List<File> fileList) {
        try {
            File file = new File(path);
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                for (File childFile : fileArray) {
                    if (childFile.isDirectory()) {
                        getFilesUnderPath(childFile.getCanonicalPath(), fileList);
                    } else {
                        fileList.add(childFile);
                    }
                }
            } else {
                fileList.add(file);
            }
        } catch (Exception e) {
            System.out.println("获取文件错误" + e.getStackTrace());
        }

        return fileList;
    }

}
