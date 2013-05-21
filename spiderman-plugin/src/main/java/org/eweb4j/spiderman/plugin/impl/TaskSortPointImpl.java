package org.eweb4j.spiderman.plugin.impl;

import java.util.Collection;

import org.eweb4j.spiderman.plugin.TaskSortPoint;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.SourceUrlChecker;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;

public class TaskSortPointImpl implements TaskSortPoint {

	private Site site;
	
	public void init(Site site, SpiderListener listener) {
		this.site = site;
	}

	public void destroy() {
	}
	
	public synchronized Collection<Task> sortTasks(Collection<Task> tasks) throws Exception {
		float i = 0f;
		for (Task task : tasks) {
			i += 0.00001;
			// 检查url是否符合target的url规则，并且是否是来自于来源url，如果符合排序调整为0
			Target tgt = Util.matchTarget(task);
			Rules rules = site.getTargets().getSourceRules();
			Rule fromSourceRule = SourceUrlChecker.checkSourceUrl(rules, task.sourceUrl);
			if (tgt != null && fromSourceRule != null){
				task.sort = 0 + CommonUtil.toDouble("0."+System.currentTimeMillis()) + i;
			}else{
				//检查url是否符合target的sourceUrl规则，如果符合排序调整为5，否则为10
				Rule sourceRule = SourceUrlChecker.checkSourceUrl(rules, task.url);
				if (sourceRule != null){
					task.sort = 5 + CommonUtil.toDouble("0."+System.currentTimeMillis()) + i;
				}else{
					task.sort = 10 + CommonUtil.toDouble("0."+System.currentTimeMillis()) + i;
				}
			}
		}

		return tasks;
	}

}
