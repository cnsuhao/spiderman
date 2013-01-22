package org.eweb4j.spiderman.fetcher;


public class Page {

	private String url;
	private String content;
	private byte[] contentData;
	private String contentType;
	private String encoding;
	private String charset;
	
	public String getUrl() {
		return this.url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return this.content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentType() {
		return this.contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getEncoding() {
		return this.encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getCharset() {
		return this.charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public byte[] getContentData() {
		return this.contentData;
	}

	public void setContentData(byte[] contentData) {
		this.contentData = contentData;
	}
	
}
