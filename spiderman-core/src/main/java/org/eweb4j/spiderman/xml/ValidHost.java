package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-8 上午11:18:09
 */
public class ValidHost {

	@AttrTag
	private String value;

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ValidHost [value=" + this.value + "]";
	}
	
}
