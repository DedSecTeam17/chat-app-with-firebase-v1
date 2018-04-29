package com.example.mohamed.chatapp;

/**
 * Created by mohamed on 4/15/2018.
 */

public class Message {

    private  String message;
    private  String seen;
    private  String time;
    private  String type;
    private  String from;
    private String thumb_image_for_current_user;
    private String thumb_image_for_chat_user;

    public String getThumb_image_for_current_user() {
        return thumb_image_for_current_user;
    }

    public void setThumb_image_for_current_user(String thumb_image_for_current_user) {
        this.thumb_image_for_current_user = thumb_image_for_current_user;
    }

    public String getThumb_image_for_chat_user() {
        return thumb_image_for_chat_user;
    }

    public void setThumb_image_for_chat_user(String thumb_image_for_chat_user) {
        this.thumb_image_for_chat_user = thumb_image_for_chat_user;
    }

    public Message(String message, String seen, String time, String type, String from, String thumb_image_for_current_user, String thumb_image_for_chat_user) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;
        this.thumb_image_for_current_user = thumb_image_for_current_user;
        this.thumb_image_for_chat_user = thumb_image_for_chat_user;
    }

    public String getFrom() {

        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Message() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
