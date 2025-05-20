package com.BusBooking.Bus.service;


import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    public List<Bus> searchBuses(String from, String to) {
        return busRepository.findByFromAndTo(from, to);
    }

    public Optional<Bus> getBusById(String id) {
        return busRepository.findById(id);
    }

    public void updateBus(Bus bus) {
        busRepository.save(bus);
    }

    public void addBus(Bus bus) {
        busRepository.save(bus);
    }
}
