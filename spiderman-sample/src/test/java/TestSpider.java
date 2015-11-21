import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eweb4j.config.EWeb4JConfig;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.spider.SpiderListenerAdaptor;
import org.eweb4j.spiderman.spider.Spiderman;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.util.CommonUtil;

/**
 * @author wchao
 *
 */
public class TestSpider {
	
	public static void main(String[] args){
    	try {
			new TestSpider().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void start() throws Exception {
        String err = EWeb4JConfig.start();
        if (err != null)
            throw new Exception(err);
        
        //实例化Spiderman
        final Spiderman spiderman = Spiderman.me();
        //爬虫监听适配器
        SpiderListener listener = new SpiderListenerAdaptor(){
        	@Override
        	public void onDigUrls(Thread thread, Task task, String fieldName,Collection<Object> urls) {
        		System.out.println("[DIG-URL] ~ "+urls);
        	}
        	@Override
        	public void onInfo(Thread thread, FetchRequest request, String info) {
        		 System.out.println(CommonUtil.getNowTime("HH:mm:ss")+"[INFO] ~ "+info);
        	}
        	@Override
        	public void onTargetPage(Thread thread,FetchRequest request, Page page) {
                System.out.println("[TARGET] ~ "+page.getUrl());
            }
        	@Override
        	public void onParse(Thread thread,FetchRequest request, List<Map<String, Object>> models) {
             	System.out.println("on_Parse->" + models);
            }
        };
        
        //启动爬虫|初始化|
        //调度，爬虫运行10s
        spiderman.init(listener).startup()/*.keep("10s")*/;//启动
        //File file = new File("E:\\jukeyuan\\spiderman-sample\\target\\test-classes\\sites\\tianya site of site_sample.xml");
        //spiderman.listen(listener).init(file).startup(file);
        /*spiderman.init(listener)
        .schedule("10s")
        .startup()//启动
        .times(3);//调度 3 次*/
        Thread.currentThread().join();
  }
}
