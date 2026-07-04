package com.carrental.service.Impl;


import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.carrental.service.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


public class CommonServiceImpl implements CommonService {

	public void removeSessionMessage() {

		HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes()))
				.getRequest();
		HttpSession session = request.getSession();

		session.removeAttribute("SuccessMsg");
		session.removeAttribute("ErrorMsg");

	}

}
