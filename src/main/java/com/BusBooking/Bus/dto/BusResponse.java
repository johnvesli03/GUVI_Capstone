package com.BusBooking.Bus.dto;

import lombok.Data;

import java.util.List;

@Data
public class BusResponse {
    private String id;
    private String operatorName;
    private String from;
    private String to;
    private String departureTime;
    private String arrivalTime;
    private Double price;
    private Integer totalSeats;
    private List<Integer> bookedSeats;
}
