package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.xml.Site;

public interface Point {

	public void init(Site site, SpiderListener listener);
	
	public void destroy();
	
}
