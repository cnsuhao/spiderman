package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eweb4j.spiderman.plugin.TaskPushPoint;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.SourceUrlChecker;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.spiderman.xml.ValidHost;
import org.eweb4j.spiderman.xml.ValidHosts;
import org.eweb4j.util.CommonUtil;

public class TaskPushPointImpl implements TaskPushPoint{
	
	private SpiderListener listener;
	private Site site;
	
	public void init(Site site, SpiderListener listener) {
		this.listener = listener;
		this.site = site;
	}

	public void destroy() {
	}
	
	public synchronized Collection<Task> pushTask(Collection<Task> validTasks) throws Exception{
		Collection<Task> newTasks = new ArrayList<Task>();
		for (Task task : validTasks){
			try{
				//如果不是在给定的合法host列表里则不给于抓取
				ValidHosts vhs = task.site.getValidHosts();
				if (vhs == null || vhs.getValidHost() == null || vhs.getValidHost().isEmpty()){
					if (!CommonUtil.isSameHost(task.site.getUrl(), task.url)) {
						listener.onInfo(Thread.currentThread(), task, "task.url->"+task.url+"'s host is not the same as site.host->" + task.site.getUrl());
						continue;
					}
				}else{
					boolean isOk = false;
					String taskHost = new URL(task.url).getHost();
					for (ValidHost h : vhs.getValidHost()){
						if (taskHost.equals(h.getValue())){
							isOk = true;
							break;
						}
					}
					
					if (!isOk) {
					    listener.onInfo(Thread.currentThread(), task, "task.url->"+task.url+"'s host is not in the validHosts");
						continue;
					}
				}
				
				boolean isValid = false;
				try {
					//如果是目标url且是从sourceUrl来的，就是有效的
					Target tgt = Util.matchTarget(task);
					Rules rules = site.getTargets().getSourceRules();
//					Rule fromSourceRule = SourceUrlChecker.checkSourceUrl(rules, task.sourceUrl);
					if (tgt != null 
//					        && fromSourceRule != null
					        ){
						isValid = true;
					}
					
					//如果它本身就是sourceUrl，也应该是有效的
					Rule sourceRule = SourceUrlChecker.checkSourceUrl(rules, task.url);
					if (sourceRule != null){
						isValid = true;
					}
				} catch (Exception e){
					listener.onError(Thread.currentThread(), task, "", e);
				}
				
				String sIsStrict = site.getQueueRules().getIsStrict();
				boolean isStrict = true;
				if ("0".equals(sIsStrict) || "false".equals(sIsStrict))
					isStrict = false;
				
				//如果是有效的，或者是不严格的规则那么任务都可以进入队列
				if (isValid || !isStrict) {
					boolean isOk = task.site.queue.pushTask(task);
					if (isOk)
						newTasks.add(task);
					
//					System.out.println("push task->"+task+" push the queue ... result -> " + isOk);
				}
			}catch(Exception e){
				continue;
			}
		}
		
		return newTasks;
	}
	
}
