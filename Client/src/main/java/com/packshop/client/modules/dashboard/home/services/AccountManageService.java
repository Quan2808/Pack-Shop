package com.packshop.client.modules.dashboard.home.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.services.ApiBaseService;
import com.packshop.client.dto.identity.UserResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountManageService extends ApiBaseService {

    private static final String API_URL = "users";

    public AccountManageService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public List<UserResponse> getAllUsers() {
        try {
            return getAllFromApi(API_URL, UserResponse[].class);
        } catch (Exception e) {
            log.info("Failed to fetch all Users: {}", e.getMessage(), e);
            throw e;
        }
    }

}
