package org.eweb4j.spiderman.infra;


public interface SpiderIOC {

	public <T> T createExtensionInstance(String name);
	
}
