package com.envisionTech.Parking.System.service;

import com.envisionTech.Parking.System.dto.VehicleDto;

import java.util.List;

public interface VehicleService {
    
    VehicleDto addVehicle(VehicleDto dto, String userToken);
    
    VehicleDto setDefaultVehicle(Long vehicleId, String userToken);
    
    VehicleDto getDefaultVehicle(String userToken);
    
    List<VehicleDto> getUserVehicles(String userToken);
    
    VehicleDto updateVehicle(Long vehicleId, VehicleDto dto, String userToken);
    
    void deleteVehicle(Long vehicleId, String userToken);
}