package com.envisionTech.Parking.System.dto;

import com.envisionTech.Parking.System.entity.VehicleType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDto {
    private Long vehicleId;
    private String vehicleNumber;
    private VehicleType vehicleType;
    private String vehicleModel;
    private String vehicleColor;
    private Boolean isDefault = false;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime addedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updatedAt;
    
    private Long userId;
    private String userEmail;
}