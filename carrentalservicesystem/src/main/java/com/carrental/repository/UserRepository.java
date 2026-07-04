package com.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carrental.model.Booking;
import com.carrental.model.UserDtls;

public interface UserRepository extends JpaRepository<UserDtls  , Integer>{
	
	public UserDtls findByEmail(String email);

	public List<UserDtls> findByRole(String role);

	
	
	 
	 
}
