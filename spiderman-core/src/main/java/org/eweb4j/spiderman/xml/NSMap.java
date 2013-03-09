package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-1 下午04:45:33
 */
public class NSMap {

	@AttrTag
	private String prefix;
	
	@AttrTag
	private String uri;

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
