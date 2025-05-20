package com.BusBooking.Bus.controller;

import com.BusBooking.Bus.model.Booking;
import com.BusBooking.Bus.model.Bus;
import com.BusBooking.Bus.model.Passenger;
import com.BusBooking.Bus.model.User;
import com.BusBooking.Bus.repository.BookingRepository;
import com.BusBooking.Bus.repository.BusRepository;
import com.BusBooking.Bus.repository.UserRepository;
import com.BusBooking.Bus.service.EmailService;
import com.BusBooking.Bus.util.TicketPDFGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.sql.Date;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "Endpoints for managing bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/book/{busId}")
    @ResponseBody
    @Operation(summary = "Book a bus")
    public ResponseEntity<?> bookBus(@PathVariable String busId,
                                     @RequestParam List<String> passengerName,
                                     @RequestParam List<Integer> passengerAge,
                                     @RequestParam List<String> passengerGender,
                                     @RequestParam String seatNumber,
                                     @RequestParam List<String> seatType,
                                     @RequestParam(required = false) String travelDate,
                                     @RequestParam(required = false) String paymentId,
                                     @RequestParam(required = false) String orderId,
                                     @RequestParam(required = false) String receipt,
                                     Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User must be logged in"));
        }

        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }

        if (travelDate == null || travelDate.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Travel date is required"));
        }

        Date travelSqlDate;
        try {
            travelSqlDate = Date.valueOf(travelDate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid travel date format"));
        }

        User user = userOpt.get();
        Bus bus = busRepository.findById(busId).orElse(null);
        if (bus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bus not found"));
        }

        List<Integer> seatNumbers = Arrays.stream(seatNumber.split(","))
                .map(String::trim).map(Integer::parseInt).toList();

        // ✅ Step: Block seats already booked (respecting seatType)
        List<Booking> existingBookings = bookingRepository.findByBusIdAndTravelDate(busId, travelSqlDate);
        Set<String> alreadyBooked = new HashSet<>();

        for (Booking b : existingBookings) {
            for (Passenger p : b.getPassengers()) {
                alreadyBooked.add(p.getSeatType().toLowerCase() + "-" + p.getSeatNumber());
            }
        }

        for (int i = 0; i < seatNumbers.size(); i++) {
            String key = seatType.get(i).toLowerCase() + "-" + seatNumbers.get(i);
            if (alreadyBooked.contains(key)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of("error", "Seat " + seatNumbers.get(i) + " (" + seatType.get(i) + ") is already booked.")
                );
            }
        }


        List<Passenger> passengers = new ArrayList<>();
        for (int i = 0; i < passengerName.size(); i++) {
            passengers.add(new Passenger(
                    passengerName.get(i),
                    passengerAge.get(i),
                    passengerGender.get(i),
                    seatNumbers.get(i),
                    seatType.get(i)
            ));
        }

        double total = passengers.stream().mapToDouble(p ->
                p.getSeatType().equalsIgnoreCase("Seater")
                        ? bus.getSeaterPrice()
                        : bus.getSleeperPrice()
        ).sum();

        Booking booking = Booking.builder()
                .busId(busId)
                .userId(user.getId())
                .email(user.getEmail())
                .bookingDate(new Date(System.currentTimeMillis()))
                .travelDate(travelSqlDate)
                .passengers(passengers)
                .totalAmount(total)
                .paymentId(paymentId)
                .orderId(orderId)
                .receipt(receipt)
                .paymentStatus("Success")
                .build();

        if (bus.getBookedSeats() == null) bus.setBookedSeats(new ArrayList<>());
        bus.getBookedSeats().addAll(seatNumbers);

        busRepository.save(bus);
        bookingRepository.save(booking);

        try {
            byte[] pdf = TicketPDFGenerator.generateTicketPDF(booking, bus);
            emailService.sendTicket(booking.getEmail(), pdf, "ticket_" + booking.getId() + ".pdf");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Booking successful",
                "bookingId", booking.getId(),
                "totalAmount", booking.getTotalAmount()
        ));
    }

    @GetMapping("/confirmation")
    @Operation(summary = "Booking confirmation page")
    public String showConfirmation(@RequestParam String bookingId,
                                   @RequestParam String status,
                                   Model model) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        Bus bus = booking != null ? busRepository.findById(booking.getBusId()).orElse(null) : null;

        if (booking == null || bus == null) {
            model.addAttribute("message", "Booking not found.");
            return "error";
        }

        model.addAttribute("status", status);
        model.addAttribute("booking", booking);
        model.addAttribute("bus", bus);
        return "confirmation";
    }

    @GetMapping("/download-ticket/{id}")
    @Operation(summary = "Download booking ticket PDF")
    public void downloadTicket(@PathVariable String id, HttpServletResponse response) throws IOException {
        Booking booking = bookingRepository.findById(id).orElse(null);
        Bus bus = booking != null ? busRepository.findById(booking.getBusId()).orElse(null) : null;

        if (booking == null || bus == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            byte[] pdfBytes = TicketPDFGenerator.generateTicketPDF(booking, bus);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=ticket_" + id + ".pdf");
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/booked-seats/{busId}")
    @ResponseBody
    @Operation(summary = "Get booked seats by type for a bus")
    public Map<String, Object> getAllBookedSeats(@PathVariable String busId,
                                                 @RequestParam(required = false) String travelDate) {
        if (travelDate == null || travelDate.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Travel date is required");
        }

        Date travelSqlDate;
        try {
            travelSqlDate = Date.valueOf(travelDate);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }

        List<Booking> bookings = bookingRepository.findByBusIdAndTravelDate(busId, travelSqlDate);
        Set<Integer> seater = new HashSet<>();
        Set<Integer> sleeper = new HashSet<>();

        for (Booking booking : bookings) {
            for (Passenger p : booking.getPassengers()) {
                if (p.getSeatType().equalsIgnoreCase("Seater")) {
                    seater.add(p.getSeatNumber());
                } else if (p.getSeatType().equalsIgnoreCase("Sleeper")) {
                    sleeper.add(p.getSeatNumber());
                }
            }
        }

        return Map.of("Seater", seater, "Sleeper", sleeper);
    }
    @GetMapping("/history")
    @Operation(summary = "Booking history page")
    public String bookingHistory(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return "redirect:/login";

        User user = userOpt.get();
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        // Load bus info for each booking
        Map<String, Bus> busMap = new HashMap<>();
        for (Booking b : bookings) {
            busRepository.findById(b.getBusId()).ifPresent(bus -> busMap.put(b.getId(), bus));
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("busMap", busMap);
        model.addAttribute("user", user);
        return "history"; // <-- must match history.html
    }

}
