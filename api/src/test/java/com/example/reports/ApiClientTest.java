package com.example.reports;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApiClientTest {
    public static void main(String[] args) {
        try {
            // Получаем токен
            String token = KeycloakTokenFetcher.getAccessToken();
            if (token == null || token.isEmpty()) {
                System.err.println("❌ Failed to retrieve access token.");
                return;
            }

            // Проверяем, корректен ли токен
            if (!isValidJwt(token)) {
                System.err.println("❌ Received an invalid JWT token. Please check Keycloak settings.");
                return;
            }

            System.out.println("🔑 Access Token: " + token.substring(0, 30) + "..."); // Вывод части токена

            // Запрос к API
            URL url = new URL("http://localhost:8000/reports");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                parseAndPrintJson(response);
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.err.println("⚠️ Unauthorized (401). The token may be expired or invalid.");
                System.err.println("🔄 Try logging in again.");
            } else {
                System.err.println("❌ GET request failed. Response Code: " + responseCode);
                String errorResponse = readResponse(connection);
                System.err.println("⚠️ Error Response: " + errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для проверки JWT токена
    private static boolean isValidJwt(String token) {
        String[] parts = token.split("\\.");
        return parts.length == 3; // JWT состоит из трех частей (header.payload.signature)
    }

    // Метод для чтения ответа API
    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    // Метод для обработки JSON-ответа (обрабатывает массив данных)
    private static void parseAndPrintJson(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            System.out.println("✅ Received " + jsonArray.length() + " reports:");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject reportObj = jsonArray.getJSONObject(i);

                System.out.println("\n📌 Report #" + (i + 1));
                System.out.println("🔹 User ID: " + reportObj.getString("user_id"));
                System.out.println("📄 Report: " + reportObj.getString("report"));

                if (reportObj.has("timestamp")) {
                    System.out.println("⏳ Generated at: " + reportObj.getString("timestamp"));
                }
                if (reportObj.has("status")) {
                    System.out.println("📊 Status: " + reportObj.getString("status"));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to parse JSON response: " + response);
            e.printStackTrace();
        }
    }
}