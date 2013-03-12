package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.infra.DefaultLinkFinder;
import org.eweb4j.spiderman.infra.FrameLinkFinder;
import org.eweb4j.spiderman.infra.IframeLinkFinder;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.plugin.util.DefaultLinkNormalizer;
import org.eweb4j.spiderman.plugin.util.LinkNormalizer;
import org.eweb4j.spiderman.plugin.util.URLCanonicalizer;
import org.eweb4j.spiderman.plugin.util.UrlUtils;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.UrlRuleChecker;
import org.eweb4j.spiderman.xml.Model;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;

public class DigPointImpl implements DigPoint{

//	private FetchResult result = null;
//	private Task task = null;
	private Site site ;
	private SpiderListener listener;
	
	public void init(Site site, SpiderListener listener) {
		this.site = site;
		this.listener = listener;
	}

	public void destroy() {
	}

	public Collection<String> digNewUrls(FetchResult result, Task task, Collection<String> urls) throws Exception {
		return this.digNewUrls(result, task);
	}

	private Collection<String> digNewUrls(FetchResult result, Task task) throws Exception{
		if (result == null)
			return null;
		
		Collection<String> urls = new ArrayList<String>();
		String moveUrl = result.getMovedToUrl();
		if (moveUrl != null){
			if (!moveUrl.equals(task.url))
				urls.add(moveUrl);
		}
		// 如果定义了sourceUrl的digUrls，只是用这个方式发现新url
		Target target = site.getTargets().getTarget().get(0);
		Rules rules = target.getSourceRules();
		if (rules != null && rules.getRule() != null && !rules.getRule().isEmpty()){
			for (Rule r : rules.getRule()){
				boolean isSourceUrl = UrlRuleChecker.check(task.url, Arrays.asList(r));
				//判断当前url是否是sourceUrl
				if (!isSourceUrl)
					continue;
				
				//判断是否定义了digUrls
				Model mdl = r.getDigUrls();
				Target tgt = new Target();
				tgt.setCType(target.getCType());
				tgt.setIsForceUseXmlParser(target.getIsForceUseXmlParser());
				tgt.setName(target.getName());
				tgt.setNamespaces(target.getNamespaces());
				tgt.setModel(mdl);
				Collection<String> newUrls = UrlUtils.digUrls(result.getPage(), task, r, tgt, listener);
//				System.out.println("newUrls->"+newUrls);
//				System.out.println("from->"+task.url);
				//解析Model获得urls
				urls.addAll(newUrls);
			}
			
		}
		
		if (urls.isEmpty()){
			if (result.getPage() == null) return null;
			String html = result.getPage().getContent();
			if (html == null) return null;
			
			urls.addAll(Util.findAllLinkHref(html, task.site.getUrl()));
			urls.addAll(new DefaultLinkFinder(html).getLinks());
			urls.addAll(new IframeLinkFinder(html).getLinks());
			urls.addAll(new FrameLinkFinder(html).getLinks());
		}
		
		//resolveUrl
		String hostUrl = new StringBuilder("http://").append(new URL(task.site.getUrl()).getHost()).append("/").toString();
		List<String> newUrls = new ArrayList<String>(urls.size());
		for (String url : urls) {
			LinkNormalizer ln = new DefaultLinkNormalizer(hostUrl);
			String newUrl = ln.normalize(url);
//			String newUrl = URLCanonicalizer.getCanonicalURL(ln.normalize(url));
			if (newUrl.startsWith("mailto:"))
				continue;
			//去重复
			if (newUrls.contains(newUrl))
				continue;
			
			newUrls.add(newUrl);
		}
		
		return newUrls;
	}

	public static void main(String[] args){
		String s = "../../question?catalog=1&show=&p=1";
		System.out.println(s.startsWith("http://www.oschina.net/question?catalog=1&show=&p="));
		LinkNormalizer ln = new DefaultLinkNormalizer("http://www.oschina.net");
		String ts = ln.normalize(s);
		System.out.println(ts);
		String newUrl = URLCanonicalizer.getCanonicalURL(ts);
		System.out.println(newUrl);
	}
}
