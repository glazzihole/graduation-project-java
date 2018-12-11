package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dto.SentenceDto;
import com.hugailei.graduation.corpus.elasticsearch.SentenceElasticSearch;
import com.hugailei.graduation.corpus.service.SentenceService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private SentenceElasticSearch sentenceElasticSearch;

    @Override
    public List<SentenceDto> sentenceElasticSearch(String content, String corpus) {
        try {
            log.info("sentenceElasticSearch | content: {}, corpus: {}", content, corpus);
            // 查询条件
            QueryBuilder queryBuilder = QueryBuilders.disMaxQuery()
                    .add(QueryBuilders.termQuery("sentence", content))
                    .add(QueryBuilders.termQuery("corpus", corpus));
            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder)
                    .withFields("id","sentence")
                    .build();

            List<SentenceDto> resultList = sentenceElasticSearch.search(nativeSearchQuery).stream().map(s -> {
                SentenceDto sentenceDto = new SentenceDto();
                sentenceDto.setId(s.getId());
                sentenceDto.setSentence(s.getSentence());
                return sentenceDto;
            }).collect(Collectors.toList());

            return resultList;
        } catch (Exception e) {
            log.error("sentenceElasticSearch | error: {}", e);
            return null;
        }
    }
}
