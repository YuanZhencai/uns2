package com.wcs.uns.mdb;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;


/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
@MessageDriven(mappedName="jms/CAS2TDS", activationConfig={
	@ActivationConfigProperty(propertyName="acknowledgeMode", propertyValue="Auto-acknowledge")
})
public class TdsMDB implements MessageListener{
	
	Logger logger = Logger.getLogger("com.wcs.uns");
	
	public void onMessage(Message message) {
		logger.log(Level.INFO, ">>> Help app to register in TDS ...");
	}

}
