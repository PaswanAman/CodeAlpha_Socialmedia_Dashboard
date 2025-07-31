package com.envisionTech.Parking.System.controller;

import com.envisionTech.Parking.System.dto.VehicleDto;
import com.envisionTech.Parking.System.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleDto> addVehicle(
            @RequestBody VehicleDto vehicleDto,
            @RequestHeader("Authorization") String token) {
        VehicleDto savedVehicle = vehicleService.addVehicle(vehicleDto, token);
        return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getUserVehicles(
            @RequestHeader("Authorization") String token) {
        List<VehicleDto> vehicles = vehicleService.getUserVehicles(token);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/default")
    public ResponseEntity<VehicleDto> getDefaultVehicle(
            @RequestHeader("Authorization") String token) {
        VehicleDto defaultVehicle = vehicleService.getDefaultVehicle(token);
        return ResponseEntity.ok(defaultVehicle);
    }

    @PutMapping("/{vehicleId}/set-default")
    public ResponseEntity<VehicleDto> setDefaultVehicle(
            @PathVariable Long vehicleId,
            @RequestHeader("Authorization") String token) {
        VehicleDto defaultVehicle = vehicleService.setDefaultVehicle(vehicleId, token);
        return ResponseEntity.ok(defaultVehicle);
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleDto> updateVehicle(
            @PathVariable Long vehicleId,
            @RequestBody VehicleDto vehicleDto,
            @RequestHeader("Authorization") String token) {
        VehicleDto updatedVehicle = vehicleService.updateVehicle(vehicleId, vehicleDto, token);
        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long vehicleId,
            @RequestHeader("Authorization") String token) {
        vehicleService.deleteVehicle(vehicleId, token);
        return ResponseEntity.noContent().build();
    }
}