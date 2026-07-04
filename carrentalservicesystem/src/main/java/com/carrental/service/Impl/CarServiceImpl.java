package com.carrental.service.Impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.carrental.model.Car;
import com.carrental.repository.CarRepository;
import com.carrental.service.CarService;

@Service
public class CarServiceImpl implements CarService {

	@Autowired
	private CarRepository carRepository;

	@Override
	public Car saveCar(Car car) {
		car.calculateDiscountPrice();
		carRepository.save(car);

		return carRepository.save(car);
	}

	@Override
	public List<Car> getAllCars() {
		return carRepository.findAll();
	}

	@Override
	public Boolean deleteCar(Integer id) {
		Car car = carRepository.findById(id).orElseGet(null);
		if (!ObjectUtils.isEmpty(car)) {
			carRepository.delete(car);
			return true;
		}
		return false;
	}

	@Override
	public Car getCarById(Integer id) {
		Car car = carRepository.findById(id).orElse(null);
		return car;
	}

	@Override
	public Car updateCar(Car car, MultipartFile image) {

	    Car dbCar = getCarById(car.getId());

	    dbCar.setTitle(car.getTitle());
	    dbCar.setDescription(car.getDescription());
	    dbCar.setCategory(car.getCategory());
	    dbCar.setFuelType(car.getFuelType());
	    dbCar.setPrice(car.getPrice());
	    dbCar.setAvailable(car.isAvailable());

	    // ✅ ADD THIS PART HERE (VERY IMPORTANT)
	    dbCar.setDiscount(car.getDiscount());

	    if (car.getDiscount() > 0) {
	        double discountPrice = car.getPrice() - (car.getPrice() * car.getDiscount() / 100.0);
	        dbCar.setDiscountPrice(discountPrice);
	    } else {
	        dbCar.setDiscountPrice(car.getPrice());
	    }
	    // ✅ END OF DISCOUNT LOGIC

	    if (!image.isEmpty()) {

	        String projectPath = System.getProperty("user.dir");
	        String uploadDir = projectPath + "/src/main/resources/static/images/cars/";

	        Path uploadPath = Paths.get(uploadDir);

	        try {
	            if (!Files.exists(uploadPath)) {
	                Files.createDirectories(uploadPath);
	            }

	            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

	            Path filePath = uploadPath.resolve(fileName);

	            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	            dbCar.setImageName(fileName);

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    }

	    Car updatedCar = carRepository.save(dbCar);

	    return updatedCar;
	}

	@Override
	public List<Car> getCarsByCategory(String category) {
		
		return carRepository.findByCategory(category);
	}
	
	@Override
	public List<Car> searchCars(String keyword) {
	    return carRepository.findByTitleContainingIgnoreCase(keyword);
	}
	

}
