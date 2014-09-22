package org.eweb4j.spiderman.xml;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.EndPoint;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.ParsePoint;
import org.eweb4j.spiderman.plugin.Point;
import org.eweb4j.spiderman.plugin.PojoPoint;
import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.plugin.TaskPollPoint;
import org.eweb4j.spiderman.plugin.TaskPushPoint;
import org.eweb4j.spiderman.plugin.TaskSortPoint;
import org.eweb4j.spiderman.spider.Counter;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.TaskDbServer;
import org.eweb4j.spiderman.task.TaskQueue;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.RegexList;
import org.eweb4j.util.xml.AttrTag;
import org.eweb4j.util.xml.Skip;


public class Site {

	@AttrTag
	private String name;//网站名
	
	@AttrTag
	private String code;//网站编码
	
	@AttrTag
	private String country;//网站所属国家
	
	@AttrTag
	private String isDupRemovalStrict;//是否严格去掉重复的TargetUrl，即已访问过一次的TargetUrl不会再被访问，若否，就算是重复的TargetUrl，只要它的来源URL不同，都会被访问
	
	@AttrTag
	private String isFollowRedirects;//是否自动跟随重定向网页，默认是
	
	private ValidHosts validHosts;//限制在这些host里面抓取数据
	
	private Options options;//一些其他的业务数据
	
	@AttrTag
	private String url;//网站url
	
	@AttrTag
	private String httpMethod;
	
	@AttrTag
	private String userAgent = "Spiderman[https://git.oschina.net/l-weiwei/spiderman]";//爬虫一些标识
	
	@AttrTag
	private String includeHttps; //是否抓取https页
	
	@AttrTag
	private String skipStatusCode;//设置忽略哪些状态码，例如设置为500,那么针对这个网站的访问请求，就算返回500状态码，依然会去解析相应内容
	
	@AttrTag
	private String timeout;//HTTP请求最大等待时间
	
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

	private Seeds seeds ;
	
	private Headers headers = new Headers();//HTTP头
	
	private Cookies cookies = new Cookies();//HTTP Cookie
	
	private Rules queueRules;//允许进入抓取队列的url规则
	
	private Targets targets ;//抓取目标
	
	private Plugins plugins;//插件
	
	@AttrTag
	private String downloader;
	
	//------------------------------------------
	@Skip
	public TaskDbServer db = null;//每个网站都有属于自己的一个任务去重DB服务
	@Skip
	public ExecutorService pool;//每个网站都有属于自己的一个线程池
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
	
	public String getTimeout() {
		return this.timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIsFollowRedirects() {
		return this.isFollowRedirects;
	}

	public void setIsFollowRedirects(String isFollowRedirects) {
		this.isFollowRedirects = isFollowRedirects;
	}

	public String getIsDupRemovalStrict() {
		return isDupRemovalStrict;
	}

	public void setIsDupRemovalStrict(String isDupRemovalStrict) {
		this.isDupRemovalStrict = isDupRemovalStrict;
	}

	public ValidHosts getValidHosts() {
		return this.validHosts;
	}

	public void setValidHosts(ValidHosts validHosts) {
		this.validHosts = validHosts;
	}

	public static void main(String[] args) {
	    String exp = "http://bf.win007.com/football/Next_{yyyyMMdd}.htm";
	    List<String> list = CommonUtil.findByRegex(exp, RegexList.path_var_regexp);
	    if (list != null) {
	        for (String s : list) {
	            String fmt = s.replace("{", "").replace("}", "");
	            String time = CommonUtil.getNowTime(fmt);
	            exp = exp.replace(s, time);
	        }
	    }
	    System.out.println(exp);
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getIncludeHttps() {
		return includeHttps;
	}

	public void setIncludeHttps(String includeHttps) {
		this.includeHttps = includeHttps;
	}

	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getSkipStatusCode() {
		return this.skipStatusCode;
	}

	public void setSkipStatusCode(String skipStatusCode) {
		this.skipStatusCode = skipStatusCode;
	}

	public Options getOptions() {
		return this.options;
	}

	public void setOptions(Options options) {
		this.options = options;
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

	public String getDownloader() {
        return this.downloader;
    }

    public void setDownloader(String downloader) {
        this.downloader = downloader;
    }

    public Plugins getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

	public Rules getQueueRules() {
		return queueRules;
	}

	public void setQueueRules(Rules queueRules) {
		this.queueRules = queueRules;
	}

	public String getCharset() {
		return this.charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Seeds getSeeds() {
		return this.seeds;
	}

	public void setSeeds(Seeds seeds) {
		this.seeds = seeds;
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
	
	public String getHttpMethod() {
		return this.httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getOption(String name){
		if (options == null)
			return null;
		
		for (Option option: options.getOption()) {
			if (option == null || option.getName() == null || option.getName().trim().length() == 0)
				continue;
			if (!option.getName().equals(name))
				continue;
			
			return option.getValue();
		}
		
		return null;
	}
	
	private void destroyPoint(Collection<? extends Point> points, SpiderListener listener){
		if (points == null)
			return ;
		for (Point point : points){
			try {
				point.destroy();
			} catch (Exception e){
				listener.onError(Thread.currentThread(), null, "Plugin.point->"+point+" destroy failed.", e);
			}finally{
				point = null;
			}
		}
	}
	
	public void destroy(SpiderListener listener, boolean isShutdownNow) {
	    try {
	        // 停止抓取器线程
	        this.fetcher.close();
	    } catch(Throwable e) {
	        listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".Fetcher close failed.", e);
	    }
		try {
			this.queue.stop();
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskQueue stop failed.", e);
		}
		
		try {
			if (isShutdownNow)
				this.pool.shutdownNow();
			else
				this.pool.shutdown();
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".ThreadPool shutdown failed.", e);
		}
		
		try {
			destroyPoint(this.taskPollPointImpls, listener);
			if (this.taskPollPointImpls != null){
				this.taskPollPointImpls.clear();
				this.taskPollPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskPollPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.beginPointImpls, listener);
			if (this.beginPointImpls != null){
				this.beginPointImpls.clear();
				this.beginPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".BeginPlugin destroy failed.", e);
		}
		try {
			destroyPoint(this.fetchPointImpls, listener);
			if (this.fetchPointImpls != null) {
				this.fetchPointImpls.clear();
				this.fetchPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".FetchPlugin destroy failed.", e);
		}
		try{
			destroyPoint(this.digPointImpls, listener);
			if (this.digPointImpls != null){
				this.digPointImpls.clear();
				this.digPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".DigPlugin destroy failed.", e);
		}
		try {
			destroyPoint(this.dupRemovalPointImpls, listener);
			if (this.dupRemovalPointImpls != null){
				this.dupRemovalPointImpls.clear();
				this.dupRemovalPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".DupRemovalPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.taskSortPointImpls, listener);
			if (this.taskSortPointImpls != null){
				this.taskSortPointImpls.clear();
				this.taskSortPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskSortPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.taskPushPointImpls, listener);
			if (this.taskPushPointImpls != null){
				this.taskPushPointImpls.clear();
				this.taskPollPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskPushPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.targetPointImpls, listener);
			if (this.targetPointImpls != null){
				this.targetPointImpls.clear();
				this.targetPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TargetPlugin destroy failed.", e);
		}
		
		try{
			destroyPoint(this.parsePointImpls, listener);
			if (this.parsePointImpls != null) {
				this.parsePointImpls.clear();
				this.parsePointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".ParserPlugin destroy failed.", e);
		}
		try{
			destroyPoint(this.pojoPointImpls, listener);
			if (this.pojoPointImpls != null) {
				this.pojoPointImpls.clear();
				this.pojoPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".PojoPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.endPointImpls, listener);
			if (this.endPointImpls != null) {
				this.endPointImpls.clear();
				this.endPointImpls = null;
			}
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".EndPlugin destroy failed.", e);
		}
		
		this.isStop = true;
		
//		this.queue = null;
//		this.counter = null;
//		this.fetcher = null;
	}

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
	
	
}
