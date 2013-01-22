package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 下午08:10:09
 */
public class Cookie {

	@AttrTag
	private String name;
	
	@AttrTag
	private String value;
	
	@AttrTag
	private String host;
	
	@AttrTag
	private String path;

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

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
