package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 下午08:10:09
 */
public class Cookies {

	private List<Cookie> cookie = new ArrayList<Cookie>();

	public List<Cookie> getCookie() {
		return this.cookie;
	}

	public void setCookie(List<Cookie> cookie) {
		this.cookie = cookie;
	}
	
}
