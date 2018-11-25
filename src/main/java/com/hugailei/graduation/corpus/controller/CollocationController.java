package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description: 搭配查询相关接口
 * </p>
 **/
@RestController
@RequestMapping("/corpus/collocation")
@Slf4j
public class CollocationController {


    @Autowired
    private CollocationService collocationService;

    /**
     * 单词的搭配词查询
     *
     * @param collocationDto
     * @return
     */
    @PostMapping("/word")
    @ResponseBody
    public ResponseVO searchCollocationOfWord(@RequestBody @Valid CollocationDto collocationDto,
                                              Pageable pageable) {
        log.info("searchCollocationOfWord | request to search collocation of word, {}", collocationDto.toString());
        List<CollocationDto> result = collocationService.searchCollocationOfWord(collocationDto);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }

    /**
     * 获取同义搭配
     *
     * @param collocationDto
     * @return
     */
    @PostMapping("/synonym")
    @ResponseBody
    public ResponseVO searchSynonymousCollocation(@RequestBody @Valid CollocationDto collocationDto) {
        log.info("searchSynonymousCollocation | request to search synonymous collocation");
        List<CollocationDto> result = collocationService.searchSynonymousCollocation(collocationDto);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
