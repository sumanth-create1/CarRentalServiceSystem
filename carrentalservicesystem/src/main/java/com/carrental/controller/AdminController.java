package com.carrental.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityProperties.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carrental.service.CarService;
import com.carrental.service.CategoryService;
import com.carrental.service.UserService;

import jakarta.servlet.http.HttpSession;

import com.carrental.model.Booking;
import com.carrental.model.Car;
import com.carrental.model.Category;
import com.carrental.model.UserDtls;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CarService carService;
	
	@Autowired
	private BookingRepository bookingRepository;

	// ✅ Home page
	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	// ✅ Load add car page
	@GetMapping("/loadAddCar")
	public String loadAddCar(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);

		return "admin/add_car";
	}

	// ✅ Load category page
	@GetMapping("/category")
	public String category(Model m) {
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/category";
	}
	
	 @GetMapping("/rentals")
	    public String rentals(Model model) {

	        List<Booking> list = bookingRepository.findAll();

	        model.addAttribute("bookings", list);

	        return "admin/rentals";
	    }

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		String imageName = (!file.isEmpty()) ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if (existCategory) {
			redirectAttributes.addFlashAttribute("ErrorMsg", "Category already exists");
			return "redirect:/admin/category";
		}

		Category savedCategory = categoryService.saveCategory(category);

		if (ObjectUtils.isEmpty(savedCategory)) {
			redirectAttributes.addFlashAttribute("ErrorMsg", "Something went wrong");
		} else {

			if (!file.isEmpty()) {

				String uploadDir = "src/main/resources/static/uploads/category_img/";
				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				Path filePath = uploadPath.resolve(file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			}

			redirectAttributes.addFlashAttribute("SuccessMsg", "Saved Successfully");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, RedirectAttributes redirectAttributes) {

		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			redirectAttributes.addFlashAttribute("SuccessMsg", "Category deleted successfully");
		} else {
			redirectAttributes.addFlashAttribute("ErrorMsg", "Something went wrong");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {

		Category category = categoryService.getCategoryById(id);

		if (category == null) {
			return "redirect:/admin/category";
		}

		m.addAttribute("category", category);

		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category,
	                             @RequestParam("file") MultipartFile file,
	                             RedirectAttributes redirectAttributes) throws IOException {

	    Category oldCategory = categoryService.getCategoryById(category.getId());

	    if (oldCategory == null) {
	        redirectAttributes.addFlashAttribute("ErrorMsg", "Category not found");
	        return "redirect:/admin/category";
	    }

	    oldCategory.setName(category.getName());
	    oldCategory.setIsActive(category.getIsActive());

	    String uploadDir = "src/main/resources/static/uploads/category_img/";
	    File uploadPath = new File(uploadDir);

	    if (!uploadPath.exists()) {
	        uploadPath.mkdirs();
	    }

	    // update image only if new file uploaded
	    if (!file.isEmpty()) {

	        String imageName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

	        File saveFile = new File(uploadDir + imageName);
	        file.transferTo(saveFile);

	        oldCategory.setImageName(imageName);
	    }

	    categoryService.saveCategory(oldCategory);

	    redirectAttributes.addFlashAttribute("SuccessMsg", "Category Updated Successfully");

	    return "redirect:/admin/category";
	}

	@PostMapping("/saveCar")
	public String saveCar(@ModelAttribute Car car,
	                     @RequestParam("file") MultipartFile file,
	                     RedirectAttributes redirectAttributes) throws IOException {

	    // ✅ IMAGE NAME
	    String imageName = (!file.isEmpty()) ? file.getOriginalFilename() : "default.jpg";
	    car.setImageName(imageName);

	    // ✅ DISCOUNT LOGIC (DO THIS BEFORE SAVE)
	    double price = car.getPrice();
	    int discount = car.getDiscount();

	    double discountAmount = (price * discount) / 100;
	    double finalPrice = price - discountAmount;

	    car.setDiscountPrice(finalPrice);

	    // ✅ SAVE CAR
	    Car savedCar = carService.saveCar(car);

	    if (savedCar == null) {
	        redirectAttributes.addFlashAttribute("errorMsg", "Something went wrong");
	        return "redirect:/admin/loadAddCar";
	    }

	    // ✅ FILE UPLOAD
	    if (!file.isEmpty()) {

	        String projectPath = System.getProperty("user.dir");
	        String uploadDir = projectPath + "/src/main/resources/static/images/cars/";

	        Path uploadPath = Paths.get(uploadDir);

	        if (!Files.exists(uploadPath)) {
	            Files.createDirectories(uploadPath);
	        }

	        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

	        Path filePath = uploadPath.resolve(fileName);

	        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	        savedCar.setImageName(fileName);
	        carService.saveCar(savedCar);
	    }

	    redirectAttributes.addFlashAttribute("successMsg", "Car added successfully");

	    return "redirect:/admin/loadAddCar";
	}

	@GetMapping("/car")
	public String loadViewProduct(Model m) {
		m.addAttribute("cars", carService.getAllCars());
		return "/admin/car";
	}

	@GetMapping("/deleteCar/{id}")
	public String deleteCar(@PathVariable int id, RedirectAttributes redirectAttributes) {
		Boolean deleteCar = carService.deleteCar(id);

		if (deleteCar) {
			redirectAttributes.addFlashAttribute("SuccessMsg", "Category deleted successfully");
		} else {
			redirectAttributes.addFlashAttribute("ErrorMsg", "Something went wrong");
		}
		return "redirect:/admin/cars";
	}

	@GetMapping("/editCar/{id}")
	public String editCar(@PathVariable int id, Model m) {

		m.addAttribute("car", carService.getCarById(id));
		m.addAttribute("categories", categoryService.getAllCategory());

		return "admin/edit_Car";
	}
	
	@GetMapping("/updateCar/{id}")
	public String loadEditCar(@PathVariable int id, Model m) {

	    Car car = carService.getCarById(id);
	    List<Category> categories = categoryService.getAllCategory();

	    m.addAttribute("car", car);
	    m.addAttribute("categories", categories);

	    return "admin/edit_Car";
	}

	@PostMapping("/updateCar")
	public String updateCar(@ModelAttribute Car car,
	                        @RequestParam("file") MultipartFile file,
	                        RedirectAttributes redirectAttributes) {

	    Car updatedCar = carService.updateCar(car, file);

	    if (updatedCar != null) {
	        redirectAttributes.addFlashAttribute("successMsg", "Car updated successfully");
	    } else {
	        redirectAttributes.addFlashAttribute("errorMsg", "Something went wrong");
	    }

	    return "redirect:/admin/updateCar/" + car.getId();
	}
	
	@GetMapping("/users")
	public String getAllUsers(Model m) 
	{
		List<UserDtls> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users" , users);
		return "admin/users";
	}
	
	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(
	        @RequestParam Boolean status,
	        @RequestParam Integer id,
	        RedirectAttributes redirectAttributes) {

	    Boolean f = userService.updateAccountStatus(id, status);

	    if (f) {
	        redirectAttributes.addFlashAttribute("SuccessMsg", "Account status Updated");
	    } else {
	        redirectAttributes.addFlashAttribute("ErrorMsg", "Something went wrong on server");
	    }

	    return "redirect:/admin/users";
	}
	
	
	@GetMapping("/add-admin")
	public String addAdminPage(Model model) {
	    model.addAttribute("user", new UserDtls());
	    return "admin/add_admin"; // HTML file
	}
	
	

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user) {

	    user.setRole("ROLE_ADMIN");

	    user.setPassword(passwordEncoder.encode(user.getPassword())); // 🔥 MUST

	    user.setIsEnable(true); // if you have this field

	    userRepository.save(user);

	    return "redirect:/admin/add_admin";
	}
	
	
}
