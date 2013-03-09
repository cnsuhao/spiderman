package org.eweb4j.spiderman.task;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import org.eweb4j.spiderman.url.UrlRuleChecker;

/**
 * 任务队列，阻塞+优先级排序
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-15 上午10:53:24
 */
public class TaskQueue {
	
	public void init(){
	}
	
	private PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<Task>(500, new Comparator<Task>(){
		public int compare(Task t1, Task t2) {
			if (t1.sort == t2.sort) return 0;
			return t1.sort < t2.sort ? 1 : -1;
		}
	});
	
	private Boolean isStop = false;
	
	public synchronized Task pollTask() throws Exception{
		if (isStop)
			return null;
		
		return queue.poll();
	}
	
	/**
	 * 将任务压入队列，注意，在此之前要保证任务不重复
	 * @date 2013-1-15 上午10:57:47
	 * @param task
	 * @return
	 */
	public synchronized boolean pushTask(Task task){
		if (isStop)
			return false;
		
		if (task == null)
			return false;
		
		if (null == task.url || task.url.trim().length() == 0)
			return false;
		
		//检查是否匹配xml配置的url规则
		if (!UrlRuleChecker.check(task.url, task.site.getQueueRules().getRule()))
			return false;
		return queue.add(task);
	}
	
	public synchronized void stop(){
		this.queue.clear();
		isStop = true;
	}
}
