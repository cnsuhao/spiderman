package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.spider.SpiderListener;

public interface Point {

	public void init(Component component, SpiderListener listener);
	
	public void destroy();
	
}
