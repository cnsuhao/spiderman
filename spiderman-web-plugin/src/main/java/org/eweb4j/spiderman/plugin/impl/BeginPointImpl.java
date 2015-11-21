package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.xml.ValidHost;
import org.eweb4j.spiderman.xml.ValidHosts;
import org.eweb4j.util.CommonUtil;

public class BeginPointImpl implements BeginPoint{

	private SpiderListener listener;
	
	public FetchResult preProcess(FetchRequest request, FetchResult result) throws Exception{
		//如果不是在给定的合法host列表里则不给于抓取
		ValidHosts vhs = request.task.site.getValidHosts();
		if (vhs == null || vhs.getValidHost() == null || vhs.getValidHost().isEmpty()){
			if (!CommonUtil.isSameHost(request.task.site.getUrl(),request.task.url)) {
				listener.onInfo(Thread.currentThread(),request, "task.url->"+request.task.url+"'s host is not the same as site.host->" + request.task.site.getUrl());
				return null;
			}
		}else{
			boolean isOk = false;
			String taskHost = new URL(request.task.url).getHost();
			for (ValidHost h : vhs.getValidHost()){
				if (taskHost.equals(h.getValue())){
					isOk = true;
					break;
				}
			}
			
			if (!isOk)
				return null;
		}
		return result;
	}

	public void init(Component site, SpiderListener listener) {
		this.listener = listener;
	}

	public void destroy() {
	}

}
