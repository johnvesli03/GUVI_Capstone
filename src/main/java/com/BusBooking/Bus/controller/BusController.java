package com.BusBooking.Bus.controller;

import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.repository.BusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Tag(name = "Bus", description = "Bus search and details endpoints")
public class BusController {

    private final BusRepository busRepository;

    @GetMapping("/search")
    @Operation(summary = "Search buses based on from and to")
    public Object searchBuses(@RequestParam(required = false) String from,
                              @RequestParam(required = false) String to,
                              @RequestParam(required = false, defaultValue = "false") boolean json,
                              Model model) {
        if (from != null && to != null) {
            List<Bus> buses = busRepository.findByFromAndTo(from, to);

            if (json) {
                return ResponseEntity.ok(Map.of(
                        "from", from,
                        "to", to,
                        "results", buses,
                        "count", buses.size()
                ));
            }

            model.addAttribute("buses", buses);
        }

        return "search"; // Browser renders search.html
    }

    @GetMapping("/bus/{id}")
    @Operation(summary = "Show bus details by ID")
    public Object showBusDetails(@PathVariable String id,
                                 @RequestParam(required = false, defaultValue = "false") boolean json,
                                 Model model) {
        Bus bus = busRepository.findById(id).orElse(null);

        if (bus == null) {
            if (json) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Bus not found", "busId", id));
            }
            return "redirect:/search";
        }

        int booked = (bus.getBookedSeats() != null) ? bus.getBookedSeats().size() : 0;
        int availableSeats = bus.getTotalSeats() - booked;

        if (json) {
            return ResponseEntity.ok(Map.of(
                    "bus", bus,
                    "availableSeats", availableSeats,
                    "bookedSeats", booked
            ));
        }

        model.addAttribute("bus", bus);
        model.addAttribute("availableSeats", availableSeats);
        return "bus_details"; // Browser renders bus_details.html
    }
}
