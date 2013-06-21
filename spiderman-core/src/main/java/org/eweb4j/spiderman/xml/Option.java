package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * @author weiwei l.weiwei@163.com
 * @date 2013-6-9 上午10:31:31
 */
public class Option {

	@AttrTag
	private String name;

	@AttrTag
	private String value;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
