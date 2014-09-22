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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.eweb4j.util.CommonUtil;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Web 页面内容获取器
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 上午11:04:50
 */
public class HtmlUnitDownloader extends PageFetcher{

    private WebClient client = null;
	private final Object mutex = new Object();
	private long lastFetchTime = 0;
	private SpiderConfig config;
	private Map<String, String> headers = new Hashtable<String, String>();
	private Map<String, List<String>> cookies = new Hashtable<String, List<String>>();
	private Site site;
	
	public HtmlUnitDownloader() {
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
        com.gargoylesoftware.htmlunit.util.Cookie cok = new com.gargoylesoftware.htmlunit.util.Cookie(host,key,val,path,-1,false);
        this.client.getCookieManager().addCookie(cok);
    }

    public void addHeader(String key, String val) {
        if (this.headers.containsKey(key)) {
           this.headers.put(key, this.headers.get(key) + "; " + val);
        } else {
            this.headers.put(key, val);
        }
        this.client.addRequestHeader(key, val);
    }
    
	public void init(SpiderConfig config, Site _site) {
	    this.config = config;
	    client = new WebClient(BrowserVersion.CHROME);
	    
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
			HtmlPage pageResult = this.client.getPage(toFetchURL);
			
			//设置已访问URL
			fetchResult.setFetchedUrl(toFetchURL);
			String uri = pageResult.getDocumentURI();
			if (!uri.equals(toFetchURL)) 
				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) 
					fetchResult.setFetchedUrl(uri);
			//服务端返回的状态码
			int statusCode = pageResult.getWebResponse().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode != HttpStatus.SC_NOT_FOUND) {
					String redirect = pageResult.getWebResponse().getResponseHeaderValue("Location");
					//如果是301、302跳转，获取跳转URL即可返回
					if (redirect != null && (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)) 
						fetchResult.setMovedToUrl(URLCanonicalizer.getCanonicalURL(redirect, toFetchURL));
				}
				//只要不是OK的除了设置跳转URL外设置statusCode即可返回
				//判断是否有忽略状态码的设置
				if (this.site.getSkipStatusCode() != null && this.site.getSkipStatusCode().trim().length() > 0){
					String[] scs = this.site.getSkipStatusCode().split(",");
					for (String code : scs){
						int c = CommonUtil.toInt(code);
						//忽略此状态码，依然解析entity
						if (statusCode == c){
							assemPage(fetchResult, pageResult);
							break;
						}
					}
				}
				fetchResult.setStatusCode(statusCode);
				return fetchResult;
			}

			//处理服务端返回的实体内容
			if (pageResult != null) {
				fetchResult.setStatusCode(statusCode);
				assemPage(fetchResult, pageResult);
			}
		} catch (Throwable e) {
		    e.printStackTrace();
			fetchResult.setFetchedUrl(e.toString());
			fetchResult.setStatusCode(Status.INTERNAL_SERVER_ERROR.ordinal());
		} 
		
		return fetchResult;
	}
	
	private void assemPage(FetchResult fetchResult, HtmlPage p)
			throws Exception {
		Page page = load(p);
		page.setUrl(fetchResult.getFetchedUrl());
		fetchResult.setPage(page);
	}
	
	/**
	 * 将Entity的内容载入Page对象
	 */
	private Page load(HtmlPage p) throws Exception {
		Page page = new Page();
		
		//设置返回内容的ContentType
		String contentType = p.getWebResponse().getContentType();
		page.setContentType(contentType);
		
		//设置返回内容的字符编码
		String contentEncoding = p.getPageEncoding();
		page.setEncoding(contentEncoding);
		
		//设置返回内容的字符集
		String contentCharset = p.getWebResponse().getContentCharset();
		page.setCharset(contentCharset);
		//根据配置文件设置的字符集参数进行内容二进制话
		String charset = config.getCharset();
		String content = this.read(p.getWebResponse().getContentAsStream(), charset);
		page.setContent(content);
		
		return page;
	}
	
	/**
	 * 根据字符集从输入流里面读取String内容
	 * @date 2013-1-7 上午11:25:04
	 * @param inputStream
	 * @param charset
	 * @return
	 */
	private String read(final InputStream inputStream, String charset) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			if (charset == null || charset.trim().length() == 0)
				reader = new BufferedReader(new InputStreamReader(inputStream));
			else
				reader = new BufferedReader(new InputStreamReader(inputStream, charset));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
		}

		return sb.toString();
	}

    public void close() throws Exception {
        this.client.closeAllWindows();
    }
	
}
