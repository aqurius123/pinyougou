package cn.itcast.core.controller;

import cn.itcast.core.utils.FastDFSClient;
import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传管理
 */
@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    /**
     * 上传图片
     */
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) throws Exception {
        try {
            //获取文件名称
            String filename = file.getOriginalFilename();
            //获取扩展名
            String exeName = FilenameUtils.getExtension(filename);
            //获取文件系统客户端
            FastDFSClient client = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            String path = client.uploadFile(file.getBytes(), exeName, null);
            return new Result(true, FILE_SERVER_URL+path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }

    }
}
