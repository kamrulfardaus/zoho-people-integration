package com.zoho.getemp.service;

import com.zoho.getemp.dto.ZohoTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;

@Service
public class ZohoAuthService {

    @Value("${zoho.client.id}")
    private String clientId;

    @Value("${zoho.client.secret}")
    private String clientSecret;

    @Value("${zoho.refresh.token}")
    private String refreshToken;

    @Value("${zoho.token.url}")
    private String tokenUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔑 In-memory cache
    private String accessToken;
    private Instant expiryTime;

    // refresh 1 minute before expiry
    private static final long BUFFER_SECONDS = 60;

    // ✅ MAIN METHOD (use this everywhere)
    public synchronized String getValidAccessToken() {

        // if token exists and not expired → reuse
        if (accessToken != null && expiryTime != null) {
            if (Instant.now().isBefore(expiryTime.minusSeconds(BUFFER_SECONDS))) {
                System.out.println("Using cached token");
                return accessToken;
            }
        }

        // else → refresh
        System.out.println("Refreshing Zoho token");
        return refreshToken();
    }

    // 🔄 Refresh token
    private String refreshToken() {

        System.out.println("Refreshing Zoho token...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<ZohoTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                ZohoTokenResponse.class
        );

        ZohoTokenResponse tokenResponse = response.getBody();

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("Failed to get Zoho token");
        }

        // save in memory
        accessToken = tokenResponse.getAccessToken();

        int expiresIn = tokenResponse.getExpiresIn() != null
                ? tokenResponse.getExpiresIn()
                : 3600;

        expiryTime = Instant.now().plusSeconds(expiresIn);

        return accessToken;
    }

    // optional: force refresh (used on 401)
    public synchronized void clearToken() {
        System.out.println("Clearing cached token");
        accessToken = null;
        expiryTime = null;
    }

    public String refreshAccessToken() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<ZohoTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                ZohoTokenResponse.class
        );

        ZohoTokenResponse tokenResponse = response.getBody();

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new RuntimeException("Failed to get Zoho access token");
        }

        return tokenResponse.getAccessToken();
    }
}