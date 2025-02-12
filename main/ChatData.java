package com.example.main;


public class ChatData {

    private String msg;
    private String date;
    private String sender;
    private String receiver;

    public String getMsg() { return msg; }

    public void setMsg(String msg) { this.msg = msg; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getSender() { return sender; }

    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }

    public void setReceiver(String receiver) { this.receiver = receiver; }

    public ChatData(String msg, String date, String sender, String receiver) {

        this.msg = msg;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
    }

    public ChatData(){
        super();
    }
}
