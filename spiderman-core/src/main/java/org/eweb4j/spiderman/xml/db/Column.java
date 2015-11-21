package org.eweb4j.spiderman.xml.db;

import org.eweb4j.util.xml.AttrTag;

public class Column {
	//列名称;
	@AttrTag
	public String name;
	//列值
	@AttrTag
	public String value;
	//就列名称;
	public String oldname;
	//列类型
	@AttrTag
	public String type;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
