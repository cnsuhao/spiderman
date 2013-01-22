package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.infra.DefaultLinkFinder;
import org.eweb4j.spiderman.infra.FrameLinkFinder;
import org.eweb4j.spiderman.infra.IframeLinkFinder;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;

import org.eweb4j.spiderman.plugin.util.DefaultLinkNormalizer;
import org.eweb4j.spiderman.plugin.util.LinkNormalizer;
import org.eweb4j.spiderman.plugin.util.URLCanonicalizer;
import org.eweb4j.spiderman.plugin.util.Util;

public class DigPointImpl implements DigPoint{

	private FetchResult result = null;
	private Task task = null;
	
	public void init(Site site, SpiderListener listener) {
		
	}

	public void destroy() {
	}

	public void context(FetchResult result, Task task) throws Exception {
		this.result = result;
		this.task = task;
	}
	
	public Collection<String> digNewUrls(Collection<String> urls) throws Exception {
		return this.digNewUrls(result);
	}

	private Collection<String> digNewUrls(FetchResult result) throws Exception{
		if (result == null)
			return null;
		
		Collection<String> urls = new HashSet<String>();
		String moveUrl = result.getMovedToUrl();
		
		if (moveUrl != null){
			if (!moveUrl.equals(task.url))
				urls.add(moveUrl);
		}else {
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
			String newUrl = URLCanonicalizer.getCanonicalURL(ln.normalize(url));
			if (newUrl.startsWith("mailto:"))
				continue;
			newUrls.add(newUrl);
		}
		
		return newUrls;
	}
	
}
