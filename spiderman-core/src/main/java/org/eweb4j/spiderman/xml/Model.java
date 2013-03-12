package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

public class Model {

	@AttrTag
	private String clazz ;
	
	@AttrTag
	private String isArray ;
	
	@AttrTag
	private String xpath;
	
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
}
