package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dto.DependencyDto;
import com.hugailei.graduation.corpus.dto.SentenceDto;
import com.hugailei.graduation.corpus.service.SentenceService;
import com.hugailei.graduation.corpus.util.StanfordDependencyUtil;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SentenceServiceImpl implements SentenceService  {

    @Override
    public List<SentenceDto> searchSentenceFromDataBase() {
        return null;
    }
}
