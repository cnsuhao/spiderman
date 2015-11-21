package org.eweb4j.spiderman.xml.site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.container.Container;
import org.eweb4j.spiderman.plugin.DoneException;
import org.eweb4j.spiderman.plugin.Point;
import org.eweb4j.spiderman.plugin.TaskPollPoint;
import org.eweb4j.spiderman.spider.Spider;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Option;
import org.eweb4j.spiderman.xml.Options;
import org.eweb4j.spiderman.xml.Output;
import org.eweb4j.spiderman.xml.Plugins;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.spiderman.xml.Seed;
import org.eweb4j.spiderman.xml.Seeds;
import org.eweb4j.spiderman.xml.Targets;
import org.eweb4j.spiderman.xml.ValidHosts;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.RegexList;
import org.eweb4j.util.xml.AttrTag;
import org.eweb4j.util.xml.XMLReader;

public class Site extends Component{

	
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
	private String reqDelay = "60";//每个请求的延迟时间
	
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

	@AttrTag
	private String downloader;
	
	private Seeds seeds ;
	
	private Headers headers = new Headers();//HTTP头
	
	private Cookies cookies = new Cookies();//HTTP Cookie
	
	private Rules queueRules;//允许进入抓取队列的url规则
	
	private Targets targets ;//抓取目标
	
	private Output output;//输出源;
	
	private Plugins plugins;//插件
	
	@Override
	public Component init(Container container, SpiderListener listener) throws Exception {
        XMLReader reader = container.getReader();
        reader.setBeanName("site");
        reader.setClass("site", Site.class);
        Site site = reader.readOne();
		if (site == null || !"1".equals(site.getEnable()))
		return null;
		site.listener = listener;
		site.container = container;
		site.initPlugins();
		//初始化线程池;
		site.initPool();
		return site;
	}

	
	@Override
	public void initPool(){
		String strSize = this.getThread();
		int size = Integer.parseInt(strSize);
		listener.onInfo(Thread.currentThread(), null, "site[" + getName() + "] of the container["+container.getId()+"] thread size -> " + size);
		RejectedExecutionHandler rejectedHandler = new RejectedExecutionHandler() {
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				//拿到被弹出来的爬虫引用
				Spider spider = (Spider)r;
				try {
					//将该爬虫的任务 task 放回队列
					spider.result.setValidTasks(Arrays.asList(spider.request.task));
					spider.pushTask(spider.request,spider.result);
					String info = "repush the task->"+spider.request.task+" to the Queue.";
					spider.listener.onError(Thread.currentThread(),spider.request.task, info, new Exception(info));
				} catch (Exception e) {
					String err = "could not repush the task to the Queue. cause -> " + e.toString();
					spider.listener.onError(Thread.currentThread(),spider.request.task, err, e);
				}
			}
		};
		if (size > 0)
			this.pool = new ThreadPoolExecutor(size, size, 60L, TimeUnit.SECONDS,  new LinkedBlockingQueue<Runnable>(),rejectedHandler);
		else
			this.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), rejectedHandler);
	}
	
	@Override
	public Component startup() {
		listener.onInfo(Thread.currentThread(), null, "spider tasks of site[" + getName() + "] in the container["+this.container.getId()+"] start... ");
		listener.onStartup(this);
		new Site._Executor(this).run();
		return this;
	}
	
	private class _Executor implements Runnable{
		private Site site = null;
		
		public _Executor(Site site){
			this.site = site;
		}
		
		public void run() {
			if (site.isStop)
				return ;
			
			// 获取种子url
			Seeds seeds = site.getSeeds();
			Collection<Task> seedTasks = new ArrayList<Task>();
			if (seeds == null || seeds.getSeed() == null || seeds.getSeed().isEmpty()) {
				
				seedTasks.add(new Task(this.site.getUrl(), this.site.getHttpMethod(), null, this.site, 10));
			}else{
				for (Iterator<Seed> it = seeds.getSeed().iterator(); it.hasNext(); ){
					Seed s = it.next();
					seedTasks.add(new Task(s.getUrl(), s.getHttpMethod(), null, this.site, 10));
				}
			}
			
			// 运行种子任务
			for (Iterator<Task> it = seedTasks.iterator(); it.hasNext(); ) {
				Task seedTask = it.next();
				Spider seedSpider = new Spider();
				seedSpider.init(seedTask, listener);
				//this.site.pool.execute(seedSpider);
				seedSpider.run();
			}
			while(true){
				if (site.isStop)
					break;
				try {
					//扩展点：TaskPoll
					Task task = null;
					Collection<TaskPollPoint> taskPollPoints = site.taskPollPointImpls;
					if (taskPollPoints != null && !taskPollPoints.isEmpty()){
						for (Iterator<TaskPollPoint> it = taskPollPoints.iterator(); it.hasNext(); ){
							TaskPollPoint point = it.next();
							task = point.pollTask();
						}
					}
					
					if (task == null){
						long wait = CommonUtil.toSeconds(site.getWaitQueue()).longValue();
						listener.onInfo(Thread.currentThread(), null, "queue empty wait for -> " + wait + " seconds");
						if (wait > 0) {
							try {
								Thread.sleep(wait * 1000);
							} catch (Exception e){
								
							}
						}
						continue;
					}
					
					Spider spider = new Spider();
					spider.init(task, listener);
					
					this.site.pool.execute(spider);
				}catch (DoneException e) {
					listener.onInfo(Thread.currentThread(), null, e.toString());
					return ;
				} catch (Exception e) {
					listener.onError(Thread.currentThread(), null, e.toString(), e);
				}finally{
					if (site.isStop)
						break;
					if (site.pool == null)
						break;
				}
			}
		}
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
		if (points != null){
			points.clear();
			points = null;
		}
	}
	@Override
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
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskPollPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.beginPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".BeginPlugin destroy failed.", e);
		}
		try {
			destroyPoint(this.fetchPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".FetchPlugin destroy failed.", e);
		}
		try{
			destroyPoint(this.digPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".DigPlugin destroy failed.", e);
		}
		try {
			destroyPoint(this.dupRemovalPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".DupRemovalPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.taskSortPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskSortPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.taskPushPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TaskPushPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.targetPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".TargetPlugin destroy failed.", e);
		}
		
		try{
			destroyPoint(this.parsePointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".ParserPlugin destroy failed.", e);
		}
		try{
			destroyPoint(this.pojoPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".PojoPlugin destroy failed.", e);
		}
		
		try {
			destroyPoint(this.endPointImpls, listener);
		}catch(Throwable e){
			listener.onError(Thread.currentThread(), null, "Site.name->"+this.getName()+".EndPlugin destroy failed.", e);
		}
		
		this.isStop = true;
		
//		this.queue = null;
//		this.counter = null;
//		this.fetcher = null;
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
	
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

	public Plugins getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}
    
}
