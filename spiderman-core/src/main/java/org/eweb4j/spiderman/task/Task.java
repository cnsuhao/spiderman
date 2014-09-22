package org.eweb4j.spiderman.task;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;



public class Task {

	public Task(String url, String httpMethod, String sourceUrl, Site site, int sort) {
		super();
		this.url = url;
		this.sourceUrl = sourceUrl;
		this.site = site;
		this.sort = sort;
		this.httpMethod = httpMethod;
	}
	
	public Task(Site site) {
	    this.site = site;
	}

	public Site site ;
	public Target target;
	public Page page;
	public double sort = 10;
	public String url;
	public String sourceUrl;//task.url的来源
	public List<String> digNewUrls = new ArrayList<String>();
	public String httpMethod;
//	public List<Header> headers = new ArrayList<Header>();
//	public List<Cookie> cookies = new ArrayList<Cookie>();
	
	public String toString() {
		return "Task [site=" + site.getName() + ", sort=" + sort + ", url=" + url + ", sourceUrl=" + sourceUrl + "]";
	}
}
