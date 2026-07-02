package com.chatapplication.chatbusinessservice.pojo;
import java.util.List;

public class GroupCreationRequest {
    private String groupChatId;
    private List<String> initialMembers;

    public GroupCreationRequest() {}
    public String getGroupChatId() { return groupChatId; }
    public void setGroupChatId(String id) { this.groupChatId = id; }
    public List<String> getInitialMembers() { return initialMembers; }
    public void setInitialMembers(List<String> members) { this.initialMembers = members; }
}