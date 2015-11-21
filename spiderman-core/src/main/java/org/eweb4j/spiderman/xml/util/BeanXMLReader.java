package org.eweb4j.spiderman.xml.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eweb4j.util.ClassUtil;
import org.eweb4j.util.ReflectUtil;

@SuppressWarnings("all")
public class BeanXMLReader implements XMLReader {
	private String rootElementName;
	private String beanName;
	private File file;
	private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();

	public void setClass(Class<?> clazz) {
		this.setClass(this.beanName, clazz);
	}

	public void setClass(String key, Class<?> clazz) {
		if (ClassUtil.isPojo(clazz)) {

			Field[] fields = clazz.getDeclaredFields();
			for (Field f : fields) {
				if (List.class.isAssignableFrom(f.getType())) {
					ParameterizedType pt = (ParameterizedType) f
							.getGenericType();
					Type type = pt.getActualTypeArguments()[0];

					Class<?> cls = ClassUtil.getPojoClass(type.toString()
							.replace("class ", ""));

					setClass(f.getName(), cls);

				} else {
					setClass(f.getName(), f.getType());
				}
			}

			this.classes.put(key, clazz);
		}
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setRootElementName(String name) {
		this.rootElementName = name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public BeanXMLReader(File file) {
		this.setFile(file);
	}

	public BeanXMLReader() {
	}

	public String toXml() throws Exception {
		return createDoc(new ArrayList<Object>()).asXML();
	}

	public <T> List<T> read() throws Exception {
		List<T> tList = new ArrayList<T>();

		createDoc(tList);

		return tList;
	}

	private <T> Document createDoc(List<T> tList) throws DocumentException,
			Exception {
		SAXReader reader = new SAXReader();
		T t;
		Document doc = reader.read(this.file);
		// 列出beans下的所有bean元素节点
		String sub;
		if (this.beanName == null || this.beanName.trim().length() == 0)
			sub = BeanXMLConstant.SUBROOT_ELEMENT;
		else
			sub = this.beanName;

		if (this.rootElementName == null
				|| this.rootElementName.trim().length() == 0)
			this.rootElementName = BeanXMLConstant.ROOT_ELEMENT;

		List<?> list = doc.selectNodes("//" + this.rootElementName + "/" + sub);
		for (Iterator<?> it = list.iterator(); it.hasNext();) {
			Element bean = (Element) it.next();
			// 进入递归
			t = (T) this.readRecursion(bean);
			tList.add(t);
		}
		return doc;
	}

	public <T> T readOne() throws Exception {
		T t = null;
		List<T> list = this.read();
		if (list != null) {
			t = list.get(0);
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	private <T> T readRecursion(Element bean) throws Exception {
		Class<T> clazz = (Class<T>) this.classes.get(bean.getName());

		T o = clazz.newInstance();
		ReflectUtil ru = new ReflectUtil(o);
		Field[] fields = ru.getFields();
		for (Field f : fields) {
			String n = f.getName();
			Method m = ru.getSetter(n);
			if (m == null)
				continue;

			Skip skip = f.getAnnotation(Skip.class);
			if (skip != null)
				continue;
			
			AttrTag attrTag = f.getAnnotation(AttrTag.class);
			Writeonly writeonly = f.getAnnotation(Writeonly.class);

			if (writeonly != null)
				continue;

			if (attrTag != null) {
				if ("clazz".equals(n))
					n = "class";

				Attribute a = bean.attribute(n);
				if (a != null)
					m.invoke(o, a.getData());

			} else if (ClassUtil.isPojo(f.getType())) {
				Element el = bean.element(n);
				if (el == null)
					continue;

				String cls = this.classes.get(el.getName()).getName();

				Object object = Thread.currentThread().getContextClassLoader().loadClass(cls).newInstance();

				// 递归
				object = readRecursion(el);
				m.invoke(o, object);

			} else if (ClassUtil.isListClass(f)) {
				List<?> eList = bean.elements(n);
				if (eList == null)
					continue;

				List<Object> list = new ArrayList<Object>();
				for (Iterator<?> it = eList.iterator(); it.hasNext();) {
					Element e = (Element) it.next();

					// 递归
					list.add(readRecursion(e));
				}

				m.invoke(o, list);

			} else if (ClassUtil.isListString(f)) {
				List<?> eList = bean.elements(n);
				if (eList == null)
					continue;

				List<String> list = new ArrayList<String>();
				for (Iterator<?> it = eList.iterator(); it.hasNext();) {
					Element e = (Element) it.next();
					list.add(e.getText());
				}

				m.invoke(o, list);
			} else {
				if ("clazz".equals(n))
					n = "class";

				Element a = bean.element(n);
				if (a == null)
					continue;

				m.invoke(o, String.valueOf(a.getData()));
			}

		}

		return o;
	}
}
