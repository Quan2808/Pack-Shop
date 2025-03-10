package com.packshop.client.modules.client.shopping.address.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packshop.client.common.exceptions.ApiException;
import com.packshop.client.common.services.ApiBaseService;
import com.packshop.client.dto.shopping.address.AddressDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddressService extends ApiBaseService {

  private static final String ADDRESSES_API_URL = "addresses";

  public AddressService(RestTemplate restTemplate, ObjectMapper objectMapper) {
    super(restTemplate, objectMapper);
  }

  public List<AddressDTO> getUserAddresses(Long userId) {
    try {
      return getAllFromApi(ADDRESSES_API_URL, AddressDTO[].class);
    } catch (ApiException e) {
      log.error("Error fetching addresses for user {}: {}", userId, e.getMessage());
      throw new ApiException(
          "Couldn’t load your addresses. Please try again later.",
          e.getStatusCode(),
          e.getErrorCode(),
          e.getErrors(),
          e);
    }
  }

  public void removeAddress(Long addressId) {
    log.info("Removing item from cart: addressId={}", addressId);
    deleteFromApi(ADDRESSES_API_URL + "/", addressId);
  }
}
