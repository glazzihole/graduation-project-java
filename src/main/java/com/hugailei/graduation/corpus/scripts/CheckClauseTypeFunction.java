package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.enums.SentencePatternType;
import com.hugailei.graduation.corpus.util.SentenceAnalysisUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/26
 * <p>
 * description: 句型匹配准确率校验
 * </p>
 **/
public class CheckClauseTypeFunction {
    private static final String SENTENCE_FILE_PATH = "E:\\毕业论文相关\\从句\\长难句.txt";
    private static final String SUBJECT_CLAUSE_FILE_PATH = "E:\\毕业论文相关\\从句\\结果\\主语从句\\";
    private static final String OBJECT_CLAUSE_FILE_PATH = "E:\\毕业论文相关\\从句\\结果\\宾语从句\\";
    private static final String ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE_FILE_PATH = "E:\\毕业论文相关\\从句\\结果\\定语同位语从句\\";
    private static final String PREDICATIVE_CLAUSE_FILE_PATH = "E:\\毕业论文相关\\从句\\结果\\表语从句\\";
    private static final String ADVERBIAL_CLAUSE_FILE_PATH = "E:\\毕业论文相关\\从句\\结果\\状语从句\\";

    public static void main(String[] args) throws Exception{
        FileReader fileReader = new FileReader(new File(SENTENCE_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        FileWriter fileWriter = null;
        String sentence;
        int lineNum = 1;
        while((sentence = bufferedReader.readLine()) != null) {
            List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
            if (coreMapList == null || coreMapList.isEmpty() || coreMapList.size() == 0) {
                continue;
            }
            List<SentencePattern> sentencePatternList = SentenceAnalysisUtil.matchSubjectClause(coreMapList.get(0));
            if (sentencePatternList != null && !sentencePatternList.isEmpty()) {
                fileWriter = new FileWriter(new File(SUBJECT_CLAUSE_FILE_PATH + lineNum + ".txt"));
                for (SentencePattern sentencePattern : sentencePatternList) {
                    fileWriter.write(sentencePattern.getClauseContent() + "\r\n");
                    fileWriter.flush();
                }
            }

            sentencePatternList = SentenceAnalysisUtil.matchObjectClauseOrPredicativeClause(coreMapList.get(0));
            if (sentencePatternList != null && !sentencePatternList.isEmpty()) {
                for (SentencePattern sentencePattern : sentencePatternList) {
                    if (sentencePattern.getType() == SentencePatternType.OBJECT_CLAUSE.getType()) {
                        fileWriter = new FileWriter(new File(OBJECT_CLAUSE_FILE_PATH + lineNum + ".txt"));
                        fileWriter.flush();
                    }
                }
                for (SentencePattern sentencePattern : sentencePatternList) {
                    if (sentencePattern.getType() == SentencePatternType.OBJECT_CLAUSE.getType()) {
                        fileWriter.write(sentencePattern.getClauseContent() + "\r\n");
                        fileWriter.flush();
                    }
                }

                for (SentencePattern sentencePattern : sentencePatternList) {
                    if (sentencePattern.getType() == SentencePatternType.PREDICATIVE_CLAUSE.getType()) {
                        fileWriter = new FileWriter(new File(PREDICATIVE_CLAUSE_FILE_PATH + lineNum + ".txt"));
                        fileWriter.flush();
                    }
                }
                for (SentencePattern sentencePattern : sentencePatternList) {
                    if (sentencePattern.getType() == SentencePatternType.PREDICATIVE_CLAUSE.getType()) {
                        fileWriter.write(sentencePattern.getClauseContent() + "\r\n");
                        fileWriter.flush();
                    }
                }
            }
            sentencePatternList = SentenceAnalysisUtil.matchAppositiveClauseOrAttributiveClause(coreMapList.get(0));
            if (sentencePatternList != null && !sentencePatternList.isEmpty()) {
                fileWriter = new FileWriter(new File(ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE_FILE_PATH + lineNum + ".txt"));
                for (SentencePattern sentencePattern : sentencePatternList) {
                    fileWriter.write(sentencePattern.getClauseContent() + "\r\n");
                    fileWriter.flush();
                }
            }

            sentencePatternList = SentenceAnalysisUtil.matchAdverbialClause(coreMapList.get(0));
            if (sentencePatternList != null && !sentencePatternList.isEmpty()) {
                fileWriter = new FileWriter(new File(ADVERBIAL_CLAUSE_FILE_PATH + lineNum + ".txt"));
                for (SentencePattern sentencePattern : sentencePatternList) {
                    fileWriter.write(sentencePattern.getClauseContent() + "\r\n");
                    fileWriter.flush();
                }
            }

            lineNum ++;
            System.out.println(lineNum);
        }
    }
}
