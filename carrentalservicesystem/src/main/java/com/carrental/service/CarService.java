package com.carrental.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.carrental.model.Car;
import com.carrental.model.Category;

public interface CarService {
	
	public Car saveCar(Car car);
	
	public List<Car> getAllCars();
	
	public Boolean deleteCar(Integer id);
	
	public Car getCarById(Integer id);
	
	public Car updateCar(Car car , MultipartFile file);
	
	public List<Car> getCarsByCategory(String category);
	
	List<Car> searchCars(String keyword);

	   

}
 