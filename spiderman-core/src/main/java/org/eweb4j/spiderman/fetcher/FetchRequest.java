package org.eweb4j.spiderman.fetcher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eweb4j.mvc.Http;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-7 下午05:28:08
 */
public class FetchRequest {

	private String url;
	private String httpMethod = Http.Method.GET;
	private Map<String, List<Object>> params = new HashMap<String, List<Object>>();
	private Map<String, List<File>> files = new HashMap<String, List<File>>();
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
	private Map<String, List<String>> cookies = new HashMap<String, List<String>>();
	
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
	public Map<String, List<String>> getCookies() {
		return this.cookies;
	}
	public void setCookies(Map<String, List<String>> cookies) {
		this.cookies = cookies;
	}
	public String getHttpMethod() {
		return this.httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	
	public Map<String, List<Object>> getParams() {
		return this.params;
	}
	public void setParams(Map<String, List<Object>> params) {
		this.params = params;
	}
	public Map<String, List<File>> getFiles() {
		return this.files;
	}
	public void setFiles(Map<String, List<File>> files) {
		this.files = files;
	}
	@Override
	public String toString() {
		return "FetchRequest [url=" + this.url + ", httpMethod="
				+ this.httpMethod + ", params=" + this.params + ", files="
				+ this.files + ", headers=" + this.headers + ", cookies="
				+ this.cookies + "]";
	}
	
}
