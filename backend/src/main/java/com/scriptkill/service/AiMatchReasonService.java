package com.scriptkill.service;

import com.scriptkill.entity.Script;
import com.scriptkill.entity.TeamUp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用大语言模型生成 AI 拼场推荐原因（简短中文一句话）。
 */
@Service
public class AiMatchReasonService {

    @Value("${ai.match.api-key:sk-3178dbb622084469aaee537a8a5d4771}")
    private String apiKey;

    /**
     * 兼容 OpenAI / 兼容接口，可根据需要在配置中修改。
     */
    @Value("${ai.match.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String apiUrl;

    /**
     * 可在配置中指定模型名称，默认一个轻量模型。
     */
    @Value("${ai.match.model:qwen3.5-plus}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用大模型生成简短推荐原因。
     * 若 API Key 未配置或调用失败，返回 null 交给上层做兜底。
     */
    public String generateReason(Long userId, Script script, TeamUp teamUp, int remaining) {
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一个剧本杀拼场推荐助手，请用简短的一句中文解释：");
            prompt.append("为什么要向用户推荐这个拼场。");
            prompt.append("要求：不超过20个汉字，语气友好口语化，不要出现“因为”“推荐”这些字眼。");
            prompt.append("\n剧本名称：").append(script != null ? script.getName() : "未知剧本");
            prompt.append("\n剧本类型：").append(script != null ? script.getType() : "未知类型");
            prompt.append("\n难度：").append(script != null ? script.getDifficulty() : "未知难度");
            prompt.append("\n预计开场时间：").append(teamUp.getExpectedTime());
            prompt.append("\n总人数：").append(teamUp.getTotalPlayers());
            prompt.append("\n当前人数：").append(teamUp.getCurrentPlayers());
            prompt.append("\n剩余名额：").append(remaining);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            // 强制使用非流式返回，避免流式接口导致 RestTemplate 无法解析
            body.put("stream", false);
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt.toString());
            body.put("messages", List.of(msg));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> respBody = resp.getBody();
            if (respBody == null){
                return null;
            }
            Object choicesObj = respBody.get("choices");
            if (!(choicesObj instanceof List)) {
                return null;
            }
            List<?> choices = (List<?>) choicesObj;
            if (choices.isEmpty()) {
                return null;
            }
            Object first = choices.get(0);
            if (!(first instanceof Map)) {
                return null;
            }
            Map<?, ?> firstMap = (Map<?, ?>) first;

            // 1) 优先兼容 Chat Completions: choices[0].message.content
            Object messageObj = firstMap.get("message");
            String content = null;
            if (messageObj instanceof Map) {
                Map<?, ?> message = (Map<?, ?>) messageObj;
                Object contentObj = message.get("content");
                if (contentObj instanceof String) {
                    content = ((String) contentObj).trim();
                } else if (contentObj instanceof List) {
                    // 有些实现会把 content 拆成多段
                    StringBuilder sb = new StringBuilder();
                    for (Object c : (List<?>) contentObj) {
                        if (c instanceof String) {
                            sb.append((String) c);
                        } else if (c instanceof Map) {
                            Object t = ((Map<?, ?>) c).get("text");
                            if (t instanceof String){
                                sb.append((String) t);
                            }
                        }
                    }
                    content = sb.toString().trim();
                }
            }

            // 2) 兼容老的 text completions: choices[0].text
            if ((content == null || content.isEmpty()) && firstMap.get("text") instanceof String) {
                content = ((String) firstMap.get("text")).trim();
            }

            return (content != null && !content.isEmpty()) ? content : null;
        } catch (Exception e) {
            // 打印一次错误，方便你在控制台看到是否真正发起了调用
            System.err.println("[AiMatchReasonService] 调用大模型接口失败：" + e.getMessage());
            return null;
        }
    }
}

