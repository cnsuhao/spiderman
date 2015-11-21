package org.eweb4j.spiderman.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.plugin.DigPoint;
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
import org.eweb4j.spiderman.spider.Counter;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.TaskDbServer;
import org.eweb4j.spiderman.task.TaskQueue;
import org.eweb4j.spiderman.xml.Plugin;
import org.eweb4j.spiderman.xml.Plugins;
import org.eweb4j.util.xml.AttrTag;
import org.eweb4j.util.xml.Skip;

public abstract class Component{
	
	public Container container;//所属容器
	
	public SpiderListener listener;//监听器;
	//------------------------------------------
	@AttrTag
	private String name;//目标站点名称
	@Skip
	public TaskDbServer removaldb = null;//每个组件都有属于自己的一个任务去重DB服务
	@Skip
	public ExecutorService pool;//每个组件都有属于自己的一个线程池
	@Skip
	public Boolean isStop = false;//每个组件都有属于自己的一个停止信号，用来标识该组件的状态是否停止完全
	@Skip
	public TaskQueue queue;//每个组件都有属于自己的一个任务队列容器
	@Skip
	public PageFetcher fetcher;//每个组件都有属于自己的一个抓取器
	@Skip
	public Counter counter;//针对本数据源已完成的任务数量
	
	private Plugins plugins;//插件
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

	public abstract Component startup();
	public abstract void destroy(SpiderListener listener, boolean isShutdownNow);
	public abstract Component init(Container container,SpiderListener listener)throws Exception;
	public abstract void initPool();
	
	public void initPlugins() throws Exception{
		//---------------------插件初始化开始----------------------------
		listener.onInfo(Thread.currentThread(), null, "plugins loading begin...");
		
		if(this.getPlugins() == null){
			//加载默认插件及其扩展点...
			Plugins deFaultPlugins = new Plugins();
			List<Plugin> deFaultPluginArray = new ArrayList<Plugin>();
			deFaultPluginArray.add(PluginManager.createPlugin());
			deFaultPlugins.setPlugin(deFaultPluginArray);
			this.setPlugins(deFaultPlugins);
		}
		
		Collection<Plugin> plugins = this.getPlugins().getPlugin();
		//加载网站插件配置
		try {
			PluginManager pluginMgr = new PluginManager();
			pluginMgr.loadPluginConf(plugins, listener);
			
			//加载TaskPoll扩展点实现类
			ExtensionPoint<TaskPollPoint> taskPollPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_poll);
			if (taskPollPoint != null) {
				this.taskPollPointImpls = taskPollPoint.getExtensions();
				firstInitPoint(this.taskPollPointImpls, this, listener);
			}
			
			//加载Begin扩展点实现类
			ExtensionPoint<BeginPoint> beginPoint = pluginMgr.getExtensionPoint(ExtensionPoints.begin);
			if (beginPoint != null){
				this.beginPointImpls = beginPoint.getExtensions();
				firstInitPoint(this.beginPointImpls, this, listener);
			}
			
			//加载Fetch扩展点实现类
			ExtensionPoint<FetchPoint> fetchPoint = pluginMgr.getExtensionPoint(ExtensionPoints.fetch);
			if (fetchPoint != null){
				this.fetchPointImpls = fetchPoint.getExtensions();
				firstInitPoint(this.fetchPointImpls, this, listener);
			}
			
			//加载Dig扩展点实现类
			ExtensionPoint<DigPoint> digPoint = pluginMgr.getExtensionPoint(ExtensionPoints.dig);
			if (digPoint != null){
				this.digPointImpls = digPoint.getExtensions();
				firstInitPoint(this.digPointImpls, this, listener);
			}
			
			//加载DupRemoval扩展点实现类
			ExtensionPoint<DupRemovalPoint> dupRemovalPoint = pluginMgr.getExtensionPoint(ExtensionPoints.dup_removal);
			if (dupRemovalPoint != null){
				this.dupRemovalPointImpls = dupRemovalPoint.getExtensions();
				firstInitPoint(this.dupRemovalPointImpls, this, listener);
			}
			//加载TaskSort扩展点实现类
			ExtensionPoint<TaskSortPoint> taskSortPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_sort);
			if (taskSortPoint != null){
				this.taskSortPointImpls = taskSortPoint.getExtensions();
				firstInitPoint(this.taskSortPointImpls, this, listener);
			}
			
			//加载TaskPush扩展点实现类
			ExtensionPoint<TaskPushPoint> taskPushPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_push);
			if (taskPushPoint != null){
				this.taskPushPointImpls = taskPushPoint.getExtensions();
				firstInitPoint(this.taskPushPointImpls, this, listener);
			}
			
			//加载Target扩展点实现类
			ExtensionPoint<TargetPoint> targetPoint = pluginMgr.getExtensionPoint(ExtensionPoints.target);
			if (targetPoint != null){
				this.targetPointImpls = targetPoint.getExtensions();
				firstInitPoint(this.targetPointImpls, this, listener);
			}
			
			//加载Parse扩展点实现类
			ExtensionPoint<ParsePoint> parsePoint = pluginMgr.getExtensionPoint(ExtensionPoints.parse);
			if (parsePoint != null){
				this.parsePointImpls = parsePoint.getExtensions();
				firstInitPoint(this.parsePointImpls, this, listener);
			}
			
			//加载Pojo扩展点实现类
			ExtensionPoint<PojoPoint> pojoPoint = pluginMgr.getExtensionPoint(ExtensionPoints.pojo);
			if (pojoPoint != null){
				this.pojoPointImpls = pojoPoint.getExtensions();
				firstInitPoint(this.pojoPointImpls, this, listener);
			}
			
			//加载End扩展点实现类
			ExtensionPoint<EndPoint> endPoint = pluginMgr.getExtensionPoint(ExtensionPoints.end);
			if (endPoint != null){
				this.endPointImpls = endPoint.getExtensions();
				firstInitPoint(this.endPointImpls, this, listener);
			}
		//---------------------------插件初始化完毕----------------------------------
		} catch(Exception e){
			throw new Exception("Site["+this.getName()+"] loading plugins fail", e);
		}
		
		//初始化网站的队列容器
		this.queue = new TaskQueue();
		this.queue.init();
		//初始化网站目标Model计数器
		this.counter = new Counter();
		
	}
	
	public void firstInitPoint(Collection<? extends Point> points, Component component, SpiderListener listener){
		for (Point point : points){
			point.init(component, listener);
		}
	}
	
	public Plugins getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public Container getContainer() {
		return container;
	}
	public void setContainer(Container container) {
		this.container = container;
	}
}
