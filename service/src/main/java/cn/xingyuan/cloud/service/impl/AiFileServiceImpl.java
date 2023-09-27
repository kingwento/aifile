package cn.xingyuan.cloud.service.impl;

import cn.xingyuan.cloud.model.dto.AiFileAnswerDTO;
import cn.xingyuan.cloud.model.utils.HttpUtils;
import cn.xingyuan.cloud.model.utils.RedisUtil;
import cn.xingyuan.cloud.model.utils.Response;
import cn.xingyuan.cloud.service.AiFileService;
import cn.xy.domain.ResultDTO;
import cn.xy.enums.CommonErrorEnum;
import cn.xy.enums.SuccessMsgEnum;
import cn.xy.utils.ResultTools;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jinwenutilstao
 * @version 1.0
 * @date 2023/9/22 14:20
 */
@Service
public class AiFileServiceImpl implements AiFileService {
    @Value("${foxit.host}")
    private String host;

    @Value("${foxit.account}")
    private String account;

    @Value("${foxit.password}")
    private String password;

    @Value("${file.upload.path}")
    private String filePath;

    @Resource
    private RedisUtil redisUtils;

    @Override
    public String getToken() {
        Response response = new Response();
        String url = "/api/user/sign-in-multi";
        Map<String, String> headers = new HashMap<>();

        JSONObject params =  new JSONObject();
        params.put("account", account);
        params.put("password", password);
        String path = host+url;
        Map<String, String> querys = new HashMap<>();
        try {
            response = HttpUtils.httpsPost(path,3000, headers,querys,params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if(jsonObject.getString("code").equals("100000")){
            JSONObject object = (JSONObject)jsonObject.get("data");
            JSONObject data = JSONObject.parseObject(object.toJSONString());
            String access_token = data.getString("access_token");
            redisUtils.setToString("token",access_token);
            return access_token;
        }

        return null;
    }

    @Override
    public ResultDTO list() {
        ResultDTO resultDTO = new ResultDTO<>();
        String token = redisUtils.getToString("token").toString();
        if(token == null){
            token = getToken();
        }

        String url = "/openai/get_documents?token="+token+"&page=1&page_size=100";
        Map<String, String> headers = new HashMap<>();
        Response response = new Response();
        Map<String, String> params = new HashMap<>();
        response = HttpUtils.httpsGet(host, url, 3000, headers, params);
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        if(jsonObject.getString("code").equals("200")){
            JSONObject object = (JSONObject)jsonObject.get("data");
            JSONObject data = JSONObject.parseObject(object.toJSONString());
            JSONArray list = data.getJSONArray("list");
            resultDTO.setResult(list);
            return resultDTO;
        } else if(jsonObject.getString("msg").equals("TOKEN_ERROR")){
            token = getToken();
            url = "/openai/get_documents?token="+token+"&page=1&page_size=100";
            response = HttpUtils.httpsGet(host, url, 3000, headers, params);
            jsonObject = JSONObject.parseObject(response.getBody());
            if(jsonObject.getString("code").equals("200")){
                JSONObject object = (JSONObject)jsonObject.get("data");
                JSONObject data = JSONObject.parseObject(object.toJSONString());
                JSONArray list = data.getJSONArray("list");
                resultDTO.setResult(list);
                return resultDTO;
            } else {
                return ResultTools.buildFailure("101012",jsonObject.getString("msg"));
            }
        } else {
            return ResultTools.buildFailure("101012",jsonObject.getString("msg"));
        }
    }

    @Override
    public ResultDTO uploadFile(MultipartFile file) {

        if(file == null){
            return ResultTools.buildFailure("101012","请上传文件！");
        }

        String token = redisUtils.getToString("token").toString();
        if(token == null){
            token = getToken();
        }

        String url = "/openai/load_documents?token="+token;
        String path = host+url;

        // 创建 HTTP POST 请求
        HttpPost post = new HttpPost(path);
        ContentType contentType = ContentType.create("multipart/form-data", Charset.forName("UTF-8"));
        // 创建 HTTP 客户端
        HttpClient client = HttpClientBuilder.create().build();
        // 构造表单数据,解决中文乱码问题
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532)
                .setCharset(Charset.forName("UTF-8"))
                .setContentType(contentType);
        String originalFilename = file.getOriginalFilename();
        String responses = "";
        try {
            File tmpFile = new File(filePath + originalFilename);
            // 写入临时文件
            if (!tmpFile.getParentFile().exists()) {
                tmpFile.getParentFile().mkdirs();
            }
            file.transferTo(tmpFile);
            builder.addPart("file", new FileBody(tmpFile));
            // 设置请求实体
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            // 执行请求并获取响应
            responses = EntityUtils.toString(client.execute(post).getEntity());
            System.out.println(responses);
            tmpFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(responses);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray error_load_files = data.getJSONArray("error_load_files");
        if(error_load_files.size()>0){
            JSONObject error_load_file = (JSONObject)error_load_files.get(0);
            return ResultTools.buildFailure("101012","文件："+error_load_file.getString("filename")+"上传失败！"+"原因："+error_load_file.getString("msg"));
        }
        if(jsonObject == null || !jsonObject.getString("code").equals("200")){
            return ResultTools.buildFailure(CommonErrorEnum.FAILURE);
        }
        return ResultTools.buildSuccess(SuccessMsgEnum.SUCCESS);
    }

    @Override
    public ResultDTO getAnswer(AiFileAnswerDTO dto) {
        String token = redisUtils.getToString("token").toString();
        if(token == null){
            token = getToken();
        }

        Response response = new Response();
        String url = "/openai/get_answer?token="+token;
        Map<String, String> headers = new HashMap<>();

        //设置请求参数
        JSONObject params =  new JSONObject();
        params.put("question",dto.getQuestion());
        params.put("md5s",dto.getMd5s());

        String path = host+url;
        Map<String, String> querys = new HashMap<>();
        try {
            response = HttpUtils.httpsPost(path,60000, headers,querys,JSONObject.toJSONString(params));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultTools.buildFailure(CommonErrorEnum.FAILURE);
        }
        String body = response.getBody();
        if(body != null){
            String[] split = body.split("\\n\\n");
            StringBuffer stringBuffer = new StringBuffer();
            for (String data:split) {
                int i = data.indexOf(":");
                String substring = data.substring(i+1);
                JSONObject jsonObject = JSONObject.parseObject(substring);
                JSONObject data1 = jsonObject.getJSONObject("data");
                stringBuffer.append(data1.getString("answer"));
            }
            return ResultTools.buildSuccess(stringBuffer.toString());
        } else {
            return ResultTools.buildFailure(CommonErrorEnum.FAILURE);
        }
    }

    @Override
    public ResultDTO deleteFile(String md5s) {
        String[] split = md5s.split(",");
        for (String md5:split) {
            String token = redisUtils.getToString("token").toString();
            if(token == null){
                token = getToken();
            }

            Response response = new Response();
            String url = "/openai/document?token="+token+"&md5="+md5;
            Map<String, String> headers = new HashMap<>();
            Map<String, String> params = new HashMap<>();
            response = HttpUtils.httpsDelete(host, url, 3000, headers, params);
            JSONObject jsonObject = JSONObject.parseObject(response.getBody());
            JSONObject body = jsonObject.getJSONObject("body");
            if(body != null && !body.getString("code").equals("200")){
                return ResultTools.buildFailure(CommonErrorEnum.FAILURE);
            }
            return ResultTools.buildSuccess(SuccessMsgEnum.SUCCESS);
        }
        return ResultTools.buildSuccess(SuccessMsgEnum.SUCCESS);
    }
}
