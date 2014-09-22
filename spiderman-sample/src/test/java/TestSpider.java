
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.eweb4j.util.xml.Tags;
import org.junit.Test;

public class TestSpider {
    
    public static void main(String[] args){
        String html = "" +
                "<div class='test'>" +
                "    <ul>" +
                "        <li>1</li>" +
                "    </ul>" +
                "    <p style='width:100px; height:50px;'>This is p</p>" +
                "</div>";
        
        System.out.println(Tags.me().xml(html).rm("ul").empty().Attrs().rm().ok());
        
        html = FileUtil.readFile(new File("d:/txt.html"));
        System.out.println(Tags.me().xml(html).rm("li").empty().ok());
    }
    
    @Test
    public void test() throws Exception {
        
        String err = EWeb4JConfig.start();
        if (err != null)
            throw new Exception(err);
        
        //实例化spiderman
        final Spiderman spiderman = Spiderman.me();
        SpiderListener listener = new SpiderListenerAdaptor(){
            public void onAfterScheduleCancel(){
                //调度结束回调
            }
            /**
             * 每次调度执行前回调此方法
             * @date 2013-4-1 下午03:33:11
             * @param theLastTimeScheduledAt 上一次调度时间
             */
            public void onBeforeEveryScheduleExecute(Date theLastTimeScheduledAt){
                System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [LAST_SCHEDULE_AT] ~ ");
                System.err.println("at -> " + CommonUtil.formatTime(theLastTimeScheduledAt));
            }
            public void onFetch(Thread thread, Task task, FetchResult result) {
              System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [FETCH] ~ ");
              System.out.println("fetch result ->" + result + " from -> " + task.sourceUrl);
            }
            
            public void onDigUrls(Thread thread, Task task, String fieldName, Collection<String> urls) {
                System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DIG] ~ ");
                System.out.println("field->" + fieldName + ", "+urls.size() + ", " + urls);
                System.out.println("\t from -> "+task.url);
            }
            public void onNewUrls(Thread thread, Task task, Collection<String> urls) {
//                System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [NEW_URLS] ~ ");
//                System.out.println(urls.size() + ", " + urls);
//                System.out.println("\t from -> "+task.url);
            }
            
            public void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks) {
//              for (Task t : validTasks){
//                  System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DUPREMOVE] ~ ");
//                  System.out.println(t.url+" from->"+t.sourceUrl);
//              }
            }
            public void onTaskSort(Thread currentThread, Task task, Collection<Task> afterSortTasks) {
//              for (Task t : afterSortTasks){
//                  System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [SORT] ~ ");
//                  System.out.println(t.url+" from->"+t.sourceUrl);
//                  System.out.println("after->" + afterSortTasks);
//              }
            }
            
            public void onNewTasks(Thread thread, Task task, Collection<Task> newTasks) {
//                for (Task t : newTasks){
//                    System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [NEWTASK] ~ ");
//                    System.out.println(t.sort + ",,,," + t.url+" from->"+t.sourceUrl);
//                }
            }
            public void onTargetPage(Thread thread, Task task, Page page) {
              System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [TARGET] ~ ");
              System.out.println(page.getUrl());
              //System.out.println(page.getContent());
            }
            public void onInfo(Thread thread, Task task, String info) {
                System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
                System.out.println(info);
            }
            
            public void onError(Thread thread, Task task, String err, Throwable e) {
                System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [ERROR] ~ ");
                System.err.print(err);
                e.printStackTrace();
            }
            public void onParseOne(Thread thread, Task task, int size, int index, Map<String, Object> model) {
                System.out.println("on_parse->" + model);
            }
            public void onParse(Thread thread, Task task, List<Map<String, Object>> models) {
                if (task.site.counter.getCount() >= 30) {
                    onInfo(thread, task, "!!!!!shutdown...............");
                    spiderman.shutdownNow();
                }
                final String projectRoot = FileUtil.getTopClassPath(TestSpider.class);
                final File dir = new File(projectRoot+"/Data/"+task.site.getName()+"/"+task.target.getName());
                try {
                    if (!dir.exists())
                        dir.mkdirs();
                    for (int i = 0; i < models.size(); i++) {
                        Map<String, Object> map = models.get(i);
                        int c = task.site.counter.getCount() + i;
                        String fileName = dir + "/count_" + c;
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
                        
                        File file = new File(fileName + "_" + CommonUtil.getNow() + ".json");
                        FileUtil.writeFile(file, content);
                        System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
                        System.out.println(fileName + " create finished...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        //启动爬虫
        spiderman.init(listener)//初始化
            .startup();//启动
        
        Thread.currentThread().join();
        //启动爬虫 + 调度定时重启
//      Spiderman.me()
//          .listen(listener)//设置监听器
//          .schedule("10s")//调度，爬虫运行10s
//          .delay("2s")//每隔 10 + 2 秒后重启爬虫
//          .times(3)//调度 3 次
//          .startup()//启动
//          .blocking();//阻塞直到所有调度完成
    }
    
//  try {
//      List<String> pics = (List<String>) map.get("pics");
//      for (String pc : pics){
//          if (pc == null || pc.trim().length() == 0)
//              continue;
//          
//          final String pic = pc;
//          picPool.execute(new Runnable() {
//              public void run() {
//                  try {
//                      File file = new File(dir.getAbsoluteFile()+"/"+pic.replace("http://", "").replace("/", "_"));
//                      ImageIO.write(FileUtil.getBufferedImage(pic, true, 1, 1*1000), "jpg", new FileOutputStream(file));
//                      System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
//                      System.out.println(file.getAbsolutePath() + " create finished...");
//                  } catch (Exception e){
//                      e.printStackTrace();
//                  }
//              }
//          });
//      }
//  }catch(Exception e){
//      e.printStackTrace();
//  }
    
}