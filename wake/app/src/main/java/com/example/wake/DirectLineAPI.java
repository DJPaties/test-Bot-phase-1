package com.example.wake;


import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DirectLineAPI {
    private final String directLineSecret;
    private final String baseUrl = "https://directline.botframework.com/v3/directline";
    private String token;
    private String conversationId;
    private OkHttpClient client = new OkHttpClient();

    public DirectLineAPI(String directLineSecret) {
        this.directLineSecret = directLineSecret;
    }

    public Request.Builder setHeaders() {
        return new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + directLineSecret);}
    public void generateToken() {
        JSONObject json = null;
        try {
            Request request = setHeaders().url(baseUrl + "/tokens/generate").build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            json = new JSONObject(responseBody);

            if (json.has("error")) {
                System.out.println("NO token");
            } else {
                this.token = json.getString("token");
            }
            System.out.println("Response JSON: " + json);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Response JSON: " + json);
        }
    }
    public void startConversation() {
        try {
            RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json"));
            Request request = setHeaders().url(baseUrl + "/conversations").post(requestBody).build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            if (json.has("error")) {
                System.out.println("ConversationId not available [request failed]");
            } else {
                System.out.println("ConversationId available [request succeeded]");
                this.conversationId = json.getString("conversationId");
            }
            // Print the JSON content using toString()
            System.out.println("Response JSON: " + json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String sendMessage(String text) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("locale", "en-EN");
            jsonObject.put("type", "message");
            jsonObject.put("from", new JSONObject().put("id", "user1"));
            jsonObject.put("text", text);
            RequestBody body = RequestBody.create(
                    jsonObject.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = setHeaders().url(baseUrl + "/conversations/" + conversationId + "/activities").post(body).build();

            Response response = client.newCall(request).execute();
            return response.isSuccessful() ? "message sent" : "error contacting bot";
        } catch (Exception e) {
            e.printStackTrace();
            return "error in sending message";
        }}
    public String getMessage() {
        try {
            Request request = setHeaders().url(baseUrl + "/conversations/" + conversationId + "/activities").get().build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject json = new JSONObject(response.body().string());
                return json.getJSONArray("activities").getJSONObject(2).getString("text");
            } else {
                return "error contacting bot for response";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error in getting message";
        }
    }
}

