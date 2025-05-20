package com.BusBooking.Bus.controller;

import com.BusBooking.Bus.model.Booking;
import com.BusBooking.Bus.model.User;
import com.BusBooking.Bus.repository.BookingRepository;
import com.BusBooking.Bus.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Tag(name = "User", description = "User registration, login, dashboard, and profile")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;

    @GetMapping("/register")
    @Operation(summary = "Show user registration form")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public Object register(@ModelAttribute User user,
                           @RequestParam(required = false, defaultValue = "false") boolean json,
                           Model model) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            if (json) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists."));
            }
            model.addAttribute("error", "Email already exists.");
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        if (json) {
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "userId", user.getId(),
                    "email", user.getEmail()
            ));
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    @Operation(summary = "Show login page")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    @Operation(summary = "User dashboard")
    public Object dashboard(Principal principal,
                            @RequestParam(required = false, defaultValue = "false") boolean json,
                            Model model) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return json ? ResponseEntity.status(401).body(Map.of("error", "Unauthorized")) : "redirect:/login";
        }

        User user = userOpt.get();

        if (json) {
            return ResponseEntity.ok(Map.of(
                    "user", user,
                    "message", "Dashboard data fetched successfully"
            ));
        }

        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/profile")
    @Operation(summary = "User profile")
    public Object userProfile(Principal principal,
                              @RequestParam(required = false, defaultValue = "false") boolean json,
                              Model model) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return json ? ResponseEntity.status(401).body(Map.of("error", "Unauthorized")) : "redirect:/login";
        }

        User user = userOpt.get();

        if (json) {
            return ResponseEntity.ok(Map.of("user", user));
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/user/history")
    @Operation(summary = "View user's booking history")
    public Object bookingHistory(Principal principal,
                                 @RequestParam(required = false, defaultValue = "false") boolean json,
                                 Model model) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return json ? ResponseEntity.status(401).body(Map.of("error", "Unauthorized")) : "redirect:/login";
        }

        User user = userOpt.get();
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        if (json) {
            return ResponseEntity.ok(Map.of(
                    "user", Map.of("id", user.getId(), "email", user.getEmail()),
                    "bookings", bookings
            ));
        }

        model.addAttribute("bookings", bookings);
        return "booking_history";
    }
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password"; // will render forgot-password.html
    }
    @PostMapping("/forgot-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No user found with that email.");
            return "forgot-password";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("message", "✅ Password reset successfully. Please log in.");
        return "forgot-password";
    }

}
