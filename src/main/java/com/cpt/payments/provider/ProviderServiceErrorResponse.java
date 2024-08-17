package com.cpt.payments.provider;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProviderServiceErrorResponse {
	private String errorCode;
	private String errorMessage;
	private boolean tpProviderError;
}