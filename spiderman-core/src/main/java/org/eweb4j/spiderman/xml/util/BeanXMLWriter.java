package org.eweb4j.spiderman.xml.util;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.eweb4j.util.ClassUtil;
import org.eweb4j.util.ReflectUtil;

@SuppressWarnings("all")
public class BeanXMLWriter implements XMLWriter {

	private boolean isCheckStatck = false;
	private boolean isSubNameAuto = false;
	private String rootElementName = BeanXMLConstant.ROOT_ELEMENT;
	private String beanName = BeanXMLConstant.SUBROOT_ELEMENT;
	private File file;
	private Collection<?> list;

	private Set<String> pool = new HashSet<String>();

	private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();

	public void setClass(Class<?> clazz) {
		this.setClass(this.beanName, clazz);
	}

	public void setClass(String key, final Class<?> clazz) {
		if (key == null || clazz == null)
			return;

		if (ClassUtil.isPojo(clazz)) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field f : fields) {
				if (Collection.class.isAssignableFrom(f.getType())) {
					ParameterizedType pt = (ParameterizedType) f
							.getGenericType();
					Type type = pt.getActualTypeArguments()[0];

					Class<?> cls = ClassUtil.getPojoClass(type.toString()
							.replace("class ", ""));
					if (cls == null)
						continue;

					if (this.isCheckStatck) {
						if (pool.contains(cls.getName()))
							continue;

						pool.add(cls.getName());
					}

					setClass(f.getName(), cls);
				} else {
					if (this.isCheckStatck) {
						if (pool.contains(f.getType().getName()))
							continue;

						pool.add(f.getType().getName());
					}

					setClass(f.getName(), f.getType());
				}
			}

			this.classes.put(key, clazz);
		}
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setRootElementName(String rootElementName) {
		this.rootElementName = rootElementName;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return this.file;
	}

	public void setList(Collection<?> list) {
		this.list = list;
	}

	public Collection<?> getList() {
		return this.list;
	}

	public BeanXMLWriter() {
	}

	public BeanXMLWriter(File file) {
		this.setFile(file);
	}

	public BeanXMLWriter(File file, Collection<?> list) {
		this.setFile(file);
		this.setList(list);
	}

	public BeanXMLWriter(File file, Class<?>... clazzs) {
		this.setFile(file);
		Collection<Class<?>> list = new ArrayList<Class<?>>();
		for (Class<?> c : clazzs) {
			list.add(c);
		}

		this.setList(list);
	}

	public <T> BeanXMLWriter(File file, T... ts) {
		this.setFile(file);
		Collection<T> list = new ArrayList<T>();
		for (T t : ts) {
			if (Collection.class.isAssignableFrom(t.getClass()))
				list.addAll((Collection<? extends T>) t);
			else
				list.add(t);
		}

		this.setList(list);
	}

	public <T> BeanXMLWriter(File file, Class<T> clazz) {
		this.setFile(file);
		Collection<T> list = new ArrayList<T>();
		try {
			list.add(clazz.newInstance());
			this.setList(list);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public <T> BeanXMLWriter(File file, T t) {
		this.setFile(file);
		Collection<T> list = new ArrayList<T>();
		if (Collection.class.isAssignableFrom(t.getClass()))
			list.addAll((Collection<? extends T>) t);
		else
			list.add(t);
		this.setList(list);
	}

	public String toXml() throws Exception {
		return createDoc().asXML();
	}

	public File write() throws Exception {
		Document doc = createDoc();

		// 读取文件
		FileOutputStream fos = new FileOutputStream(this.file);
		// 设置文件编码
		OutputFormat format = OutputFormat.createPrettyPrint();
		// 创建写文件方法
		org.dom4j.io.XMLWriter xmlWriter = new org.dom4j.io.XMLWriter(fos,
				format);
		// 写入文件

		xmlWriter.write(doc);
		// 关闭
		fos.close();
		xmlWriter.close();
		return this.file;
	}

	private Document createDoc() throws Exception {
		Document doc = DocumentHelper.createDocument();
		if ((this.rootElementName == null || this.rootElementName.trim()
				.length() == 0) && this.list.size() == 1) {

			String name;
			if (this.beanName == null || this.beanName.trim().length() == 0)
				name = BeanXMLConstant.SUBROOT_ELEMENT;
			else
				name = this.beanName;

			Element bean = doc.addElement(name);
			for (Object t : this.list) {
				// 递归
				writeRecursion(bean, t);
			}

		} else {
			if (this.rootElementName == null
					|| this.rootElementName.trim().length() == 0)
				this.rootElementName = BeanXMLConstant.ROOT_ELEMENT;

			Element beans = doc.addElement(this.rootElementName);
			Element bean;
			String sub;
			if (this.beanName == null || this.beanName.trim().length() == 0)
				sub = BeanXMLConstant.SUBROOT_ELEMENT;
			else
				sub = this.beanName;

			for (Object t : this.list) {
				if (this.isSubNameAuto)
					bean = beans.addElement(t.getClass().getSimpleName()
							.toLowerCase());
				else
					bean = beans.addElement(sub);
				// 递归
				writeRecursion(bean, t);
			}
		}

		return doc;
	}

	private <T> void writeRecursion(final Element bean, T t) throws Exception {
		ReflectUtil ru = new ReflectUtil(t);
		Field[] fields = ru.getFields();
		for (Field f : fields) {
			String n = f.getName();
			Method m = ru.getGetter(n);
			if (m == null)
				continue;

			if ("clazz".equals(n))
				n = "class";

			Object obj = m.invoke(t);

			Skip skip = f.getAnnotation(Skip.class);
			if (skip != null)
				continue;

			AttrTag attrTag = f.getAnnotation(AttrTag.class);

			Readonly readonly = f.getAnnotation(Readonly.class);

			if (readonly != null)
				continue;

			if (ClassUtil.isPojo(f.getType())) {
				// 属性为class，进入递归
				Class<?> cls = this.classes.get(n);
				if (cls == null)
					continue;

				if (obj == null)
					obj = cls.newInstance();

				writeRecursion(bean.addElement(n), cls.cast(obj));

			} else if (attrTag != null) {
				if (obj == null)
					obj = "";

				bean.addAttribute(n, String.valueOf(obj));
			} else if (ClassUtil.isListClass(f)) {
				Collection<?> list = (Collection<?>) obj;
				Class<?> cls = this.classes.get(n);

				if (list.size() == 0 && cls != null)
					writeRecursion(bean.addElement(n), cls.newInstance());
				else
					for (Iterator<?> it = list.iterator(); it.hasNext();)
						writeRecursion(bean.addElement(n), it.next());

			} else if (ClassUtil.isListString(f)) {
				Collection<?> list = (Collection<?>) obj;
				for (Iterator<?> it = list.iterator(); it.hasNext();) {
					Object v2 = it.next();
					if (v2 == null)
						v2 = "";

					bean.addElement(n).addText(String.valueOf(v2));
				}
			} else {
				if (obj == null)
					obj = "";

				bean.addElement(n).addText(String.valueOf(obj));
			}

		}
	}

	public boolean isSubNameAuto() {
		return isSubNameAuto;
	}

	public void setSubNameAuto(boolean isSubNameAuto) {
		this.isSubNameAuto = isSubNameAuto;
	}

	public void setCheckStatck(boolean isCheckStatck) {
		this.isCheckStatck = isCheckStatck;
	}

}
