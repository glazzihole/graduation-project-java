package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 单词的搭配查询
     *
     * @param collocationDto
     * @return
     */
    @GetMapping("/word")
    @ResponseBody
    public ResponseVO searchCollocationOfWord(@RequestBody @Valid CollocationDto collocationDto) {
        log.info("searchCollocationOfWord | request to search collocation of word, {}", collocationDto.toString());
        List<CollocationDto> result = collocationService.searchCollocationOfWord(collocationDto);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
