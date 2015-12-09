package org.eweb4j.spiderman.fetcher;

import java.net.MalformedURLException;
import java.net.URL;

import org.eweb4j.util.CommonUtil;

public class Page {

	private String url;
	private String content;
	private byte[] contentData;
	private String contentType;
	private String encoding;
	private String charset;
	
	public String getHost() {
		if (CommonUtil.isBlank(url)) return null;
		try {
			URL url = new URL(this.url);
			String protocol = url.getProtocol();
			String host = url.getHost();
			int p = url.getPort();
			String port = p > 0 ? ":"+p : "";
			return protocol+"://"+host+port;
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public static void main(String[] args) {
		Page p = new Page();
		p.setUrl("http://news.baidu.com:80/test");
		System.out.println(p.getHost());
	}
	
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

	@Override
	public String toString() {
		return "Page [url=" + url + ", contentType=" + contentType + ", encoding=" + encoding + ", charset=" + charset
				+ "]";
	}

	public byte[] getContentData() {
		return this.contentData;
	}

	public void setContentData(byte[] contentData) {
		this.contentData = contentData;
	}
	
}
