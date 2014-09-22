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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.eweb4j.mvc.Http;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.fetcher.PageFetcher;
import org.eweb4j.spiderman.fetcher.SpiderConfig;
import org.eweb4j.spiderman.fetcher.Status;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.util.CommonUtil;

/**
 * Web 页面内容获取器
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 上午11:04:50
 */
public class HttpClientDownloader extends PageFetcher{

	private ThreadSafeClientConnManager connectionManager;
	private DefaultHttpClient httpClient;
	private final Object mutex = new Object();
	private long lastFetchTime = 0;
	private SpiderConfig config;
	private Map<String, String> headers = new Hashtable<String, String>();
	private Map<String, List<String>> cookies = new Hashtable<String, List<String>>();
	private Site site;
	
	public Object getClient() {
        return this.httpClient;
    }
	
	/**
	 * 处理GZIP解压缩
	 * @author weiwei l.weiwei@163.com
	 * @date 2013-1-7 上午11:26:24
	 */
	private static class GzipDecompressingEntity extends HttpEntityWrapper {
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}
		public InputStream getContent() throws IOException, IllegalStateException {
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}
		public long getContentLength() {
			return -1;
		}
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
		
		BasicClientCookie clientCookie = new BasicClientCookie(name, value);
		clientCookie.setPath(c.path());
		clientCookie.setDomain(c.domain());
		httpClient.getCookieStore().addCookie(clientCookie);
	}

	public void addHeader(String key, String val) {
		if (this.headers.containsKey(key))
			this.headers.put(key, this.headers.get(key) + "; " + val);
		else
			this.headers.put(key, val);
	}

	/**
	 * 构造器，进行client的参数设置，包括Header、Cookie等
	 * @param aconfig
	 * @param cookies
	 */
	public void init(SpiderConfig config, Site _site) {
	    this.config = config;
		//设置HTTP参数
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.USER_AGENT, config.getUserAgentString());
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSocketTimeout());
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());
		
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF-8");
		paramsBean.setUseExpectContinue(false);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		if (config.isIncludeHttpsPages()) 
			schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

		connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		connectionManager.setMaxTotal(config.getMaxTotalConnections());
		connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());
		httpClient = new DefaultHttpClient(connectionManager, params);
		
		httpClient.getParams().setIntParameter("http.socket.timeout", 60000);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, config.isFollowRedirects());
//		HttpClientParams.setCookiePolicy(httpClient.getParams(),CookiePolicy.BEST_MATCH);

		//设置响应拦截器
        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header contentEncoding = entity.getContentEncoding();
                if (contentEncoding != null) {
                    HeaderElement[] codecs = contentEncoding.getElements();
                    for (HeaderElement codec : codecs) {
                    	//处理GZIP解压缩
                        if (codec.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });
        
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
	 * @date 2013-1-7 上午11:08:54
	 * @param toFetchURL
	 * @return
	 */
	public FetchResult fetch(FetchRequest req) throws Exception{
		if (req.getHttpMethod() != null && !Http.Method.GET.equals(req.getHttpMethod())) {
			//获取到URL后面的QueryParam
			String query = new URL(req.getUrl()).getQuery();
			for (String q : query.split("\\&")) {
				String[] qv = q.split("=");
				String name = qv[0];
				String val = qv[1];
				List<Object> vals = req.getParams().get(name);
				if (vals == null) {
					vals = new ArrayList<Object>();
					req.getParams().put(name, vals);
				}
				
				vals.add(val);
			}
			
			return request(req);
		}
		FetchResult fetchResult = new FetchResult();
		HttpGet get = null;
		HttpEntity entity = null;
		String toFetchURL = req.getUrl();
		try {
			get = new HttpGet(toFetchURL);
			//设置请求GZIP压缩，注意，前面必须设置GZIP解压缩处理
			get.addHeader("Accept-Encoding", "gzip");
			for (Iterator<Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();){
				Entry<String, String> entry = it.next();
				get.addHeader(entry.getKey(), entry.getValue());
			}
			
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
			Header[] headers = get.getAllHeaders();
			for (Header h : headers){
				Map<String, List<String>> hs = req.getHeaders();
				String key = h.getName();
				List<String> val = hs.get(key);
				if (val == null)
					val = new ArrayList<String>();
				val.add(h.getValue());
				
				hs.put(key, val);
			}
			
			req.getCookies().putAll(this.cookies);
			
			fetchResult.setReq(req);
			//执行get访问，获取服务端返回内容
			HttpResponse response = httpClient.execute(get);
			headers = response.getAllHeaders();
			for (Header h : headers){
				Map<String, List<String>> hs = fetchResult.getHeaders();
				String key = h.getName();
				List<String> val = hs.get(key);
				if (val == null)
					val = new ArrayList<String>();
				val.add(h.getValue());
				
				hs.put(key, val);
			}
			//设置已访问URL
			fetchResult.setFetchedUrl(toFetchURL);
			String uri = get.getURI().toString();
			if (!uri.equals(toFetchURL)) 
				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) 
					fetchResult.setFetchedUrl(uri);
			
			entity = response.getEntity();
			//服务端返回的状态码
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode != HttpStatus.SC_NOT_FOUND) {
					Header locationHeader = response.getFirstHeader("Location");
					//如果是301、302跳转，获取跳转URL即可返回
					if (locationHeader != null && (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)) 
						fetchResult.setMovedToUrl(URLCanonicalizer.getCanonicalURL(locationHeader.getValue(), toFetchURL));
				}
				//只要不是OK的除了设置跳转URL外设置statusCode即可返回
				//判断是否有忽略状态码的设置
				if (this.site.getSkipStatusCode() != null && this.site.getSkipStatusCode().trim().length() > 0){
					String[] scs = this.site.getSkipStatusCode().split(",");
					for (String code : scs){
						int c = CommonUtil.toInt(code);
						//忽略此状态码，依然解析entity
						if (statusCode == c){
							assemPage(fetchResult, entity);
							break;
						}
					}
				}
				fetchResult.setStatusCode(statusCode);
				return fetchResult;
			}

			//处理服务端返回的实体内容
			if (entity != null) {
				fetchResult.setStatusCode(statusCode);
				assemPage(fetchResult, entity);
				return fetchResult;
			}
		} catch (Throwable e) {
			fetchResult.setFetchedUrl(e.toString());
			fetchResult.setStatusCode(Status.INTERNAL_SERVER_ERROR.ordinal());
			return fetchResult;
		} finally {
			try {
				if (entity == null && get != null) 
					get.abort();
			} catch (Exception e) {
				throw e;
			}
		}
		
		fetchResult.setStatusCode(Status.UNSPECIFIED_ERROR.ordinal());
		return fetchResult;
	}
	
	/**
	 * 请求
	 * @date 2013-1-7 上午11:08:54
	 * @param toFetchURL
	 * @return
	 */
	public FetchResult request(FetchRequest req) throws Exception{
		FetchResult fetchResult = new FetchResult();
		HttpUriRequest request = null;
		HttpEntity entity = null;
		String toFetchURL = req.getUrl();
		boolean isPost = false;
		try {
			if (Http.Method.GET.equalsIgnoreCase(req.getHttpMethod()))
				request = new HttpGet(toFetchURL);
			else if (Http.Method.POST.equalsIgnoreCase(req.getHttpMethod())){
				request = new HttpPost(toFetchURL);
				isPost = true;
			}else if (Http.Method.PUT.equalsIgnoreCase(req.getHttpMethod()))
				request = new HttpPut(toFetchURL);
			else if (Http.Method.HEAD.equalsIgnoreCase(req.getHttpMethod()))
				request = new HttpHead(toFetchURL);
			else if (Http.Method.OPTIONS.equalsIgnoreCase(req.getHttpMethod()))
				request = new HttpOptions(toFetchURL);
			else if (Http.Method.DELETE.equalsIgnoreCase(req.getHttpMethod()))
				request = new HttpDelete(toFetchURL);
			else 
				throw new Exception("Unknown http method name");
			
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
			
			//设置请求GZIP压缩，注意，前面必须设置GZIP解压缩处理
			request.addHeader("Accept-Encoding", "gzip");
			for (Iterator<Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();){
				Entry<String, String> entry = it.next();
				request.addHeader(entry.getKey(), entry.getValue());
			}
			
			//记录请求信息
			Header[] headers = request.getAllHeaders();
			for (Header h : headers){
				Map<String, List<String>> hs = req.getHeaders();
				String key = h.getName();
				List<String> val = hs.get(key);
				if (val == null)
					val = new ArrayList<String>();
				val.add(h.getValue());
				
				hs.put(key, val);
			}
			req.getCookies().putAll(this.cookies);
			fetchResult.setReq(req);
			
			HttpEntity reqEntity = null;
			if (Http.Method.POST.equalsIgnoreCase(req.getHttpMethod())
						|| Http.Method.PUT.equalsIgnoreCase(req.getHttpMethod())){
				if (!req.getFiles().isEmpty()) {
					reqEntity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
					for (Iterator<Entry<String, List<File>>> it = req.getFiles().entrySet().iterator(); it.hasNext(); ){
						Entry<String, List<File>> e = it.next();
						String paramName = e.getKey();
						for (File file : e.getValue()) {
							// For File parameters
							((MultipartEntity)reqEntity).addPart( paramName, new FileBody(file));
						}
					}
					
					for (Iterator<Entry<String, List<Object>>> it = req.getParams().entrySet().iterator(); it.hasNext(); ){
						Entry<String, List<Object>> e = it.next();
						String paramName = e.getKey();
						for (Object paramValue : e.getValue()) {
							// For usual String parameters
							((MultipartEntity)reqEntity).addPart( paramName, new StringBody(String.valueOf(paramValue), "text/plain", Charset.forName( "UTF-8" )));
						}
					}
				}else{
					List<NameValuePair> params = new ArrayList<NameValuePair>(req.getParams().size());
					for (Iterator<Entry<String, List<Object>>> it = req.getParams().entrySet().iterator(); it.hasNext(); ){
						Entry<String, List<Object>> e = it.next();
						String paramName = e.getKey();
						for (Object paramValue : e.getValue()) {
							 params.add(new BasicNameValuePair(paramName, String.valueOf(paramValue)));
						}
					}
					reqEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
				}
				
				if (isPost)
					((HttpPost)request).setEntity(reqEntity);
				else
					((HttpPut)request).setEntity(reqEntity);
			}
			
			//执行请求，获取服务端返回内容
			HttpResponse response = httpClient.execute(request);
			headers = response.getAllHeaders();
			for (Header h : headers){
				Map<String, List<String>> hs = fetchResult.getHeaders();
				String key = h.getName();
				List<String> val = hs.get(key);
				if (val == null)
					val = new ArrayList<String>();
				val.add(h.getValue());
				
				hs.put(key, val);
			}
			//设置已访问URL
			fetchResult.setFetchedUrl(toFetchURL);
			String uri = request.getURI().toString();
			if (!uri.equals(toFetchURL)) 
				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) 
					fetchResult.setFetchedUrl(uri);
			
			entity = response.getEntity();
			//服务端返回的状态码
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode != HttpStatus.SC_NOT_FOUND) {
					Header locationHeader = response.getFirstHeader("Location");
					//如果是301、302跳转，获取跳转URL即可返回
					if (locationHeader != null && (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)) 
						fetchResult.setMovedToUrl(URLCanonicalizer.getCanonicalURL(locationHeader.getValue(), toFetchURL));
				}
				//只要不是OK的除了设置跳转URL外设置statusCode即可返回
				//判断是否有忽略状态码的设置
				if (this.site.getSkipStatusCode() != null && this.site.getSkipStatusCode().trim().length() > 0){
					String[] scs = this.site.getSkipStatusCode().split(",");
					for (String code : scs){
						int c = CommonUtil.toInt(code);
						//忽略此状态码，依然解析entity
						if (statusCode == c){
							assemPage(fetchResult, entity);
							break;
						}
					}
				}
				fetchResult.setStatusCode(statusCode);
				return fetchResult;
			}

			//处理服务端返回的实体内容
			if (entity != null) {
				fetchResult.setStatusCode(statusCode);
				assemPage(fetchResult, entity);
				return fetchResult;
			}
		} catch (Throwable e) {
			fetchResult.setFetchedUrl(e.toString());
			fetchResult.setStatusCode(Status.INTERNAL_SERVER_ERROR.ordinal());
			return fetchResult;
		} finally {
			try {
				if (entity == null && request != null) 
					request.abort();
			} catch (Exception e) {
				throw e;
			}
		}
		
		fetchResult.setStatusCode(Status.UNSPECIFIED_ERROR.ordinal());
		return fetchResult;
	}
	
	private void assemPage(FetchResult fetchResult, HttpEntity entity)
			throws Exception {
		Page page = load(entity);
		page.setUrl(fetchResult.getFetchedUrl());
		fetchResult.setPage(page);
	}
	
	/**
	 * 将Entity的内容载入Page对象
	 * @date 2013-1-7 上午11:22:06
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private Page load(HttpEntity entity) throws Exception {
		Page page = new Page();
		
		//设置返回内容的ContentType
		String contentType = null;
		Header type = entity.getContentType();
		if (type != null) 
			contentType = type.getValue();
		page.setContentType(contentType);
		
		//设置返回内容的字符编码
		String contentEncoding = null;
		Header encoding = entity.getContentEncoding();
		if (encoding != null) 
			contentEncoding = encoding.getValue();
		page.setEncoding(contentEncoding);
		
		//设置返回内容的字符集
		String contentCharset = EntityUtils.getContentCharSet(entity);
		page.setCharset(contentCharset);
		//根据配置文件设置的字符集参数进行内容二进制话
		String charset = config.getCharset();
		String content = this.read(entity.getContent(), charset);
		page.setContent(content);
//		if (charset == null || charset.trim().length() == 0)
//			page.setContentData(content.getBytes());
//		else
//			page.setContentData(content.getBytes(charset));
		
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
	
	/**
	 * 从输入流里读取二进制数据
	 * @date 2013-1-7 上午11:25:38
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	private byte[] read(final InputStream inputStream) throws Exception {
		byte[] bytes = new byte[1000];
		int i = 0;
		int b;
		try {
			while ((b = inputStream.read()) != -1) {
				bytes[i++] = (byte) b;
				if (bytes.length == i) {
					byte[] newBytes = new byte[(bytes.length * 3) / 2 + 1];
					for (int j = 0; j < bytes.length; j++) {
						newBytes[j] = bytes[j];
					}
					bytes = newBytes;
				}
			}
		} catch (IOException e) {
			throw new Exception("There was a problem reading stream.", e);
		}

		byte[] copy = Arrays.copyOf(bytes, i);

		return copy;
	}


	public HttpClient getHttpClient() {
		return httpClient;
	}

    public void close() throws Exception {
        this.httpClient.close();
    }

	/**
	 * Proxy
	 * if (config.getProxyHost() != null) {
			if (config.getProxyUsername() != null) {
				httpClient.getCredentialsProvider().setCredentials(
						new AuthScope(config.getProxyHost(), config.getProxyPort()),
						new UsernamePasswordCredentials(config.getProxyUsername(), config.getProxyPassword()));
			}

			HttpHost proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
	 */
}
