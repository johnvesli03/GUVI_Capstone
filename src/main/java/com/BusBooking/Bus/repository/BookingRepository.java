package com.BusBooking.Bus.repository;


import com.BusBooking.Bus.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByBusIdAndTravelDate(String busId, Date travelDate);


}
