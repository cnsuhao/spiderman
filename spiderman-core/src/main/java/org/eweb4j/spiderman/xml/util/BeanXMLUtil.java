package org.eweb4j.spiderman.xml.util;

import java.io.File;
import java.util.List;


/**
 * 
 * @author weiwei
 *
 */
public class BeanXMLUtil {
	
	public static XMLReader getBeanXMLReader(){
		return new BeanXMLReader();
	}
	
	public static XMLReader getBeanXMLReader(File file) {
		return new BeanXMLReader(file);
	}

	public static <T> XMLWriter getBeanXMLWriter(T... t){
		return new BeanXMLWriter(null, t);
	}
	
	public static <T> XMLWriter getBeanXMLWriter(Class<T>... clazz){
		return new BeanXMLWriter(null, clazz);
	}
	
	public static XMLWriter getBeanXMLWriter(File file, List<?> list) {
		return new BeanXMLWriter(file, list);
	}

	public static XMLWriter getBeanXMLWriter(File file, Class<?> clazz) {
		return new BeanXMLWriter(file, clazz);
	}

	public static XMLWriter getBeanXMLWriter(File file, Object obj) {
		return new BeanXMLWriter(file, obj);
	}
	
	public static XMLWriter getBeanXMLWriter(File file, Object... objs) {
		return new BeanXMLWriter(file, objs);
	}
	
	public static XMLWriter getBeanXMLWriter(File file, Class<?>... clazzs) {
		return new BeanXMLWriter(file, clazzs);
	}
}
