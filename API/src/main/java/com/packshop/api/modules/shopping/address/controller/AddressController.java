package com.packshop.api.modules.shopping.address.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.address.dto.AddressDTO;
import com.packshop.api.modules.shopping.address.service.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/addresses")
public class AddressController {

  private final AddressService addressService;

  @PostMapping
  public ResponseEntity<AddressDTO> createAddress(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody AddressDTO addressDTO) {
    AddressDTO createdAddress = addressService.createAddress(user, addressDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
  }

  @GetMapping
  public ResponseEntity<List<AddressDTO>> getUserAddresses(
      @AuthenticationPrincipal User user) {
    List<AddressDTO> addresses = addressService.getUserAddresses(user);
    return ResponseEntity.ok(addresses);
  }

  @GetMapping("/default")
  public ResponseEntity<List<AddressDTO>> getUserDefaultAddresses(
      @AuthenticationPrincipal User user) {
    List<AddressDTO> addresses = addressService.getUserDefaultAddresses(user);
    return ResponseEntity.ok(addresses);
  }

  @PutMapping("/{addressId}")
  public ResponseEntity<AddressDTO> updateAddress(
      @AuthenticationPrincipal User user,
      @PathVariable Long addressId,
      @Valid @RequestBody AddressDTO addressDTO) {
    AddressDTO updatedAddress = addressService.updateAddress(user, addressId, addressDTO);
    return ResponseEntity.ok(updatedAddress);
  }

  @DeleteMapping("/{addressId}")
  public ResponseEntity<Void> deleteAddress(
      @AuthenticationPrincipal User user,
      @PathVariable Long addressId) {
    addressService.deleteAddress(user, addressId);
    return ResponseEntity.noContent().build();
  }
}