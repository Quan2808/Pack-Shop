package com.packshop.api.modules.shopping.address.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.DuplicateResourceException;
import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.address.dto.AddressDTO;
import com.packshop.api.modules.shopping.address.entity.Address;
import com.packshop.api.modules.shopping.address.repository.AddressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

  private final AddressRepository addressRepository;
  private final ModelMapper modelMapper;

  @Transactional(readOnly = true)
  public List<AddressDTO> getUserAddresses(User user) {
    log.debug("Fetching addresses for user: {}", user.getId());
    return addressRepository.findByUser(user)
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<AddressDTO> getUserDefaultAddresses(User user) {
    log.debug("Fetching addresses for user: {}", user.getId());
    return addressRepository.findByUserAndIsDefault(user, true)
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public AddressDTO createAddress(User user, AddressDTO addressDTO) {
    log.info("Creating new address for user: {}", user.getId());

    Address address = convertToEntity(addressDTO);

    address.setFullAddress(address.generateFullAddress());
    address.setUser(user);

    // Check for duplicate and handle default logic in one pass
    List<Address> userAddresses = addressRepository.findByUser(user);

    assignAliasNameIfEmpty(address, addressDTO, user, userAddresses);

    checkForDuplicateAddress(address.getFullAddress(), userAddresses);
    checkForDuplicateAliasName(address.getAliasName(), userAddresses);
    setDefaultAddressLogic(address, userAddresses);

    Address savedAddress = addressRepository.save(address);
    log.info("Successfully created address with ID: {} - {}", savedAddress.getId(), savedAddress.getFullAddress());
    return convertToDTO(savedAddress);
  }

  @Transactional
  public AddressDTO updateAddress(User user, Long addressId, AddressDTO addressDTO) {
    log.info("Updating address ID: {} for user: {}", addressId, user.getId());

    Address address = addressRepository.findByIdAndUser(addressId, user)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("Address with ID %d not found for user %d", addressId, user.getId())));

    updateAddressFields(address, addressDTO);

    List<Address> userAddresses = addressRepository.findByUser(user);

    assignAliasNameIfEmpty(address, addressDTO, user, userAddresses);
    checkForDuplicateAddress(address.getFullAddress(), userAddresses, addressId);
    checkForDuplicateAliasName(address.getAliasName(), userAddresses, addressId);

    setDefaultAddressLogic(address, userAddresses);

    Address updatedAddress = addressRepository.save(address);
    log.info("Successfully updated address ID: {}", updatedAddress.getId());
    return convertToDTO(updatedAddress);
  }

  @Transactional
  public void deleteAddress(User user, Long addressId) {
    log.info("Deleting address ID: {} for user: {}", addressId, user.getId());

    Address address = addressRepository.findByIdAndUser(addressId, user)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("Address with ID %d not found for user %d", addressId, user.getId())));

    addressRepository.delete(address);
    log.info("Successfully deleted address ID: {}", addressId);
  }

  private void checkForDuplicateAddress(String fullAddress, List<Address> userAddresses, Long excludeId) {
    boolean duplicateExists = userAddresses.stream()
        .anyMatch(a -> a.getFullAddress().equals(fullAddress) && !a.getId().equals(excludeId));
    if (duplicateExists) {
      throw new DuplicateResourceException(
          "Another address with the same full address already exists for this user");
    }
  }

  private void checkForDuplicateAddress(String fullAddress, List<Address> userAddresses) {
    checkForDuplicateAddress(fullAddress, userAddresses, null);
  }

  private void checkForDuplicateAliasName(String aliasName, List<Address> userAddresses, Long excludeId) {
    if (aliasName == null || aliasName.trim().isEmpty())
      return;

    boolean duplicateExists = userAddresses.stream()
        .anyMatch(a -> aliasName.equalsIgnoreCase(a.getAliasName()) && !a.getId().equals(excludeId));
    if (duplicateExists) {
      throw new DuplicateResourceException("Another address with the same alias name already exists for this user");
    }
  }

  private void checkForDuplicateAliasName(String aliasName, List<Address> userAddresses) {
    checkForDuplicateAliasName(aliasName, userAddresses, null);
  }

  private void assignAliasNameIfEmpty(Address address, AddressDTO addressDTO, User user, List<Address> userAddresses) {
    if (addressDTO.getAliasName() == null || addressDTO.getAliasName().trim().isEmpty()) {
      address.setAliasName(address.generateDefaultAliasName(user, userAddresses));
    } else {
      address.setAliasName(addressDTO.getAliasName());
    }
  }

  private void setDefaultAddressLogic(Address address, List<Address> userAddresses) {
    if (userAddresses.isEmpty()) {
      log.debug("Setting first address as default for user: {}", address.getUser().getId());
      address.setIsDefault(true);
    } else if (Boolean.TRUE.equals(address.getIsDefault())) {
      log.debug("Setting address as default and unsetting others for user: {}", address.getUser().getId());
      userAddresses.stream()
          .filter(a -> Boolean.TRUE.equals(a.getIsDefault()) && !a.getId().equals(address.getId()))
          .forEach(a -> {
            a.setIsDefault(false);
            addressRepository.save(a);
          });
    }
  }

  private void updateAddressFields(Address address, AddressDTO addressDTO) {
    address.setAliasName(addressDTO.getAliasName());
    address.setStreetAddress(addressDTO.getStreetAddress());
    address.setWard(addressDTO.getWard());
    address.setDistrict(addressDTO.getDistrict());
    address.setProvince(addressDTO.getProvince());
    address.setFullAddress(address.generateFullAddress());
    address.setIsDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : address.getIsDefault());
  }

  public AddressDTO convertToDTO(Address address) {
    return modelMapper.map(address, AddressDTO.class);
  }

  public Address convertToEntity(AddressDTO dto) {
    return modelMapper.map(dto, Address.class);
  }
}