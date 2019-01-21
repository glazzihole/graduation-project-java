package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.dto.CollocationDto;
import com.hugailei.graduation.corpus.dto.SentencePatternDto;
import com.hugailei.graduation.corpus.dto.StudentTextDto;
import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.service.CollocationService;
import com.hugailei.graduation.corpus.service.StudentTextService;
import com.hugailei.graduation.corpus.util.ResponseUtil;
import com.hugailei.graduation.corpus.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/10/6
 * <p>
 * description: 对学生作文进行分析，存储、检索
 * <p/>
 */
@RestController
@RequestMapping("/student/text")
@Slf4j
public class StudentTextController {

    @Autowired
    private StudentTextService studentTextService;

    @Autowired
    private CollocationService collocationService;

    /**
     * 存储学生作文
     *
     * @param studentTextDto
     * @return
     */
    @PostMapping
    @ResponseBody
    public ResponseVO saveStudentText(@RequestBody StudentTextDto studentTextDto) {
        log.info("saveStudentText | request to save student text");
        StudentTextDto result = studentTextService.insertText(studentTextDto);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 更新学生作文
     *
     * @param studentTextDto
     * @return
     */
    @PutMapping
    @ResponseBody
    public ResponseVO updateStudentText(@RequestBody StudentTextDto studentTextDto) {
        log.info("saveStudentText | request to update student text");
        if (studentTextDto.getId() == null) {
            log.error("saveStudentText | id is required");
            return ResponseUtil.error();
        }
        StudentTextDto result = studentTextService.insertText(studentTextDto);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 获取学生作文
     *
     * @param textId
     * @return
     */
    @GetMapping
    @ResponseBody
    public ResponseVO getStudentText(@RequestParam Long textId) {
        log.info("getStudentText | request to get student text by id: {}", textId);
        StudentTextDto result = studentTextService.getStudentText(textId);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 获取学生作文中的所有搭配
     *
     * @param text
     * @return
     */
    @PostMapping("/collocation")
    @ResponseBody
    public ResponseVO getCollocationInText(@RequestParam String text) {
        log.info("getCollocationInText | request to get collocation in student's text");
        CollocationDto.CollocationInfo result = collocationService.getCollocationInText(text);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 获取学生作文中的所有句型
     *
     * @param text
     * @return
     */
    @PostMapping("/sentence-pattern")
    @ResponseBody
    public ResponseVO getSentencePatternInText(@RequestParam String text) {
        log.info("getSentencePatternInText | request to get sentence pattern in student's text");
        List<SentencePatternDto> result = studentTextService.getSentencePatternInText(text);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    @PostMapping("/topic")
    @ResponseBody
    public ResponseVO getTextTopic(@RequestParam String text) {
        log.info("getTextTopic | request to get topic of student's text");
        List<TopicDto> result = studentTextService.getTopic(text);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }
}
