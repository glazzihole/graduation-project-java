package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.service.StudentRankWordService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author HU Gailei
 * @date 2019/3/16
 * <p>
 * description: 用于存储学生的各项临时信息
 * </p>
 **/
@RestController
@RequestMapping("/student")
@Slf4j
public class StudentRankWordController {

    @Autowired
    private StudentRankWordService studentRankWordService;

    @PostMapping("/session/rank-word")
    public ResponseVO saveStudentRankWordInSession(@RequestParam("student_id") long studentId,
                                                   @RequestParam("rank_num") int rankNum,
                                                   HttpServletRequest request) {
        log.info("saveStudentRankWordInSession | request to save student's rank words");
        Set<String> result = studentRankWordService.saveStudentRankWordInSession(studentId, rankNum, request);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success();
    }
}
