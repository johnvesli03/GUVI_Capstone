package com.BusBooking.Bus.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {
    private String name;
    private int age;
    private String gender;
    private int seatNumber;
    private String seatType; // now used
}
