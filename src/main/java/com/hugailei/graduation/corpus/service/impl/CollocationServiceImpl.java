package com.hugailei.graduation.corpus.service.impl;

import com.hugailei.graduation.corpus.dao.CollocationDao;
import com.hugailei.graduation.corpus.domain.Collocation;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
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

    @Override
    public List<CollocationDto> searchCollocationOfWord(CollocationDto collocationDto) {
        try {
            log.info("searchCollocationOfWord | collocationDto:{}", collocationDto.toString());
            Collocation collocation = new Collocation();
            BeanUtils.copyProperties(collocationDto, collocation);
            Example<Collocation> example = Example.of(collocation);
            List<CollocationDto> result = collocationDao.findAll(example).stream().map(coll -> {
                CollocationDto data = new CollocationDto();
                BeanUtils.copyProperties(coll, data);
                return data;
            }).collect(Collectors.toList());
            log.info("searchCollocationOfWord | result size: {}", (result != null ? result.size() : 0));
            return result;
        } catch (Exception e) {
            log.error("searchCollocationOfWord | error: {}", e);
            return null;
        }
    }
}
