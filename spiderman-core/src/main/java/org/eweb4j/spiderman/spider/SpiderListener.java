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

public interface SpiderListener {

	void onDigUrls(Thread thread, Task task, String fieldName, Collection<Object> urls);
	
    void onNewUrls(Thread thread, FetchRequest request, Collection<Object> newUrls);
	
    void onFetch(Thread thread, FetchRequest request, FetchResult result);
    
    void onDupRemoval(Thread currentThread, FetchRequest request, Collection<Task> validTasks);
    
    void onTaskSort(Thread currentThread, FetchRequest request, Collection<Task> afterSortTasks);
    
	void onNewTasks(Thread thread, FetchRequest request, Collection<Task> newTasks);
	
	void onTargetPage(Thread thread, FetchRequest request, Page page);
	
	void onParseField(Thread thread, FetchRequest request, Object selector, String field, Object value);
	
	void onParseOne(Thread thread, FetchRequest request, int size, int index, Map<String, Object> model);
	
	void onParse(Thread thread, FetchRequest request, List<Map<String, Object>> models);
	
	void onPojo(Thread thread, FetchRequest request, List<Object> pojos);

	void onInfo(Thread thread, FetchRequest request, String info);
	
	void onStartup(Component component);
	
	void onError(Thread thread, Task task, String err, Throwable e);
	
	void onInitError(Component componen, String err, Throwable e);
	
	/**
	 * 调度结束后回调此方法
	 * @date 2013-4-1 下午03:17:23
	 */
	void onAfterScheduleCancel();
	
	/**
	 * 每次调度执行前回调此方法
	 * @date 2013-4-1 下午03:33:11
	 * @param theLastTimeScheduledAt 上一次调度时间
	 */
	void onBeforeEveryScheduleExecute(Date theLastTimeScheduledAt);
	
	/**
	 * 
	 * Spiderman.shutdown()被调用之前回调此方法
	 * @date 2013-6-3 下午05:00:43
	 */
	void onBeforeShutdown(Object... args);
	
	/**
	 * Spiderman.shutdown()被调用之后回调此方法
	 * @date 2013-6-3 下午05:01:02
	 */
	void onAfterShutdown(Object... args);
	
	/**
	 * 
	 * Spiderman.shutdown()被调用之前回调此方法
	 * @date 2013-6-3 下午05:00:43
	 */
	void onBeforeShutdown(Component component, Object... args);
	
	/**
	 * Spiderman.shutdown()被调用之后回调此方法
	 * @date 2013-6-3 下午05:01:02
	 */
	void onAfterShutdown(Component componen, Object... args);
}
