package com.hugailei.graduation.corpus.controller;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.domain.StudentDependency;
import com.hugailei.graduation.corpus.dto.DependencyDto;
import com.hugailei.graduation.corpus.dto.StudentTextDto;
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
 * description: 对学生作文进行存储、检索
 * <p/>
 */
@RestController
@RequestMapping("/student/text")
@Slf4j
public class StudentTextController {

    @Autowired
    private StudentTextService studentTextService;

    /**
     * 存储学生作文
     *
     * @param studentTextDto
     * @return
     */
    @PostMapping("")
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
    @PutMapping("")
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
    @GetMapping("")
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
     *  获取篇章并存储一个篇章中的句法依存关系
     *
     * @param textId    作文id
     * @return
     */
    @GetMapping("/dependency")
    @ResponseBody
    public ResponseVO getAndSaveDependency(@RequestParam Long textId) {
        log.info("getAndSaveDependency | request to get syntactic dependency of text");
        List<StudentDependency> result = studentTextService.getAndSaveDependency(textId);
        if (result == null) {
            return ResponseUtil.error();
        }
        return ResponseUtil.success(result);
    }

    /**
     * 获取一段文本中的句法关系
     *
     * @param text
     * @return
     */
    @PostMapping("/dependency")
    @ResponseBody
    public ResponseVO getDependency(@RequestParam String text) {
        log.info("getDependency | request to get dependency");
        List<DependencyDto> dependencyList = studentTextService.getDependency(text);
        if (dependencyList == null) {
            return ResponseUtil.error(CorpusConstant.FAILED, "something wrong with parsing");
        }
        return ResponseUtil.success(dependencyList);
    }
}
