package org.eweb4j.spiderman.xml.file;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.container.Container;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.util.xml.AttrTag;

public class File extends Component{
	@AttrTag
	private String type;
	
	@Override
	public void initPool() {
		listener.onInfo(Thread.currentThread(),null,"File文件系统组件线程池初始化完成...");
		
	}
	@Override
	public Component init(Container container, SpiderListener listener)throws Exception {
		this.container = container;
		this.listener = listener;
		listener.onInfo(Thread.currentThread(),null,"File文件系统组件初始化完成...");
		return null;
	}
	
	@Override
	public Component startup() {
		listener.onInfo(Thread.currentThread(),null,"File文件系统组件启动完成...");
		return null;
	}

	@Override
	public void destroy(SpiderListener listener, boolean isShutdownNow) {
		listener.onInfo(Thread.currentThread(),null,"File文件系统组件销毁了...");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
