package org.eweb4j.spiderman.fetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FetchResult {

	private FetchRequest req;
	private int statusCode;
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
	private String fetchedUrl = null;
	private String movedToUrl = null;
	private Page page = null;
	
	public FetchRequest getReq() {
		return this.req;
	}
	public void setReq(FetchRequest req) {
		this.req = req;
	}
	public int getStatusCode() {
		return this.statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getFetchedUrl() {
		return this.fetchedUrl;
	}
	public void setFetchedUrl(String fetchedUrl) {
		this.fetchedUrl = fetchedUrl;
	}
	public String getMovedToUrl() {
		return this.movedToUrl;
	}
	public void setMovedToUrl(String movedToUrl) {
		this.movedToUrl = movedToUrl;
	}
	public Page getPage() {
		return this.page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
	
	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	@Override
	public String toString() {
		return "FetchResult [statusCode=" + this.statusCode + ", fetchedUrl="
				+ this.fetchedUrl + ", movedToUrl=" + this.movedToUrl + ", headers=" + this.headers + ", request=" + this.req + "]";
	}

}
