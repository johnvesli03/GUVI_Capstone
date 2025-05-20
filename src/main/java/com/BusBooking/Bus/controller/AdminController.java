package com.BusBooking.Bus.controller;

import com.BusBooking.Bus.model.Booking;
import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.model.User;
import com.BusBooking.Bus.repository.BookingRepository;
import com.BusBooking.Bus.repository.BusRepository;
import com.BusBooking.Bus.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin Dashboard & Bus Management")
public class AdminController {

    private final BusRepository busRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard showing all buses")
    public Object adminDashboard(@RequestParam(defaultValue = "false") boolean json, Model model) {
        List<Bus> buses = busRepository.findAll();

        if (json) {
            return ResponseEntity.ok(Map.of("buses", buses, "count", buses.size()));
        }

        model.addAttribute("buses", buses);
        return "admin_dashboard";
    }

    @GetMapping("/add")
    @Operation(summary = "Show form to add a new bus")
    public String showAddBusForm(Model model) {
        model.addAttribute("bus", new Bus());
        return "add_bus";
    }

    @PostMapping("/add")
    @Operation(summary = "Add new bus")
    public Object addBus(@ModelAttribute Bus bus,
                         @RequestParam(defaultValue = "false") boolean json) {
        int total = (bus.getSeaterCount() != null ? bus.getSeaterCount() : 0)
                + (bus.getSleeperCount() != null ? bus.getSleeperCount() : 0);
        bus.setTotalSeats(total);
        busRepository.save(bus);

        if (json) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Bus added successfully", "busId", bus.getId()));
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/edit/{id}")
    @Operation(summary = "Edit existing bus")
    public Object editBusForm(@PathVariable String id,
                              @RequestParam(defaultValue = "false") boolean json,
                              Model model) {
        Bus bus = busRepository.findById(id).orElse(null);
        if (bus == null) {
            if (json) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bus not found"));
            return "redirect:/admin/dashboard";
        }

        if (json) return ResponseEntity.ok(bus);

        model.addAttribute("bus", bus);
        return "edit_bus";
    }

    @PostMapping("/update")
    @Operation(summary = "Update existing bus")
    public Object updateBus(@ModelAttribute Bus bus,
                            @RequestParam(defaultValue = "false") boolean json) {
        busRepository.save(bus);

        if (json) {
            return ResponseEntity.ok(Map.of("message", "Bus updated successfully", "busId", bus.getId()));
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "Delete bus by ID")
    public Object deleteBus(@PathVariable String id,
                            @RequestParam(defaultValue = "false") boolean json) {
        if (!busRepository.existsById(id)) {
            if (json) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bus not found"));
            return "redirect:/admin/dashboard";
        }

        busRepository.deleteById(id);

        if (json) {
            return ResponseEntity.ok(Map.of("message", "Bus deleted successfully", "busId", id));
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/bookings")
    @Operation(summary = "View all bookings in admin panel")
    public Object viewAllBookings(@RequestParam(defaultValue = "false") boolean json, Model model) {
        List<Booking> bookings = bookingRepository.findAll();
        Map<String, Bus> busMap = new HashMap<>();
        Map<String, User> userMap = new HashMap<>();

        for (Booking booking : bookings) {
            Bus bus = busRepository.findById(booking.getBusId()).orElse(null);
            User user = userRepository.findById(booking.getUserId()).orElse(null);
            if (bus != null) busMap.put(booking.getBusId(), bus);
            if (user != null) userMap.put(booking.getUserId(), user);
        }

        if (json) {
            return ResponseEntity.ok(Map.of(
                    "bookings", bookings,
                    "busMap", busMap,
                    "userMap", userMap
            ));
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("busMap", busMap);
        model.addAttribute("userMap", userMap);
        return "booking_admin";
    }
}
