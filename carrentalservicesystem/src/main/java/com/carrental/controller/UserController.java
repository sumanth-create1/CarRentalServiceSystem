package com.carrental.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.carrental.model.Booking;
import com.carrental.model.Car;
import com.carrental.repository.UserRepository;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private CarRepository carRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private UserRepository userRepository;

	// =========================
	// 1. HOME
	// =========================
	@GetMapping("/home")
	public String userHome() {
		return "user/home";
	}

	// =========================
	// 2. BOOKING FORM PAGE (SINGLE CAR)
	// =========================
	@GetMapping("/booking-page/{carId}")
	public String bookingPage(@PathVariable Integer carId, Model model, Principal principal) {

		Booking booking = new Booking();

		booking.setUserEmail(principal.getName());
		booking.setCarId(carId);

		Car car = carRepository.findById(carId).orElse(null);

		if (car != null) {
			booking.setCarName(car.getTitle());
		}

		model.addAttribute("booking", booking);

		return "user/booking";
	}

	// =========================
	// 3. SAVE BOOKING
	// =========================
	@PostMapping("/booking")
	public String saveBooking(@ModelAttribute Booking booking, RedirectAttributes redirectAttributes) {

		boolean exists = bookingRepository.existsByCarIdAndReturnDateAfterAndPickupDateBefore(booking.getCarId(),
				booking.getPickupDate(), booking.getReturnDate());

		if (exists) {
			redirectAttributes.addFlashAttribute("errorMsg", "Car already booked for selected dates!");
			return "redirect:/user/booking-page/" + booking.getCarId();
		}

		LocalDate today = LocalDate.now();

		if (booking.getPickupDate().isBefore(today)) {
			redirectAttributes.addFlashAttribute("errorMsg", "Pickup date cannot be in the past!");
			return "redirect:/user/booking-page/" + booking.getCarId();
		}

		if (booking.getReturnDate().isBefore(booking.getPickupDate())) {
			redirectAttributes.addFlashAttribute("errorMsg", "Return date must be after pickup date!");
			return "redirect:/user/booking-page/" + booking.getCarId();
		}

		Car car = carRepository.findById(booking.getCarId()).orElse(null);

		if (car != null) {
			car.setAvailable(false);
			carRepository.save(car);
		}

		bookingRepository.save(booking); // ✅ IMPORTANT (you missed this)

		redirectAttributes.addFlashAttribute("booking", booking);

		return "redirect:/user/booking_success";
	}

	// =========================
	// 4. SUCCESS PAGE
	// =========================
	@GetMapping("/booking_success")
	public String bookingSuccess(Model model, @ModelAttribute("booking") Booking booking) {

		model.addAttribute("booking", booking);

		return "user/booking_success";
	}

	// =========================
	// 5. MY BOOKINGS PAGE (LIST)
	// =========================
	@GetMapping("/bookings")
	public String userBookings(Model model, Principal principal) {

		String email = principal.getName();

		List<Booking> bookings = bookingRepository.findByUserEmail(email);

		model.addAttribute("bookings", bookings);

		return "user/userbookings";
	}

	// =========================
	// CANCEL BOOKING
	// =========================
	@GetMapping("/cancel-booking/{id}")
	public String cancelBooking(@PathVariable Integer id, Principal principal, RedirectAttributes redirectAttributes) {

		Booking booking = bookingRepository.findById(id).orElse(null);

		// ✅ सुरक्षा: only logged-in user can cancel their booking
		if (booking != null && booking.getUserEmail().equals(principal.getName())) {

			// ✅ Make car available again
			Car car = carRepository.findById(booking.getCarId()).orElse(null);
			if (car != null) {
				car.setAvailable(true);
				carRepository.save(car);
			}

			// ✅ Delete booking
			bookingRepository.deleteById(id);

			redirectAttributes.addFlashAttribute("msg", "Booking cancelled successfully!");
		}

		return "redirect:/user/bookings";
	}
}