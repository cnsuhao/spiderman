package org.eweb4j.spiderman.web;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.eweb4j.cache.Props;
import org.eweb4j.config.Log;
import org.eweb4j.config.LogFactory;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.spider.SpiderListenerAdaptor;
import org.eweb4j.spiderman.spider.Spiderman;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.util.CommonUtil;

@Path("${spiderman.controller}")
public class SpidermanControl extends SpiderListenerAdaptor{

	private final static String control_action = Props.getMap("spiderman").get("controller");
	private final static Log logger = LogFactory.getLogger(SpidermanControl.class);
	private static Vector<String> logs = new Vector<String>();
	private static Integer serverStatus = 0;
	private static Integer refreshStatus = 0;
	private static Integer refreshSeconds = 3;
	private static Spiderman server = new Spiderman();
	private static String time = "1h";//1个小时重启爬虫
	private static String delay = "10m";//重启爬虫的延时
	private static Integer scheduleStatus = 0;
	
	public void onFetch(Thread thread, Task task, FetchResult result) {
		logs.add("<font color='purple'>"+logger.debug("fetch-->"+result+" from sourceUrl->"+task.sourceUrl)+"</font>");
	}
	
	public void onNewUrls(Thread thread, Task task, Collection<String> newUrls) {
		logs.add("<font color='blue'>"+logger.debug("dig-->"+newUrls.size()+" from sourceUrl->"+task.url)+"</font>");
	}
	
	//第二次开始，每一次调度执行前回调此方法
	public void beforeEveryScheduleExecute(Date theLastTimeScheduledAt){
	}
	
	public void onInfo(Thread thread, Task task, String info) {
		try {
			StringBuilder sb = new StringBuilder();
			for(Iterator<String> it=logs.iterator();it.hasNext();){
				sb.append("<p>").append(it.next()).append("</p>");
			}
			if(sb.toString().getBytes().length > 1024*1024){
				logs.clear();
			}
		} catch (Throwable e){
			logs.clear();
		}
		
		logs.add("<font color='green'>"+logger.debug(info)+"</font>");
	}
	
	public void onError(Thread thread, Task task, String error, Exception e) {
		try {
			StringBuilder sb = new StringBuilder();
			for(Iterator<String> it=logs.iterator();it.hasNext();){
				sb.append("<p>").append(it.next()).append("</p>");
			}
			if(sb.toString().getBytes().length > 1024*1024){
				logs.clear();
			}
		} catch (Throwable e2){
			logs.clear();
		}
		
		logs.add("<font color='red'>"+logger.error(error+", " + CommonUtil.getExceptionString(e))+"</font>");
	}
	
	@Path("/admin")
	public String doAdmin(Map<String, Object> model){
		synchronized (logs) {
			try {
				StringBuilder sb = new StringBuilder();
				for(Iterator<String> it=logs.iterator();it.hasNext();){
					sb.append("<p>").append(it.next()).append("</p>");
				}
				model.put("logs", sb.toString());
				if(sb.toString().getBytes().length > 512*1024){
					logs.clear();
				}
			} catch (Throwable e){
				logs.clear();
			}
		}
		
		synchronized(serverStatus){
			model.put("serverStatus", serverStatus);
		}
		
		synchronized(refreshStatus){
			model.put("refreshStatus", refreshStatus);
		}
		
		synchronized(refreshSeconds){
			model.put("refreshSeconds", refreshSeconds);
		}
		
		synchronized(scheduleStatus){
			model.put("scheduleStatus", scheduleStatus);
		}
		
		synchronized (time) {
			model.put("time", time);
		}
		
		synchronized (delay) {
			model.put("delay", delay);
		}
		
		return Props.getMap("spiderman").get("admin_result");
	}
	

	/**
	 * 清除日志
	 * @return
	 */
	@Path("/clear_logs")
	public String doClearLogs(){
		logs.clear();
		this.onInfo(null, null, "clear logs...");
		return "action:"+control_action+"/admin@GET";
	}

	/**
	 * 调度服务，定时重启爬虫.
	 * @param time 每隔多长时间重启爬虫
	 * 注意，调度器的调度特别延长了 10 分钟时间，让爬虫休息10分钟
	 */
	@Path("/schedule")
	public String doScheduleServer(@QueryParam("time")final String _time, @QueryParam("delay")final String _delay){
		synchronized (time) {
			if (_time != null && _time.trim().length() > 0)
				time = _time;
		}
		synchronized (delay) {
			if (_delay != null && _delay.trim().length() > 0)
				delay = _delay;
		}
		synchronized (server) {
			server.listen(this)//设置监听器
				.schedule(time)//调度
				.delay(delay)//每隔 time + delay 重启爬虫
				.startup();//启动
			
			synchronized (scheduleStatus) {
				scheduleStatus = 1;
			}
		}
		
		return "action:"+control_action+"/admin@GET";
	}
	
	@Path("/cancel_schedule")
	public String doCancleSchedule(){
		synchronized (server) {
			server.cancel();
			server.shutdownNow();
			server = null;
			server = new Spiderman();
			synchronized (scheduleStatus) {
				scheduleStatus = 0;
			}
		}
		
		return "action:"+control_action+"/admin@GET";
	}

	/**
	 * 刷新页面
	 * @return
	 */
	@Path("/refresh")
	public String doRefresh(@QueryParam("s")int seconds){
		this.onInfo(null, null, "refresh ...");
		synchronized(refreshStatus){
			refreshStatus = 1;
			
			synchronized(refreshSeconds){
				refreshSeconds = seconds;
			}
		}
		
		return "action:"+control_action+"/admin@GET";
	}
	
	/**
	 * 停止刷新页面
	 */
	@Path("/cancel_refresh")
	public String doCancelRefresh(){
		this.onInfo(null, null, "cancel refresh ...");
		synchronized(refreshStatus){
			refreshStatus = 0;
		}
		
		return "action:"+control_action+"/admin@GET";
	}
}
