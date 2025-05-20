package com.BusBooking.Bus.repository;


import com.BusBooking.Bus.model.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<PaymentRecord, String> {
}
