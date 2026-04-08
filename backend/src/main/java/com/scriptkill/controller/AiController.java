package com.scriptkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scriptkill.common.Result;
import com.scriptkill.entity.Script;
import com.scriptkill.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class AiController {
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekUrl;
    
    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;
    
    @Autowired
    private ScriptService scriptService;
    
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Result.error("问题不能为空");
        }
        
        // 如果未配置API Key，返回友好提示
        if (deepseekApiKey == null || deepseekApiKey.trim().isEmpty()) {
            return Result.error("AI服务暂未配置，请联系管理员");
        }
        
        try {
            // 获取所有上架的剧本
            QueryWrapper<Script> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            List<Script> scripts = scriptService.list(wrapper);
            
            // 转换剧本数据为文本描述
            String scriptListText = scripts.stream()
                .map(s -> String.format("- 【%s】%s | 人数:%d-%d人 | 类型:%s | 难度:%s | 时长:%d分钟 | 价格:¥%.2f",
                    s.getName(),
                    s.getDescription() != null ? s.getDescription().substring(0, Math.min(30, s.getDescription().length())) : "暂无简介",
                    s.getMinPlayers(),
                    s.getMaxPlayers(),
                    s.getType(),
                    s.getDifficulty(),
                    s.getDuration(),
                    s.getPrice()))
                .collect(Collectors.joining("\n"));
            
            if (scriptListText.isEmpty()) {
                scriptListText = "暂无剧本数据";
            }
            
            // 构建请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);
            
            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen-plus");
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统提示 - 包含剧本库信息
            String systemPrompt = "你是一个专业的剧本杀助手，名为'ScriptKiller AI'。\n" +
                "你必须根据用户的需求，从以下剧本库中选择最合适的剧本进行推荐。\n" +
                "重要：只能推荐剧本库中存在的剧本，不要凭空编造！\n\n" +
                "【剧本库】：\n" + scriptListText + "\n\n" +
                "你可以回答：\n" +
                "1. 剧本推荐（根据人数、类型、难度）- 必须从剧本库中选择\n" +
                "2. 剧本攻略和线索分析\n" +
                "3. 剧本杀基础知识\n" +
                "4. 主持技巧\n" +
                "请用友好、专业的语气回答。回答时如果推荐剧本，请列出剧本名称、特点和推荐理由。";
            
            messages.add(Map.of("role", "system", "content", systemPrompt));
            
            // 用户问题
            messages.add(Map.of("role", "user", "content", question));
            
            body.put("messages", messages);
            body.put("max_tokens", 800);
            body.put("temperature", 0.7);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                deepseekUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<?> choices = (List<?>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    String answer = (String) message.get("content");
                    return Result.success(answer);
                }
            }
            
            return Result.error("AI响应异常");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI服务调用失败: " + e.getMessage());
        }
    }
}
