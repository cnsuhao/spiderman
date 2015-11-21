package org.eweb4j.spiderman.xml.util;

import java.io.File;

public interface XMLWriter {
	File write() throws Exception;
	
	String toXml() throws Exception;

	void setClass(String key, Class<?> clazz);

	void setClass(Class<?> clazz);

	public void setBeanName(String beanName);

	public void setRootElementName(String name);
	
	public void setSubNameAuto(boolean flag);
	
	public void setCheckStatck(boolean isCheckStatck) ;
}
