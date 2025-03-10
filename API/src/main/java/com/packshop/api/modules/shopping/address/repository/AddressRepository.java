package com.packshop.api.modules.shopping.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.address.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUser(User user);

  Optional<Address> findByIdAndUser(Long id, User user);

  Address findByFullAddressAndUser(String fullAddress, User user);

  List<Address> findByUserAndIsDefault(User user, boolean isDefault);

}
