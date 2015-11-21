package org.eweb4j.spiderman.plugin.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.fetcher.SpiderConfig;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.util.HttpClientDownloader;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.xml.site.Site;
import org.eweb4j.util.CommonUtil;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.function.CommonFunction;
import com.greenpineyu.fel.function.Function;

/**
 * 一个Host一个FetchPointImpl对象
 * @author weiwei l.weiwei@163.com
 * @author wchao wchaojava@163.com
 * @date 2013-1-7 下午06:40:05
 */
public class FetchPointImpl implements FetchPoint{

	private SpiderListener listener = null;
	private Site site = null;
	
	public void init(Component site, SpiderListener listener) {
		this.site = (Site)site;
		this.listener = listener;
	}

	public void destroy() {
	}
	
	public static void main(String[] args) throws Exception{
		long start = System.currentTimeMillis();
		FelEngine fel = new FelEngineImpl();
		Function func = new CommonFunction() {
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "$output";
			}
			
			@Override
			public Object call(Object[] arguments) {
				// TODO Auto-generated method stub
				return arguments[0];
			}
		};
		fel.addFun(func);
		String url = "https://www.baidu.com/s?wd=%E5%BC%80%E6%BA%90%E9%A1%B9%E7%9B%AE&pn=1&oq=%E5%BC%80%E6%BA%90%E9%A1%B9%E7%9B%AE&ie=utf-8&f=1&rsv_idx=1&rsv_pq=b8460d37000008ff&rsv_t=e533UdHQ4vtlVjUWYYKDEjYfjlOsfofr1zz0gsBT4oIJRkwJWaSMF7rW7RU&rsv_page=1"; 
		fel.getContext().set("$this",2);
		System.out.println(fel.eval("$this+2"));
		 HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36");
		HttpResponse response = httpClient.execute(get);
		InputStream is = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String sbLine;
		StringBuilder sb = new StringBuilder();
		while((sbLine = br.readLine()) != null)
		{
			sb.append(sbLine); 
		}
	}
	
	public FetchResult fetch(FetchRequest request, FetchResult result) throws Exception {
		synchronized (site) {
			if (site.fetcher == null){
				SpiderConfig config = new SpiderConfig();
				if (request.task.site.getCharset() != null && request.task.site.getCharset().trim().length() > 0)
					config.setCharset(request.task.site.getCharset());
				if (request.task.site.getUserAgent() != null && request.task.site.getUserAgent().trim().length() > 0)
					config.setUserAgentString(request.task.site.getUserAgent());
				if ("1".equals(request.task.site.getIncludeHttps()) || "true".equals(request.task.site.getIncludeHttps()))
					config.setIncludeHttpsPages(true);
				if ("1".equals(request.task.site.getIsFollowRedirects()) || "true".equals(request.task.site.getIsFollowRedirects()))
					config.setFollowRedirects(true);
				String sdelay = request.task.site.getReqDelay();
				if (sdelay == null || sdelay.trim().length() == 0)
					sdelay = "60";
				
				int delay = CommonUtil.toSeconds(sdelay).intValue()*1000;
				if (delay < 0)
					delay = 60;
				
				config.setPolitenessDelay(delay);
				
				String timeout = request.task.site.getTimeout();
				if (timeout != null && timeout.trim().length() > 0){
					int to = CommonUtil.toSeconds(sdelay).intValue()*1000;
					if (to > 0)
						config.setConnectionTimeout(to);
				}
				
				PageFetcher fetcher = null;
				String downloader = site.getDownloader();
				if (!CommonUtil.isBlank(downloader)) {
    				try {
    				    Class<?> cls = Class.forName(downloader);
    				    fetcher = (PageFetcher) cls.newInstance();
    				} catch (Throwable e) {
    				    e.printStackTrace();
    				}
				}
				
				//默认是HttpClient下载器
				if (fetcher == null) {
				    fetcher = new HttpClientDownloader();
				}
				
				fetcher.init(config, site);
				site.fetcher = fetcher;
			}
			
			String url = request.task.url.replace(" ", "");
			
			FetchRequest req = new FetchRequest();
			req.setUrl(url);
			req.setHttpMethod(request.task.httpMethod);
			
			return site.fetcher.fetch(req);
		}
	}
}
