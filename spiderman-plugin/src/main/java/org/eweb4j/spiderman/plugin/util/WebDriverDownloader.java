/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eweb4j.spiderman.plugin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.fetcher.SpiderConfig;
import org.eweb4j.spiderman.fetcher.Status;
import org.eweb4j.spiderman.xml.Site;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Web 页面内容获取器
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 上午11:04:50
 */
public class WebDriverDownloader extends PageFetcher{

    private WebDriver client = null;
	private final Object mutex = new Object();
	private long lastFetchTime = 0;
	private SpiderConfig config;
	private Map<String, String> headers = new Hashtable<String, String>();
	private Map<String, List<String>> cookies = new Hashtable<String, List<String>>();
	private Site site;
	
	public WebDriverDownloader() {
	}
	public Object getClient() {
	    return this.client;
	}
	
	public void addCookie(String key, String val, String host, String path) {
        Cookie c = new Cookie(key, val, host, path);
        //设置Cookie
        String name = c.name();
        String value = c.value();
        List<String> vals = this.cookies.get(name);
        if (vals == null)
            vals = new ArrayList<String>();
        vals.add(value);
        this.cookies.put(key, vals);
    }

    public void addHeader(String key, String val) {
        if (this.headers.containsKey(key)) {
           this.headers.put(key, this.headers.get(key) + "; " + val);
        } else {
            this.headers.put(key, val);
        }
    }
    
	public void init(SpiderConfig config, Site _site) {
	    this.config = config;
	    String opt = _site.getOption("webdriver.chrome.driver");
	    System.getProperties().setProperty("webdriver.chrome.driver", opt);
	    client = new ChromeDriver();
	    
	    if (_site != null) {
            this.site = _site;
            if (this.site.getHeaders() != null && this.site.getHeaders().getHeader() != null){
                for (org.eweb4j.spiderman.xml.Header header : this.site.getHeaders().getHeader()){
                    this.addHeader(header.getName(), header.getValue());
                }
            }
            if (this.site.getCookies() != null && this.site.getCookies().getCookie() != null){
                for (org.eweb4j.spiderman.xml.Cookie cookie : this.site.getCookies().getCookie()){
                    this.addCookie(cookie.getName(), cookie.getValue(), cookie.getHost(), cookie.getPath());
                }
            }
        }
	}
	
	/**
	 * 抓取目标url的内容
	 */
	public FetchResult fetch(FetchRequest req) throws Exception{
		FetchResult fetchResult = new FetchResult();
		String toFetchURL = req.getUrl();
		try {
			//同步信号量,在真正对服务端进行访问之前进行访问间隔的控制
			// TODO 针对每个请求有一个delay的参数设置
			synchronized (mutex) {
				//获取当前时间
				long now = (new Date()).getTime();
				//对同一个Host抓取时间间隔进行控制，若在设置的时限内则进行休眠
				if (now - lastFetchTime < config.getPolitenessDelay()) 
					Thread.sleep(config.getPolitenessDelay() - (now - lastFetchTime));
				//不断更新最后的抓取时间，注意，是针对HOST的，不是针对某个URL的
				lastFetchTime = (new Date()).getTime();
			}
			
			//记录get请求信息
			
			for (String header : this.headers.keySet()){
				String key = header;
				List<String> val = Arrays.asList(this.headers.get(key).split(","));
				req.getHeaders().put(key, val);
			}
			
			req.getCookies().putAll(this.cookies);
			
			fetchResult.setReq(req);
			
			//执行get访问，获取服务端返回内容
            this.client.get(toFetchURL);
            if (this.site.getCookies() != null && this.site.getCookies().getCookie() != null){
                for (org.eweb4j.spiderman.xml.Cookie cookie : this.site.getCookies().getCookie()){
                    org.openqa.selenium.Cookie cok = new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getHost(), cookie.getPath(), null);
                    this.client.manage().addCookie(cok);
                }
            }
            
            this.client.get(toFetchURL);
			WebElement html = this.client.findElement(By.tagName("html"));
			//设置已访问URL
			fetchResult.setFetchedUrl(toFetchURL);
			String uri = toFetchURL;
			if (!uri.equals(toFetchURL)) 
				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) 
					fetchResult.setFetchedUrl(uri);
			
			if (html != null) {
				fetchResult.setStatusCode(HttpStatus.SC_OK);
				assemPage(fetchResult, html);
			}
		} catch (Throwable e) {
		    e.printStackTrace();
			fetchResult.setFetchedUrl(e.toString());
			fetchResult.setStatusCode(Status.INTERNAL_SERVER_ERROR.ordinal());
		} 
		
		return fetchResult;
	}
	
	private void assemPage(FetchResult fetchResult, WebElement html)
			throws Exception {
		Page page = load(html);
		page.setUrl(fetchResult.getFetchedUrl());
		fetchResult.setPage(page);
	}
	
	private Page load(WebElement html) throws Exception {
		Page page = new Page();
		page.setContent(html.getAttribute("outerHTML"));
		return page;
	}

    public void close() throws Exception {
        for (String h : this.client.getWindowHandles()) {
            WebDriver d = this.client.switchTo().window(h);
            try {
                d.quit();
            } catch (Throwable e) {
            }
            try {
                d.quit();
            } catch (Throwable e) {
            }
        }
        try {
            this.client.close();
            
        } catch (Throwable e) {
        }
        
        try {
            this.client.quit();
        } catch (Throwable e) {
        }
    }
	
}
