package cn.itcast.core.controller;

import cn.itcast.core.utils.FastDFSClient;
import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传管理
 */
@RestController
@RequestMapping("/upload")
public class UploadController{

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;    //文件服务地址

    /**
     * 上传图片
     */
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file){
        try {
            String filename = file.getOriginalFilename();
            //上传到文件操作系统
            //创建一个FastDFS客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            //获取扩展名
            String extName = FilenameUtils.getExtension(filename);
            //上传图片
            String path = fastDFSClient.uploadFile(file.getBytes(), extName, null);
            return new Result(true, FILE_SERVER_URL+path);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "上传失败");
        }

    }
}

