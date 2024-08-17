package com.cpt.payments.service.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.cpt.payments.constants.ProviderEnum;
import com.cpt.payments.service.ProviderHandler;
import com.cpt.payments.service.impl.provider.handler.StripeProviderHandler;
import com.cpt.payments.utils.LogMessage;

@Component
public class ProviderHandlerFactory {

	private static final Logger LOGGER = LogManager.getLogger(ProviderHandlerFactory.class);

	@Autowired
	private ApplicationContext context;

	public ProviderHandler getProviderHandler(Integer providerId) {
		ProviderEnum providerEnum = ProviderEnum.getProviderEnumById(providerId);
		if(null == providerEnum) {
			LogMessage.log(LOGGER, " provider not found ");
			return null;
		}
		switch(providerEnum){
		
		case STRIPE:	
			return context.getBean(StripeProviderHandler.class);
		
		default:
			return null;
		}
	}
}
