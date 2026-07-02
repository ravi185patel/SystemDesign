package com.chatapplication.chatbusinessservice.pojo;

public class GroupMemberRequest {
    private String groupChatId;
    private String userId;

    public GroupMemberRequest() {}
    public String getGroupChatId() { return groupChatId; }
    public void setGroupChatId(String id) { this.groupChatId = id; }
    public String getUserId() { return userId; }
    public void setUserId(String id) { this.userId = id; }
}