package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dto.NgramDto;
import com.hugailei.graduation.corpus.dto.WordDto;
import com.hugailei.graduation.corpus.service.WordService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.WordRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/9
 * <p>
 * description: 对语料库中的单词进行查询，包括形态查询、词性查询等
 * <p/>
 */
@RestController
@RequestMapping("/corpus/word")
@Slf4j
public class WordController {

    private Handler handler = new Handler();
    private BlackLabServer blackLabServer = new BlackLabServer();

    @Autowired
    private WordService wordService;

    /**
     * 单词——频率表查询
     *
     * @param corpus
     * @param topic
     * @param pageable
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseVO wordList(@RequestParam String corpus,
                               @RequestParam(required = false, defaultValue = "0") int topic,
                               Pageable pageable) {
        log.info("wordList | request to get word list of corpus");
        List<WordDto> result = wordService.wordList(corpus, topic);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }


    /**
     * 单词原型分布查询
     *
     * @param word
     * @param corpus
     * @return
     */
    @GetMapping("/lemma")
    @ResponseBody
    public ResponseVO searchAllLemma(@RequestParam String word,
                                     @RequestParam String corpus) {
        log.info("searchAllLemma | request to search all lemma of word in corpus ");
        List<WordDto> result = wordService.searchAll(word, corpus, CorpusConstant.LEMMA);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 单词词性分布查询
     *
     * @param word
     * @param corpus
     * @return
     */
    @GetMapping("/pos")
    @ResponseBody
    public ResponseVO searchAllPos(@RequestParam String word,
                                   @RequestParam String corpus) {
        log.info("searchAllPos | request to search all pos of word in corpus");
        List<WordDto> result = wordService.searchAll(word, corpus, CorpusConstant.POS);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 单词形态分布查询
     *
     * @param lemma
     * @param corpus
     * @return
     */
    @GetMapping("/form")
    @ResponseBody
    public ResponseVO searchAllForm(@RequestParam String lemma,
                                    @RequestParam String corpus) {
        log.info("searchAllForm | request to search all form of lemma in corpus");
        List<WordDto> result = wordService.searchAll(lemma, corpus, CorpusConstant.FORM);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 查看单词详情，查询单词所有形态、词性、原型，及相关频率信息
     *
     * @param query     关键词
     * @param corpus    语料库
     * @param queryType 关键词的类型：lemma——原型，form——单词（词形）
     * @return
     */
    @GetMapping("/detail")
    @ResponseBody
    public ResponseVO searchWordDetail(@RequestParam String query,
                                       @RequestParam String corpus,
                                       @RequestParam("query_type") String queryType) {
        log.info("searchWordDetail | request to search word detail in corpus");
        List<WordDto> result = wordService.searchDetail(query, corpus, queryType);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 通过CQL等复杂表达式表达式查询单词
     *
     * @param request
     * @param response
     * @param corpus
     * @param patt
     */
    @GetMapping("/word")
    public void searchWord(Pageable pageable,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam String corpus,
                           @RequestParam String patt) {
        log.info("searchWord | request to search word by patt: {}", patt);
        User user = User.loggedIn("admin", "1");
        request.setAttribute( "group", "hit:word:i" );
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new WordRequestHandler(blackLabServer, request, user, corpus, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }

    /**
     * 查询单词在不同语料库中的分布
     *
     * @param type  查询内容的类型，可以为“word”——词形，“lemma”——原型，“pos”——词性
     * @param query 查询内容
     * @return
     */
    @GetMapping("/distribution/corpus")
    @ResponseBody
    public ResponseVO corpusDistribution(@RequestParam String type, @RequestParam String query) {
        log.info("corpusDistribution | request to get corpus distribution");
        List<WordDto> result = wordService.corpusDistribution(query, type);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 查询单词在指定语料库中不同主题中的分布
     *
     * @param type  查询内容的类型，可以为“word”——词形，“lemma”——原型
     * @param query 查询内容
     * @return
     */
    @GetMapping("/distribution/topic")
    @ResponseBody
    public ResponseVO topicDistribution(@RequestParam String type,
                                        @RequestParam String query,
                                        @RequestParam String corpus) {
        log.info("topicDistribution | request to get topic distribution");
        List<WordDto> result = wordService.topicDistribution(query, type, corpus);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
