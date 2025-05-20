package com.BusBooking.Bus.seeder;

import com.BusBooking.Bus.model.Booking;
import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.model.Passenger;
import com.BusBooking.Bus.repository.BookingRepository;
import com.BusBooking.Bus.repository.BusRepository;
import com.BusBooking.Bus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class BookingSeeder implements CommandLineRunner {

    private final BookingRepository bookingRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (bookingRepository.count() == 0) {
            Optional<Bus> busOpt = busRepository.findAll().stream().findFirst();
            String userId = userRepository.findByEmail("user@bus.com").map(u -> u.getId()).orElse(null);

            if (busOpt.isEmpty() || userId == null) {
                System.out.println("⚠️ Cannot seed booking: missing bus or user.");
                return;
            }

            Bus bus = busOpt.get();

            List<Passenger> passengers = new ArrayList<>();
            List<Integer> seatNumbers = List.of(5, 6, 7);
            List<String> seatTypes = List.of("Seater", "Sleeper", "Seater");

            double totalAmount = 0.0;

            for (int i = 0; i < seatNumbers.size(); i++) {
                String type = seatTypes.get(i);
                double price = type.equalsIgnoreCase("Sleeper") ? bus.getSleeperPrice() : bus.getSeaterPrice();

                passengers.add(Passenger.builder()
                        .name("Passenger " + (i + 1))
                        .age(22 + i)
                        .gender(i % 2 == 0 ? "Male" : "Female")
                        .seatNumber(seatNumbers.get(i))
                        .seatType(type)
                        .build());

                totalAmount += price;
            }

            if (bus.getBookedSeats() == null) {
                bus.setBookedSeats(new ArrayList<>());
            }
            bus.getBookedSeats().addAll(seatNumbers);
            busRepository.save(bus);

            Booking booking = Booking.builder()
                    .userId(userId)
                    .busId(bus.getId())
                    .passengers(passengers)
                    .bookingDate(new Date())
                    .travelDate(new Date()) // Can be customized
                    .totalAmount(totalAmount)
                    .build();

            bookingRepository.save(booking);

            System.out.println("✅ Demo booking created for user@bus.com with mixed seat types.");
        } else {
            System.out.println("ℹ️ Bookings already exist.");
        }
    }
}
