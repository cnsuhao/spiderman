import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eweb4j.config.EWeb4JConfig;
import org.eweb4j.spiderman.fetcher.FetchResult;
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
			throw new Exception();

		SpiderListener listener = new SpiderListenerAdaptor() {
			public void onFetch(Thread thread, Task task, FetchResult result) {
				System.out.print("[SPIDERMAN] " + CommonUtil.getNowTime("HH:mm:ss") + " [FETCH] ~ ");
				System.out.println("fetch result ->" + result + " from -> " + task.url);
			}

			public void onNewUrls(Thread thread, Task task,
					Collection<String> newUrls) {
				// System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DIG] ~ ");
				// System.out.println(newUrls);
			}

			public void onInfo(Thread thread, Task task, String info) {
				System.out.print("[SPIDERMAN] " + CommonUtil.getNowTime("HH:mm:ss") + " [INFO] ~ ");
				System.out.println(info);
			}

			public void onError(Thread thread, Task task, String err, Exception e) {
				System.err.print("[SPIDERMAN] " + CommonUtil.getNowTime("HH:mm:ss") + " [ERROR] ~ ");
				System.err.println(err);
				e.printStackTrace();
			}

			public void onParse(Thread thread, Task task, List<Map<String, Object>> models) {
				File dir = null;
				synchronized (mutex) {
					try {
						dir = new File(FileUtil.getTopClassPath(TestSpider.class)+"/Data/" + task.site.getName());
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
						String fileName = dir + "/count_" + i;
						// String fileName = "count_" +
						// task.site.counter.getCount();
						StringBuilder sb = new StringBuilder();
						for (Iterator<Entry<String, Object>> it = map
								.entrySet().iterator(); it.hasNext();) {
							Entry<String, Object> e = it.next();
							boolean isBlank = false;

							if (e.getValue() == null)
								isBlank = true;
							else if (e.getValue() instanceof String
									&& ((String) e.getValue()).trim().length() == 0)
								isBlank = true;
							else if (e.getValue() instanceof List
									&& ((ArrayList<?>) e.getValue()).isEmpty())
								isBlank = true;
							else if (e.getValue() instanceof List
									&& !((ArrayList<?>) e.getValue()).isEmpty()) {
								if (((ArrayList<?>) e.getValue()).size() == 1&& String.valueOf(((ArrayList<?>) e.getValue()).get(0)).trim().length() == 0)
									isBlank = true;
							}

							if (isBlank) {
								if (sb.length() > 0)
									sb.append("_");
								sb.append(e.getKey());
							}
						}
						String content = CommonUtil.toJson(map);
						if (sb.length() > 0)
							fileName = fileName + "_no_" + sb.toString() + "_";

						File file = new File(fileName + ".json");
						FileUtil.writeFile(file, content);
						System.out.print("[SPIDERMAN] " + CommonUtil.getNowTime("HH:mm:ss")	+ " [INFO] ~ ");
						System.out.println(fileName + " create finished...");
						i++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		// 启动爬虫
		Spiderman.me()
				.init(listener)//初始化
				.startup()// 启动
				.keep("30s");// 存活时间，过了存活时间后马上关闭

		// ------拿到引用后你还可以这样关闭-------------------------
		// spiderman.shutdown();//等待正在活动的线程都死掉再关闭爬虫
		// spiderman.shutdownNow();//马上关闭爬虫
		
		// 定时重启爬虫
//		Spiderman.me()
//			.listen(listener)// 监听
//			.schedule("10s")// 调度，爬虫运行10s
//			.delay("2s")// 每隔 10 + 2 秒后重启爬虫
//			.times(3)// 调度 3 次
//			.startup()// 启动
//			.blocking();// 阻塞当前线程直到所有调度完成
	}
}
