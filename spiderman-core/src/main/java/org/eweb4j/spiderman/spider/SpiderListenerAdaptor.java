package org.eweb4j.spiderman.spider;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.task.Task;

/**
 * 爬虫监听适配器
 * @author weiwei l.weiwei@163.com
 * @author wchao wchaojava@163.com
 * @date 2013-1-7 上午11:39:57
 */
public class SpiderListenerAdaptor implements SpiderListener{
	public void onDigUrls(Thread thread, Task task, String fieldName, Collection<Object> urls) {}
	public void onFetch(Thread thread, FetchRequest request, FetchResult result) {}
	public void onNewUrls(Thread thread, FetchRequest request, Collection<Object> newUrls) {}
	public void onDupRemoval(Thread currentThread, FetchRequest request, Collection<Task> validTasks) {}
	public void onTaskSort(Thread currentThread, FetchRequest request, Collection<Task> afterSortTasks) {}
	public void onNewTasks(Thread thread, FetchRequest request, Collection<Task> newTasks) {}
	public void onTargetPage(Thread thread, FetchRequest request, Page page) {}
	public void onParse(Thread thread, FetchRequest request, List<Map<String, Object>> models) {}
	public void onPojo(Thread thread, FetchRequest request, List<Object> pojos) {}
	public void onInfo(Thread thread, FetchRequest request, String info) {}
	public void onStartup(Component component) {}
	public void onError(Thread thread, Task task, String err, Throwable e) {e.printStackTrace();}
	public void onInitError(Component component, String err, Throwable e){e.printStackTrace();}
	public void onAfterScheduleCancel() {}
	public void onBeforeEveryScheduleExecute(Date theLastTimeScheduledAt){}
	public void onBeforeShutdown(Object... args) {}
	public void onAfterShutdown(Object... args) {}
	public void onBeforeShutdown(Component component, Object... args) {}
	public void onAfterShutdown(Component component, Object... args) {}
    public void onParseField(Thread thread, FetchRequest request, Object selector, String field, Object value) {}
    public void onParseOne(Thread thread, FetchRequest request, int size, int index, Map<String, Object> model) {}
}
