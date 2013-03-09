package org.eweb4j.spiderman.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.duplicate.DocIDServer;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.SourceUrlChecker;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;

public class DupRemovalPointImpl implements DupRemovalPoint{
	
	private SpiderListener listener;
	private Site site  = null;
	
	public void init(Site site, SpiderListener listener) {
		this.site = site;
		this.listener = listener;
		if (this.site.db == null) {
			this.site.db = new DocIDServer(site.getName(), listener);
			listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName() + " initial success...");
		}
	}

	public void destroy() {
		if (this.site.db != null) {
			this.site.db.close();
			this.site.db = null;
			listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName() + " destroy success...");
		}
	}
	
	public synchronized Collection<Task> removeDuplicateTask(Task task, Collection<String> newUrls, Collection<Task> tasks){
		if (this.site.db == null)
			return null;
		
		Collection<Task> validTasks = new ArrayList<Task>();
		for (String url : newUrls){
			Task newTask = new Task(url, task.url, site, 10);
			try {
				Target tgt = Util.isTargetUrl(newTask);
				boolean isFromSourceUrl = SourceUrlChecker.checkSourceUrl(site.getTargets().getTarget().get(0).getSourceRules(), newTask.sourceUrl);
				//如果是目标url，但不是来自来源url，跳过
				if (tgt != null && !isFromSourceUrl){
					continue;
				}
			}catch (Exception e){
				listener.onError(Thread.currentThread(), newTask, "", e);
			}
			
			//如果db里面不存在该url则认为是有效的task，否则认为是重复的task，要去掉
			int docId = this.site.db.getDocId(url);
			if (docId < 0){
				validTasks.add(newTask);
				this.site.db.newDocID(url);
			}
		}
		
		return validTasks;
	}

}
