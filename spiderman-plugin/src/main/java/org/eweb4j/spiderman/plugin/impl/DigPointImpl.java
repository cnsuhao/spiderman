package org.eweb4j.spiderman.plugin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.infra.DefaultLinkFinder;
import org.eweb4j.spiderman.infra.FrameLinkFinder;
import org.eweb4j.spiderman.infra.IframeLinkFinder;
import org.eweb4j.spiderman.plugin.DigPoint;
import org.eweb4j.spiderman.plugin.util.DefaultLinkNormalizer;
import org.eweb4j.spiderman.plugin.util.LinkNormalizer;
import org.eweb4j.spiderman.plugin.util.ModelParser;
import org.eweb4j.spiderman.plugin.util.URLCanonicalizer;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.UrlRuleChecker;
import org.eweb4j.spiderman.xml.Field;
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

//	public void context(FetchResult result, Task task) throws Exception {
//		this.result = result;
//		this.task = task;
//	}
	
	public Collection<String> digNewUrls(FetchResult result, Task task, Collection<String> urls) throws Exception {
		return this.digNewUrls(result, task);
	}

	private Collection<String> digNewUrls(FetchResult result, Task task) throws Exception{
		if (result == null)
			return null;
		
		Collection<String> urls = new HashSet<String>();
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
				//判断是否定义了digUrls
				boolean isDigUrls = false;
				Field digUrlField = r.getDigUrls();
				if (digUrlField != null && digUrlField.getParsers() != null && !digUrlField.getParsers().getParser().isEmpty())
					isDigUrls = true;
				
				if (isDigUrls) {
					//判断当前url是否是sourceUrl
					boolean isSourceUrl = UrlRuleChecker.check(task.url, Arrays.asList(r));
					if (isSourceUrl){
						// 按照digUrlPaser的配置对页面进行解析得到URL
						Target digTarget = new Target();
						Model digModel = new Model();
						digUrlField.setName("url");
						digModel.setField(Arrays.asList(digUrlField));
						digTarget.setModel(digModel);
						digTarget.setNamespaces(site.getTargets().getTarget().get(0).getNamespaces());
						ModelParser parser = new ModelParser(task, digTarget, listener);
						Page sourcePage = result.getPage();
						List<Map<String, Object>> models = parser.parse(sourcePage);
						for (Field f : digTarget.getModel().getField()){
							if ("url".equals(f.getName())){
								for (Map<String, Object> model : models){
									Object val = model.get("url");
									//如果url是数组
									if ("1".equals(f.getIsArray()) || "true".equals(f.getIsArray())){
//										listener.onInfo(Thread.currentThread(), task, "dig new urls->"+(List<String>)val);
										urls.addAll((List<String>)val);
									}else{
//										listener.onInfo(Thread.currentThread(), task, "dig new urls->"+val);
										urls.add(String.valueOf(val));
									}
								}
								
								break;
							}
						}
					}
				}
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
			String newUrl = URLCanonicalizer.getCanonicalURL(ln.normalize(url));
			if (newUrl.startsWith("mailto:"))
				continue;
			newUrls.add(newUrl);
		}
		
		return newUrls;
	}
	
}
