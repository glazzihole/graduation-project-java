package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.SentenceDao;
import com.hugailei.graduation.corpus.dto.SentenceDto;
import com.hugailei.graduation.corpus.elasticsearch.SentenceElasticSearch;
import com.hugailei.graduation.corpus.service.SentenceService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Autowired
    private SentenceDao sentenceDao;

    @Override
    public List<SentenceDto> sentenceElasticSearch(String keyword, String corpus) {
        try {
            log.info("sentenceElasticSearch | keyword: {}, corpus: {}", keyword, corpus);
            // 查询条件
            QueryBuilder queryBuilder = QueryBuilders.disMaxQuery()
                    .add(QueryBuilders.termQuery("sentence", keyword))
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

    @Override
    public List<SentenceDto> searchSentenceById(List<Long> sentenceIdList) {
        try {
            log.info("searchSentenceById | sentence id list: {}", sentenceIdList.toString());
            List<SentenceDto> resultList = sentenceDao.findAllById(sentenceIdList).stream().map(sentence -> {
                SentenceDto sentenceDto = new SentenceDto();
                BeanUtils.copyProperties(sentence, sentenceDto);
                return sentenceDto;
            }).collect(Collectors.toList());
            return resultList;
        } catch (Exception e) {
            log.error("searchSentenceById | error: {}", e);
           return null;
        }
    }

    @Override
    public List<SentenceDto> filterSentence(String keyword, List<Long> sentenceIdList, String corpus) {
        try {
            log.info("filterSentence | keyword: {}, sentence size: {}, corpus: {}", keyword, sentenceIdList.size(), corpus);
            List<Long> elasticSearchIdList = sentenceElasticSearch(keyword, corpus).stream().map(sentenceDto -> {
                return sentenceDto.getId();
            }).collect(Collectors.toList());
            Set<Long> elasticSearchSentenceIdSet = new LinkedHashSet<>(elasticSearchIdList);
            Set<Long> sentenceIdSet = new LinkedHashSet<>(sentenceIdList);
            // 求两个集合中的并集
            elasticSearchSentenceIdSet.retainAll(sentenceIdSet);
            List<Long> resultSentenceIdList = new ArrayList<>(elasticSearchSentenceIdSet);
            log.info("filterSentence | sentence size after filter: {}", resultSentenceIdList.size());
            List<SentenceDto> resultList = sentenceDao.findAllById(resultSentenceIdList).stream().map(sentence -> {
                SentenceDto sentenceDto = new SentenceDto();
                BeanUtils.copyProperties(sentence, sentenceDto);
                return sentenceDto;
            }).collect(Collectors.toList());
            return resultList;
        } catch (Exception e) {
            log.error("filterSentence | error: {}", e);
            return null;
        }
    }
}
