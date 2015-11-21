package org.eweb4j.spiderman.spider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.plugin.BeginPoint;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.plugin.DoneException;
import org.eweb4j.spiderman.plugin.DupRemovalPoint;
import org.eweb4j.spiderman.plugin.EndPoint;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.ParsePoint;
import org.eweb4j.spiderman.plugin.PojoPoint;
import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.plugin.TaskPushPoint;
import org.eweb4j.spiderman.plugin.TaskSortPoint;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.SourceUrlChecker;
import org.eweb4j.spiderman.xml.Field;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.util.CommonUtil;


/**
 * 网络蜘蛛
 * @author weiwei
 * @author wchao
 *
 */
public class Spider implements Runnable{

	public FetchRequest request;
	public FetchResult result;
	public SpiderListener listener;
	
	public void init(Task task, SpiderListener listener) {
		this.request = new FetchRequest(task);
		this.result = new FetchResult(this.request);
		this.listener = listener;
	}
	
	public void run() {
		try {
			//扩展点：begin 蜘蛛开始
			Collection<BeginPoint> beginPoints = request.task.site.beginPointImpls;
			if (beginPoints != null && !beginPoints.isEmpty()){
				for (Iterator<BeginPoint> it = beginPoints.iterator(); it.hasNext(); ){
					BeginPoint point = it.next();
					result = point.preProcess(request,result);
				}
			}
			
			if (result == null || request.task.site.isStop)
				return ;
			
			//扩展点：fetch 获取HTTP内容
			Collection<FetchPoint> fetchPoints = request.task.site.fetchPointImpls;
			if (fetchPoints != null && !fetchPoints.isEmpty()){
				for (Iterator<FetchPoint> it = fetchPoints.iterator(); it.hasNext(); ){
					FetchPoint point = it.next();
					result = point.fetch(request, result);
				}
			}
			
			listener.onFetch(Thread.currentThread(), request, result);
			
			if (result == null || request.task.site.isStop) 
				return ;
			
			
			//扩展点：dig new url 发觉新URL
			Collection<DigPoint> digPoints = request.task.site.digPointImpls;
			if (digPoints != null && !digPoints.isEmpty()){
				for (Iterator<DigPoint> it = digPoints.iterator(); it.hasNext(); ){
					DigPoint point = it.next();
					point.digNewUrls(request,result);
				}
			}
			
			if (request.task.site.isStop)
				return ;
			
			handleNewUrls(request,result);
			
			if (request.task.site.isStop)
				return ;
			
			Page page = result.getPage();
			
			if (page == null || request.task.site.isStop)
				return ;
			
			//扩展点：target 确认是否有目标配置匹配当前URL
			Collection<TargetPoint> targetPoints = request.task.site.targetPointImpls;
			if (targetPoints != null && !targetPoints.isEmpty()){
				for (Iterator<TargetPoint> it = targetPoints.iterator(); it.hasNext(); ){
					TargetPoint point = it.next();
					point.confirmTarget(request, result);
				}
			}
			
			if (result.getTarget() == null) {
				return ;
			}
			
			this.listener.onTargetPage(Thread.currentThread(), request, page);
			
			if (request.task.site.isStop)
				return ;
			
			//检查sourceUrl
			Rules rules = request.task.site.getTargets().getSourceRules();
			Rule sourceRule = SourceUrlChecker.checkSourceUrl(rules, request.task.sourceUrl);
			if (sourceRule == null) {
			    listener.onInfo(Thread.currentThread(), request, "target url->"+request.task.url+"'s source url->"+request.task.sourceUrl+" is not match the SourceRules");
				return ;
			}
			
			//扩展点：parse 把已确认好的目标页面解析成为Map对象
			Collection<ParsePoint> parsePoints = request.task.site.parsePointImpls;
			if (parsePoints != null && !parsePoints.isEmpty()){
				for (Iterator<ParsePoint> it = parsePoints.iterator(); it.hasNext(); ){
					ParsePoint point = it.next();
					point.parse(request,result);
				}
			}
			
			if (result.getModels() == null) {
				return ;
			}
			
			for (Iterator<Map<String, Object>> _it = result.getModels().iterator(); _it.hasNext(); ){
				 Map<String,Object> model = _it.next();
				 if(result.getTarget().getModel()!=null){
					 for (Iterator<Field> it = result.getTarget().getModel().getField().iterator(); it.hasNext(); ){
						 Field f = it.next();
						 //去掉那些被定义成 参数 的field
						 if ("1".equals(f.getIsParam()) || "true".equals(f.getIsParam()))
							 model.remove(f.getName());
					 }
				 }
				model.put("source_url", request.task.sourceUrl);
				model.put("task_url", request.task.url);
			}
			
			// 统计任务完成数+1
			this.request.task.site.counter.plus();
			listener.onParse(Thread.currentThread(), request,result.getModels());
			//parse解析时，挖掘到的新的资源url;
			if (request.task.digNewUrls != null && !request.task.digNewUrls.isEmpty()) {
				Set<Object> urls = new HashSet<Object>(request.task.digNewUrls.size());
				for (String s : request.task.digNewUrls){
					if (s == null || s.trim().length() == 0)
						continue;
					urls.add(s);
				}
				if (!urls.isEmpty()) {
					result.setNewUrls(urls);//设置新挖掘的url
					handleNewUrls(request,result);
					request.task.digNewUrls.clear();
					request.task.digNewUrls = null;
				}
			}
			
			listener.onInfo(Thread.currentThread(), request, "site -> " + request.task.site.getName() + " task parse finished count ->" + request.task.site.counter.getCount());
			
			if (request.task.site.isStop)
				return ;
			
			//扩展点：pojo 将Map数据映射为POJO
			String modelCls = result.getTarget().getModel()==null?null:result.getTarget().getModel().getClazz();
			Class<?> cls = null;
			if (modelCls != null)
				cls = Thread.currentThread().getContextClassLoader().loadClass(modelCls);
			
			Collection<PojoPoint> pojoPoints = request.task.site.pojoPointImpls;
			if (pojoPoints != null && !pojoPoints.isEmpty()){
				for (Iterator<PojoPoint> it = pojoPoints.iterator(); it.hasNext(); ){
					PojoPoint point = it.next();
					point.mapping(request, result,cls);
				}
			}
			if (result.getPojos()!= null) 
				listener.onPojo(Thread.currentThread(),request,result.getPojos());
			
			if (request.task.site.isStop)
				return ;
			
			//扩展点：end 蜘蛛完成工作，该收尾了
			Collection<EndPoint> endPoints =request.task.site.endPointImpls;
			if (endPoints != null && !endPoints.isEmpty()){
				for (Iterator<EndPoint> it = endPoints.iterator(); it.hasNext(); ){
					EndPoint point = it.next();
					point.complete(request, result);
				}
			}
			
		}catch(ConcurrentModificationException e)
		{
			//this.listener.onError(Thread.currentThread(),request.task,e.getMessage(), e);
		}catch (DoneException e){
			if (this.listener != null)
				this.listener.onInfo(Thread.currentThread(), request, "Spiderman has shutdown already...");
		} catch(Throwable e){
			if (this.listener != null)
				this.listener.onError(Thread.currentThread(), request.task, CommonUtil.getExceptionString(e), e);
		}
	}

	private void handleNewUrls(FetchRequest request,FetchResult result) throws Exception {
		if (result.getNewUrls() != null && !result.getNewUrls().isEmpty())
			this.listener.onNewUrls(Thread.currentThread(), request, result.getNewUrls());
		else
			result.setNewUrls(new ArrayList<Object>());
		
		if (request.task.site.isStop)
			return ;
		
		//扩展点：dup_removal URL去重,然后变成Task
		Collection<DupRemovalPoint> dupRemovalPoints = request.task.site.dupRemovalPointImpls;
		if (dupRemovalPoints != null && !dupRemovalPoints.isEmpty()){
			for (Iterator<DupRemovalPoint> it = dupRemovalPoints.iterator(); it.hasNext(); ){
				DupRemovalPoint point = it.next();
 				point.removeDuplicateTask(request,result);
			}
		}
		
		if (result.getNewUrls() != null && !result.getNewUrls().isEmpty())
			this.listener.onDupRemoval(Thread.currentThread(), request, result.getValidTasks());
		
		if (result.getValidTasks() == null)
			result.setValidTasks(new ArrayList<Task>());
		
		if (request.task.site.isStop)
			return ;
		
		//扩展点：task_sort 给任务排序
		Collection<TaskSortPoint> taskSortPoints = request.task.site.taskSortPointImpls;
		if (taskSortPoints != null && !taskSortPoints.isEmpty()){
			for (Iterator<TaskSortPoint> it = taskSortPoints.iterator(); it.hasNext(); ){
				TaskSortPoint point = it.next();
				point.sortTasks(result);
			}
		}
		
		this.listener.onTaskSort(Thread.currentThread(), request, result.getValidTasks());
		
		if (result.getValidTasks() == null)
			result.setValidTasks(new ArrayList<Task>());
		
		if (request.task.site.isStop)
			return ;
		
		//扩展点：task_push 将任务放入队列
		result = pushTask(request,result);
		if (result.getValidTasks() != null && !result.getValidTasks().isEmpty())
			this.listener.onNewTasks(Thread.currentThread(), request, result.getValidTasks());
	}

	public FetchResult pushTask(FetchRequest request,FetchResult result) throws Exception {
		Collection<TaskPushPoint> taskPushPoints = request.task.site.taskPushPointImpls;
		if (taskPushPoints != null && !taskPushPoints.isEmpty()){
			for (Iterator<TaskPushPoint> it = taskPushPoints.iterator(); it.hasNext(); ){
				TaskPushPoint point = it.next();
				point.pushTask(request,result);
			}
		}
		return result;
	}

}
