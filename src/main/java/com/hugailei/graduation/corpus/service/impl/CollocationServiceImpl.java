package com.hugailei.graduation.corpus.service.impl;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dao.CollocationDao;
import com.hugailei.graduation.corpus.dao.WordExtensionDao;
import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.domain.WordExtension;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description:
 * </p>
 **/
@Service
@Slf4j
public class CollocationServiceImpl implements CollocationService {

    @Autowired
    private CollocationDao collocationDao;

    @Autowired
    private WordExtensionDao wordExtensionDao;

    @Override
    public List<CollocationDto> searchCollocationOfWord(CollocationDto collocationDto) {
        try {
            log.info("searchCollocationOfWord | collocationDto:{}", collocationDto.toString());
            Collocation collocation = new Collocation();
            BeanUtils.copyProperties(collocationDto, collocation);
            Example<Collocation> example = Example.of(collocation);
            Sort sort = new Sort(Sort.Direction.DESC, "freq");
            List<CollocationDto> resultList = collocationDao.findAll(example, sort).stream().map(coll -> {
                CollocationDto data = new CollocationDto();
                BeanUtils.copyProperties(coll, data);
                return data;
            }).collect(Collectors.toList());

            log.info("searchCollocationOfWord | result size: {}", (resultList != null ? resultList.size() : 0));
            return resultList;
        } catch (Exception e) {
            log.error("searchCollocationOfWord | error: {}", e);
            return null;
        }
    }

    @Override
    public List<CollocationDto> searchSynonymousCollocation(CollocationDto collocationDto) {
        try {
            log.info("searchSynonymousCollocation | collocationDto: {}", collocationDto.toString());
            // 对各单词的词性进行基本类型还原
            if (!StringUtils.isBlank(collocationDto.getFirstPos())) {
                String basePos = getBasePos(collocationDto.getFirstPos());
                collocationDto.setFirstPos(basePos);
            }

            if (!StringUtils.isBlank(collocationDto.getSecondPos())) {
                String basePos = getBasePos(collocationDto.getSecondPos());
                collocationDto.setSecondPos(basePos);
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
                default:
                    throw new Exception("wrong position num");
            }

            // 查询数据库，获取待检索词在指定词性下的同义词和相似词
            wordExtension.setRelation("syn");
            Example<WordExtension> example = Example.of(wordExtension);
            Optional<WordExtension> tempResult = wordExtensionDao.findOne(example);
            String words = tempResult.isPresent() ? tempResult.get().getResults() : "";

            wordExtension.setRelation("sim");
            example = Example.of(wordExtension);
            tempResult = wordExtensionDao.findOne(example);
            words += tempResult.isPresent() ? tempResult.get().getResults() : "";

            // 将每个同义词和相似词和原来的搭配词进行组合，进行搭配查询，看是否存在该搭配f，若存在，则放入结果集中
            Collocation collocation = new Collocation();
            BeanUtils.copyProperties(collocationDto, collocation);
            List<CollocationDto> resultList = new ArrayList<>();
            for (String word : words.split(",")) {
                if (collocationDto.getPosition() == 1) {
                    collocation.setFirstWord(word);
                } else {
                    collocation.setSecondWord(word);
                }
                Example<Collocation> collocationExample = Example.of(collocation);

                List<Collocation> collocationList = collocationDao.findAll(collocationExample);
                if (collocationList.size() >= 1) {
                    CollocationDto result = new CollocationDto();
                    collocationList.forEach(coll->{
                        BeanUtils.copyProperties(coll, result);
                        resultList.add(result);
                    });
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
     * 获取基本词性，如VBZ还原为VB
     * @param pos
     * @return
     */
    private String getBasePos(String pos) {
        for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
            String posRegex = (String) entry.getKey();
            String basePos = (String) entry.getValue();
            if (pos.matches(posRegex)) {
                return basePos;
            }
        }

        return pos;
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
