package org.eweb4j.spiderman.xml.util;

import java.util.List;

public interface XMLReader {
	<T> List<T> read() throws Exception;

	<T> T readOne() throws Exception;
	
	String toXml() throws Exception;

	public void setClass(String key, Class<?> clazz);
	
	public void setClass(Class<?> clazz);
	
	public void setBeanName(String beanName);
	
	public void setRootElementName(String name);
}
