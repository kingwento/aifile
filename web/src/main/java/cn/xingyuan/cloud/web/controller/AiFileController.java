package cn.xingyuan.cloud.web.controller;

import cn.xingyuan.cloud.model.dto.AiFileAnswerDTO;
import cn.xingyuan.cloud.service.AiFileService;
import cn.xy.domain.ResultDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinwentao
 * @version 1.0
 * @date 2023/9/22 14:18
 */
@RestController
@RequestMapping("aiFile")
@Api(tags = "智能文档")
public class AiFileController {

    @Autowired
    AiFileService service;

    @GetMapping(value = "/list")
    @ApiOperation("获取文件列表")
    public ResultDTO list() {
        return service.list();
    }

    @GetMapping(value = "/getToken")
    @ApiOperation("getToken")
    public String getToken() {
        return service.getToken();
    }

    @PostMapping(value = "/uploadFile")
    @ApiOperation("文件上传")
    public ResultDTO uploadFile(MultipartFile file) {
        return service.uploadFile(file);
    }

    @PostMapping(value = "/getAnswer")
    @ApiOperation("提问")
    public ResultDTO getAnswer(@RequestBody AiFileAnswerDTO dto) {
        return service.getAnswer(dto);
    }

    @GetMapping(value = "/deleteFile")
    @ApiOperation("删除文件")
    public ResultDTO deleteFile(String md5) {
        return service.deleteFile(md5);
    }

}
