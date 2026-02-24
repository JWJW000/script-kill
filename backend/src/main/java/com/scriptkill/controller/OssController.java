package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用文件上传接口（参考若依Plus OSS模块）
 * 统一管理所有文件上传，按 category 分类存储
 */
@RestController
@RequestMapping("/oss")
public class OssController {

    @Autowired
    private OssService ossService;

    /**
     * 通用文件上传
     *
     * @param file     文件
     * @param category 分类：avatar(头像) / cover(封面) / other(其他)
     * @return 包含 url 和 fileName 的结果
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "other") String category) {
        try {
            String url = ossService.upload(file, category);

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("fileName", file.getOriginalFilename());

            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param url 文件URL
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam("url") String url) {
        try {
            ossService.delete(url);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
