/**
 * 
 */
package org.eweb4j.spiderman.spider;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eweb4j.spiderman.container.Container;
import org.eweb4j.spiderman.container.ContainerManager;
import org.eweb4j.spiderman.infra.SpiderIOC;
import org.eweb4j.spiderman.infra.SpiderIOCs;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.xml.BeanXMLUtil;
import org.eweb4j.util.xml.XMLReader;
import org.eweb4j.util.xml.XMLWriter;

/**
 * @author yangc
 *
 */
public class Spiderman{
	
	public final SpiderIOC ioc = SpiderIOCs.create();
	private ContainerManager cm = null;
	public Boolean isShutdownNow = false;
	private SpiderListener listener = null;
	private ExecutorService pool = null;
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
	
	public Spiderman init(File file){
        if (this.listener == null)
            this.listener = new SpiderListenerAdaptor();
        isShutdownNow = false;
        try {
            if (file == null)
                loadConfigFiles();
            else
                loadConfigFile(file);
            	initContainers();
            	initPool();
        } catch (Throwable e){
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
	    return this.startup(null);
	} 
	
	public Spiderman startup(final File file) {
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
						//阻塞，判断之前所有的容器是否都已经停止完全
						//加个超时
						long start = System.currentTimeMillis();
						long timeout = 1*60*1000;
						while (true) {
							try {
								if ((System.currentTimeMillis() - start) > timeout){
									_this.listener.onError(Thread.currentThread(), null, "timeout of restart blocking check...", new Exception());
									for (Container container : _this.cm.getContainers()) {
										if (!container.isStop){
											try {
												container.destroy(_this.listener, _this.isShutdownNow);
											} catch (Throwable e){
												e.printStackTrace();
												_this.listener.onError(Thread.currentThread(), null, e.toString(), e);
											}
										}
									}
									break;
								}
								if (_this.cm.getContainers() == null || _this.cm.getContainers().isEmpty())
									break;
								
								Thread.sleep(1*1000);
								boolean canBreak = true;
								for (Container container : _this.cm.getContainers()) {
									if (!container.isStop){
										canBreak = false;
										_this.listener.onInfo(Thread.currentThread(), null, "can not restart Spiderman cause there has running-components of this container -> "+container.getId()+"...");
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
							//只有所有的容器资源都已被释放[特殊情况timeout]完全才重启Spiderman
							_this.scheduleTimes++;
							String strTimes = _this.scheduleTimes + "";
							if (_this.maxScheduleTimes > 0)
								strTimes += "/"+_this.maxScheduleTimes;
							//记录每一次调度执行的时间
							_this.scheduleAt.add(new Date());
							
							_this.listener.onInfo(Thread.currentThread(), null, "Spiderman has scheduled "+strTimes+" times.");
							
							if (_this.scheduleTimes > 1)
								_this.listener.onBeforeEveryScheduleExecute(_this.scheduleAt.get(_this.scheduleTimes-2));
							
							_this.init(file)._startup().keepStrict(scheduleTime);
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
		for (Container container : cm.getContainers()){
			container.startup();
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
		if (cm.getContainers() != null) {
			for (Container container : cm.getContainers()){
				container.destroy(listener, false);
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
		if (cm.getContainers() != null) {
			for (Container container : cm.getContainers()){
				container.destroy(listener, true,args);
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
		File containerFolder = new File(Settings.website_xml_folder());
		if (!containerFolder.exists())
			throw new Exception("can not found Cointainers folder -> " + containerFolder.getAbsolutePath());
		
		if (!containerFolder.isDirectory())
			throw new Exception("WebSites -> " + containerFolder.getAbsolutePath() + " must be folder !");
		
		File[] files = containerFolder.listFiles();
		if (files == null || files.length == 0){
			//generate a container.xml file
			File file = new File(containerFolder.getAbsoluteFile()+File.separator+"_container_sample_.xml");
			Container container = new Container("default");
			XMLWriter writer = BeanXMLUtil.getBeanXMLWriter(file, container);
			writer.setBeanName("container");
			writer.setClass("container",Container.class);
			writer.write();
		}
		
		cm = ContainerManager.me();
		
		for (File file : files){
			if (!file.exists())
				continue;
			if (!file.isFile())
				continue;
			if (!file.getName().endsWith(".xml"))
				continue;
			this.loadConfigFile(file);
		}
	}
	
	@SuppressWarnings("unused")
	public void loadConfigFile(File file) throws Exception {
	    if (!file.exists())
            return;
        if (!file.isFile())
            return;
        if (!file.getName().endsWith(".xml"))
            return;
        XMLReader reader = BeanXMLUtil.getBeanXMLReader(file);
        reader.setBeanName("container");
        reader.setClass("container", Container.class);
        
        Container container = reader.readOne();
        reader.setRootElementName("container");
        container.setReader(reader);
        if (container == null)
            throw new Exception("container xml file error -> " + file.getAbsolutePath());
        
        if (!"1".equals(container.getEnable())) {
            return;
        }
        if(cm == null)
        cm = ContainerManager.me();
        cm.add(container);
	}
	
	private void initContainers() throws Exception{
		for (Container container : cm.getContainers()){
			container.init(listener);
		}
	}
	
	private void initPool(){
		if (pool == null){
			int size = cm.getContainers().size();
			if (size == 0)
				throw new RuntimeException("there is no container to load...");
			pool = new ThreadPoolExecutor(size, size, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			listener.onInfo(Thread.currentThread(), null, "init thread pool size->"+size+" success ");
		}
	}
}
