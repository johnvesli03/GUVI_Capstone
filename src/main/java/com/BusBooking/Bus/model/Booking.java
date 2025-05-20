package com.BusBooking.Bus.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    private String id;
    private String paymentId;
    private String orderId;
    private String receipt;
    private String paymentStatus;
    private String userId;
    private String busId;
    private Date bookingDate;
    private Double totalAmount;
    private Date travelDate;
    private String email;
    private List<Passenger> passengers;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
