package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.StudentTextDao;
import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.domain.StudentText;
import com.hugailei.graduation.corpus.dto.SentencePatternDto;
import com.hugailei.graduation.corpus.dto.StudentTextDto;
import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.enums.SentencePatternType;
import com.hugailei.graduation.corpus.service.StudentRankWordService;
import com.hugailei.graduation.corpus.service.StudentTextService;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import com.hugailei.graduation.corpus.util.TopicClassifyUtil;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description:
 * <p/>
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class StudentTextServiceImpl implements StudentTextService {

    @Autowired
    private StudentTextDao studentTextDao;

    @Autowired
    private StudentRankWordService studentRankWordService;

    @Override
    public StudentTextDto insertText(StudentTextDto studentTextDto) {
        try {
            log.info("insertText | studentTextDto: {}", studentTextDto);
            StudentText studentText = new StudentText();
            BeanUtils.copyProperties(studentTextDto, studentText);
            // 首次插入，id为空，需要设置创建时间
            if (studentTextDto.getId() == null) {
                studentText.setCreateTime(System.currentTimeMillis());
            }
            studentText.setUpdateTime(System.currentTimeMillis());
            StudentText result = studentTextDao.save(studentText);
            BeanUtils.copyProperties(result, studentTextDto);
            boolean saveRankWordResult = studentRankWordService.saveRankWordInText(studentTextDto);
            if (!saveRankWordResult) {
                throw new Exception("failed to update rank word");
            }
            return studentTextDto;
        } catch (Exception e) {
            log.error("insertText | error: {}", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public StudentTextDto getStudentText(Long textId) {
        try {
            log.info("getStudentText | text id: {}", textId);
            StudentText studentText = studentTextDao.findById(textId).get();
            StudentTextDto studentTextDto = new StudentTextDto();
            BeanUtils.copyProperties(studentText, studentTextDto);
            log.info("getStudentText | result: {}", studentTextDto);
            return studentTextDto;
        } catch (Exception e) {
            log.error("getStudentText | error: {}", e);
            return null;
        }
    }


    @Override
    @Cacheable(value = "student", key = "'sentence_pattern_' + #text", unless = "#result eq null")
    public List<SentencePatternDto> getSentencePatternInText(String text) {
        try {
            log.info("getSentencePatternInText | text: {}", text);
            Map<Integer, SentencePatternDto> type2SentencePatternDto = new HashMap<>();
            List<CoreMap> sentences = StanfordParserUtil.parse(text);
            for (CoreMap sentence : sentences) {
                List<SentencePattern> tempList = SentencePatternUtil.matchSubjectClause(sentence);
                if (tempList != null) {
                    int type = SentencePatternType.SUBJECT_CLAUSE.getType();
                    String typeName = SentencePatternType.SUBJECT_CLAUSE.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, tempList.size(), sentence.toString());
                }

                tempList = SentencePatternUtil.matchObjectClauseOrPredicativeClause(sentence);
                if (tempList != null) {
                    for (SentencePattern sentencePattern : tempList) {
                        int type = sentencePattern.getType();
                        if (type2SentencePatternDto.containsKey(type)) {
                            SentencePatternDto sentencePatternDto = type2SentencePatternDto.get(type);
                            sentencePatternDto.setFreq(sentencePatternDto.getFreq() + 1);
                            Set<String> sentenceSet = sentencePatternDto.getSentenceSet();
                            sentenceSet.add(sentence.toString());
                            sentencePatternDto.setSentenceSet(sentenceSet);
                            type2SentencePatternDto.put(type, sentencePatternDto);
                        } else {
                            SentencePatternDto sentencePatternDto = new SentencePatternDto();
                            sentencePatternDto.setFreq(1);
                            String typeName = (type == SentencePatternType.OBJECT_CLAUSE.getType()) ? SentencePatternType.OBJECT_CLAUSE.getTypeName() : SentencePatternType.PREDICATIVE_CLAUSE.getTypeName();
                            sentencePatternDto.setPatternTypeName(typeName);
                            Set<String> sentenceSet = new HashSet<>();
                            sentenceSet.add(sentence.toString());
                            sentencePatternDto.setSentenceSet(sentenceSet);
                            type2SentencePatternDto.put(type, sentencePatternDto);
                        }
                    }
                }

                tempList = SentencePatternUtil.matchAppositiveClauseOrAttributiveClause(sentence);
                if (tempList != null) {
                    int type = SentencePatternType.ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE.getType();
                    String typeName = SentencePatternType.ATTRIBUTIVE_CLAUSE_OR_APPOSITIVE_CLAUSE.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, tempList.size(), sentence.toString());
                }

                tempList = SentencePatternUtil.matchAdverbialClause(sentence);
                if (tempList != null) {
                    int type = SentencePatternType.ADVERBIAL_CLAUSE.getType();
                    String typeName = SentencePatternType.ADVERBIAL_CLAUSE.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, tempList.size(), sentence.toString());
                }

                tempList = SentencePatternUtil.matchDoubleObject(sentence);
                if (tempList != null) {
                    int type = SentencePatternType.DOUBLE_OBJECT.getType();
                    String typeName = SentencePatternType.DOUBLE_OBJECT.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, tempList.size(), sentence.toString());
                }

                tempList = SentencePatternUtil.matchPassiveVoice(sentence);
                if (tempList != null) {
                    int type = SentencePatternType.PASSIVE_VOICE.getType();
                    String typeName = SentencePatternType.PASSIVE_VOICE.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, tempList.size(), sentence.toString());
                }

                if (SentencePatternUtil.hasSoThat(sentence)) {
                    int type = SentencePatternType.S_THAT.getType();
                    String typeName = SentencePatternType.S_THAT.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, 1, sentence.toString());
                }

                if (SentencePatternUtil.hasTooTo(sentence)) {
                    int type = SentencePatternType.TOO_TO.getType();
                    String typeName = SentencePatternType.TOO_TO.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, 1, sentence.toString());
                }

                if (SentencePatternUtil.hasInvertedStructure(sentence)) {
                    int type = SentencePatternType.INVERTED_STRUCTURE.getType();
                    String typeName = SentencePatternType.INVERTED_STRUCTURE.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, 1, sentence.toString());
                }

                if (SentencePatternUtil.hasEmphaticStructure(sentence)) {
                    int type = SentencePatternType.EMPHATIC_STRUCTURE.getType();
                    String typeName = SentencePatternType.EMPHATIC_STRUCTURE.getTypeName();
                    updateSentecePatternDto(type2SentencePatternDto, type, typeName, 1, sentence.toString());
                }
            }
            return new ArrayList<>(type2SentencePatternDto.values());
        } catch (Exception e) {
            log.error("getSentencePatternInText | error: {}", e);
            return null;
        }
    }

    /**
     * 获取文章的主题信息
     *
     * @param text
     * @return
     */
    @Override
    public List<TopicDto> getTopic(String text) {
        try {
            log.info("getTopic | text: {}", text);
            List<TopicDto> topicDtoList = TopicClassifyUtil.getTopicInfoList(text);
            log.info("getTopic | topic list info: {}", topicDtoList.toString());
            return topicDtoList;
        } catch (Exception e) {
            log.info("getTopic | error: {}", e);
            return null;
        }
    }

    /**
     * 更新map
     *
     * @param type2SentencePatternDto
     * @param type
     * @param typeName
     * @param freq
     * @param sentence
     */
    private void updateSentecePatternDto(Map<Integer, SentencePatternDto> type2SentencePatternDto,
                                         int type,
                                         String typeName,
                                         int freq,
                                         String sentence) {
        if (type2SentencePatternDto.containsKey(type)) {
            SentencePatternDto sentencePatternDto = type2SentencePatternDto.get(type);
            sentencePatternDto.setFreq(sentencePatternDto.getFreq() + freq);
            Set<String> sentenceSet = sentencePatternDto.getSentenceSet();
            sentenceSet.add(sentence);
            sentencePatternDto.setSentenceSet(sentenceSet);
            type2SentencePatternDto.put(type, sentencePatternDto);
        } else {
            SentencePatternDto sentencePatternDto = new SentencePatternDto();
            sentencePatternDto.setFreq(freq);
            sentencePatternDto.setPatternTypeName(typeName);
            Set<String> sentenceSet = new HashSet<>();
            sentenceSet.add(sentence);
            sentencePatternDto.setSentenceSet(sentenceSet);
            type2SentencePatternDto.put(type, sentencePatternDto);
        }
    }
}
