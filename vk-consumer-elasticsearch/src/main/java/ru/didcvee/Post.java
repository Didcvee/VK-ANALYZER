package ru.didcvee;

public class Post {
    String text;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Post(String text) {
        this.text = text;
    }
}
