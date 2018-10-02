package com.hugailei.graduation.corpus.controller;

import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.SentenceRequestHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description: 对句子进行检索
 * </p>
 **/
@RestController
@RequestMapping("/sentence")
public class SentenceController {
    private Handler handler = new Handler();
    private final BlackLabServer blackLabServer = new BlackLabServer();

    @GetMapping("/search")
    public void searchSentence (PageRequest pageRequest,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                String corpus,
                                String patt) {
        RequestHandler requestHandler = new SentenceRequestHandler(blackLabServer, request, null, corpus, null, null);
        handler.checkAndHandler(pageRequest, corpus, null, null, request, response, requestHandler);
    }
}
