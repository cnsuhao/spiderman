package org.eweb4j.spiderman.fetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-7 下午05:28:08
 */
public class FetchRequest {

	private String url;
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
	
	public String getUrl() {
		return this.url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	@Override
	public String toString() {
		return "FetchRequest [url=" + this.url + ", headers=" + this.headers
				+ "]";
	}
	
}
