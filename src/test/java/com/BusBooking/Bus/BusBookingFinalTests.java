package com.BusBooking.Bus;

import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.model.User;
import com.BusBooking.Bus.model.Booking;
import com.BusBooking.Bus.model.Passenger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BusBookingFinalTests {

    @Test
    public void testUserFields() {
        User user = new User();
        user.setName("Dhanush");
        user.setEmail("dhanush@example.com");
        user.setPassword("secret");
        user.setRole("ADMIN");
        user.setVerified(true);

        assertEquals("Dhanush", user.getName());
        assertEquals("dhanush@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertTrue(user.isVerified());
    }

    @Test
    public void testBusFields() {
        Bus bus = new Bus();
        bus.setOperatorName("TSRTC");
        bus.setFrom("Hyderabad");
        bus.setTo("Chennai");
        bus.setDepartureTime("22:00");
        bus.setArrivalTime("06:00");
        bus.setSeaterPrice(450.0);
        bus.setSleeperPrice(800.0);
        bus.setSeaterCount(20);
        bus.setSleeperCount(10);
        bus.setTotalSeats(30);

        assertEquals("TSRTC", bus.getOperatorName());
        assertEquals("Hyderabad", bus.getFrom());
        assertEquals("Chennai", bus.getTo());
        assertEquals(450.0, bus.getSeaterPrice());
        assertEquals(800.0, bus.getSleeperPrice());
        assertEquals(20, bus.getSeaterCount());
    }

    @Test
    public void testBookingBasic() {
        Booking booking = new Booking();
        booking.setUserId("user123");
        booking.setBusId("bus789");
        booking.setTotalAmount(999.0);
        booking.setBookingDate(new Date());

        assertEquals("user123", booking.getUserId());
        assertEquals("bus789", booking.getBusId());
        assertEquals(999.0, booking.getTotalAmount());
        assertNotNull(booking.getBookingDate());
    }

    @Test
    public void testPassengerFields() {
        Passenger p = new Passenger();
        p.setName("Ravi");
        p.setAge(25);
        p.setGender("Male");
        p.setSeatNumber(4);
        p.setSeatType("Sleeper");

        assertEquals("Ravi", p.getName());
        assertEquals(25, p.getAge());
        assertEquals("Male", p.getGender());
        assertEquals(4, p.getSeatNumber());
        assertEquals("Sleeper", p.getSeatType());
    }

    @Test
    public void testMultiplePassengersInBooking() {
        Passenger p1 = new Passenger();
        p1.setName("A");

        Passenger p2 = new Passenger();
        p2.setName("B");

        Booking booking = new Booking();
        booking.setPassengers(Arrays.asList(p1, p2));

        assertEquals(2, booking.getPassengers().size());
        assertEquals("A", booking.getPassengers().get(0).getName());
    }

    @Test
    public void testBusTotalSeatsLogic() {
        Bus bus = new Bus();
        bus.setSeaterCount(10);
        bus.setSleeperCount(5);
        bus.setTotalSeats(15);

        assertEquals(bus.getSeaterCount() + bus.getSleeperCount(), bus.getTotalSeats());
    }

    @Test
    public void testBusBookingStructure() {
        Booking booking = new Booking();
        booking.setTravelDate(new Date());

        assertNotNull(booking.getTravelDate());
    }

    @Test
    public void testUserRoleVerification() {
        User user = new User();
        user.setRole("USER");
        assertNotEquals("ADMIN", user.getRole());
    }

    @Test
    public void testBusPriceValidity() {
        Bus bus = new Bus();
        bus.setSeaterPrice(300.0);
        bus.setSleeperPrice(600.0);

        assertTrue(bus.getSleeperPrice() > bus.getSeaterPrice());
    }

    @Test
    public void testPassengerSeatLogic() {
        Passenger p = new Passenger();
        p.setSeatNumber(12);
        p.setSeatType("Seater");

        assertTrue(p.getSeatNumber() > 0);
        assertEquals("Seater", p.getSeatType());
    }

    @Test
    public void testEmailFormat() {
        User user = new User();
        user.setEmail("someone@domain.com");
        assertTrue(user.getEmail().contains("@"));
    }

    @Test
    public void testBusOperatorPresence() {
        Bus bus = new Bus();
        bus.setOperatorName("APSRTC");
        assertNotNull(bus.getOperatorName());
    }

    @Test
    public void testUserVerificationDefaultFalse() {
        User user = new User();
        assertFalse(user.isVerified());  // default value = false
    }

    @Test
    public void testBookingPassengerListEmptyInitially() {
        Booking booking = new Booking();
        assertNull(booking.getPassengers());
    }

    @Test
    public void testSetAndGetBusRoute() {
        Bus bus = new Bus();
        bus.setFrom("Vijayawada");
        bus.setTo("Vizag");

        assertEquals("Vijayawada", bus.getFrom());
        assertEquals("Vizag", bus.getTo());
    }
}
