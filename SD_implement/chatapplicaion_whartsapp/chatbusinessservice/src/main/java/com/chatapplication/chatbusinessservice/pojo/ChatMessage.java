package com.chatapplication.chatbusinessservice.pojo;

public class ChatMessage {
    public enum ChatType { INDIVIDUAL, GROUP }

    private String senderId;
    private String recipientId; // Can be a userId OR a groupChatId
    private ChatType chatType;
    private String content;

    public ChatMessage() {}

    public String getSenderId() { return senderId; }
    public void setSenderId(String id) { this.senderId = id; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String id) { this.recipientId = id; }

    public ChatType getChatType() { return chatType; }
    public void setChatType(ChatType type) { this.chatType = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
