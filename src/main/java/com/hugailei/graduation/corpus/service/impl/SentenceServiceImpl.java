package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dto.DependencyDto;
import com.hugailei.graduation.corpus.dto.SentenceDto;
import com.hugailei.graduation.corpus.service.SentenceService;
import com.hugailei.graduation.corpus.util.StanfordDependencyUtil;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
@Service
public class SentenceServiceImpl implements SentenceService  {

    @Override
    public List<SentenceDto> searchSentenceFromDataBase() {
        return null;
    }

    @Override
    public List<DependencyDto> getDependency(String text) {
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
    }
}
