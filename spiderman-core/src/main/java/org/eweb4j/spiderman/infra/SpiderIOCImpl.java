package org.eweb4j.spiderman.infra;

import org.eweb4j.ioc.IOC;


public class SpiderIOCImpl implements SpiderIOC{

	public <T> T createExtensionInstance(String name) {
		return IOC.getBean(name);
	}

}
