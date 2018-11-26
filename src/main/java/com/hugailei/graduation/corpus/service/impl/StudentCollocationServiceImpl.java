package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.StudentCollocationService;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hugailei.graduation.corpus.service.impl.CollocationServiceImpl.sortCollocationDtoList;

/**
 * @author HU Gailei
 * @date 2018/11/26
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
public class StudentCollocationServiceImpl implements StudentCollocationService {
    @Override
    public List<CollocationDto> getCollocationInText(String text) {
        try {
            log.info("getCollocationInText | text: {}", text);
            List<SemanticGraphEdge> dependencyList = StanfordParserUtil.getDependency(text);
            Map<String, Integer> key2Freq = new HashMap<>();

            for (SemanticGraphEdge edge : dependencyList) {
                String firstWord, secondWord, firstPos, secondPos;
                // 根据单词在句子中的位置调整在搭配中的先后顺序
                int govIndex = edge.getGovernor().index();
                int depIndex = edge.getDependent().index();

                if (govIndex < depIndex) {
                    firstWord = edge.getGovernor().lemma();
                    secondWord = edge.getDependent().lemma();
                    firstPos = edge.getGovernor().tag();
                    secondPos = edge.getDependent().tag();
                } else {
                    firstWord = edge.getDependent().lemma();
                    secondWord = edge.getGovernor().lemma();
                    firstPos = edge.getDependent().tag();
                    secondPos = edge.getGovernor().tag();
                }

                switch (edge.getRelation().toString()) {
                    case "nsubj": case "top":
                        String regex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                        if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(regex)) {
                            firstWord = edge.getGovernor().lemma();
                            secondWord = edge.getDependent().lemma();
                            firstPos = edge.getGovernor().tag();
                            secondPos = edge.getDependent().tag();
                        }else{
                            firstWord = edge.getDependent().lemma();
                            secondWord = edge.getGovernor().lemma();
                            firstPos = edge.getDependent().tag();
                            secondPos = edge.getGovernor().tag();
                        }
                        break;
                    case "nsubjpass": case "dobj": case "attr": case "iobj":
                        firstWord = edge.getGovernor().lemma();
                        secondWord = edge.getDependent().lemma();
                        firstPos = edge.getGovernor().tag();
                        secondPos = edge.getDependent().tag();
                        break;
                    default:
                        break;
                }

                // 判断搭配形式是否为指定的搭配类型
                for (String regex : CorpusConstant.COLLOCATION_PATT_SET) {
                    // 若满足，放入结果Map中
                    if ((firstPos + "-" + secondPos).matches(regex)) {
                        // 词性同一存储为该词性下原型的词性
                        for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
                            String posRegex = (String) entry.getKey();
                            String lemmaPos = (String) entry.getValue();
                            if (firstPos.matches(posRegex)) {
                                firstPos = lemmaPos;
                                break;
                            }
                        }

                        // 词性同一存储为该词性下原型的词性
                        for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
                            String posRegex = (String) entry.getKey();
                            String lemmaPos = (String) entry.getValue();
                            if (secondPos.matches(posRegex)) {
                                secondPos = lemmaPos;
                                break;
                            }
                        }

                        String key = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();

                        // 更新频次
                        int freq = 1;
                        if (key2Freq.containsKey(key)) {
                            freq = key2Freq.get(key) + 1;
                        }
                        key2Freq.put(key, freq);
                        break;
                    }
                }
            } // for (SemanticGraphEdge edge : dependency.edgeListSorted())

            // 遍历map， 获取搭配频次，填充返回结果
            List<CollocationDto> resultList = new ArrayList<>();
            for (Map.Entry entry : key2Freq.entrySet()) {
                CollocationDto collocationDto = new CollocationDto();
                String key = (String)entry.getKey();
                int freq = (Integer) entry.getValue();
                collocationDto.setFirstWord(key.split("_")[0]);
                collocationDto.setFirstPos(key.split("_")[1].toUpperCase());
                collocationDto.setSecondWord(key.split("_")[2]);
                collocationDto.setSecondPos(key.split("_")[3].toUpperCase());
                collocationDto.setFreq(freq);
                resultList.add(collocationDto);
            }

            sortCollocationDtoList(resultList);
            return resultList;
        } catch (Exception e) {
            log.error("getCollocationInText | error: {}", e);
            return null;
        }
    }
}
