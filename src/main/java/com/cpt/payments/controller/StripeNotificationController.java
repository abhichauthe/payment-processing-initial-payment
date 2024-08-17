package com.cpt.payments.controller;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cpt.payments.adapter.StripeNotificationAdapter;
import com.cpt.payments.utils.LogMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/stripe")
public class StripeNotificationController {

	private static final Logger LOGGER = LogManager.getLogger(StripeNotificationController.class);
	
	@Autowired
	StripeNotificationAdapter adapter;
	
	@PostMapping("/notification")
	public int processNotification(HttpServletRequest request) throws IOException {
		LogMessage.setLogMessagePrefix("/PROCESS_STRIPE_NOTIFICATION");
		LogMessage.log(LOGGER, " Stripe Notification invoked");

		String jsonData = readJsonNotification(request);

		adapter.processNotification(jsonData);
		
		LogMessage.log(LOGGER, "Stripe Notification jsonData:" + jsonData);
		
		return 1;
	}

	private String readJsonNotification(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
	}

}
