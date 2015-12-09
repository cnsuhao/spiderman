package org.eweb4j.spiderman.spider;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;

/**
 * 爬虫监听适配器
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 上午11:39:57
 */
public class SpiderListenerAdaptor implements SpiderListener{
	public void onInfo(Thread thread, Task task, String info) {System.out.println("\r\n[INFO]:::"+info);}
	public void onDigUrls(Thread thread, Task task, String fieldName, Collection<String> urls) {System.out.println("\r\n[DIG]:::"+fieldName+", "+urls.size()+", \r\n\t"+urls+"\r\n\t FROM " + task.url);}
	public void onFetch(Thread thread, Task task, FetchResult result) {System.out.println("\r\n[FETCH]:::\r\n\turl:"+result.getFetchedUrl()+", \r\n\tstatusCode:"+result.getStatusCode()+", \r\n\tmove_to:"+result.getMovedToUrl());}
	public void onNewUrls(Thread thread, Task task, Collection<String> newUrls) {}
	public void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks) {}
	public void onTaskSort(Thread currentThread, Task task, Collection<Task> afterSortTasks) {}
	public void onNewTasks(Thread thread, Task task, Collection<Task> newTasks) {System.out.println("\r\n[NEW-TASK]:::"+newTasks.size());}
	public void onTargetPage(Thread thread, Task task, Page page){System.out.println("\r\n[TARGET]:::"+task.target.getName()+"\r\n\t:::"+page);}
	public void onParse(Thread thread, Task task, List<Map<String, Object>> models){System.out.println("\r\n[PARSE]:::"+task.target.getName()+"\r\n\t:::"+models+"\r\n\t from"+task.url);}
	public void onPojo(Thread thread, Task task, List<Object> pojos) {}
	public void onStartup(Site site) {}
	public void onError(Thread thread, Task task, String err, Throwable e) {System.err.println("\r\n[ERROR]:::"+task.url+", " + err);}
	public void onInitError(Site site, String err, Throwable e){e.printStackTrace();}
	public void onAfterScheduleCancel() {}
	public void onBeforeEveryScheduleExecute(Date theLastTimeScheduledAt){}
	public void onBeforeShutdown(Object... args) {}
	public void onAfterShutdown(Object... args) {}
	public void onBeforeShutdown(Site site, Object... args) {}
	public void onAfterShutdown(Site site, Object... args) {}
    public void onParseField(Thread thread, Task task, Object selector, String field, Object value) {System.out.println("\r\n[PARSE-FIELD]:::"+task.target.getName()+"\r\n\t:::"+field+"=>"+value);}
    public void onParseOne(Thread thread, Task task, int size, int index, Map<String, Object> model) {}
}
