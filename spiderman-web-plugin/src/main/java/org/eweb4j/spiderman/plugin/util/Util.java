package org.eweb4j.spiderman.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.UrlRuleChecker;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.spiderman.xml.Target;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

public class Util {
	
	public static Logger logger=Logger.getLogger(Util.class.getName());
	
	public static Map<String,String> imgSrcDownLoadMap=new HashMap<String,String>();
	/**
	 * 匹配目标对象将根据Task获取Target;
	 * @param task
	 * @return
	 * @throws Exception
	 */
	public static Target matchTarget(Task task) throws Exception{
		for (Target target : task.site.getTargets().getTarget()){
			Rules rules = target.getUrlRules();
			if(rules != null)
			{
				Rule tgtRule = UrlRuleChecker.check(task.url, rules.getRule(), rules.getPolicy());
				if (tgtRule != null){
					if (task.target == null)
						task.target = target;
					task.httpMethod = tgtRule.getHttpMethod();
					
					return target;
				}
			}else{
				if (task.target == null)
					task.target = target;
				return target;
			}
		}

		return null;
	}
	
	/**
	 * 获取所有页面a标签中的链接;
	 * @param html
	 * @param hostUrl
	 * @return
	 * @throws Exception
	 */
	public static Collection<String> findAllLinkHref(String html, String hostUrl) throws Exception{
		Collection<String> urls = new ArrayList<String>();

		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode node = cleaner.clean(html);
		Object[] ns = node.evaluateXPath("//a[@href]");
		for (Object object : ns) {
			TagNode node2=(TagNode) object;
			String href = node2.getAttributeByName("href");
			if (href == null || href.trim().length() == 0)
				continue;

			if (!href.startsWith("https://") && !href.startsWith("http://")){
				StringBuilder sb = new StringBuilder("http://").append(new URL(hostUrl).getHost());
				if (!href.startsWith("/"))
					sb.append("/");
				href = sb.append(href).toString();
			}
//			href = URLCanonicalizer.getCanonicalURL(href);
			if (href == null)
				continue;
			if (href.startsWith("mailto:"))
				continue;

			urls.add(href);
		}

		return urls;
	}
	/**
	 * 下载网络图片并重置本地图片路径;
	 * @param tn
	 * @param srcUrl
	 * @param targetUrl
	 * @return
	 * @throws Exception
	 */
	public static TagNode downloadToResetImageSrc(HtmlCleaner hc,String page,String srcUrl,String targetUrl,String redownUrl) throws Exception{
		
		System.out.println("========================================图片模块路径处理中===============================");
		
		HttpClient http=new DefaultHttpClient(FileUtil.getParams());
		Pattern pt=Pattern.compile("[^\\s='\"(]+\\.(gif|png|jpg|jpeg|bmp|swf)(?=[\"')]?)");//正则匹配规则获取所有图片链接+swf的链接;
 		Matcher mc=pt.matcher(page);
 		Map<String,String> isReplace=new HashMap<String, String>();//是否已经替换临时内存区;
 		File imagesDir=new File(targetUrl);
        if(!imagesDir.exists()){
         	imagesDir.mkdirs();
        }
 		while(mc.find()){
 			String str=mc.group();
 			String hostStr=getFormateImgSrc(str,srcUrl);
        	String hostAbosutePath=imagesDir.getAbsolutePath()+File.separator;
            String jpgName=FilenameUtils.getName(hostStr);//图片名
            if(!imgSrcDownLoadMap.containsKey(jpgName)){
            	hostStr=processDownImgSrc(http,str,hostAbosutePath+jpgName,srcUrl,imagesDir,redownUrl);
            	imgSrcDownLoadMap.put(jpgName,str);
            }
			String absolutePath=(hostAbosutePath+jpgName).replaceAll("\\\\","/");
			if(isReplace.get(jpgName) == null){
				page=page.replace("'"+str+"'","'"+absolutePath+"'").replace("\""+str+"\"","\""+absolutePath+"\"");
				isReplace.put(jpgName,hostStr);
			}
 		}
		return resetJsDynamicSrc(hc,page,targetUrl);
	}
	/**
	 * 图片路径不对，自动重组寻址重新尝试下载;
	 * @param http
	 * @param imgPosiveUrl
	 * @param oldHostPath
	 * @param srcUrl
	 * @param fileDir
	 * @param redownUrl
	 * @return
	 */
	public static String processDownImgSrc(HttpClient http,String imgPosiveUrl,String oldHostPath,String srcUrl,File fileDir,String redownUrl){
		String hostStr=getFormateImgSrc(imgPosiveUrl,srcUrl);
		File imgFile=dowLoadFile(http,hostStr,oldHostPath);//第一次下载;
		while(imgPosiveUrl.startsWith(".")||imgPosiveUrl.startsWith("/")){
			imgPosiveUrl=imgPosiveUrl.substring(1);
		}
		String newHostStr=imgPosiveUrl;
		if (!imgPosiveUrl.startsWith("https://") && !imgPosiveUrl.startsWith("http://")){
			   newHostStr=srcUrl+imgPosiveUrl;
		}
		if(redownUrl!=null){
				String type = FileUtil.getFileType(oldHostPath);
				String imgName=imgPosiveUrl.substring(imgPosiveUrl.lastIndexOf("/")+1);
				if(type!=null){
					if(!"gif|png|jpg|jpeg|bmp|swf".contains(type)){//图片路径不对，自动组装寻址重新尝试下载;
						newHostStr=srcUrl+redownUrl+"/"+imgPosiveUrl;
						File reDownImgFile=dowLoadFile(http,newHostStr,fileDir+File.separator+imgName);//第二次重组下载;
					}
				}
		}
		System.out.println("下载图片:"+newHostStr);
		return newHostStr;
	}
	/**
	 * 将获取到的图片相对路径组装格式化成网络资源全路径;
	 * @param src
	 * @param hostUrl
	 * @return
	 */
	public static String getFormateImgSrc(String src,String hostUrl){
		if (src == null || src.trim().length() == 0)
			return "";
		if (!src.startsWith("https://") && !src.startsWith("http://")){
			StringBuilder sb = new StringBuilder("http://");
			Pattern pattern = Pattern.compile("\\..*?\\.");//存在域名不带http://开头的src；比如://dcs.conac.cn/image/red.png;
			while(src.startsWith("/")||src.startsWith(".")){
				src = src.substring(1);
			}
			Matcher matcher = pattern.matcher(src);
			if(matcher.find()){
				src=sb.append(src).toString();
			}else{
				try {
					sb.append(new URL(hostUrl).getHost()).append("/");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				src = sb.append(src).toString();
			}
		}
		if (src == null)
			return "";
		if (src.startsWith("mailto:"))
			return "";
		return src;
	}
	/**
	 * 重置动态JS中的动态的Src;
	 * @param page
	 * @param absolutePath
	 */
	public static TagNode resetJsDynamicSrc(HtmlCleaner hc,String page,String absolutePath){
		Pattern pt=Pattern.compile("[^\\s=\"]+\\.(gif|png|jpg|jpeg|bmp|swf|css)(?=[\"']?)");
		Map<String,String> imgMap=new HashMap<String, String>();
		/*清理成Tree结构*/
		TagNode tn=hc.clean(page);
	    Object[] objs;
		try {
			objs = tn.evaluateXPath("//script");
	        for(Object object:objs){
	        	TagNode jsNode=(TagNode) object;
	        	if(jsNode.getChildren().size()>0){
	        		for(int i=0;i<jsNode.getChildren().size();i++){
	        			String jsCodeStr=jsNode.getChildren().get(i).toString();
	        			Matcher mc=pt.matcher(jsCodeStr);
	        			while(mc.find()){
			    			String str=mc.group();
			    			String temp=jsCodeStr.substring(mc.start(),mc.end());
			    			temp=temp.substring(temp.lastIndexOf("=")+1);
			    			while(temp.startsWith("'")||temp.startsWith("\"")){
			    				temp=temp.substring(1).trim();
			    			}
			    			if(!temp.contains(":")&&!temp.contains("(")){
			    				String resetStr=absolutePath;
				    			if(absolutePath.endsWith("/")){
				    				resetStr=resetStr+temp.substring(temp.lastIndexOf("/")+1);
				    			}else{
				    				int lastIndex=temp.lastIndexOf("/")!=-1?temp.lastIndexOf("/"):0;
				    				if(lastIndex==-1){
				    					resetStr=resetStr+"/";
				    				}
				    				resetStr=resetStr+temp.substring(lastIndex);
				    			}
				    			resetStr=resetStr.replaceAll("\\\\","/");
			    				imgMap.put(temp, resetStr);
			    			}
			    		}
	        		}
	        	}
	        }
	        for(String key:imgMap.keySet()){
        		page=page.replace(key,imgMap.get(key));
        	}
    		tn=hc.clean(page);
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		return tn;
	}
	/**
	 * 下载网络Css并重置本地Css路径;
	 * @param tn
	 * @param srcUrl
	 * @param targetUrl
	 * @return
	 * @throws Exception
	 */
	public static Collection<String> downloadToResetCssLinkHref(TagNode tn, String srcUrl,String targetUrl,String charset,String redownUrl) throws Exception{
		
		System.out.println("========================================Css模块路径处理中===============================");
		
		HttpClient http=new DefaultHttpClient(FileUtil.getParams());
		Collection<String> urls = new ArrayList<String>();
        Object[] objs=tn.evaluateXPath("//link[@href]");
        File cssDir=new File(targetUrl);
        if(!cssDir.exists()){
     	   cssDir.mkdirs();
        }
        for(Object object:objs){
        	TagNode cssNode=(TagNode) object;
			String href=cssNode.getAttributeByName("href");//获得Css的src属性值  
			String hostAbosutePath=processDownCssLinkHref(href,srcUrl,cssDir,http,charset,redownUrl);
		    cssNode.setAttribute("href",hostAbosutePath); 
        }
		return urls;
	}
	
	/**
	 * 处理并下载css资源并修复css中引用的图片资源;
	 * @param href
	 * @param srcUrl
	 * @param cssDir
	 * @param http
	 * @param charset
	 * @param redownUrl
	 * @return
	 */
	public static String processDownCssLinkHref(String href,String srcUrl,File cssDir,HttpClient http,String charset,String redownUrl){
			if (href == null || href.trim().length() == 0)
				return null;
			if (!href.startsWith("https://") && !href.startsWith("http://")){
				StringBuilder sb;
				try {
					sb = new StringBuilder("http://").append(new URL(srcUrl).getHost()).append("/");
					while(href.startsWith("/")||href.startsWith(".")){
						href = href.substring(1);
					}
					href = sb.append(href).toString();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			if (href == null)
				return null;
			if (href.startsWith("mailto:"))
				return null;
	        String cssName=FilenameUtils.getName(href);//css文件名;
		    if(cssName.contains(",")){
				  cssName=cssName.substring(cssName.lastIndexOf(",")+1);
		    }
	        String hostAbosutePath=cssDir+File.separator+cssName;
	        System.out.println("下载:"+href);
	        File cssFile=dowLoadFile(http,href,hostAbosutePath);
	        String content=FileUtil.readFile(cssFile.getAbsolutePath(),charset);
	        Map<String,String> imgUrls=FileUtil.downCssUrl(http, srcUrl,content,cssDir+File.separator);
	        for(String key:imgUrls.keySet()){
	   			String imgName=key.substring(key.lastIndexOf("/")+1);
	   			String absolutePath=cssDir+File.separator+imgName;
	   			String newUrl=absolutePath.replaceAll("\\\\","/");
	   			processDownImgSrc(http,key,newUrl,srcUrl,cssDir,redownUrl);
				content=content.replace(key, newUrl);
			}
	       FileUtil.writeCssUrl(imgUrls, content, cssFile, cssDir.getAbsolutePath(),charset);
	       return hostAbosutePath;
	}
	
	/**
	 * 下载网络Js并重置本地Js路径;
	 * @param tn
	 * @param srcUrl
	 * @param targetUrl
	 * @return
	 * @throws Exception
	 */
	public static Collection<String> downloadToResetJsLinkSrc(TagNode tn, String srcUrl,String targetUrl) throws Exception{
	  
	   System.out.println("========================================Js模块路径处理中===============================");
	   
	   HttpClient http=new DefaultHttpClient(FileUtil.getParams());
	   Collection<String> urls = new ArrayList<String>();
       Object[] objs=tn.evaluateXPath("//script");
       File jsDir=new File(targetUrl);
       if(!jsDir.exists()){
      	   jsDir.mkdirs();
       }
       for(Object object:objs){
       		TagNode jsNode=(TagNode) object;
			String src=jsNode.getAttributeByName("src");//获得Js的src属性值  
			if (src == null || src.trim().length() == 0)
				continue;
			if (!src.trim().startsWith("https://") && !src.trim().startsWith("http://")){
				StringBuilder sb = new StringBuilder("http://").append(new URL(srcUrl).getHost());
				while(src.startsWith(".")||src.startsWith("/")){
					src=src.substring(1);
				}
				sb.append("/");
				src = sb.append(src).toString();
			}
			if (src == null)
				continue;
			if (src.startsWith("mailto:"))
				continue;
		   String filterSrc=src;//带重置文件资源路径;
           String jsName=FilenameUtils.getName(src);//js文件名;
		   String hostAbosutePath=jsDir+File.separator+jsName;
		   if(filterSrc.indexOf("?")>=0){
        	   filterSrc=filterSrc.substring(0,filterSrc.indexOf("?"));
           }
		   String filterJsName=FilenameUtils.getName(filterSrc);//js去除?文件名;
		   if(filterJsName.contains(",")){
			      filterJsName=filterJsName.substring(filterJsName.lastIndexOf(",")+1);
		   }
		   if(filterSrc.endsWith(".js")){
			   System.out.println("下载:"+filterSrc);
			   dowLoadFile(http,src,jsDir+File.separator+filterJsName);
			   jsNode.setAttribute("src",hostAbosutePath); 
			   urls.add(hostAbosutePath);
		   }
       }
		return urls;
	}
	/**
	 * 下载文件;
	 * @param httpClient
	 * @param srcPath
	 * @param targetPath
	 * @return
	 */
	public static File dowLoadFile(HttpClient httpClient,String srcPath,String targetPath){
		while(targetPath.indexOf("?")>0){
			targetPath=targetPath.substring(0,targetPath.indexOf("?"));
		}
		File isExistFile=new File(targetPath);
		try {
		       if(!isExistFile.getParentFile().exists())
		        	 isExistFile.getParentFile().mkdirs();
	     	   byte[] im=FileUtil.getByteFile(httpClient,srcPath);
			   IOUtils.write(im, new FileOutputStream(targetPath));
	        
	    }catch (IOException e) {
			e.printStackTrace();
		}
        return isExistFile;
	}
	
	public static String getHtml(String urlString) {  
	    try {  
	      StringBuffer html = new StringBuffer();  
	      URL url = new URL(urlString);  
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
	      InputStreamReader isr = new InputStreamReader(conn.getInputStream());  
	      BufferedReader br = new BufferedReader(isr);  
	      String temp;  
	      while ((temp = br.readLine()) != null) {  
	        html.append(temp).append("\n");  
	      }  
	      br.close();  
	      isr.close();  
	      return html.toString();  
	    } catch (Exception e) {  
	      e.printStackTrace();  
	      return null;  
	    }  
	}
	public static void main(String[] args){
		/*String html = getHtml("http://www.groupon.my/all-deals/klang");
		List<String> rs = CommonUtil.findByRegex(html, "(?<=(\"dealPermaLink\":\")).[^\"]*");
		System.out.println(rs);*/
		/*Pattern pt=Pattern.compile("(src=|background=).*?.(gif|jpg|png|jpeg|bmp)|\\([']*[^\\)]*.(gif|jpg|png|jpeg|bmp)?[']*\\)");*/
		Pattern pt=Pattern.compile("[^\\s=\"]+\\.(gif|png|jpg|jpeg|bmp|swf|css)(?=[\"']?)");
		String str="  <script language='javascript'>"
				+ "$(function(){$('#silder').imgSilder({s_width:'341', //容器宽度 s_height:227, "
				+ "//容器高度is_showTit:true, // 是否显示图片标题 false :不显示，true :显示s_times:3000,//设置滚动时间"
				+ "css_link:'../css/style.css'});});"
				+ " var img=document.getElementById('Image0'+i);"
				+ " if (img!=null){document.top_imgs[i]=img;"
				+ "document.top_imgs_src[i]='/images/menu-0'+i+'.gif';}}"
				+ "</script>";
		Matcher mc=pt.matcher(str);
		while(mc.find()){
			System.out.println(mc.group());
		}
	}
}
