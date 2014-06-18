package org.eweb4j.spiderman.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.duplicate.DocIDServer;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.SourceUrlChecker;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;

public class DupRemovalPointImpl implements DupRemovalPoint {

	private SpiderListener listener;
	private Site site = null;

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

	public synchronized Collection<Task> removeDuplicateTask(Task task, Collection<String> newUrls, Collection<Task> tasks) {
		if (this.site.db == null)
			return null;

		Collection<Task> validTasks = new ArrayList<Task>();
		for (String url : newUrls) {
			Task newTask = new Task(url, null, task.url, site, 10);
			try {
				Target tgt = Util.matchTarget(newTask);
//				Rule fromSourceRule = SourceUrlChecker.checkSourceUrl(site.getTargets().getSourceRules(), newTask.sourceUrl);
//				//如果是目标url，但不是来自来源url，跳过
//				if (tgt != null && fromSourceRule == null) {
//					continue;
//				}

				// 默认是严格限制重复URL的访问，只要是重复的URL，都只能访问一次
				String docKey = CommonUtil.md5(newTask.url);

				// 如果配置的是不严格限制重复URL的访问，则将去重复判断的key变成 TargetUrl + SourceUrl
				// 这样就表示不同来源的TargetUrl，就算相同，也是可以访问的
				String isStrict = site.getIsDupRemovalStrict();
				if ("0".equals(isStrict) && tgt != null) {
					docKey = CommonUtil.md5(newTask.url + newTask.sourceUrl);
				}

				int docId = this.site.db.getDocId(docKey);
				if (docId < 0) {
					validTasks.add(newTask);
					this.site.db.newDocID(docKey);
				}
			} catch (Exception e) {
				listener.onError(Thread.currentThread(), newTask, "", e);
			}
		}

		return validTasks;
	}

}
