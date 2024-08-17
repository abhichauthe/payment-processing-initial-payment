package com.cpt.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessPayment {
	private long transactionId;
	private String firstName;
	private String lastName;
	private String email;
	private String productDescription;
	private String successUrl; 
	private Long quantity;
	private String cancelUrl;
	
}
