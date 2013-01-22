package org.eweb4j.spiderman.fetcher;


public class FetchResult {

	private int statusCode;
	private String fetchedUrl = null;
	private String movedToUrl = null;
	private Page page = null;
	
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
	@Override
	public String toString() {
		return "FetchResult [statusCode=" + this.statusCode + ", fetchedUrl="
				+ this.fetchedUrl + ", movedToUrl=" + this.movedToUrl + "]";
	}

}
