package cn.xingyuan.cloud.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author jinwentao
 * @version 1.0
 * @date 2023/9/22 14:29
 */
@Data
@ApiModel("问题请求类")
public class AiFileAnswerDTO {

    @ApiModelProperty("问题内容")
    private String question;

    @ApiModelProperty("文件列表md5")
    private List<String> md5s;

}
