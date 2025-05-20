package com.BusBooking.Bus.dto;

import com.BusBooking.Bus.dto.PassengerDTO;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private String busId;
    private List<PassengerDTO> passengers;
}
