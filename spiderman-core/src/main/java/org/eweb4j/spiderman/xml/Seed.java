package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-4 下午08:00:53
 */
public class Seed {

	@AttrTag
	private String name;
	
	@AttrTag
	private String url;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
