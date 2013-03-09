
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eweb4j.config.EWeb4JConfig;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.spider.SpiderListenerAdaptor;
import org.eweb4j.spiderman.spider.Spiderman;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.FileUtil;
import org.junit.Test;

public class TestSpider {
	
	private final Object mutex = new Object();
	
	@Test
	public void test() throws Exception {
		String err = EWeb4JConfig.start();
		if (err != null)
			throw new Exception(err);
		
		SpiderListener listener = new SpiderListenerAdaptor(){
			public void onFetch(Thread thread, Task task, FetchResult result) {
//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [FETCH] ~ ");
//				System.out.println("fetch result ->" + result + " from -> " + task.sourceUrl);
			}
			public void onNewUrls(Thread thread, Task task, Collection<String> newUrls) {
//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DIG] ~ ");
//				System.out.println(newUrls);
			}
			public void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks) {
//				for (Task t : validTasks){
//					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DUPREMOVE] ~ ");
//					System.out.println(t.url+" from->"+t.sourceUrl);
//				}
			}
			public void onTaskSort(Thread currentThread, Task task, Collection<Task> afterSortTasks) {
//				for (Task t : afterSortTasks){
//					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [SORT] ~ ");
//					System.out.println(t.url+" from->"+t.sourceUrl);
//				}
			}
			public void onNewTasks(Thread thread, Task task, Collection<Task> newTasks) {
//				for (Task t : newTasks){
//					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [NEWTASK] ~ ");
//					System.out.println(t.sort + ",,,," + t.url+" from->"+t.sourceUrl);
//				}
			}
			public void onTargetPage(Thread thread, Task task, Page page) {
//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [TARGET] ~ ");
//				System.out.println(page.getUrl());
			}
			public void onInfo(Thread thread, Task task, String info) {
//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
//				System.out.println(info);
			}
			
			public void onError(Thread thread, Task task, String err, Exception e) {
				System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [ERROR] ~ ");
				e.printStackTrace();
			}
			
			public void onParse(Thread thread, Task task, List<Map<String, Object>> models) {
				File dir = null;
				synchronized (mutex) {
					try {
						dir = new File("d:/jsons/"+task.site.getName());
						if (!dir.exists())
							dir.mkdirs();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				try {
					int i = 0;
					if (models.size() <= 1)
						i = task.site.counter.getCount();
					
					for (Map<String, Object> map : models) {
						String fileName = dir+"/count_" + i;
	//					String fileName = "count_" + task.site.counter.getCount();
						StringBuilder sb = new StringBuilder();
						for (Iterator<Entry<String,Object>> it = map.entrySet().iterator(); it.hasNext();){
							Entry<String,Object> e = it.next();
							boolean isBlank = false;
							
							if (e.getValue() == null)
								isBlank = true;
							else if (e.getValue() instanceof String && ((String)e.getValue()).trim().length() == 0)
								isBlank = true;
							else if (e.getValue() instanceof List && ((ArrayList<?>)e.getValue()).isEmpty())
								isBlank = true;
							else if (e.getValue() instanceof List && !((ArrayList<?>)e.getValue()).isEmpty()) {
								if (((ArrayList<?>)e.getValue()).size() == 1 && String.valueOf(((ArrayList<?>)e.getValue()).get(0)).trim().length() == 0)
								isBlank = true;
							}
								
							if (isBlank){
								if (sb.length() > 0)
									sb.append("_");
								sb.append(e.getKey());
							}
						}
						String content = CommonUtil.toJson(map);
						if (sb.length() > 0)
							fileName = fileName + "_no_"+sb.toString()+"_";
						
						File file = new File(fileName+".json");
						FileUtil.writeFile(file, content);
						System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
						System.out.println(fileName + " create finished...");
						i++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		//启动爬虫
		Spiderman.me()
			.init(listener)//初始化
			.startup()//启动
			.keepStrict("20m");//存活时间，过了存活时间后马上关闭
		
		//启动爬虫 + 调度定时重启
//		Spiderman.me()
//			.listen(listener)//设置监听器
//			.schedule("20s")//调度，爬虫运行10s
//			.delay("2s")//每隔 10 + 2 秒后重启爬虫
//			.times(1)//重启 3 次
//			.startup()//启动
//			.blocking();//阻塞直到所有调度完成
	}
	
}
