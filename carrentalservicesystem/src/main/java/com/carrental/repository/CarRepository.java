package com.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carrental.model.Car;

public interface CarRepository extends JpaRepository<Car , Integer> {
	
	List<Car> findByCategory(String category);

	List<Car> findByTitleContainingIgnoreCase(String keyword);

}
