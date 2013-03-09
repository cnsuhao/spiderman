package org.eweb4j.spiderman.spider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.task.Task;

/**
 * 爬虫监听适配器
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 上午11:39:57
 */
public class SpiderListenerAdaptor implements SpiderListener{
	public void onFetch(Thread thread, Task task, FetchResult result) {}
	public void onNewUrls(Thread thread, Task task, Collection<String> newUrls) {}
	public void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks) {}
	public void onTaskSort(Thread currentThread, Task task, Collection<Task> afterSortTasks) {}
	public void onNewTasks(Thread thread, Task task, Collection<Task> newTasks) {}
	public void onTargetPage(Thread thread, Task task, Page page) {}
	public void onParse(Thread thread, Task task, List<Map<String, Object>> models) {}
	public void onPojo(Thread thread, Task task, List<Object> pojos) {}
	public void onInfo(Thread thread, Task task, String info) {}
	public void onError(Thread thread, Task task, String err, Exception e) {}
}
