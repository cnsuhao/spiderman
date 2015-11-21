package org.eweb4j.spiderman.fetcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Target;


public class FetchResult {

	private FetchRequest req;
	private int statusCode;
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
	private String fetchedUrl = null;
	private String movedToUrl = null;
	private Page page = null;//抓取结果信息;
	private Collection<Object> newUrls = null;//dig挖掘到新的资源Url
	private Collection<Task> validTasks;//挖掘到的有效的任务集合;
	private Target target = null;//是否有目标匹配当前Url;
	private List<Map<String, Object>> models = null;//已确认好的目标对象解析成为Map对象;
	private List<Object> pojos;//将解析好的Map数据映射为POJO
	public FetchResult(){};
	public FetchResult(FetchRequest request)
	{
		this.req = request;
	}
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
	
	public Collection<Object> getNewUrls() {
		return newUrls;
	}
	public void setNewUrls(Collection<Object> newUrls) {
		this.newUrls = newUrls;
	}
	
	public Collection<Task> getValidTasks() {
		return validTasks;
	}
	public void setValidTasks(Collection<Task> validTasks) {
		this.validTasks = validTasks;
	}
	
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
	
	public List<Map<String, Object>> getModels() {
		return models;
	}
	public void setModels(List<Map<String, Object>> models) {
		this.models = models;
	}
	
	public List<Object> getPojos() {
		return pojos;
	}
	public void setPojos(List<Object> pojos) {
		this.pojos = pojos;
	}
	@Override
	public String toString() {
		return "FetchResult [statusCode=" + this.statusCode + ", fetchedUrl="
				+ this.fetchedUrl + ", movedToUrl=" + this.movedToUrl + ", headers=" + this.headers + ", request=" + this.req + "]";
	}

}
