package org.eweb4j.spiderman.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.duplicate.DocIDServer;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.spiderman.xml.site.Site;
import org.eweb4j.util.CommonUtil;

public class DupRemovalPointImpl implements DupRemovalPoint {

	private SpiderListener listener;
	private Site site = null;

	public void init(Component site, SpiderListener listener) {
		this.site =(Site)site;
		this.listener = listener;
		if (this.site.removaldb == null) {
			this.site.removaldb = new DocIDServer(this.site.getName(), listener);
			listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + this.site.getName() + " initial success...");
		}
	}

	public void destroy() {
		if (this.site.removaldb != null) {
			this.site.removaldb.close();
			this.site.removaldb = null;
			listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName() + " destroy success...");
		}
	}

	public synchronized Collection<Task> removeDuplicateTask(FetchRequest request,FetchResult result) {
		if (this.site.removaldb == null)
			return null;

		Collection<Task> validTasks = new ArrayList<Task>();
		result.setValidTasks(validTasks);
		for (Object url : result.getNewUrls()) {
			Task newTask = new Task((String)url, null,request.task.url, site, 10);
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

				int docId = this.site.removaldb.getDocId(docKey);
				if (docId < 0) {
					validTasks.add(newTask);
					this.site.removaldb.newDocID(docKey);
				}
			} catch (Exception e) {
				listener.onError(Thread.currentThread(), newTask, "", e);
			}
		}

		return validTasks;
	}

}
