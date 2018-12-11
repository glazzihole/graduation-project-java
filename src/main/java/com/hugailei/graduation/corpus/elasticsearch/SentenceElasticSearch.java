package com.hugailei.graduation.corpus.elasticsearch;

import com.hugailei.graduation.corpus.domain.Sentence;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * @author HU Gailei
 * @date 2018/12/10
 * <p>
 * description: 句子es检索
 * </p>
 **/
@Component
public interface SentenceElasticSearch extends ElasticsearchRepository<Sentence, Long> {
}
