package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.NgramDto;
import com.hugailei.graduation.corpus.service.NgramService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.NGramRequestHandler;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/26
 * <p>
 * description:
 * </p>
 **/
@RestController
@RequestMapping("/corpus/ngram")
@Slf4j
public class NgramController {

    private Handler handler = new Handler();
    private BlackLabServer blackLabServer = new BlackLabServer();

    @Autowired
    private NgramService ngramService;

    /**
     * 按表达式查询包含指定形式的ngram
     *
     * @param pageable
     * @param request
     * @param response
     * @param corpus    语料库
     * @param patt      CQL表达式
     */
    @GetMapping
    public void searchNgramByPatt(Pageable pageable,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam String corpus,
                                  @RequestParam String patt,
                                  @RequestParam(value = "rank_num", required = false) Integer rankNum) {
        log.info("searchNgramByPatt | request to search ngram in corpus {} by patt: {}", corpus, patt);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new NGramRequestHandler(blackLabServer, request, user, corpus, patt, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }

    /**
     * 查找指定语料库中N为指定值（且为指定主题的）的ngram
     *
     * @param corpus
     * @param nValue
     * @param topic     主题
     * @param rankNum   难度等级
     * @param pageable
     * @param request
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseVO ngramList(@RequestParam String corpus,
                                @RequestParam("n_value") int nValue,
                                @RequestParam(required = false) Integer topic,
                                @RequestParam(value = "rank_num", required = false) Integer rankNum,
                                Pageable pageable,
                                HttpServletRequest request) {
        log.info("ngramList | request to get ngram list of corpus");
        List<NgramDto> result = ngramService.ngramList(corpus, nValue, topic, rankNum, request);
        if (result == null) {
            ResponseUtil.error();
        }
        return ResponseUtil.createPageResponse(result, pageable);
    }
}
