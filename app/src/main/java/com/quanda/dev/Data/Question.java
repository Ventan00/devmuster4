package com.quanda.dev.Data;

import android.graphics.Bitmap;

import java.sql.Timestamp;
import java.util.Date;

public class Question {
    private String category, text, senderName;
    private int answersAmount, viewsAmount, qId;
    private Timestamp timestamp;
    private boolean isFinished;
    private Bitmap imgBitMap = null;

    public Question(int qId, String senderName, String timestamp, String category, String text, int answersAmount, int viewsAmount, boolean isFinished){
        this.qId = qId;
        this.senderName = senderName;
        this.timestamp = Timestamp.valueOf(timestamp);
        this.category = category;
        this.text = text;
        this.answersAmount = answersAmount;
        this.viewsAmount = viewsAmount;
        this.isFinished = isFinished;
    }

    public int hoursAgo() {
        Timestamp currTimeStamp = new Timestamp(new Date().getTime());
        long diff = currTimeStamp.getTime() - timestamp.getTime();
        return (int)diff / (60 * 60 * 1000);
    }
    public int minutesAgo() {
        Timestamp currTimeStamp = new Timestamp(new Date().getTime());
        long diff = currTimeStamp.getTime() - timestamp.getTime();
        return (int) ((diff / (1000*60)) % 60);
    }

    public int getQId() {
        return qId;
    }
    public int getAnswersAmount() {
        return answersAmount;
    }
    public String getCategory() {
        return category;
    }
    public String getText() {
        return text;
    }
    public int getViewsAmount() {
        return viewsAmount;
    }
    public String getSenderName() {
        return senderName;
    }
    public boolean isFinished() {
        return isFinished;
    }
    public Bitmap getImgBitMap() {
        return imgBitMap;
    }

    public void setImgBitMap(Bitmap imgBitMap) {
        this.imgBitMap = imgBitMap;
    }

    @Override
    public String toString() {
        return "Question{" +
                "category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", senderName='" + senderName + '\'' +
                ", answersAmount=" + answersAmount +
                ", viewsAmount=" + viewsAmount +
                ", qId=" + qId +
                ", timestamp=" + timestamp +
                ", isFinished=" + isFinished +
                '}';
    }
}
