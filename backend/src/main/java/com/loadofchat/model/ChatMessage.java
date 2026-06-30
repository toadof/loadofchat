package com.loadofchat.model;

/**
 * WebSocket message payload broadcast to room subscribers.
 */
public class ChatMessage {

    public enum MessageType {
        JOIN, CHAT, LEAVE, PARTICIPANT_COUNT
    }

    private MessageType type;
    private String sender;
    private String content;
    private String timestamp;
    private Integer participantCount;

    public ChatMessage() {
    }

    public ChatMessage(MessageType type, String sender, String content, String timestamp) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static ChatMessage participantCount(int count) {
        ChatMessage msg = new ChatMessage();
        msg.type = MessageType.PARTICIPANT_COUNT;
        msg.participantCount = count;
        msg.timestamp = java.time.Instant.now().toString();
        return msg;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
}
