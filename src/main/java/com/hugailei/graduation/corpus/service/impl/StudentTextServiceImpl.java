package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.StudentDependencyDao;
import com.hugailei.graduation.corpus.dao.StudentSentenceDao;
import com.hugailei.graduation.corpus.dao.StudentTextDao;
import com.hugailei.graduation.corpus.domain.StudentDependency;
import com.hugailei.graduation.corpus.domain.StudentSentence;
import com.hugailei.graduation.corpus.domain.StudentText;
import com.hugailei.graduation.corpus.dto.DependencyDto;
import com.hugailei.graduation.corpus.dto.StudentTextDto;
import com.hugailei.graduation.corpus.service.StudentTextService;
import com.hugailei.graduation.corpus.util.StanfordDependencyUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private StudentSentenceDao studentSentenceDao;

    @Autowired
    private StudentDependencyDao studentDependencyDao;

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
    public List<StudentDependency> getAndSaveDependency(Long textId) {
        try {
            // 存储每个句子和句子中的句法关系
            List<StudentSentence> studentSentenceList = new ArrayList<>();
            List<StudentDependency> studentDependencyList = new ArrayList<>();
            StudentText studentText = studentTextDao.findById(textId).get();
            String text = studentText.getText();
            Long stuId = studentText.getStuId();
            log.info("getAndSaveDependency | start to parse text: {}", text);
            List<CoreMap> coreMapList = StanfordParserUtil.parse(text);
            for(CoreMap coreMap : coreMapList) {
                String sentence = coreMap.toString();
                studentSentenceList.add(new StudentSentence(null, stuId, sentence, textId));

                SemanticGraph dependency = coreMap.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                //提取依存关系
                for(SemanticGraphEdge edge : dependency.edgeListSorted()) {
                    studentDependencyList.add(new StudentDependency(null,
                                                                    stuId,
                                                                    edge.getRelation().toString(),
                                                                    edge.getGovernor().index(),
                                                                    edge.getGovernor().lemma(),
                                                                    edge.getGovernor().tag(),
                                                                    edge.getDependent().index(),
                                                                    edge.getDependent().lemma(),
                                                                    edge.getDependent().tag(),
                                                                    null
                                                                    ));
                }
            }
            log.info("getAndSaveDependency | start to save sentence");
            List<StudentSentence> saveSentenceResultList = studentSentenceDao.saveAll(studentSentenceList);

            log.info("getAndSaveDependency | start to save dependency");
            int i = 0;
            for (StudentSentence studentSentence : saveSentenceResultList) {
                StudentDependency temp = studentDependencyList.get(i);
                temp.setSentenceId(studentSentence.getId());
                studentDependencyList.set(i, temp);
                i++;
            }
            List<StudentDependency> saveDependencyResultList = studentDependencyDao.saveAll(studentDependencyList);
            return saveDependencyResultList;
        } catch (Exception e) {
            log.error("getAndSaveDependency | error: {}", e);
            return null;
        }
    }

    @Override
    public List<DependencyDto> getDependency(String text) {
        try {
            log.info("getAndSaveDependency | text: {}", text);
            List<DependencyDto> resultList = new ArrayList<>();
            List<SemanticGraphEdge> semanticGraphEdgeList = StanfordDependencyUtil.getDependency(text);
            int i = 0;

            for (SemanticGraphEdge edge : semanticGraphEdgeList) {
                resultList.add(new DependencyDto(
                        Long.valueOf(i),
                        edge.getRelation().toString(),
                        edge.getGovernor().index(),
                        edge.getGovernor().lemma(),
                        edge.getGovernor().tag(),
                        edge.getDependent().index(),
                        edge.getDependent().lemma(),
                        edge.getDependent().tag(),
                        null));
                i++;
            }
            return resultList;
        } catch (Exception e) {
            log.error("getAndSaveDependency | error: {}", e);
            return null;
        }
    }
}
