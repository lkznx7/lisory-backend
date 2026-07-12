package com.lisory.backend.user.services;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.user.dto.AddressRequest;
import com.lisory.backend.user.dto.AddressResponse;
import com.lisory.backend.user.entity.Address;
import com.lisory.backend.user.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {
        AuthEntity user = new AuthEntity();
        user.setId(userId);

        if (Boolean.TRUE.equals(request.isDefault())) {
            addressRepository.findByUserId(userId).stream()
                    .filter(Address::getDefault)
                    .forEach(a -> a.setDefault(false));
        }

        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setComplement(request.complement());
        address.setNeighborhood(request.neighborhood());
        address.setCity(request.city());
        address.setState(request.state());
        address.setZipCode(request.zipCode());
        address.setCountry(request.country() != null ? request.country() : "Brasil");
        address.setDefault(request.isDefault() != null && request.isDefault());

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(UUID userId, UUID addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (Boolean.TRUE.equals(request.isDefault()) && !address.getDefault()) {
            addressRepository.findByUserId(userId).stream()
                    .filter(Address::getDefault)
                    .forEach(a -> a.setDefault(false));
        }

        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setComplement(request.complement());
        address.setNeighborhood(request.neighborhood());
        address.setCity(request.city());
        address.setState(request.state());
        address.setZipCode(request.zipCode());
        address.setCountry(request.country() != null ? request.country() : address.getCountry());
        address.setDefault(request.isDefault() != null && request.isDefault());

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }

    public List<AddressResponse> findByUserId(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse findById(UUID userId, UUID addressId) {
        return toResponse(addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId)));
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                address.getDefault(),
                address.getCreatedAt()
        );
    }
}
