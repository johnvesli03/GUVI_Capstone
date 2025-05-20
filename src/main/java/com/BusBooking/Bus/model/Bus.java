package com.BusBooking.Bus.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {
    @Id
    private String id;

    private String operatorName;
    private String from;
    private String to;
    private String departureTime;
    private String arrivalTime;
    private String status;

    private Double seaterPrice;
    private Double sleeperPrice;

    private Integer seaterCount;
    private Integer sleeperCount;
    private Integer totalSeats;
    private String amenities;

    private List<Integer> bookedSeats;

    public boolean hasAmenity(String amenity) {
        return amenities != null && amenities.contains(amenity);
    }

}

