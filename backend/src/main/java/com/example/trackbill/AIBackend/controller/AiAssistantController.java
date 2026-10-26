package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.AiChatRequest;
import com.example.trackbill.AIBackend.dto.AiChatResponse;
import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiAssistantService.chat(request);
        return ResponseEntity.ok(ApiResponse.ok("AI response generated", response));
    }
}
