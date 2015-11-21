package org.eweb4j.spiderman.xml.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 强大的XML标签过滤【通过正则】
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-5 下午09:44:24
 */
public class Tags {
	
	public static void main(String[] args) {
		String html = "<p><strong>What You Get</strong></p>For $38 per pax, you get a 5D4N Beijing Guided Tour with International 5-Star Hotel Stays, Meals and 2 way Airport Transfer (worth $888).	"+
	
"<a href=\"https://static.groupon.sg/97/77/1357554037797.jpg\" target=\"_blank\">View tour itinerary</a>.	"+
"<p><strong>Deal Attractions</strong></p><p>Jump headfirst into the middle of the Middle Kingdom for the full experience – be filled to the gills with offerings of traditional Chinese cuisine and a bounty of sightseeing opportunities unique to the East.	"+
	
"<img src=\"https://static.groupon.sg/60/36/1346664173660.jpg\" /></p>Enjoy a feast for the senses as well, with a tour around iconic attractions such as Tiananmen Square, The Forbidden City, Summer Place and the Great Wall of China. Tourists will trot through Tiananmen Square, a large city square at the center of Beijing named after the Tiananmen Gate that separates the Forbidden City with the rest of China."+ 	
	
"<img src=\"https://static.groupon.sg/15/67/1312803396715.jpg\" /> 	"+
	
"he Forbidden City is the historic Chinese imperial palace that housed China’s ancient dynasties: Juyongguan Great Wall, a series of stone fortifications that protected China from conquering nomadic tribes, and Olympic Village, an accommodation built for the premier athletes of the world during the 2008 Olympics."+	
	
"<img src=\"https://static.groupon.sg/62/88/1340596698862.jpg\" />	"+
"<p>Indulge in a spot of retail therapy at Donghuamen night market and large departmental stores if conditions permit, and rest travel-weary feet in comfort at the 5-Star Xinyuan Hotel, with a coach and guide to keep from straying off the Great Wall into unfamiliar territory.</p><p><img src=\"https://static.groupon.sg/35/87/1340596698735.jpg\" />"+	
	
"<img src=\"https://static.groupon.sg/30/91/1340596699130.jpg\" /></p>Before and after the guided tours, Groupon holders may walk at their own pace to visit the splendor of Beijing’s streets or ride vintage sidecars to cover more land and see more sightseeables. Eating authentic Chinese cuisine and more shopping adventures may also be done along the way."+	
"<h2><font color=\"#0981be\"><img src=\"https://static.groupon.sg/48/23/1340603452348.jpg\" /></font></h2><img src=\"https://static.groupon.sg/55/32/1357876073255.jpg\" /> <map name=\"grouponsg_map\"> <area shape=\"rect\" coords=\" 355, 101, 462, 148\" href=\"http://www.groupon.sg/deals/singapore-exclusive?utm_source=banner&utm_medium=cp_sgx_side&utm_campaign=fbanners\" target=\"_blank\" /> <area shape=\"rect\" coords=\" 137, 92, 244, 140\" href=\"http://www.groupon.sg/deals/shopping?utm_source=banner&utm_medium=cp_goods&utm_campaign=fbanners\" target=\"_blank\" /> <area shape=\"rect\" coords=\" 211, 10, 319, 59\" href=\"http://www.groupon.sg/deals/singapore?utm_source=banner&utm_medium=cp_singapore&utm_campaign=fbanners\" target=\"_blank\" /> <area shape=\"rect\" coords=\" 23, 73, 129, 121\" href=\"http://www.groupon.sg/deals/deals-near-me?utm_source=banner&utm_medium=cp_nearme&utm_campaign=fbanners\" target=\"_blank\" /> <area shape=\"rect\" coords=\" 3, 4, 110, 53\" href=\"http://www.groupon.sg/deals/travel-deals?utm_source=banner&utm_medium=cp_travel&utm_campaign=fbanners\" target=\"_blank\" /> <area shape=\"default\" /> </map>";
//		String rs = Tags.me().xml(html).kp("h1").kp("h2").kp("h3").kp("h4").kp("h5").kp("h6").kp("em").kp("i").kp("u").kp("er").kp("table").kp("th").kp("tr").kp("td").kp("p").kp("ul").kp("ol").kp("li").kp("a").kp("b").kp("strong").empty().ok();
		html = "<div>Tick off more than one city from your must-visit list of 2013 with today's Groupon to either Eastern or Western Europe. Choose between 2 options:"	
			+"<p><strong>What You Get</strong></p><ul><li>For $1,998 per pax, you get a 10D8N Eastern or Western Europe Tour via China Eastern Airlines (worth $2,888).</li><li>For $2,398 per pax, you get a 10D8N Eastern or Western Europe Tour via Singapore Airlines (worth $3,250).</li></ul><div>	"+
			"View Eastern Europe tour itinerary <a href=\"https://static.groupon.sg/60/45/1357723144560.jpg\" target=\"_blank\">page 1</a>, <a href=\"https://static.groupon.sg/99/34/1357724203499.jpg\" target=\"_blank\">page 2</a> & <a href=\"https://static.groupon.sg/89/47/1357723144789.jpg\" target=\"_blank\">page 3</a>.	"+
			"View Western Europe tour itinerary <a href=\"https://static.groupon.sg/75/68/1357723206875.jpg\" target=\"_blank\">page 1</a>, <a href=\"https://static.groupon.sg/95/69/1357723206995.jpg\" target=\"_blank\">page 2</a> & <a href=\"https://static.groupon.sg/12/71/1357723207112.jpg\" target=\"_blank\">page 3</a>.</div><p><strong>Deal Attractions</strong></p>Must-see on Europe's maps, faraway cities the likes of Prague, Frankfurt and Budapest are amongst the world's emerging capitals of cosmopolitan culture, enticing history and all-encompassing visual wonderment. Architectural splendours are commonplace on the streets of these fascinating cities, and each exhibits its own rendering of form and beauty with buildings dating back to the middle ages. "+	
				
			"<img src=\"https://static.groupon.sg/48/21/1357721932148.jpg\" />	"+
			"<em>Prague	"+
				
			"<img src=\"https://static.groupon.sg/70/21/1357721932170.jpg\" />	"+
			"Vienna Opera House	"+
				
			"</em>In Prague, both the new and old parts of the city present a medley of urban wonders, with medieval lanes and Gothic and Renaissance buildings artfully spread throughout. Vienna, revered for its elegance, buzzes with alfresco diners and expansive greens once walked on by notable former residents such as Sigmund Freud. With its eclectic culture, famed Turkish baths and exciting nightlife, Budapest is centre of creativity and buzz that resides within impressive neoclassical and art nouveau walls. 	"+
				
			"<em><img src=\"https://static.groupon.sg/90/21/1357721932190.jpg\" />	"+
			"Budapest</em>	"+
				
			"Between transfers from one hotel to the next, Grouponers will be guided on an edifying journey through a comprehensive sightseeing tour of the old town and the new. Those who venture east will find themselves meandering through the land's notable landmarks, including the Vienna Concert Hall, The Palace of Buda Fisherman’s Fort in Budapest and the well known ski city, Innsbruck, where the Winter Olympic Games was held in 1964 and 1976.	"+
				
			"<img src=\"https://static.groupon.sg/38/46/1357722274638.jpg\" />	"+
			"<em>Milan	"+
				
			"<img src=\"https://static.groupon.sg/62/46/1357722274662.jpg\" />	"+
			"Lucerne	"+
				
			"<img src=\"https://static.groupon.sg/99/46/1357722274699.jpg\" />	"+
			"Amsterdam</em>	"+
				
			"The tour of the west will similarly take guests on a voyage through streets resplendent in eclectic neoclassical and baroque influences, as well as beautiful parks and museums. These would include pit stops at fashion capital Milan, scenic Lucerne in Switzerland, and more familiar places like Paris and Amsterdam."+	
				
			"<img src=\"https://static.groupon.sg/29/86/1354788668629.jpg\" />	"+
				
			"With an knowledgeable tour guide shining the leading light for the full duration of the tour, and excursions, tours, inter-city transfers and breakfast each morning, Groupon galivanters will find most bases aptly covered in this truly once-in-a-lifetime Euro trip	"+
			"<h2> </h2><img src=\"https://static.groupon.sg/55/32/1357876073255.jpg\" /> </div>" ;
		System.out.println(Tags.me().xml(html).kp("h1").kp("h2").kp("h3").kp("h4").kp("h5").kp("h6").kp("table").kp("th").kp("tr").kp("td").kp("img").kp("p").kp("a").kp("ul").kp("ol").kp("li").kp("td").kp("em").kp("i").kp("u").kp("er").kp("b").kp("strong").ok());
	}
	
	public static void mains(String[] args){
		//XML文本
		String xml = "<div>This is div.</div><p>This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>";
		//删除所有标签
		String rs = Tags.me().xml(xml).rm().ok();
		System.out.println("This is div.This is p.This is li.This is link.".equals(rs));
		
		//删除a、div标签，并且清空它们标签内的所有内容
		String rs2 = Tags.me().xml(xml).rm("div", "a").empty().ok();
		System.out.println("<p>This is p.<ul><li>This is li.</li></ul></p>".equals(rs2));
		
		//保留p、a标签，其他都删除
		String kpRs = Tags.me().xml(xml).kp("p", "a").ok();
		System.out.println("This is div.<p>This is p.This is li.<a href='http://www.baidu.com'>This is link.</a></p>".equals(kpRs));
		
		//删除p、a标签，其他保留
		String rmRs = Tags.me().xml(xml).rm("p", "a").ok();
		System.out.println("<div>This is div.</div>This is p.<ul><li>This is li.This is link.</li></ul>".equals(rmRs));
		
		//Tags和Attrs两个类是可以同时使用的，切换的时候，上一个的执行结果作为下一个的参数继续处理
		//删除div、ul、li标签然后删除a标签的href属性
		String sRs = Tags.me().xml(xml).rm("div", "ul","li").Attrs().tag("a").rm("href").ok();
		System.out.println("This is div.<p>This is p.This is li.<a>This is link.</a></p>".equals(sRs));
		
		//删除所有标签的href属性，然后保留div、a标签，其他标签都删除
		String sRs2 = Attrs.me().xml(xml).rm("href").Tags().kp("div", "a").ok();
		System.out.println("<div>This is div.</div>This is p.This is li.<a>This is link.</a>".equals(sRs2));

		
		String html = "<dd class=\"frinfo line_blue\">2013-01-07 08:40:03      <a style=\"font-weight:bold;padding:5px 0px 5px 20px;background:url('http://www.2cto.com/statics/images/icon/user_comment.png') left center no-repeat\" href=\"#comment_iframe\">我来说两句 </a>    来源：雨简 的BLOG    </dd>";
		
		System.out.println(Tags.me().xml(html).kp("p").empty().ok());
		
		List<String> tag = Tags.findByRegex(html, Tags.xmlTagsRegex("a"));
		String regex = tag.get(0) + ".*" + tag.get(1);
		System.out.println(regex);
		html = html.replaceAll(regex, "");
		System.out.println(html);
		System.out.println(Attrs.me().xml(html).tag("a").rm().Tags().rm("a").empty().exe().rm("dd").ok());
		System.out.println(Attrs.regex("a", "style"));
	}
	
	private String xml = null;//需要操作的xml文本
	private Boolean empty = false;//是否清空标签内的内容
	private Collection<String> kps = new HashSet<String>();//保留的标签缓存
	private Collection<String> rms = new HashSet<String>();//删除的标签缓存
	
	/**
	 * 构造一个Tags实例对象
	 * @date 2013-1-7 下午03:53:27
	 * @return
	 */
	public static Tags me(){
		return new Tags();
	}
	
	/**
	 * 设置需要操作的XML文本
	 * @date 2013-1-7 下午03:53:14
	 * @param xml
	 * @return
	 */
	public Tags xml(String xml){
		this.xml = xml;
		return this;
	}
	
	/**
	 * 切换到Attrs，切换之前会执行清除标签操作
	 * @date 2013-1-7 下午03:52:43
	 * @return
	 */
	public Attrs Attrs(){
		exe();
		return Attrs.me().xml(xml);
	}
	
	/**
	 * 清空当前指定标签内的所有内容
	 * @date 2013-1-7 下午03:52:09
	 * @return
	 */
	public Tags empty(){
		this.empty = true;
		return this;
	}
	
	/**
	 * 删除所有标签【保留标签里的内容】
	 * @date 2013-1-7 下午03:51:50
	 * @return
	 */
	public Tags rm(){
		xml = cleanXmlTags(xml, false);
		return this;
	}
	
	/**
	 * 
	 * 删除指定标签
	 * @date 2013-1-7 下午03:51:31
	 * @param tag
	 * @return
	 */
	public Tags rm(String tag){
		this.rms.add(tag);
		return this;
	}
	
	/**
	 * 删除标签
	 * @date 2013-1-7 下午03:51:16
	 * @param tag 不给定则删除所有
	 * @return
	 */
	public Tags rm(String... tag){
		this.rms.addAll(Arrays.asList(tag));
		return this;
	}
	
	/**
	 * 保留给定标签,其他删除
	 * @date 2013-1-7 下午03:50:52
	 * @param tag
	 * @return
	 */
	public Tags kp(String tag){
		this.kps.add(tag);
		return this;
	}
	
	/**
	 * 保留给定标签,其他删除
	 * @date 2013-1-7 下午03:50:41
	 * @param tag
	 * @return
	 */
	public Tags kp(String... tag){
		this.kps.addAll(Arrays.asList(tag));
		return this;
	}
	
	/**
	 * 执行标签的清除
	 * @date 2013-1-7 下午03:50:16
	 * @return
	 */
	public Tags exe(){
		if (!this.rms.isEmpty()){
			xml = cleanXmlTags(xml, this.empty, rms.toArray(new String[]{}));
			this.rms.clear();
			this.empty = false;
		} if (!this.kps.isEmpty()){
			xml = cleanOtherXmlTags(xml, this.empty, kps.toArray(new String[]{}));
			this.kps.clear();
		}
		
		return this;
	}
	
	/**
	 * 返回处理后的字符串
	 * @date 2013-1-7 下午03:49:58
	 * @return
	 */
	public String ok(){
		exe();
		return xml;
	}
	
	/**
	 * 删除标签
	 * @date 2013-1-5 下午05:24:06
	 * @param html
	 * @isRMCnt 是否删除标签内的所有内容
	 * @param keepTags 保留的标签，如果不给定则删除所有标签
	 * @return
	 */
	public static String cleanOtherXmlTags(String html, boolean isRMCnt, String... keepTags) {
		if (isRMCnt){
			for (String keepTag : keepTags){
				String x = inverseXmlTagsRegex(keepTag);
				List<String> tag = findByRegex(html, x);
				if (tag == null || tag.isEmpty() || tag.size() % 2 != 0)
					continue;
				int size = tag.size() / 2;
				List<List<String>> tags = new ArrayList<List<String>>(size);
				
				List<String> _pair = new ArrayList<String>(2);
				for (int i = 1; i <= tag.size(); i++){
					_pair.add(tag.get(i-1));
					if (i % 2 == 0){
						tags.add(new ArrayList<String>(_pair));
						_pair.clear();
					}
				}
				
				for (List<String> _tag : tags) {
					String regex = resolveRegex(_tag.get(0)) + ".*" + resolveRegex(_tag.get(1));
					html = html.replaceAll(regex, "");
				}
			}
			return html;
		}
		return html.replaceAll(inverseXmlTagsRegex(keepTags), "");
	}
	
	/**
	 * 删除标签
	 * @date 2013-1-5 下午05:35:27
	 * @param html
	 * @param isRMCnt 是否删除标签内的所有内容 <p>This is p.<a href="#">This is a.</a></p>如果干掉a标签，就变成=><p>This is p.</p>
	 * @param delTags 需要删除的Tag，如果不给定则删除所有标签
	 * @return
	 */
	public static String cleanXmlTags(String html, boolean isRMCnt, String... delTags) {
		if (isRMCnt){
			for (String delTag : delTags){
				List<String> tag = findByRegex(html, xmlTagsRegex(delTag));
				if (tag == null || tag.isEmpty() || tag.size() != 2)
					continue;
				String regex = resolveRegex(tag.get(0)) + ".*" + resolveRegex(tag.get(1));
				html = html.replaceAll(regex, "");
			}
			return html;
		}
		
		return html.replaceAll(xmlTagsRegex(delTags), "");
	}
	
	public static String resolveRegex(String regex){
		List<String> cc = Arrays.asList("\\", "^", "$", "*", "+", "?", "{", "}", "(", ")", ".", "[", "]", "|");
		for (String c : cc) {
			regex = regex.replace(c, "\\"+c);
		}
		return regex;
	}
	
	/**
	 * 匹配除了给定标签意外其他标签的正则表达式
	 * @date 2013-1-7 下午03:45:29
	 * @param keepTags 如果不给定则匹配所有标签
	 * @return
	 */
	public static String inverseXmlTagsRegex(String... keepTags) {
		if (keepTags == null || keepTags.length == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		String fmt = "\\b%s\\b";
		StringBuilder sb = new StringBuilder();
		for (String kt : keepTags){
			if (kt == null || kt.trim().length() == 0)
				continue;
			
			if (sb.length() > 0)
				sb.append("|");
			sb.append(String.format(fmt, kt));
		}
		if (sb.length() == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		
		String pattern = "<[!/]?\\b(?!("+sb.toString()+"))+\\b\\s*[^>]*>";
		
		return pattern;
	}
	
	/**
	 * 匹配给定标签的正则表达式
	 * @date 2013-1-7 下午03:47:11
	 * @param tags 如果不给定则匹配所有标签
	 * @return
	 */
	public static String xmlTagsRegex(String... tags) {
		if (tags == null || tags.length == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		String fmt = "\\b%s\\b";
		StringBuilder sb = new StringBuilder();
		for (String kt : tags){
			if (kt == null || kt.trim().length() == 0)
				continue;
			
			if (sb.length() > 0)
				sb.append("|");
			sb.append(String.format(fmt, kt));
		}
		if (sb.length() == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		
		String pattern = "<[!/]?("+sb.toString()+")\\s*[^>]*>";
		
		return pattern;
	}
	
	public static List<String> findByRegex(String input, String regex){
		List<String> result = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		while(m.find()){
			result.add(m.group());
		}
		
		if (result.isEmpty()) return null;
		
		return result;
	}
}
