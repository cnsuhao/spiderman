package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;

import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.ValidHost;
import org.eweb4j.spiderman.xml.ValidHosts;
import org.eweb4j.util.CommonUtil;

public class BeginPointImpl implements BeginPoint{

	private SpiderListener listener;
	
	public Task confirmTask(Task task) throws Exception{
		//如果不是在给定的合法host列表里则不给于抓取
		ValidHosts vhs = task.site.getValidHosts();
		if (vhs == null || vhs.getValidHost() == null || vhs.getValidHost().isEmpty()){
			if (!CommonUtil.isSameHost(task.site.getUrl(), task.url)) {
				listener.onInfo(Thread.currentThread(), task, "task.url->"+task.url+"'s host is not the same as site.host->" + task.site.getUrl());
				return null;
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
			
			if (!isOk)
				return null;
		}
		
		return task;
	}

	public void init(Site site, SpiderListener listener) {
		this.listener = listener;
	}

	public void destroy() {
	}

}
