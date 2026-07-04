package com.carrental.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carrental.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserEmail(String email);
    
    public Booking save(Booking booking);
    
    boolean existsByCarIdAndReturnDateAfterAndPickupDateBefore(
            Integer carId,
            LocalDate pickupDate,
            LocalDate returnDate);

}
