package org.eweb4j.spiderman.xml.db;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.container.Container;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.util.xml.AttrTag;

public class Db extends Component{
	//数据库适配器;
	private String adapter;
	//源数据库
	private Database sourceDatabase;
	
	@AttrTag
	private String type;
	
	@Override
	public void initPool() {
		listener.onInfo(Thread.currentThread(),null,"DB数据库组件线程池初始化完成...");
		
	}
	@Override
	public Component init(Container container, SpiderListener listener)throws Exception {
		this.container = container;
		this.listener = listener;
		listener.onInfo(Thread.currentThread(),null,"DB数据库组件初始化完成...");
		return null;
	}
	
	@Override
	public Component startup() {
		listener.onInfo(Thread.currentThread(),null,"DB数据库组件启动完成...");
		return null;
	}

	@Override
	public void destroy(SpiderListener listener, boolean isShutdownNow) {
		listener.onInfo(Thread.currentThread(),null,"DB数据库组件销毁了...");
	}

	public String getAdapter() {
		return adapter;
	}

	public void setAdapter(String adapter) {
		this.adapter = adapter;
	}

	public Database getSourceDatabase() {
		return sourceDatabase;
	}

	public void setSourceDatabase(Database sourceDatabase) {
		this.sourceDatabase = sourceDatabase;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
