package com.scriptkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scriptkill.common.Result;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.Session;
import com.scriptkill.service.OssService;
import com.scriptkill.service.ScriptService;
import com.scriptkill.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/script")
public class ScriptController {
    @Autowired
    private ScriptService scriptService;

    @Autowired
    private OssService ossService;
    @Autowired
    private SessionService sessionService;

    @GetMapping("/list")
    public Result<List<Script>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer minPlayers,
            @RequestParam(required = false) Integer maxPlayers,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<Script> scripts = scriptService.listScripts(name, type, difficulty, minPlayers, maxPlayers, minPrice, maxPrice);
        return Result.success(scripts);
    }

    @GetMapping("/{id}")
    public Result<Script> getById(@PathVariable Long id) {
        Script script = scriptService.getById(id);
        return Result.success(script);
    }

    @PostMapping
    public Result<?> save(@RequestBody Script script, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        scriptService.save(script);
        return Result.success("保存成功");
    }

    @PutMapping
    public Result<?> update(@RequestBody Script script, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        scriptService.updateById(script);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        QueryWrapper<Session> sessionWrapper = new QueryWrapper<>();
        sessionWrapper.eq("script_id", id);
        long sessionCount = sessionService.count(sessionWrapper);
        if (sessionCount > 0) {
            return Result.error("该剧本下有 " + sessionCount + " 个场次，无法直接删除。请先删除所有关联场次，或改为下架处理");
        }
        Script script = scriptService.getById(id);
        if (script != null && script.getCover() != null) {
            ossService.delete(script.getCover());
        }
        scriptService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        Script script = scriptService.getById(id);
        if (script == null) {
            return Result.error("剧本不存在");
        }
        script.setStatus(status);
        scriptService.updateById(script);
        return Result.success(status == 1 ? "上架成功" : "下架成功");
    }

    /**
     * 上传剧本封面到 MinIO
     */
    @PostMapping("/{id}/cover")
    public Result<String> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        Script script = scriptService.getById(id);
        if (script == null) {
            return Result.error("剧本不存在");
        }
        try {
            // 删除旧封面
            if (script.getCover() != null && !script.getCover().isEmpty()) {
                ossService.delete(script.getCover());
            }

            // 上传新封面
            String coverUrl = ossService.upload(file, "cover");
            script.setCover(coverUrl);
            scriptService.updateById(script);

            return Result.success("封面上传成功", coverUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
