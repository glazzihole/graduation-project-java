package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * @author HU Gailei
 * @date 2019/4/22
 * <p>
 * description: 计算句子的易读性
 * </p>
 **/
public class FleschValueCount {
    /**
     * 单音节单词数目
     */
    private final static String WORD_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\单音节单词.txt";

    public static void main(String[] args) throws Exception{
        HashSet monosyllabicWordSet = new HashSet();
        FileReader fileReader = new FileReader(new File(WORD_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while((line = bufferedReader.readLine()) != null) {
            monosyllabicWordSet.add(line.toLowerCase());
        }
        Scanner sc = new Scanner(System.in);
        //利用hasNextXXX()判断是否还有下一输入项
        System.out.println("请输入句子：");
        while (sc.hasNext()) {
            String sentence = sc.nextLine();
            // 句法分析，获取单音节词数及句长
            int monosyllabicWordCount = 0;
            int sentenceLength = 0;
            List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
            for (CoreMap coreMap : coreMapList) {
                // 原型、词性等信息
                for (CoreLabel token : coreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    if (monosyllabicWordSet.contains(lemma)) {
                        monosyllabicWordCount++;
                    }
                    sentenceLength ++;
                }
            }
            double result = 1.599*monosyllabicWordCount/100 - 1.015*sentenceLength - 31.517;
            System.out.println(result);
            System.out.println("请输入句子：");
        }
    }
}
