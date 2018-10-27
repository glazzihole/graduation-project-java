package com.hugailei.graduation.corpus.controller;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.RequestHandlerHits;
import org.apache.commons.lang.StringUtils;
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
 * description: KWIC
 * </p>
 **/
@RequestMapping("/corpus")
@RestController
@Slf4j
public class ConcordanceController {
    private Handler handler = new Handler();
    private BlackLabServer blackLabServer = new BlackLabServer();

    /**
     * Concordance功能，并支持排序
     * @author HU Gailei
     * @param pageable
     * @param request
     * @param response
     * @param corpus
     * @param patt
     *          要查找的单词
     * @param level1
     *          一级排序  可选"H*","L*","R*","E*",*为数字
     * @param level2
     *          二级排序 可选"H*","L*","R*","E*",*为数字
     * @param level3
     *          三级排序 可选"H*","L*","R*","E*",*为数字
     * @param type
     *          按什么排序  "word"——词形；"pos"——词性；"lemma"——原型的词形
     *
     * <pre>
     * 例：查询"break"的例句，并按break单词的左边第一个单词，第二个单词，第三个单词依次排序，排序时不区分单词大小写
     * http://localhost:8080/corpus/corpus/concordance?index_name=fzzw&patt=%22break%22&type=word&level1=L1&level2=L2&level3=L3&sensitive=false&pageSize=100&pageNo=5
     * </pre>
     */
    @GetMapping("/concordance")
    public void concordance(Pageable pageable,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam String corpus,
                            @RequestParam String patt,
                            @RequestParam String level1,
                            @RequestParam String level2,
                            @RequestParam String level3,
                            @RequestParam(value = "type", defaultValue = "word") String type) {
        String sort = "context:" + type + ":i" + ":" + level1 + ";" + level2 + ";" + level3;
        log.info("searchSentence | request to search concordance sort by {}", sort);
        request.setAttribute("sort", sort);
        User user = User.loggedIn("admin", "1");
        handler.checkConfig(request, response, blackLabServer);
        RequestHandler requestHandler = new RequestHandlerHits(blackLabServer, request, user, corpus, null, null);
        handler.checkAndHandler(pageable, corpus, blackLabServer, request, response, requestHandler);
    }
}
