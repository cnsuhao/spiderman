package org.eweb4j.spiderman.fetcher;

import org.eweb4j.mvc.Http;
import org.eweb4j.spiderman.xml.Site;




/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 下午06:41:33
 */
public abstract class PageFetcher {
    public abstract void init(SpiderConfig config, Site site) throws Exception;
	public abstract FetchResult fetch(FetchRequest req) throws Exception ;
	public abstract void close() throws Exception;
	public abstract Object getClient();
	
	public String get(String url) {
        return this.fetch(Http.Method.GET, url);
    }
    
    public String post(String url) {
        return this.fetch(Http.Method.POST, url);
    }
    
    public String fetch(String method, String url) {
        FetchRequest req = new FetchRequest();
        try {
            req.setUrl(url);
            req.setHttpMethod(method);
            FetchResult r = this.fetch(req);
            return r.getPage() == null ? null : r.getPage().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
