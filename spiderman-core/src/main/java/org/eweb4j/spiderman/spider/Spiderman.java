package org.eweb4j.spiderman.spider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eweb4j.spiderman.infra.SpiderIOC;
import org.eweb4j.spiderman.infra.SpiderIOCs;
import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.plugin.DoneException;
import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.EndPoint;
import org.eweb4j.spiderman.plugin.ExtensionPoint;
import org.eweb4j.spiderman.plugin.ExtensionPoints;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.ParsePoint;
import org.eweb4j.spiderman.plugin.PluginManager;
import org.eweb4j.spiderman.plugin.Point;
import org.eweb4j.spiderman.plugin.PojoPoint;
import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.plugin.TaskPollPoint;
import org.eweb4j.spiderman.plugin.TaskPushPoint;
import org.eweb4j.spiderman.plugin.TaskSortPoint;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.task.TaskQueue;
import org.eweb4j.spiderman.xml.Plugin;
import org.eweb4j.spiderman.xml.Plugins;
import org.eweb4j.spiderman.xml.Seed;
import org.eweb4j.spiderman.xml.Seeds;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.xml.BeanXMLUtil;
import org.eweb4j.util.xml.XMLReader;
import org.eweb4j.util.xml.XMLWriter;


public class Spiderman {

	public final SpiderIOC ioc = SpiderIOCs.create();
	public Boolean isShutdownNow = false;
	private ExecutorService pool = null;
	private Collection<Site> sites = new ArrayList<Site>();;
	private SpiderListener listener = null;
	
	private boolean isSchedule = false;
	private Timer timer = new Timer();
	private String scheduleTime = "1h";
	private String scheduleDelay = "1m";
	private int scheduleTimes = 0;
	private List<Date> scheduleAt = new ArrayList<Date>();
	private int maxScheduleTimes = 0;
	
	public final static Spiderman me() {
		return new Spiderman();
	}
	
	/**
	 * @date 2013-1-17 下午01:43:52
	 * @param listener
	 * @return
	 */
	public Spiderman init(SpiderListener listener) {
		return listen(listener).init();
	}
	
	// TODO 抽象一个ConfigBuilder，可以支持从文件xml构建配置信息，从数据库表构建配置信息，从Java Class注解构建配置信息
	public Spiderman init(File file){
        if (this.listener == null)
            this.listener = new SpiderListenerAdaptor();
        isShutdownNow = false;
        sites = new ArrayList<Site>();
        pool = null;
        
        try {
            if (file == null)
                loadConfigFiles();
            else
                loadConfigFile(file);
            initSites();
            initPool();
        } catch (Throwable e){
            e.printStackTrace();
            this.listener.onError(Thread.currentThread(), null, e.toString(), e);
        }
        return this;
    }
	
	public Spiderman init(){
	    File file = null;
		return this.init(file);
	}
	
	public Spiderman listen(SpiderListener listener){
		this.listener = listener;
		return this;
	}
	
	public Spiderman startup() {
		if (isSchedule) {
			final Spiderman _this = this;
			timer.schedule(new TimerTask() {
				public void run() {
					//限制schedule的次数
					if (_this.maxScheduleTimes > 0 && _this.scheduleTimes >= _this.maxScheduleTimes){
						try {
							_this.cancel();
						} catch (Throwable e){
							e.printStackTrace();
							_this.listener.onError(Thread.currentThread(), null, e.toString(), e);
						}
						
						_this.listener.onInfo(Thread.currentThread(), null, "Spiderman has completed and cancel the schedule.");
						
						try {
							_this.listener.onAfterScheduleCancel();
						} catch (Throwable e) {
							e.printStackTrace();
							_this.listener.onError(Thread.currentThread(), null, e.toString(), e);
						}
						
						_this.isSchedule = false;
					} else {
						//阻塞，判断之前所有的网站是否都已经停止完全
						//加个超时
						long start = System.currentTimeMillis();
						long timeout = 1*60*1000;
						while (true) {
							try {
								if ((System.currentTimeMillis() - start) > timeout){
									_this.listener.onError(Thread.currentThread(), null, "timeout of restart blocking check...", new Exception());
									for (Site site : _this.sites) {
										if (!site.isStop){
											try {
												site.destroy(_this.listener, _this.isShutdownNow);
											} catch (Throwable e){
												e.printStackTrace();
												_this.listener.onError(Thread.currentThread(), null, e.toString(), e);
											}
										}
									}
									break;
								}
								if (_this.sites == null || _this.sites.isEmpty())
									break;
								
								Thread.sleep(1*1000);
								boolean canBreak = true;
								for (Site site : _this.sites) {
									if (!site.isStop){
										canBreak = false;
										_this.listener.onInfo(Thread.currentThread(), null, "can not restart spiderman cause there has running-tasks of this site -> "+site.getName()+"...");
									}
								}
								
								if (canBreak)
									break;
							} catch (Exception e) {
								_this.listener.onError(Thread.currentThread(), null, "", e);
								throw new RuntimeException("Spiderman can not schedule", e);
							}
						}
						
						try {
							//只有所有的网站资源都已被释放[特殊情况timeout]完全才重启Spiderman
							_this.scheduleTimes++;
							String strTimes = _this.scheduleTimes + "";
							if (_this.maxScheduleTimes > 0)
								strTimes += "/"+_this.maxScheduleTimes;
							//记录每一次调度执行的时间
							_this.scheduleAt.add(new Date());
							
							_this.listener.onInfo(Thread.currentThread(), null, "Spiderman has scheduled "+strTimes+" times.");
							
							if (_this.scheduleTimes > 1)
								_this.listener.onBeforeEveryScheduleExecute(_this.scheduleAt.get(_this.scheduleTimes-2));
							
							_this.init()._startup().keepStrict(scheduleTime);
						} catch (Throwable e) {
							e.printStackTrace();
							_this.listener.onError(Thread.currentThread(), null, e.toString(), e);
						}
					}
				}
			}, new Date(), (CommonUtil.toSeconds(scheduleTime).intValue() + CommonUtil.toSeconds(scheduleDelay).intValue())*1000);
			
			return this;
		}
		
		return _startup();
	}
	
	private Spiderman _startup(){
		for (Site site : sites){
			listener.onStartup(site);
			pool.execute(new Spiderman._Executor(site));
			listener.onInfo(Thread.currentThread(), null, "spider tasks of site[" + site.getName() + "] start... ");
			
		}
		return this;
	}
	
	public void shutdown(){
		shutdown(false);
	}
	/**
	 * 
	 * @date 2013-6-3 下午05:57:25
	 * @param isCallback 是否回调监听器方法，默认下，调度的话是会回调的，手动关闭则自由选择
	 */
	public void shutdown(boolean isCallback){
		listener.onInfo(Thread.currentThread(), null, "isCallback->" + isCallback);
		if (isCallback) {
			//此处添加一个监听回调
			listener.onBeforeShutdown();
		}
		if (sites != null) {
			for (Site site : sites){
				site.destroy(listener, false);
				listener.onInfo(Thread.currentThread(), null, "Site[" + site.getName() + "] destroy... ");
			}
		}
		if (pool != null) {
			pool.shutdown();
			listener.onInfo(Thread.currentThread(), null, "Spiderman shutdown... ");
		}
		
		if (isCallback) {
			//此处添加一个监听回调
			listener.onAfterShutdown();
		}
	}
	
	public void shutdownNow(){
		shutdownNow(false);
	}
	/**
	 * 
	 * @date 2013-6-3 下午05:57:25
	 * @param isCallback 是否回调监听器方法，默认下，调度的话是会回调的，手动关闭则自由选择
	 */
	public void shutdownNow(boolean isCallback, Object... args){
		listener.onInfo(Thread.currentThread(), null, "isCallback->" + isCallback);
		if (isCallback) {
			//此处添加一个监听回调
			try {
				listener.onBeforeShutdown(args);
			} catch (Throwable e){
				e.printStackTrace();
				listener.onError(Thread.currentThread(), null, e.toString(), e);
			}
		}
		if (sites != null) {
			for (Site site : sites){
				site.destroy(listener, true);
				listener.onInfo(Thread.currentThread(), null, "Site[" + site.getName() + "] destroy... ");
				listener.onAfterShutdown(site, args);
			}
		}
		
		if (pool != null) {
			pool.shutdownNow();
			listener.onInfo(Thread.currentThread(), null, "Spiderman shutdown now... ");
		}
		
		isShutdownNow = true;
		if (isCallback) {
			//此处添加一个监听回调
			try {
				listener.onAfterShutdown(args);
			} catch (Throwable e){
				e.printStackTrace();
				listener.onError(Thread.currentThread(), null, e.toString(), e);
			}
		}
	}
	
	//-------- Schedule ------------
	
	public Spiderman blocking(){
		while (isSchedule){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return this;
	}
	
	public Spiderman schedule(){
		return schedule(null);
	}
	
	public Spiderman schedule(String time){
		if (time != null && time.trim().length() > 0)
			this.scheduleTime = time;
		this.isSchedule = true;
		return this;
	}
	
	public Spiderman delay(String delay){
		if (delay != null && delay.trim().length() > 0)
			this.scheduleDelay = delay;
		return this;
	}
	
	public Spiderman times(int maxTimes){
		if (maxTimes > 0)
			this.maxScheduleTimes = maxTimes;
		return this;
	}
	
	public Spiderman cancel(){
		this.timer.cancel();
		timer = new Timer();
		return this;
	}
	//------------------------------
	
	public Spiderman keepStrict(String time){
		return keepStrict(CommonUtil.toSeconds(time).longValue()*1000);
	}
	
	public Spiderman keepStrict(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
		shutdownNow(true);
		return this;
	}
	
	public Spiderman keep(String time){
		return keep(CommonUtil.toSeconds(time).longValue()*1000);
	}
	
	public Spiderman keep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
		shutdown(true);
		return this;
	}
	
	private void loadConfigFiles() throws Exception{
		File siteFolder = new File(Settings.website_xml_folder());
		if (!siteFolder.exists())
			return;
//			throw new Exception("can not found WebSites folder -> " + siteFolder.getAbsolutePath());
		
		if (!siteFolder.isDirectory())
			return;
//			throw new Exception("WebSites -> " + siteFolder.getAbsolutePath() + " must be folder !");
		
		File[] files = siteFolder.listFiles();
		if (files == null || files.length == 0){
			//generate a site.xml file
			File file = new File(siteFolder.getAbsoluteFile()+File.separator+"_site_sample_.xml");
			Site site = new Site();
			
			Plugins plugins = new Plugins();
			plugins.getPlugin().add(PluginManager.createPlugin());
			site.setPlugins(plugins);
			
			XMLWriter writer = BeanXMLUtil.getBeanXMLWriter(file, site);
			writer.setBeanName("site");
			writer.setClass("site", Site.class);
			writer.write();
		}
		
		for (File file : files){
			this.loadConfigFile(file);
		}
	}
	
	public Spiderman loadConfigFile(File file) {
		if (file == null)
			 throw new RuntimeException("config file can not be null");
	    if (!file.exists())
	    	 throw new RuntimeException("config file not exists -> " + file.getAbsolutePath());
	    if (!file.isFile())
	    	 throw new RuntimeException("config file is not a file -> " + file.getAbsolutePath()); 
	    if (!file.getName().endsWith(".xml"))
	    	 throw new RuntimeException("config file is not xml -> " + file.getAbsolutePath());
	    
        XMLReader reader = BeanXMLUtil.getBeanXMLReader(file);
        reader.setBeanName("site");
        reader.setClass("site", Site.class);
        try {
	        Site site = reader.readOne();
	        if (site == null)
	            throw new RuntimeException("can not load the config file -> " + file.getAbsolutePath());
	        
	        if ("1".equals(site.getEnable())) {
	        	sites.add(site);
	        }
        } catch (Throwable e) {
        	throw new RuntimeException("can not load the config file -> " + file.getAbsolutePath(), e);
        }
        
        return this;
	}
	
	public static interface DynamicConfig{
		public void config(Site site);
	}
	
	public Spiderman config(DynamicConfig config) {
		for (Site site : sites) {
			config.config(site);
		}
		
		return this;
	}
	
	private void initSites() throws Exception{
		for (Site site : sites){
			if (site.getName() == null || site.getName().trim().length() == 0)
				throw new Exception("site name required");
			if (site.getUrl() == null || site.getUrl().trim().length() == 0)
				throw new Exception("site url required ->" + site.getName());
			if (site.getTargets() == null || site.getTargets().getTarget().isEmpty())
				throw new Exception("site target required ->" + site.getName());
			List<Target> targets = site.getTargets().getTarget();
			if (targets == null || targets.isEmpty())
				throw new Exception("site target required ->" + site.getName());
			
			//---------------------插件初始化开始----------------------------
			listener.onInfo(Thread.currentThread(), null, "plugins loading begin...");
			Collection<Plugin> plugins = site.getPlugins().getPlugin();
			//加载网站插件配置
			try {
				PluginManager pluginMgr = new PluginManager();
				pluginMgr.loadPluginConf(plugins, listener);
				
				//加载TaskPoll扩展点实现类
				ExtensionPoint<TaskPollPoint> taskPollPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_poll);
				if (taskPollPoint != null) {
					site.taskPollPointImpls = taskPollPoint.getExtensions();
					firstInitPoint(site.taskPollPointImpls, site, listener);
				}
				
				//加载Begin扩展点实现类
				ExtensionPoint<BeginPoint> beginPoint = pluginMgr.getExtensionPoint(ExtensionPoints.begin);
				if (beginPoint != null){
					site.beginPointImpls = beginPoint.getExtensions();
					firstInitPoint(site.beginPointImpls, site, listener);
				}
				
				//加载Fetch扩展点实现类
				ExtensionPoint<FetchPoint> fetchPoint = pluginMgr.getExtensionPoint(ExtensionPoints.fetch);
				if (fetchPoint != null){
					site.fetchPointImpls = fetchPoint.getExtensions();
					firstInitPoint(site.fetchPointImpls, site, listener);
				}
				
				//加载Dig扩展点实现类
				ExtensionPoint<DigPoint> digPoint = pluginMgr.getExtensionPoint(ExtensionPoints.dig);
				if (digPoint != null){
					site.digPointImpls = digPoint.getExtensions();
					firstInitPoint(site.digPointImpls, site, listener);
				}
				
				//加载DupRemoval扩展点实现类
				ExtensionPoint<DupRemovalPoint> dupRemovalPoint = pluginMgr.getExtensionPoint(ExtensionPoints.dup_removal);
				if (dupRemovalPoint != null){
					site.dupRemovalPointImpls = dupRemovalPoint.getExtensions();
					firstInitPoint(site.dupRemovalPointImpls, site, listener);
				}
				//加载TaskSort扩展点实现类
				ExtensionPoint<TaskSortPoint> taskSortPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_sort);
				if (taskSortPoint != null){
					site.taskSortPointImpls = taskSortPoint.getExtensions();
					firstInitPoint(site.taskSortPointImpls, site, listener);
				}
				
				//加载TaskPush扩展点实现类
				ExtensionPoint<TaskPushPoint> taskPushPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_push);
				if (taskPushPoint != null){
					site.taskPushPointImpls = taskPushPoint.getExtensions();
					firstInitPoint(site.taskPushPointImpls, site, listener);
				}
				
				//加载Target扩展点实现类
				ExtensionPoint<TargetPoint> targetPoint = pluginMgr.getExtensionPoint(ExtensionPoints.target);
				if (targetPoint != null){
					site.targetPointImpls = targetPoint.getExtensions();
					firstInitPoint(site.targetPointImpls, site, listener);
				}
				
				//加载Parse扩展点实现类
				ExtensionPoint<ParsePoint> parsePoint = pluginMgr.getExtensionPoint(ExtensionPoints.parse);
				if (parsePoint != null){
					site.parsePointImpls = parsePoint.getExtensions();
					firstInitPoint(site.parsePointImpls, site, listener);
				}
				
				//加载Pojo扩展点实现类
				ExtensionPoint<PojoPoint> pojoPoint = pluginMgr.getExtensionPoint(ExtensionPoints.pojo);
				if (pojoPoint != null){
					site.pojoPointImpls = pojoPoint.getExtensions();
					firstInitPoint(site.pojoPointImpls, site, listener);
				}
				
				//加载End扩展点实现类
				ExtensionPoint<EndPoint> endPoint = pluginMgr.getExtensionPoint(ExtensionPoints.end);
				if (endPoint != null){
					site.endPointImpls = endPoint.getExtensions();
					firstInitPoint(site.endPointImpls, site, listener);
				}
			//---------------------------插件初始化完毕----------------------------------
			} catch(Exception e){
				throw new Exception("Site["+site.getName()+"] loading plugins fail", e);
			}
			
			//初始化网站的队列容器
			site.queue = new TaskQueue();
			site.queue.init();
			//初始化网站目标Model计数器
			site.counter = new Counter();
		}
	}
	
	private void firstInitPoint(Collection<? extends Point> points, Site site, SpiderListener listener){
		for (Point point : points){
			point.init(site, listener);
		}
	}
	
	private void initPool(){
		if (pool == null){
			int size = sites.size();
			if (size == 0)
				throw new RuntimeException("there is no website to fetch...");
			pool = new ThreadPoolExecutor(size, size,
                    60L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>());
			
			listener.onInfo(Thread.currentThread(), null, "init thread pool size->"+size+" success ");
		}
	}
	
	private class _Executor implements Runnable{
		private Site site = null;
		
		public _Executor(Site site){
			this.site = site;
			String strSize = site.getThread();
			int size = Integer.parseInt(strSize);
			listener.onInfo(Thread.currentThread(), null, "site thread size -> " + size);
//			RejectedExecutionHandler rejectedHandler = new RejectedExecutionHandler() {
//				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//					//拿到被弹出来的爬虫引用
//					Spider spider = (Spider)r;
//					try {
//						//将该爬虫的任务 task 放回队列
//						pushTask(Arrays.asList(spider.task));
//						String info = "repush the task->"+spider.task+" to the Queue.";
//						spider.listener.onError(Thread.currentThread(), spider.task, info, new Exception(info));
//					} catch (Exception e) {
//						String err = "could not repush the task to the Queue. cause -> " + e.toString();
//						spider.listener.onError(Thread.currentThread(), spider.task, err, e);
//					}
//				}
//			};
			
			if (size > 0) {
//				int s = size/3;
//				int s2 = size-s;
//				if (s <= 0) {
//					s = 1;
//				}
//				if (s2 <= 0) {
//					s2 = 1;
//				}
				this.site.pool = Executors.newFixedThreadPool(size);
//				this.site.targetPool = new ThreadPoolExecutor(s2, s2,
//						60L, TimeUnit.MINUTES,
//	                    new LinkedBlockingQueue<Runnable>(),
//	                    rejectedHandler);
			} else {
//				int s = Integer.MAX_VALUE/3;
//				int s2 = Integer.MAX_VALUE-s;
//				if (s <= 0) {
//					s = 1;
//				}
//				if (s2 <= 0) {
//					s2 = 1;
//				}
				
				this.site.pool = Executors.newCachedThreadPool();
//				this.site.targetPool = new ThreadPoolExecutor(0, s2,
//	                    60L, TimeUnit.MINUTES,
//	                    new SynchronousQueue<Runnable>(),
//	                    rejectedHandler);
			}
		}
		
		public void run() {
			if (site.isStop)
				return ;
			
			Collection<Task> seedTasks = new ArrayList<Task>();
			// 获取种子url
			Seeds seeds = site.getSeeds();
			if (seeds == null || seeds.getSeed() == null || seeds.getSeed().isEmpty()) {
				Seed seed = new Seed();
				seed.setName(this.site.getName());
				seed.setUrl(this.site.getUrl());
				Task t = new Task(true, seed, this.site.getUrl(), this.site.getHttpMethod(), null, this.site, 5);
				seedTasks.add(t);
			}else{
				for (Iterator<Seed> it = seeds.getSeed().iterator(); it.hasNext(); ){
					Seed seed = it.next();
					Task t = new Task(true, seed, seed.getUrl(), seed.getHttpMethod(), null, this.site, 5);
					seedTasks.add(t);
				}
			}
			
			//种子任务放入任务队列
			pushTask(seedTasks);
			
			while(true){
				if (site.isStop)
					break;
				ThreadPoolExecutor pool = (ThreadPoolExecutor) this.site.pool;
				while (true) {
					int cps = pool.getCorePoolSize();
					long ctc = pool.getCompletedTaskCount();
					long tc = pool.getTaskCount();
					
					if ((tc - ctc) < cps) {
						break;
					}
					sleep("1s", "thread pool is too busy");
				}
//				ThreadPoolExecutor targetPool = (ThreadPoolExecutor) this.site.targetPool;
				
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
						sleep();
						continue;
					}
					
//					Target target = null;
//					Collection<TargetPoint> targetPointImpls = site.targetPointImpls;
//					if (targetPointImpls != null && !targetPointImpls.isEmpty()){
//						for (Iterator<TargetPoint> it = targetPointImpls.iterator(); it.hasNext(); ){
//							TargetPoint point = it.next();
//							target = point.confirmTarget(task, target);
//						}
//					}
					
					Spider spider = new Spider();
					spider.init(task, listener);
					
//					if (target != null) {
//						targetPool.execute(spider);
//					} else {
						pool.execute(spider);
//					}
				}catch (DoneException e) {
					listener.onInfo(Thread.currentThread(), null, e.toString());
					return ;
				} catch (Exception e) {
					listener.onError(Thread.currentThread(), null, e.toString(), e);
				}finally{
					if (site.isStop || site.pool == null)
						break;
				}
			}
		}

		private void pushTask(Collection<Task> seedTasks) {
			Collection<TaskPushPoint> taskPushPoints = site.taskPushPointImpls;
			if (taskPushPoints != null && !taskPushPoints.isEmpty()){
				for (TaskPushPoint tpp : taskPushPoints){
					try {
						tpp.pushTask(seedTasks);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		private void sleep() {
			sleep(null, null);
		}
		private void sleep(String cause) {
			sleep(null, cause);
		}
		private void sleep(String wait, String cause) {
			if (wait == null)
				wait = site.getWaitQueue();
			if (cause == null)
				cause = "queue empty";
			long _wait = CommonUtil.toSeconds(wait).longValue();
			listener.onInfo(Thread.currentThread(), null, cause+" wait for -> " + wait + " seconds");
			if (_wait > 0) {
				try {
					Thread.sleep(_wait * 1000);
				} catch (Exception e){
					
				}
			}
		}
	} 
	
}
