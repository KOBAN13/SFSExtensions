package com.a51integrated.sfs2x.services.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MailService
{
    private final String apiKey;
    private final String apiUrl;

    public MailService(String apiKey, String apiUrl)
    {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String send(String from, String to, String subject, String html) throws IOException, InterruptedException
    {
        ObjectMapper mapper = new ObjectMapper();

        var root = mapper.createObjectNode();
        root.put("from_email", from);
        root.put("to", to);
        root.put("subject", subject);
        root.put("html", html);

        var body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var client = HttpClient.newHttpClient();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
