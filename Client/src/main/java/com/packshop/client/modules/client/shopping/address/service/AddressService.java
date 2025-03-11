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

  public AddressDTO saveAddress(AddressDTO addressDTO, Long userId) {
    try {
      log.info("Saving address for user {}: {}", userId, addressDTO);

      if (addressDTO.getId() == null) {
        AddressDTO savedAddress = postToApi(ADDRESSES_API_URL, addressDTO, AddressDTO.class);
        log.info("Successfully created new address with ID: {}", savedAddress.getId());
        return savedAddress;
      } else {
        AddressDTO updatedAddress = putToApi(ADDRESSES_API_URL + "/" + addressDTO.getId(),
            addressDTO,
            AddressDTO.class);
        log.info("Successfully updated address with ID: {}", updatedAddress.getId());
        return updatedAddress;
      }
    } catch (ApiException e) {
      log.error("Error saving address for user {}: {}", userId, e.getMessage());

      if (e.getMessage().startsWith("Another address with"))
        throw new ApiException(
            e.getMessage(),
            e.getStatusCode(),
            e.getErrorCode(),
            e.getErrors(),
            e);
      throw new ApiException(
          "Couldn’t save your address. Please try again later.",
          e.getStatusCode(),
          e.getErrorCode(),
          e.getErrors(),
          e);
    } catch (Exception e) {
      log.error("Unexpected error while saving address for user {}: {}", userId, e.getMessage(), e);
      throw new ApiException(
          "Unexpected error while saving address",
          500,
          "INTERNAL_SERVER_ERROR",
          null,
          e);
    }
  }
}
