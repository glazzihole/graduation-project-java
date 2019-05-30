package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.dao.RankWordDao;
import com.hugailei.graduation.corpus.dao.StudentRankWordDao;
import com.hugailei.graduation.corpus.domain.StudentRankWord;
import com.hugailei.graduation.corpus.dto.StudentTextDto;
import com.hugailei.graduation.corpus.service.StudentRankWordService;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class StudentRankWordServiceImpl implements StudentRankWordService {

    @Autowired
    private StudentRankWordDao studentRankWordDao;

    @Autowired
    private RankWordDao rankWordDao;

    @Override
    public boolean saveRankWordInText(StudentTextDto studentTextDto) {
        try {
            log.info("saveRankWordInText | student text info: {}", studentTextDto.toString());
            int rankNum = studentTextDto.getRankNum();
            List<String> rankWordList = rankWordDao.findAllByRankNumIsGreaterThanEqual(rankNum)
                    .stream()
                    .map(e -> {
                        return e.getWord();
                    })
                    .collect(Collectors.toList());
            Set<String> rankWordSet = new HashSet<>(rankWordList);
            String text = studentTextDto.getText();
            long studentId = studentTextDto.getStudentId();
            StudentRankWord studentRankWord = studentRankWordDao.findOneByStudentIdAndRankNum(studentId, rankNum);
            Set<String> studentWordSet = new HashSet<>();
            StringBuilder studentWords = new StringBuilder();
            if (studentRankWord != null && !StringUtils.isBlank(studentRankWord.getWords())) {
                studentWords.append(studentRankWord.getWords());
                String[] wordArray = studentRankWord.getWords().split(",");
                List<String> wordList = Arrays.asList(wordArray);
                studentWordSet = new HashSet<>(wordList);
            }
            List<CoreMap> coreMapList = StanfordParserUtil.parse(text);
            for (CoreMap coreMap : coreMapList) {
                for (CoreLabel token : coreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    if (rankWordSet.contains(lemma)) {
                        if (!studentWordSet.contains(lemma)) {
                            studentWordSet.add(lemma);
                            studentWords.append(lemma).append(",");
                        }
                    }
                }
            }
            StudentRankWord newStudentRankWord = new StudentRankWord();
            newStudentRankWord.setWords(studentWords.toString());
            newStudentRankWord.setStudentId(studentTextDto.getStudentId());
            newStudentRankWord.setRankNum(studentTextDto.getRankNum());
            studentRankWordDao.save(newStudentRankWord);
            return true;
        } catch (Exception e) {
            log.error("saveRankWordInText | error: {}", e);
            return false;
        }
    }

    @Override
    public Set<String> getStudentRankWord(long studentId, int rankNum) {
        try {
            log.info("getStudentRankWord | student id: {}, rank num: {}", studentId, rankNum);
            List<StudentRankWord> studentRankWordList = studentRankWordDao.findByStudentIdAndRankNumIsGreaterThanEqual(studentId, rankNum);
            List<String> wordList = new ArrayList<>();
            if (studentRankWordList != null) {
                for (StudentRankWord studentRankWord : studentRankWordList) {
                    if (!StringUtils.isBlank(studentRankWord.getWords())) {
                        String[] wordArray = studentRankWord.getWords().split(",");
                        List<String> tempList = Arrays.asList(wordArray);
                        wordList.addAll(tempList);
                    }
                }
            }
            Set<String> studentWordSet = new HashSet<>(wordList);
            return studentWordSet;
        } catch (Exception e) {
            log.error("getStudentRankWord | error: {}", e);
            return null;
        }
    }

    @Override
    public Set<String> saveStudentRankWordInSession(long studentId, int rankNum, HttpServletRequest request) {
        try {
            log.info("saveStudentRankWordInSession | student id: {}, rank num: {}", studentId, rankNum);
            Set<String> studentRankWordSet = getStudentRankWord(studentId, rankNum);
            HttpSession session = request.getSession();
            session.setAttribute("" + studentId, studentRankWordSet);
            return studentRankWordSet;
        } catch (Exception e) {
            log.error("saveStudentRankWordInSession | error: {}", e);
            return null;
        }
    }
}
