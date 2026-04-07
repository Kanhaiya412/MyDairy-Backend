package com.MyFarmerApp.MyFarmer.dto;

public class ChatResponseDto {
    private String reply;

    public ChatResponseDto(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }
}
