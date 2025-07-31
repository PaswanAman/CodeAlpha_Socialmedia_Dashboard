package com.envisionTech.Parking.System.repository;

import com.envisionTech.Parking.System.entity.Vehicle;
import com.envisionTech.Parking.System.entity.VehicleType;
import com.envisionTech.Parking.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    boolean existsByVehicleNumberAndVehicleType(String vehicleNumber, VehicleType vehicleType);
    
    List<Vehicle> findByUser(User user);
    
    long countByUser(User user);
    
    Optional<Vehicle> findByUserAndIsDefaultTrue(User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE Vehicle v SET v.isDefault = false WHERE v.user = :user")
    void clearDefaultForUser(@Param("user") User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE Vehicle v SET v.isDefault = true WHERE v.vehicleId = :vehicleId AND v.user = :user")
    void setAsDefault(@Param("vehicleId") Long vehicleId, @Param("user") User user);
}