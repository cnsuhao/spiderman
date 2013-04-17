package org.eweb4j.spiderman.task;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;



public class Task {

	public Task(String url, String sourceUrl, Site site, int sort) {
		super();
		this.url = url;
		this.sourceUrl = sourceUrl;
		this.site = site;
		this.sort = sort;
	}

	public Site site ;
	public Target target;
	public double sort = 10;
	public String url;
	public String sourceUrl;//task.url的来源
	public List<String> digNewUrls = new ArrayList<String>();
//	public List<Header> headers = new ArrayList<Header>();
//	public List<Cookie> cookies = new ArrayList<Cookie>();
	
	public String toString() {
		return "Task [site=" + site.getName() + ", sort=" + sort + ", url=" + url + ", sourceUrl=" + sourceUrl + "]";
	}
}
