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
public class ZohoLeaveAuthService {

    @Value("${zoho.leave.client.id}")
    private String clientId;

    @Value("${zoho.leave.client.secret}")
    private String clientSecret;

    @Value("${zoho.leave.refresh.token}")
    private String refreshToken;

    @Value("${zoho.leave.token.url}")
    private String tokenUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private String accessToken;
    private Instant expiryTime;

    private static final long BUFFER_SECONDS = 60;

    public synchronized String getValidAccessToken() {
        if (accessToken != null && expiryTime != null) {
            if (Instant.now().isBefore(expiryTime.minusSeconds(BUFFER_SECONDS))) {
                System.out.println("Using cached Zoho Leave token");
                return accessToken;
            }
        }

        return refreshToken();
    }

    private String refreshToken() {
        System.out.println("Refreshing Zoho Leave token...");

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
            throw new RuntimeException("Failed to get Zoho Leave access token");
        }

        accessToken = tokenResponse.getAccessToken();

        int expiresIn = tokenResponse.getExpiresIn() != null
                ? tokenResponse.getExpiresIn()
                : 3600;

        expiryTime = Instant.now().plusSeconds(expiresIn);

        return accessToken;
    }

    public synchronized void clearToken() {
        accessToken = null;
        expiryTime = null;
    }
}