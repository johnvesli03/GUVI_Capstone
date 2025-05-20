package com.BusBooking.Bus.repository;


import com.BusBooking.Bus.model.Bus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BusRepository extends MongoRepository<Bus, String> {
    List<Bus> findByFromAndTo(String from, String to);
}
