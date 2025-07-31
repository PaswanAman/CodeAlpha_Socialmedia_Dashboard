package com.envisionTech.Parking.System.service.impl;

import com.envisionTech.Parking.System.dto.VehicleDto;
import com.envisionTech.Parking.System.entity.User;
import com.envisionTech.Parking.System.entity.Vehicle;
import com.envisionTech.Parking.System.exception.ResourceNotFoundException;
import com.envisionTech.Parking.System.repository.UserRepository;
import com.envisionTech.Parking.System.repository.VehicleRepository;
import com.envisionTech.Parking.System.service.VehicleService;
import com.envisionTech.Parking.System.util.JwtTokenHelper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtTokenHelper jwtTokenHelper;

    @Value("${app.timezone:Asia/Kathmandu}")
    private String timeZone;

    @Override
    @Transactional
    public VehicleDto addVehicle(VehicleDto dto, String userToken) {
        String token = userToken.replaceAll("Bearer", "").trim();
        String username = jwtTokenHelper.extractUsernameFromToken(token);
        
        if (vehicleRepository.existsByVehicleNumberAndVehicleType(dto.getVehicleNumber(), dto.getVehicleType())) {
            throw new IllegalArgumentException("This vehicle number already exists.");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vehicle vehicle = modelMapper.map(dto, Vehicle.class);
        ZoneId kathmanduZoneId = ZoneId.of(timeZone);
        vehicle.setAddedAt(LocalDateTime.now(kathmanduZoneId));
        vehicle.setUser(user);

        // Check if this is the user's first vehicle
        long userVehicleCount = vehicleRepository.countByUser(user);
        if (userVehicleCount == 0) {
            // If this is the first vehicle, make it default automatically
            vehicle.setIsDefault(true);
        } else {
            // If user has other vehicles, check if they want this as default
            // If no default is explicitly set in the DTO, keep it false
            vehicle.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
            
            // If user wants to make this default, clear the existing default
            if (vehicle.getIsDefault()) {
                vehicleRepository.clearDefaultForUser(user);
            }
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public VehicleDto setDefaultVehicle(Long vehicleId, String userToken) {
        String token = userToken.replaceAll("Bearer", "").trim();
        String username = jwtTokenHelper.extractUsernameFromToken(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Check if the vehicle belongs to the user
        if (!vehicle.getUser().equals(user)) {
            throw new IllegalArgumentException("You can only set your own vehicles as default");
        }

        // Clear existing default and set new default
        vehicleRepository.clearDefaultForUser(user);
        vehicleRepository.setAsDefault(vehicleId, user);

        // Fetch updated vehicle
        Vehicle updatedVehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        return mapToDto(updatedVehicle);
    }

    @Override
    public VehicleDto getDefaultVehicle(String userToken) {
        String token = userToken.replaceAll("Bearer", "").trim();
        String username = jwtTokenHelper.extractUsernameFromToken(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Vehicle> defaultVehicle = vehicleRepository.findByUserAndIsDefaultTrue(user);
        
        if (defaultVehicle.isEmpty()) {
            throw new ResourceNotFoundException("No default vehicle found");
        }

        return mapToDto(defaultVehicle.get());
    }

    @Override
    public List<VehicleDto> getUserVehicles(String userToken) {
        String token = userToken.replaceAll("Bearer", "").trim();
        String username = jwtTokenHelper.extractUsernameFromToken(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Vehicle> vehicles = vehicleRepository.findByUser(user);
        
        return vehicles.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleDto updateVehicle(Long vehicleId, VehicleDto dto, String userToken) {
        String token = userToken.replaceAll("Bearer", "").trim();
        String username = jwtTokenHelper.extractUsernameFromToken(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Check if the vehicle belongs to the user
        if (!vehicle.getUser().equals(user)) {
            throw new IllegalArgumentException("You can only update your own vehicles");
        }

        // Update vehicle details
        if (dto.getVehicleNumber() != null) {
            vehicle.setVehicleNumber(dto.getVehicleNumber());
        }
        if (dto.getVehicleType() != null) {
            vehicle.setVehicleType(dto.getVehicleType());
        }
        if (dto.getVehicleModel() != null) {
            vehicle.setVehicleModel(dto.getVehicleModel());
        }
        if (dto.getVehicleColor() != null) {
            vehicle.setVehicleColor(dto.getVehicleColor());
        }

        ZoneId kathmanduZoneId = ZoneId.of(timeZone);
        vehicle.setUpdatedAt(LocalDateTime.now(kathmanduZoneId));

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long vehicleId, String userToken) {
        String token = userToken.replaceAll("Bearer", "").trim();
        String username = jwtTokenHelper.extractUsernameFromToken(token);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Check if the vehicle belongs to the user
        if (!vehicle.getUser().equals(user)) {
            throw new IllegalArgumentException("You can only delete your own vehicles");
        }

        boolean wasDefault = vehicle.getIsDefault();
        vehicleRepository.delete(vehicle);

        // If deleted vehicle was default, make another vehicle default if available
        if (wasDefault) {
            List<Vehicle> remainingVehicles = vehicleRepository.findByUser(user);
            if (!remainingVehicles.isEmpty()) {
                Vehicle firstVehicle = remainingVehicles.get(0);
                firstVehicle.setIsDefault(true);
                vehicleRepository.save(firstVehicle);
            }
        }
    }

    private VehicleDto mapToDto(Vehicle vehicle) {
        VehicleDto dto = modelMapper.map(vehicle, VehicleDto.class);
        if (vehicle.getUser() != null) {
            dto.setUserId(vehicle.getUser().getUserId());
            dto.setUserEmail(vehicle.getUser().getEmail());
        }
        return dto;
    }
}