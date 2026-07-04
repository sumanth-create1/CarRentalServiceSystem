package com.carrental.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carrental.model.Car;
import com.carrental.model.Category;
import com.carrental.model.UserDtls;
import com.carrental.repository.CarRepository;
import com.carrental.service.CarService;
import com.carrental.service.CategoryService;
import com.carrental.service.UserService;

import org.springframework.ui.Model;

@Controller
public class HomeController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CarService carService;
	
	
	@ModelAttribute 
	public void getUserDetails(Principal p, Model m) 
	{
		if(p != null)
		{
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user" , userDtls);
		} 
		
		List<Category> allCategory = categoryService.getAllCategory();
		m.addAttribute("categories",allCategory);
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/cars")
	public String cars(Model m,
	        @RequestParam(value = "category", defaultValue = "") String category,
	        @RequestParam(value = "keyword", defaultValue = "") String keyword) {

	    List<Category> categories = categoryService.getAllCategory();
	    List<Car> cars;

	    // 🔍 SEARCH BY NAME
	    if (!keyword.isEmpty()) {
	        cars = carService.searchCars(keyword);
	    }

	    // 📂 FILTER BY CATEGORY
	    else if (!category.isEmpty()) {
	        cars = carService.getCarsByCategory(category);
	    }

	    // 📋 SHOW ALL
	    else {
	        cars = carService.getAllCars();
	    }

	    m.addAttribute("categories", categories);
	    m.addAttribute("cars", cars);
	    m.addAttribute("selectedCategory", category);
	    m.addAttribute("keyword", keyword);

	    return "cars";
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user,
	                       @RequestParam("img") MultipartFile file,
	                       RedirectAttributes redirectAttributes) {

	    String imageName;

	    // 1️⃣ Set image name
	    if (!file.isEmpty()) {
	        imageName = file.getOriginalFilename();
	        user.setProfileImage(imageName);
	    } else {
	        imageName = "default.jpg";
	        user.setProfileImage(imageName);
	    }

	    // 2️⃣ Save file if uploaded
	    if (!file.isEmpty()) {
	        try {
	            File saveDir = new File("uploads/profile_img/");
	            if (!saveDir.exists()) {
	                saveDir.mkdirs();
	            }

	            Path path = Paths.get(saveDir.getAbsolutePath(), imageName);
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	            redirectAttributes.addFlashAttribute("successMsg", "Registration successful! Profile image uploaded.");
	        } catch (IOException e) {
	            e.printStackTrace();
	            redirectAttributes.addFlashAttribute("errorMsg", "File upload failed. Registration saved without image.");
	        }
	    } else {
	        redirectAttributes.addFlashAttribute("successMsg", "Registration successful! Default profile image used.");
	    }

	    // 3️⃣ Save user to database
	    userService.saveUser(user);

	    return "redirect:/register";
	}


	@GetMapping("/car/{id}")
	public String car(@PathVariable int id, Model m) {

	    Car car = carService.getCarById(id); // 🔥 fetch car

	    m.addAttribute("car", car); // 🔥 send to UI

	    return "view_car";
	}
	
	


	
}