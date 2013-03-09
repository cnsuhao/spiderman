package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.plugin.FetchPoint;
import org.eweb4j.spiderman.plugin.util.PageFetcherImpl;
import org.eweb4j.spiderman.plugin.util.SpiderConfig;
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
	
	public static void main(String[] args){
		PageFetcherImpl fetcher = new PageFetcherImpl();
		SpiderConfig config = new SpiderConfig();
		config.setCharset("utf-8");
		config.setPolitenessDelay(200);
		fetcher.setConfig(config);
		fetcher.init(null);
		try {
			String hostUrl = "http://alldeals.groupon.sg";
			String url = "http://www.groupon.sg/deals/deals-near-me/sole-relax/716817728?utm_campaign=alldeals&utm_medium=cp_3303884&utm_source=mashup";
			List<String> validHosts = Arrays.asList("www.groupon.sg", "alldeals.groupon.sg");
			String taskHost = new URL(url).getHost();
			System.out.println(validHosts.contains(taskHost));
			
			URL siteURL = new URL(hostUrl);
			URL currURL = new URL(url);
			String siteHost = siteURL.getHost();
			System.out.println("site.host->"+siteURL.getHost());
			String currHost = currURL.getHost();
			System.out.println("curr.host->"+currHost);
			System.out.println(currHost.endsWith(siteHost));
			System.out.println(CommonUtil.isSameHost(hostUrl, url));
//			FetchRequest req = new FetchRequest();
//			req.setUrl("http://alldeals.groupon.sg");
//			FetchResult rs = fetcher.fetch(req);
//			System.out.println(rs);
//			Collection<String> urls = Util.findAllLinkHref(rs.getPage().getContent(), "http://alldeals.groupon.sg");
//			for (String u : urls){
//				if (!u.startsWith("http://www.groupon.sg/deals/"))
//					continue;
//				
//				System.out.println(u);
//				req.setUrl(u);
//				rs = fetcher.fetch(req);
//				System.out.println(rs);
//			}
//			System.out.println(rs.getPage().getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
//		String json = "{\"id\":12,\"name\":\"weiwei\"}";
//		Map<String, Object> map = CommonUtil.parse(json, Map.class);
//		System.out.println(map);
	}
	
	public FetchResult fetch(Task task, FetchResult result) throws Exception {
		synchronized (site) {
			if (site.fetcher == null){
				PageFetcherImpl fetcher = new PageFetcherImpl();
				SpiderConfig config = new SpiderConfig();
				if (task.site.getCharset() != null && task.site.getCharset().trim().length() > 0)
					config.setCharset(task.site.getCharset());
				if (task.site.getUserAgent() != null && task.site.getUserAgent().trim().length() > 0)
					config.setUserAgentString(task.site.getUserAgent());
				if ("1".equals(task.site.getIncludeHttps()) || "true".equals(task.site.getIncludeHttps()))
					config.setIncludeHttpsPages(true);
				
				String sdelay = task.site.getReqDelay();
				if (sdelay == null || sdelay.trim().length() == 0)
					sdelay = "200";
				
				int delay = CommonUtil.toSeconds(sdelay).intValue()*1000;
				if (delay < 0)
					delay = 200;
				
				config.setPolitenessDelay(delay);
				fetcher.setConfig(config);
				
				fetcher.init(site);
				site.fetcher = fetcher;
			}
			
			String url = task.url.replace(" ", "%20");
			
			FetchRequest req = new FetchRequest();
			req.setUrl(url);
			
			FetchResult fr = site.fetcher.fetch(req);
			return fr;
		}
//		return fetch();
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
