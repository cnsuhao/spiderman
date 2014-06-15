package org.eweb4j.spiderman.plugin.impl;

import org.eweb4j.mvc.Http;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.fetcher.SpiderConfig;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.util.HttpClientDownloader;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.util.CommonUtil;

/**
 * 一个Host一个FetchPointImpl对象
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 下午06:40:05
 */
public class FetchPointImpl implements FetchPoint{

	private SpiderListener listener = null;
	private Site site = null;
	
	public void init(Site site, SpiderListener listener) {
		this.site = site;
		this.listener = listener;
	}

	public void destroy() {
	}
	
	public static void main(String[] args) throws Exception{
		long start = System.currentTimeMillis();
		String url = "http://www.ionorchard.com/load_tenant.php?id=44"; 
		HttpClientDownloader fetcher = new HttpClientDownloader();
		SpiderConfig config = new SpiderConfig();
		config.setCharset("utf-8");
		config.setPolitenessDelay(200);
		config.setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17");
		
//		Cookies cookies = new Cookies();
//		Cookie c = new Cookie();
//		c.setHost("");
//		c.setPath("/");
//		c.setName("CakeCookie[currentStateId]");
//		c.setValue("6");
		Site site = new Site();
		fetcher.init(config, site);
		try {
			FetchRequest req = new FetchRequest();
			req.setHttpMethod(Http.Method.POST);
			req.setUrl(url);
			FetchResult rs = fetcher.fetch(req);
			System.out.println(rs); 
			if (rs.getPage() != null)
				System.out.println(rs.getPage().getContent());
			
			System.out.println(System.currentTimeMillis() - start);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FetchResult fetch(Task task, FetchResult result) throws Exception {
		synchronized (site) {
			if (site.fetcher == null){
				SpiderConfig config = new SpiderConfig();
				if (task.site.getCharset() != null && task.site.getCharset().trim().length() > 0)
					config.setCharset(task.site.getCharset());
				if (task.site.getUserAgent() != null && task.site.getUserAgent().trim().length() > 0)
					config.setUserAgentString(task.site.getUserAgent());
				if ("1".equals(task.site.getIncludeHttps()) || "true".equals(task.site.getIncludeHttps()))
					config.setIncludeHttpsPages(true);
				if ("1".equals(task.site.getIsFollowRedirects()) || "true".equals(task.site.getIsFollowRedirects()))
					config.setFollowRedirects(true);
				String sdelay = task.site.getReqDelay();
				if (sdelay == null || sdelay.trim().length() == 0)
					sdelay = "200";
				
				int delay = CommonUtil.toSeconds(sdelay).intValue()*1000;
				if (delay < 0)
					delay = 200;
				
				config.setPolitenessDelay(delay);
				
				String timeout = task.site.getTimeout();
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
			
			String url = task.url.replace(" ", "%20");
			
			FetchRequest req = new FetchRequest();
			req.setUrl(url);
			req.setHttpMethod(task.httpMethod);
			
			
			return site.fetcher.fetch(req);
		}
	}
	
//	private FetchResult fetch(){
//		FetchResult fetchResult = new FetchResult();
//		CrawlerConfiguration config = new CrawlerConfiguration(task.url);
//		
//		listener.onInfo(Thread.currentThread(), "crawling url: " + task.url);
//
//		Url urlToCrawl = new Url(config.beginUrl(), 0);
//        Page page = config.downloader().get(urlToCrawl.link());
//        if (page.getStatusCode() != Status.OK) {
//        	listener.onError(Thread.currentThread(), "errorUrl->" + urlToCrawl.link(), new Exception(page.getStatusCode().name() + " link->" + urlToCrawl.link()));
//        } else {
//        	org.eweb4j.spiderman.fetcher.Page _page = new org.eweb4j.spiderman.fetcher.Page();
//			_page.setContent(page.getContent());
//			_page.setContentType("text/html");
//			_page.setContentData(page.getContent().getBytes());
//			_page.setCharset(page.getCharset());
//			_page.setUrl(page.getUrl());
//			fetchResult.setPage(_page);
//			fetchResult.setFetchedUrl(page.getUrl());
//			fetchResult.setStatusCode(page.getStatusCode().ordinal());
//        }
//
//        for (String l : page.getLinks()) {
//            String link = config.normalizer().normalize(l);
//            final Url url = new Url(link, urlToCrawl.depth() + 1);
//            //是否进入递归抓取，如果进入递归就需要控制深度
//        }
//        
//        return fetchResult;
//	}

}
