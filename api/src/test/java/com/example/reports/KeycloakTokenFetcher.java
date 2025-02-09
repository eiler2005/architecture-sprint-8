package com.example.reports;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class KeycloakTokenFetcher {

    public static void main(String[] args) throws IOException {
        String token = getAccessToken();
        if (token != null) {
            System.out.println("Access Token: " + token);
        } else {
            System.out.println("Failed to retrieve token.");
        }
    }

    public static String getAccessToken() throws IOException {
        String tokenUrl = "http://localhost:8080/realms/reports-realm/protocol/openid-connect/token";
        String clientId = "reports-api";
        String clientSecret = "oNwoLQdvJAvRcL89SydqCWCe5ry1jMgq";
        String username = "prothetic1";
        String password = "prothetic123";

        String body = "grant_type=password"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&username=" + username
                + "&password=" + password;

        HttpURLConnection conn = (HttpURLConnection) new URL(tokenUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();  // Возвращаем токен в виде строки
            }
        } else {
            System.err.println("Failed to get token. Response Code: " + responseCode);
            return null;
        }
    }
}