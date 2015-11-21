/**
 * 
 */
package org.eweb4j.spiderman.plugin.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.util.EntityUtils;
import org.eweb4j.spiderman.fetcher.SpiderConfig;
/**
 * @author yangc
 *
 */
public class FileUtil {
	
	public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();     
    private static HttpParams params=null;
    private FileUtil(){}     
    static{     
        getAllFileType(); //初始化文件类型信息     
        initConfig();
    }     
         
    private static void initConfig(){
    	params = new BasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT,10000);
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,10000);
    }
    public static HttpParams getParams(){
    	return params;
    }
    /**   
     * Discription:[getAllFileType,常见文件头信息] 
     */     
    private static void getAllFileType()     
    {     
        FILE_TYPE_MAP.put("ffd8ffe000104a464946", "jpg"); //JPEG (jpg)     
        FILE_TYPE_MAP.put("89504e470d0a1a0a0000", "png"); //PNG (png)     
        FILE_TYPE_MAP.put("47494638396126026f01", "gif"); //GIF (gif)     
        FILE_TYPE_MAP.put("49492a00227105008037", "tif"); //TIFF (tif)     
        FILE_TYPE_MAP.put("424d228c010000000000", "bmp"); //16色位图(bmp)     
        FILE_TYPE_MAP.put("424d8240090000000000", "bmp"); //24位位图(bmp)     
        FILE_TYPE_MAP.put("424d8e1b030000000000", "bmp"); //256色位图(bmp)     
        FILE_TYPE_MAP.put("41433130313500000000", "dwg"); //CAD (dwg)     
        FILE_TYPE_MAP.put("3c21444f435459504520", "html"); //HTML (html)
        FILE_TYPE_MAP.put("3c21646f637479706520", "htm"); //HTM (htm)
        FILE_TYPE_MAP.put("48544d4c207b0d0a0942", "css"); //css
        FILE_TYPE_MAP.put("696b2e71623d696b2e71", "js"); //js
        FILE_TYPE_MAP.put("7b5c727466315c616e73", "rtf"); //Rich Text Format (rtf)     
        FILE_TYPE_MAP.put("38425053000100000000", "psd"); //Photoshop (psd)     
        FILE_TYPE_MAP.put("46726f6d3a203d3f6762", "eml"); //Email [Outlook Express 6] (eml)       
        FILE_TYPE_MAP.put("d0cf11e0a1b11ae10000", "doc"); //MS Excel 注意：word、msi 和 excel的文件头一样     
        FILE_TYPE_MAP.put("d0cf11e0a1b11ae10000", "vsd"); //Visio 绘图     
        FILE_TYPE_MAP.put("5374616E64617264204A", "mdb"); //MS Access (mdb)      
        FILE_TYPE_MAP.put("252150532D41646F6265", "ps");     
        FILE_TYPE_MAP.put("255044462d312e350d0a", "pdf"); //Adobe Acrobat (pdf)   
        FILE_TYPE_MAP.put("2e524d46000000120001", "rmvb"); //rmvb/rm相同  
        FILE_TYPE_MAP.put("464c5601050000000900", "flv"); //flv与f4v相同  
        FILE_TYPE_MAP.put("43575308bdb20000789c", "swf");//swf文件;
        FILE_TYPE_MAP.put("00000020667479706d70", "mp4"); 
        FILE_TYPE_MAP.put("49443303000000002176", "mp3"); 
        FILE_TYPE_MAP.put("000001ba210001000180", "mpg"); //     
        FILE_TYPE_MAP.put("3026b2758e66cf11a6d9", "wmv"); //wmv与asf相同    
        FILE_TYPE_MAP.put("52494646e27807005741", "wav"); //Wave (wav)  
        FILE_TYPE_MAP.put("52494646d07d60074156", "avi");  
        FILE_TYPE_MAP.put("4d546864000000060001", "mid"); //MIDI (mid)   
        FILE_TYPE_MAP.put("504b0304140000000800", "zip");    
        FILE_TYPE_MAP.put("526172211a0700cf9073", "rar");   
        FILE_TYPE_MAP.put("235468697320636f6e66", "ini");   
        FILE_TYPE_MAP.put("504b03040a0000000000", "jar"); 
        FILE_TYPE_MAP.put("4d5a9000030000000400", "exe");//可执行文件
        FILE_TYPE_MAP.put("3c25402070616765206c", "jsp");//jsp文件
        FILE_TYPE_MAP.put("4d616e69666573742d56", "mf");//MF文件
        FILE_TYPE_MAP.put("3c3f786d6c2076657273", "xml");//xml文件
        FILE_TYPE_MAP.put("494e5345525420494e54", "sql");//xml文件
        FILE_TYPE_MAP.put("7061636b616765207765", "java");//java文件
        FILE_TYPE_MAP.put("406563686f206f66660d", "bat");//bat文件
        FILE_TYPE_MAP.put("1f8b0800000000000000", "gz");//gz文件
        FILE_TYPE_MAP.put("6c6f67346a2e726f6f74", "properties");//bat文件
        FILE_TYPE_MAP.put("cafebabe0000002e0041", "class");//bat文件
        FILE_TYPE_MAP.put("49545346030000006000", "chm");//bat文件
        FILE_TYPE_MAP.put("04000000010000001300", "mxp");//bat文件
        FILE_TYPE_MAP.put("504b0304140006000800", "docx");//docx文件
        FILE_TYPE_MAP.put("d0cf11e0a1b11ae10000", "wps");//WPS文字wps、表格et、演示dps都是一样的
        FILE_TYPE_MAP.put("6431303a637265617465", "torrent");
        
          
        FILE_TYPE_MAP.put("6D6F6F76", "mov"); //Quicktime (mov)  
        FILE_TYPE_MAP.put("FF575043", "wpd"); //WordPerfect (wpd)   
        FILE_TYPE_MAP.put("CFAD12FEC5FD746F", "dbx"); //Outlook Express (dbx)     
        FILE_TYPE_MAP.put("2142444E", "pst"); //Outlook (pst)      
        FILE_TYPE_MAP.put("AC9EBD8F", "qdf"); //Quicken (qdf)     
        FILE_TYPE_MAP.put("E3828596", "pwl"); //Windows Password (pwl)         
        FILE_TYPE_MAP.put("2E7261FD", "ram"); //Real Audio (ram)     
    }                       
    
    /**
     * 得到上传文件的文件头
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    
    /**
     * 根据制定文件的文件头判断其文件类型
     * @param filePaht
     * @return
     */
    public static String getFileType(String filePaht){
        String res = null;
        try {
            FileInputStream is = new FileInputStream(filePaht);
            byte[] b = new byte[10];
            is.read(b, 0, b.length);
            String fileCode = bytesToHexString(b);    
            //这种方法在字典的头代码不够位数的时候可以用但是速度相对慢一点
            Iterator<String> keyIter = FILE_TYPE_MAP.keySet().iterator();
            while(keyIter.hasNext()){
                String key = keyIter.next();
                if(key.toLowerCase().startsWith(fileCode.toLowerCase().substring(0,4)) || fileCode.toLowerCase().startsWith(key.toLowerCase())){
                    res = FILE_TYPE_MAP.get(key);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static String getFile(HttpClient http,String url){  
	    try{  
	        HttpGet get=new HttpGet(url);  
	        HttpResponse hr=http.execute(get);  
	        HttpEntity he=hr.getEntity();  
	        if(he!=null){  
	            String charset=EntityUtils.getContentCharSet(he);  
	            InputStream is=he.getContent();  
	            return IOUtils.toString(is,charset);  
	    }  
	    }catch(Exception e){  
	        e.printStackTrace();  
	    }  
	    return null;  
	      
	}  
	
	public static byte[] getByteFile(HttpClient http,String url){  
	    try{
	    	URL hostUrl=new URL(url);
	    	HttpGet hg=new HttpGet(url);  
		    HttpResponse hr=http.execute(hg);  
		    HttpEntity he=hr.getEntity();  
		    if(he!=null){  
		        InputStream is=he.getContent(); 
		        return IOUtils.toByteArray(is);  
		    }  
		}  
	    catch(Exception e){  
	    	System.out.println("未知资源主机:"+url);
	    }  
	    return null;  
	}  
	/**
	*读取文件的方法   返回文件的字符串
	*
	*/
	public static String readFile(String fileName,String charset) {
		File file = new File(fileName);
		BufferedReader reader = null;
		StringBuffer sb=new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),charset));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				sb.append(tempString+"\r\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					
				}
			}
		}
		return sb.toString();
	}
	
	
	/**
	*
	*根据上面的  返回的字符串
	*获取文件中 形如 url(../images/header_bei.gif) 的图片url列表
	*
	*/
	public static List<String> getUrlFirst(String str){
		List<String> urlList=new ArrayList<String>();
		Pattern pt=Pattern.compile("[^\\s='\"(]+\\.(gif|png|jpg|jpeg|bmp)(?=[\"')]?)");
		Matcher mc=pt.matcher(str);
		while(mc.find()){
			String imgUrl=mc.group();
			urlList.add(imgUrl);
		}
		return urlList;
	}
	/**
	*
	*根据图片的域名 和上面的url列表  得到 图片完整的url
	*/
	public static Map<String,String> getStrUrl(String hostUrl,List<String> urlList){
		Map<String,String> setFile=new HashMap<String, String>();
		for(int i=0;i<urlList.size();i++){
			int n=urlList.get(i).indexOf("/")+1;
			String s=urlList.get(i).substring(n);
			if(!hostUrl.endsWith("/")){
				hostUrl+="/";
			}
			String fileName=urlList.get(i);
			setFile.put(fileName,hostUrl+s);
		}
		return setFile;
	}
	
	public static Map<String,String> downCssUrl(HttpClient hc,String srcPath,String content,String targetPath){
		List<String> urls=getUrlFirst(content);
		Map<String,String> setUrls=getStrUrl(srcPath, urls);
		return setUrls;
	}
	/**
	 * 设置并重新写入css文件;
	 * @param urls
	 * @param content
	 * @param file
	 * @param targetPath
	 * @return
	 */
	public static Map<String,String> writeCssUrl(Map<String,String> urls,String content,File file,String targetPath,String charset){
		FileOutputStream fos=null;
		OutputStreamWriter osw=null;
		BufferedWriter writer=null;
		
		try {
			 fos=new FileOutputStream(file);
			 osw=new OutputStreamWriter(fos, charset);
			 writer=new BufferedWriter(osw);   
		     writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			 try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}
	
	/**
	*
	*根据图片的完整url 
	*获得图片名     用于保存到本地取名
	*/
	public static Set<String> getPicName(Set<String> urlSet){
		
		Set<String> picNameSet=new HashSet<String>();
		
		for(Iterator<String> it=urlSet.iterator();it.hasNext();){
			String s[]=it.next().split("/");
			if(s.length>1){
				picNameSet.add(s[s.length-1]);
			}
		}
		
		return picNameSet;
		
	}
    public static void main(String[] args) throws Exception {
        SpiderConfig config=new SpiderConfig();
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

		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		connectionManager.setMaxTotal(config.getMaxTotalConnections());
		connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());
		HttpClient httpClient = new DefaultHttpClient(connectionManager, params);
		
		httpClient.getParams().setIntParameter("http.socket.timeout", 60000);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, config.isFollowRedirects());
		byte[] fileArry=getByteFile(httpClient, "http://www.jd.com//misc.360buyimg.com");
		System.out.println(fileArry);
      /*  String type = getFileType("D:/spidermanWorkSpace/spiderman/spiderman-sample/target/test-classes/Data/浙江省公安厅/page/images/szf.gif");
        System.out.println("eee.WMV : "+type);
        System.out.println(); */
        
       /* type = getFileType("C:/test/350996.wav");
        System.out.println("350996.wav : "+type);
        System.out.println(); */
                
    }
}
