package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

public class Model {

	@AttrTag
	private String clazz ;
	
	/**
	 * 目标的contentType
	 */
	@AttrTag
	private String cType;
	
	/**
	 * 如果页面是html类型，是否强制使用XML的解析器来解析xpath
	 */
	@AttrTag
	private String isForceUseXmlParser;
	
	/**
	 * 是否忽略<!---->注释内容,默认不忽略
	 */
	@AttrTag
	private String isIgnoreComments;
	
	/**
	 * XML命名空间设置
	 */
	private Namespaces namespaces;
	
	@AttrTag
	private String isArray ;
	
	@AttrTag
	private String xpath;
	
	@AttrTag
    private String parser;
	
	@AttrTag
	private String delay;
	
	private List<Field> field = new ArrayList<Field>();

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public List<Field> getField() {
		return field;
	}

	public void setField(List<Field> field) {
		this.field = field;
	}

	public String getIsArray() {
		return this.isArray;
	}

	public void setIsArray(String isArray) {
		this.isArray = isArray;
	}

	public String getXpath() {
		return this.xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	
	public boolean isArrayField(String fieldName){
		for (Field f : this.field){
			if (!fieldName.equals(f.getName()))
				continue;
			if ("1".equals(f.getIsArray()))
				return true;
		}
		
		return false;
	}
	
	public boolean isAlsoParseInNextPageField(String fieldName){
		for (Field f : this.field){
			if (!fieldName.equals(f.getName()))
				continue;
			if ("1".equals(f.getIsAlsoParseInNextPage()))
				return true;
		}
		
		return false;
	}
	
	public List<Field> getIsAlsoParseInNextPageFields(){
		List<Field> fields = new ArrayList<Field>();
		for (Field f : this.field){
			if (!"1".equals(f.getIsAlsoParseInNextPage()))
				continue;
			fields.add(f);
		}
		
		return fields;
	}
	
	public String getIsIgnoreComments() {
		return this.isIgnoreComments;
	}

	public void setIsIgnoreComments(String isIgnoreComments) {
		this.isIgnoreComments = isIgnoreComments;
	}

	public String getCType() {
		return cType;
	}

	public void setCType(String cType) {
		this.cType = cType;
	}

	public String getIsForceUseXmlParser() {
		return isForceUseXmlParser;
	}

	public void setIsForceUseXmlParser(String isForceUseXmlParser) {
		this.isForceUseXmlParser = isForceUseXmlParser;
	}

	public Namespaces getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(Namespaces namespaces) {
		this.namespaces = namespaces;
	}

    public String getParser() {
        return this.parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }

    public String getDelay() {
        return this.delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }
	
}
