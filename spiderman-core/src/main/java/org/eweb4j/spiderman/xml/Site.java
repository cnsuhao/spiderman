package org.eweb4j.spiderman.xml;

import java.util.Collection;

import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.EndPoint;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.ParsePoint;
import org.eweb4j.spiderman.plugin.PojoPoint;
import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.plugin.TaskPollPoint;
import org.eweb4j.spiderman.plugin.TaskPushPoint;
import org.eweb4j.spiderman.plugin.TaskSortPoint;
import org.eweb4j.spiderman.spider.Counter;
import org.eweb4j.spiderman.task.TaskQueue;
import org.eweb4j.util.xml.AttrTag;
import org.eweb4j.util.xml.Skip;


public class Site {

	@AttrTag
	private String name;//网站名
	
	@AttrTag
	private String url;//网站url
	
	@AttrTag
	private String reqDelay = "200";//每个请求的延迟时间
	
	@AttrTag 
	private String charset;//网站内容字符集
	
	@AttrTag
	private String enable = "1";//是否开启本网站的抓取
	
	@AttrTag
	private String schedule = "1h";//每隔多长时间重头爬起
	
	@AttrTag
	private String thread = "1";//线程数
	
	@AttrTag
	private String waitQueue = "1s";//当队列空的时候爬虫等待时间
	
	private Headers headers = new Headers();//HTTP头
	
	private Cookies cookies = new Cookies();//HTTP Cookie
	
	private Urls queueRules;//允许进入抓取队列的url规则
	
	private Targets targets ;//抓取目标
	
	private Plugins plugins;//插件
	
	//------------------------------------------
	@Skip
	public Boolean isStop = false;//每个网站都有属于自己的一个停止信号，用来标识该网站的状态是否停止完全
	@Skip
	public TaskQueue queue;//每个网站都有属于自己的一个任务队列容器
	@Skip
	public PageFetcher fetcher;//每个网站都有属于自己的一个抓取器
	@Skip
	public Counter counter;//针对本网站已完成的任务数量
	//------------------------------------------
	//--------------扩展点-----------------------
	@Skip
	public Collection<TaskPollPoint> taskPollPointImpls;
	@Skip
	public Collection<BeginPoint> beginPointImpls;
	@Skip
	public Collection<FetchPoint> fetchPointImpls;
	@Skip
	public Collection<DigPoint> digPointImpls;
	@Skip
	public Collection<DupRemovalPoint> dupRemovalPointImpls;
	@Skip
	public Collection<TaskSortPoint> taskSortPointImpls;
	@Skip
	public Collection<TaskPushPoint> taskPushPointImpls;
	@Skip
	public Collection<TargetPoint> targetPointImpls;
	@Skip
	public Collection<ParsePoint> parsePointImpls;
	@Skip
	public Collection<EndPoint> endPointImpls;
	@Skip
	public Collection<PojoPoint> pojoPointImpls;
	//-------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getReqDelay() {
		return this.reqDelay;
	}

	public void setReqDelay(String reqDelay) {
		this.reqDelay = reqDelay;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getWaitQueue() {
		return waitQueue;
	}

	public void setWaitQueue(String waitQueue) {
		this.waitQueue = waitQueue;
	}

	public Targets getTargets() {
		return targets;
	}

	public void setTargets(Targets targets) {
		this.targets = targets;
	}

	public Plugins getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

	public Urls getQueueRules() {
		return queueRules;
	}

	public void setQueueRules(Urls queueRules) {
		this.queueRules = queueRules;
	}

	public String getCharset() {
		return this.charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Headers getHeaders() {
		return this.headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}

	public Cookies getCookies() {
		return this.cookies;
	}

	public void setCookies(Cookies cookies) {
		this.cookies = cookies;
	}
	
}
