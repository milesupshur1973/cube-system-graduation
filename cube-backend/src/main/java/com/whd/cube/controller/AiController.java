package com.whd.cube.controller;

import com.whd.cube.common.Result;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    // 1. 你的 Token (从 ModelScope 网页复制的)
    private static final String API_KEY = "ms-422b2778-a99b-4927-b9a8-598c3e705c28";

    // 2. ModelScope 的兼容接口地址 (不用改)
    private static final String API_URL = "https://api-inference.modelscope.cn/v1/chat/completions";

    @PostMapping("/chat")
    public Result chat(@RequestBody String question) {
        // 使用 Spring 自带的 RestTemplate，不需要任何额外依赖
        RestTemplate restTemplate = new RestTemplate();

        // A. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        // B. 构建请求体 (完全按照 OpenAI 的标准格式)
        Map<String, Object> body = new HashMap<>();
        // 模型名称：Qwen/Qwen2.5-Coder-32B-Instruct 是代码能力很强的模型，也适合中文对话
        body.put("model", "Qwen/Qwen2.5-Coder-32B-Instruct");
        body.put("stream", false); // 咱们毕设简单点，不用流式，一次性返回

        List<Map<String, String>> messages = new ArrayList<>();

        // --- 系统提示词 (人设) ---
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个魔方赛事专家。请用简短、热情、专业的中文回答用户关于魔方的问题。如果问题与魔方无关，请礼貌拒绝。");
        messages.add(systemMsg);

        // --- 用户问题 ---
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", question); // 前端传来的问题
        messages.add(userMsg);

        body.put("messages", messages);

        // C. 发送 HTTP POST 请求
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            // D. 解析返回的 JSON 结果
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                // 这里的层级是：choices -> [0] -> message -> content
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                return Result.success(content);
            }
            return Result.error("AI 思考失败，没有返回内容");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("连接 AI 服务失败：" + e.getMessage());
        }
    }
}