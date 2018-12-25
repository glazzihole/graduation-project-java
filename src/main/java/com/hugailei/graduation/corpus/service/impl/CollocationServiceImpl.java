package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.dao.CollocationDao;
import com.hugailei.graduation.corpus.dao.CollocationWithTopicDao;
import com.hugailei.graduation.corpus.dao.WordExtensionDao;
import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.domain.CollocationWithTopic;
import com.hugailei.graduation.corpus.domain.WordExtension;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CollocationServiceImpl implements CollocationService {

    @Autowired
    private CollocationDao collocationDao;

    @Autowired
    private CollocationWithTopicDao collocationWithTopicDao;

    @Autowired
    private WordExtensionDao wordExtensionDao;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<CollocationDto> searchCollocationOfWord(CollocationDto collocationDto) {
        try {
            log.info("searchCollocationOfWord | collocationDto:{}", collocationDto.toString());
            Sort sort = new Sort(Sort.Direction.DESC, "freq");
            List<CollocationDto> resultList = new ArrayList<>();
            CollocationDto temp = new CollocationDto();
            // 不带主题的查询
            if (collocationDto.getTopic() == null ) {
                Collocation collocation = new Collocation();
                BeanUtils.copyProperties(collocationDto, collocation);
                collocationDao.findAll(Example.of(collocation), sort)
                        .forEach(coll -> {
                            BeanUtils.copyProperties(coll, temp);
                            resultList.add(temp);
                        });
            } else {
                // 带主题的查询
                CollocationWithTopic collocationWithTopic = new CollocationWithTopic();
                BeanUtils.copyProperties(collocationDto, collocationWithTopic);
                List<CollocationWithTopic> collocationWithTopicList = collocationWithTopicDao.findAll(Example.of(collocationWithTopic), sort);
                collocationWithTopicList.forEach(coll -> {
                            BeanUtils.copyProperties(coll, temp);
                            resultList.add(temp);
                        });
            }
            log.info("searchCollocationOfWord | result size: {}", (resultList != null ? resultList.size() : 0));
            return resultList;
        } catch (Exception e) {
            log.error("searchCollocationOfWord | error: {}", e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<CollocationDto> searchSynonymousCollocation(CollocationDto collocationDto) {
        try {
            log.info("searchSynonymousCollocation | collocationDto: {}", collocationDto.toString());
            // 对各单词的词性进行基本类型还原
            if (!StringUtils.isBlank(collocationDto.getFirstPos())) {
                String basePos = StanfordParserUtil.getBasePos(collocationDto.getFirstPos());
                collocationDto.setFirstPos(basePos);
            }

            if (!StringUtils.isBlank(collocationDto.getSecondPos())) {
                String basePos = StanfordParserUtil.getBasePos(collocationDto.getSecondPos());
                collocationDto.setSecondPos(basePos);
            }

            if (!StringUtils.isBlank(collocationDto.getThirdPos())) {
                String basePos = StanfordParserUtil.getBasePos(collocationDto.getThirdPos());
                collocationDto.setThirdPos(basePos);
            }

            // 设置查询所需的数据
            WordExtension wordExtension = new WordExtension();
            switch(collocationDto.getPosition()) {
                case 1:
                    if (StringUtils.isBlank(collocationDto.getFirstWord()) ||
                        StringUtils.isBlank(collocationDto.getFirstPos()) ||
                        StringUtils.isBlank(collocationDto.getSecondWord())) {
                        throw new Exception("information for search is not complete");
                    }
                    wordExtension.setWord(collocationDto.getFirstWord());
                    wordExtension.setPos(collocationDto.getFirstPos());
                    break;

                case 2:
                    if (StringUtils.isBlank(collocationDto.getSecondWord()) ||
                        StringUtils.isBlank(collocationDto.getSecondPos()) ||
                        StringUtils.isBlank(collocationDto.getFirstWord())) {
                        throw new Exception("information for search is not complete");
                    }
                    wordExtension.setWord(collocationDto.getSecondWord());
                    wordExtension.setPos(collocationDto.getSecondPos());
                    break;

                case 3:
                    if (StringUtils.isBlank(collocationDto.getThirdPos()) ||
                        StringUtils.isBlank(collocationDto.getThirdWord()) ||
                        StringUtils.isBlank(collocationDto.getFirstWord()) ||
                        StringUtils.isBlank(collocationDto.getSecondWord())) {
                        throw new Exception("information for search is not complete");
                    }
                    wordExtension.setWord(collocationDto.getThirdWord());
                    wordExtension.setPos(collocationDto.getThirdPos());
                    break;

                default:
                    throw new Exception("wrong position num");
            }

            // 查询数据库，获取待检索词在指定词性下的同义词和相似词
            wordExtension.setRelation("syn");
            Example<WordExtension> example = Example.of(wordExtension);
            Optional<WordExtension> tempResult = wordExtensionDao.findOne(example);
            String words = tempResult.isPresent() ? tempResult.get().getResults() : "";

            // 将每个同义词和相似词和原来的搭配词进行组合，进行搭配查询，看是否存在该搭配f，若存在，则放入结果集中
            List<CollocationDto> resultList = new ArrayList<>();
            CollocationWithTopic collocationWithTopic = new CollocationWithTopic();
            BeanUtils.copyProperties(collocationDto, collocationWithTopic);

            for (String word : words.split(",")) {
                if (collocationDto.getPosition() == 1) {
                    collocationWithTopic.setFirstWord(word);
                } else if (collocationDto.getPosition() == 2) {
                    collocationWithTopic.setSecondWord(word);
                } else {
                    collocationWithTopic.setThirdWord(word);
                }

                // 不带主题的查询
                if (collocationDto.getTopic() == null) {
                    Collocation collocation = new Collocation();
                    BeanUtils.copyProperties(collocationWithTopic, collocation);
                    Example<Collocation> collocationExample = Example.of(collocation);

                    List<Collocation> collocationList = collocationDao.findAll(collocationExample);
                    if (collocationList.size() >= 1) {
                        CollocationDto result = new CollocationDto();
                        collocationList.forEach(coll -> {
                            BeanUtils.copyProperties(coll, result);
                            resultList.add(result);
                        });
                    }
                }
                // 带主题的查询
                else {
                    List<CollocationWithTopic> collocationWithTopicList = collocationWithTopicDao.findAll(Example.of(collocationWithTopic));
                    if (!collocationWithTopicList.isEmpty()) {
                        CollocationDto result = new CollocationDto();
                        collocationWithTopicList.forEach(coll -> {
                            BeanUtils.copyProperties(coll, result);
                            resultList.add(result);
                        });
                    }
                }
            }
            sortCollocationDtoList(resultList);
            log.info("searchSynonymousCollocation | result size: {}", (resultList != null ? resultList.size() : 0));
            return resultList;

        } catch (Exception e) {
            log.error("searchSynonymousCollocation | error: {}", e);
            return null;
        }
    }

    /**
     * 对collocationDto的列表进行降序排序
     *
     * @param collocationDtoList
     */
    static void sortCollocationDtoList(List<CollocationDto> collocationDtoList) {
        Collections.sort(collocationDtoList, new Comparator<CollocationDto>() {
            @Override
            public int compare(CollocationDto c1, CollocationDto c2) {
                return c2.getFreq() - c1.getFreq();
            }
        });
    }
}
