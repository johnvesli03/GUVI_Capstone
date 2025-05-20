package com.BusBooking.Bus.seed;

import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class BusSeeder implements CommandLineRunner {

    private final BusRepository busRepository;

    @Override
    public void run(String... args) {
        if (busRepository.count() == 0) {
            List<String> cities = List.of("Chennai", "Bangalore", "Hyderabad", "Mumbai", "Delhi");
            Random rand = new Random();

            for (int i = 0; i < 10; i++) {
                String from = cities.get(rand.nextInt(cities.size()));
                String to;
                do {
                    to = cities.get(rand.nextInt(cities.size()));
                } while (to.equals(from));

                int seaterCount = 20 + rand.nextInt(11);   // 20 to 30
                int sleeperCount = 10 + rand.nextInt(6);   // 10 to 15
                int totalSeats = seaterCount + sleeperCount;

                double seaterPrice = 300 + rand.nextInt(401);   // ₹300–₹700
                double sleeperPrice = 500 + rand.nextInt(601);  // ₹500–₹1100

                Bus bus = Bus.builder()
                        .operatorName("Operator " + (char) (65 + i))
                        .from(from)
                        .to(to)
                        .departureTime((6 + rand.nextInt(12)) + ":00")
                        .arrivalTime((18 + rand.nextInt(6)) + ":00")
                        .seaterCount(seaterCount)
                        .sleeperCount(sleeperCount)
                        .totalSeats(totalSeats)
                        .seaterPrice(seaterPrice)
                        .sleeperPrice(sleeperPrice)
                        .bookedSeats(new ArrayList<>())
                        .build();

                busRepository.save(bus);
            }

            System.out.println("✅ 10 buses seeded with seater/sleeper & prices.");
        } else {
            System.out.println("ℹ️ Buses already exist.");
        }
    }
}
