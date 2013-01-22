package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

public class Impl {
	
	@AttrTag
	private String type;
	
	@AttrTag
	private String value;
	
	@AttrTag
	private String sort = "0";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "Impl [type=" + type + ", value=" + value + ", sort=" + sort
				+ "]";
	}
	
}
