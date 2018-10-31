package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.WordService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/27
 * <p>
 * description: 查询单词在不同语料库中的分布
 * </p>
 **/
@RestController
@Slf4j
@RequestMapping("/corpus/corpus")
public class CorpusController {
    @Autowired
    private WordService wordService;

    /**
     * 查询单词在不同语料库中的分布
     *
     * @param type      查询内容的类型，可以为“word”——词形，“lemma”——原型，“pos”——词性
     * @param query
     * @return
     */
    @GetMapping("/distribution")
    @ResponseBody
    public ResponseVO distribution(@RequestParam String type, @RequestParam String query) {
        log.info("distribution | request to get distribution, query:{}, type: {}", query, type);
        List<WordDto> result = wordService.searchCorpus(query, type);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
