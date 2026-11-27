package com.example.medibook.models;

/** chats/{chatId}/messages/{messageId}: text, an image, or both. */
public class Message {
    private String messageId;
    private String senderId;
    private String text;
    private String imageUrl;
    private long createdAt;

    public Message() {} // Firestore

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
