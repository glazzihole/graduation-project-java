package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
     * 单词在语料库中的搭配词查询
     *
     * @param collocationDto
     * @param pageable
     * @param request
     * @return
     */
    @PostMapping
    @ResponseBody
    public ResponseVO searchCollocationOfWord(@RequestBody @Valid CollocationDto collocationDto,
                                              Pageable pageable,
                                              HttpServletRequest request) {
        log.info("searchCollocationOfWord | request to search collocation of word");
        List<CollocationDto> result = collocationService.searchCollocationOfWord(collocationDto, request);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }

    /**
     * 获取同义搭配
     *
     * @param collocationDto
     * @param request
     * @return
     */
    @PostMapping("/synonym")
    @ResponseBody
    public ResponseVO searchSynonymousCollocation(@RequestBody @Valid CollocationDto collocationDto,
                                                  HttpServletRequest request) {
        log.info("searchSynonymousCollocation | request to search synonymous collocation");
        List<CollocationDto> result = collocationService.searchSynonymousCollocation(collocationDto, request);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);

    }

    /**
     * 查询搭配词典及语料库，判断词对是否为正确、常用搭配
     *
     * @param wordPair
     * @return
     */
    @GetMapping("/check")
    @ResponseBody
    public ResponseVO checkCollocation(@RequestParam("word_pair") String wordPair) {
        log.info("checkCollocation | request to check collocation");
        Boolean result = collocationService.checkCollocation(wordPair);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 查询搭配词典及语料库，判断多个词对是否为正确、常用搭配
     *
     * @param wordPairList
     * @return
     */
    @PostMapping("/check")
    @ResponseBody
    public ResponseVO checkCollocationList(@RequestParam("word_pair_list") String wordPairList) {
        log.info("checkCollocationList | request to check collocation list");
        List<Boolean> result = collocationService.checkCollocationList(wordPairList);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 同义搭配词推荐
     *
     * @param wordPair
     * @param posPair
     * @param rankNum
     * @param request
     * @return
     */
    @GetMapping("/synonym/recommend")
    @ResponseBody
    public ResponseVO recommendSynonym(@RequestParam("word_pair") String wordPair,
                                       @RequestParam("pos_pair") String posPair,
                                       @RequestParam(value = "rank_num", required = false) Integer rankNum,
                                       @RequestParam("topic") Integer topic,
                                       HttpServletRequest request) {
        log.info("recommendSynonym | request to recommend synonym collocation");
        List<CollocationDto> result = collocationService.recommendSynonym(wordPair, posPair, rankNum, topic, request);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 查询词典中该单词的搭配信息
     *
     * @param word
     * @param rankNum
     * @return
     */
    @GetMapping("/dict")
    @ResponseBody
    public ResponseVO searchCollocationInDict(@RequestParam String word,
                                              @RequestParam(value = "rank_num", required = false) Integer rankNum) {
        log.info("searchDict | request to search collocation in dict");
        List<CollocationDto.CollocationDictInfo> result = collocationService.searchCollocationInDict(word, rankNum);
        if (result == null) {
            ResponseUtil.error();
        }
        if (rankNum == null) {
            return ResponseUtil.success(result);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 搭配的主题分布查询
     *
     * @param word_pair
     * @param corpus
     * @return
     */
    @GetMapping("/distribution/topic")
    @ResponseBody
    public ResponseVO topicDistribution(@RequestParam String word_pair,
                                        @RequestParam String corpus) {
        log.info("topicDistribution | request to get topic distribution of collocation");
        List<CollocationDto> result = collocationService.topicDistribution(word_pair, corpus);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 搭配的语体（语料库）分布查询
     *
     * @param word_pair
     * @return
     */
    @GetMapping("/distribution/corpus")
    @ResponseBody
    public ResponseVO corpusDistribution(@RequestParam String word_pair) {
        log.info("topicDistribution | request to get corpus distribution of collocation");
        List<CollocationDto> result = collocationService.corpusDistribution(word_pair);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 获取文本中的所有搭配
     *
     * @param text
     * @return
     */
    @PostMapping("/text-analysis")
    @ResponseBody
    public ResponseVO getCollocationInSentence(@RequestParam String text) {
        log.info("getCollocationInSentence | request to get collocation in sentence");
        text = text
                .replaceAll(CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL, "")
                .replaceAll(CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL, "");
        CollocationDto.CollocationInfo result = collocationService.getCollocationInText(text);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
