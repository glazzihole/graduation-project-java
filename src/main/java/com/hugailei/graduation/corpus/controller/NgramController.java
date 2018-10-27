package com.hugailei.graduation.corpus.controller;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.NGramRequestHandler;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.SentenceRequestHandler;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * 按表达式查询包含指定形式的ngram
     *
     * @param pageable
     * @param request
     * @param response
     * @param corpus    语料库
     * @param patt      CQL表达式
     */
    @GetMapping("/search")
    public void searchNgramByPatt(Pageable pageable,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam String corpus,
                                  @RequestParam String patt) {
        log.info("searchNgramByPatt | request to search ngram in corpus {} by patt: {}", corpus, patt);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new NGramRequestHandler(blackLabServer, request, user, corpus, patt, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }
}
