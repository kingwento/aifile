package cn.xingyuan.cloud.service;

import cn.xingyuan.cloud.model.dto.AiFileAnswerDTO;
import cn.xy.domain.ResultDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinwentao
 * @version 1.0
 * @date 2023/9/22 14:19
 */
public interface AiFileService {

    String getToken();

    ResultDTO list();

    ResultDTO uploadFile(MultipartFile file);

    ResultDTO getAnswer(AiFileAnswerDTO dto);

    ResultDTO deleteFile(String md5);
}
