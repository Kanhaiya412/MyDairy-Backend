package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.ChatRequestDto;
import com.MyFarmerApp.MyFarmer.dto.ChatResponseDto;
import com.MyFarmerApp.MyFarmer.service.AiChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ChatResponseDto chat(@RequestBody ChatRequestDto request) {
        String reply = aiChatService.askGroq(request.getMessage());
        return new ChatResponseDto(reply);
    }
}