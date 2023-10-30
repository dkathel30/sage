package com.example.sage;

public class ChatItem {
    private String prompt;
    private String response;

    public ChatItem() {}  // Default constructor

    public ChatItem(String prompt, String response) {
        this.prompt = prompt;
        this.response = response;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
