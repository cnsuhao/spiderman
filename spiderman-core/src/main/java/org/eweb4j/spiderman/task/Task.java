package org.eweb4j.spiderman.task;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.spiderman.xml.site.Site;

public class Task {

	public Task(String url, String httpMethod, String sourceUrl, Site site, int sort) {
		super();
		this.url = url;
		this.sourceUrl = sourceUrl;
		this.site = site;
		this.sort = sort;
		this.httpMethod = httpMethod;
	}
	public Task(String url, String httpMethod, String sourceUrl, Object type,Site site, int sort) {
		super();
		this.url = url;
		this.sourceUrl = sourceUrl;
		this.site = site;
		this.sort = sort;
		this.httpMethod = httpMethod;
		this.type = type;
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
	public Object type;//任务类型;
	
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
	public Page getPage() {
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
	public double getSort() {
		return sort;
	}
	public void setSort(double sort) {
		this.sort = sort;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	public List<String> getDigNewUrls() {
		return digNewUrls;
	}
	public void setDigNewUrls(List<String> digNewUrls) {
		this.digNewUrls = digNewUrls;
	}
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public Object getType() {
		return type;
	}
	public void setType(Object type) {
		this.type = type;
	}
	public String toString() {
		return "Task [site=" + site.getName() + ", sort=" + sort + ", url=" + url + ", sourceUrl=" + sourceUrl + "]";
	}
}
