Spiderman - Java开源Web数据抽取工具
========================================
    Spiderman 是一个Java开源Web数据抽取工具。它能够收集指定的Web页面并从这些页面中提取有用的数据。
    Spiderman主要是运用了像XPath,正则表达式等这些技术来实数据抽取。
    
它包含了两部分（二者缺一不可）：
-----------------------------
	* spiderman-core 内核
	* spiderman-plugin 插件

主要特点
----------------------
    * 微内核+插件式架构、灵活、可扩展性强
    * 无需编写程序代码即可完成数据抽取
    * 多线程保证性能

怎么使用？
----------
* 首先，确定好你的目标网站以及目标网页（即某一类你想要获取数据的网页，例如网易新闻的新闻页面）
* 然后，打开目标页面，分析页面的HTML结构，得到你想要数据的XPath，具体XPath怎么获取请看下文。
* 最后，在一个xml配置文件里填写好参数，运行Spiderman吧！

近期更新
----
1. &lt;parser 的表达式支持发起HTTP请求获取内容了：
    &lt;parser exp="$Fetcher.get('http://www.baidu.com')"

2. &lt;target节点添加 &lt;before节点配置，该配置与&lt;model一样可以用来解析网页内容，主要的区别是该节点会在&lt;model节点解析之前进行工作，其解析后的结果将会作为model的上下文$before.xxx来使用

3. 重构下载器，支持多种下载器实现，允许在xml里面配置自己实现的下载器实现类，官方默认提供了三种，分别是默认的基于HttpClient的下载器、基于WebUnit的下载器、基于Selenium WebDriver的实现
    &lt;site downloader="org.eweb4j.spiderman.plugin.util.WebDriverDownloader"
    或者
    &lt;site downloader="xxx.YourDownloader">

4. 与第三点一样，重构了模型解析器，使得现在支持多种不同的实现类，且允许开发者在xml上指定自己实现的解析器，目前官方提供了两种解析器，分别是DefaultModelParser，WebDriverModelParser 
     &lt;before parser="xxx.xxx.xxx.YourModelParser"
     或者
     &lt;model parser="xxx.YourModelParser"
5. 其他一些零碎的更新、BUG修复等。

XPath获取技巧？
--------------
* 首先，下载xpathonclick插件，[猛击这里](https://chrome.google.com/webstore/search/xpathonclick)
* 安装完毕之后，打开Chrome浏览器，可以看到右上角有个“X Path” 图标。
* 在浏览器打开你的目标网页，然后点击右上角的那个图片，然后点击网标上你想要获取XPath的地方，例如某个标题
* 这时候按住F12打开JS控制台，拖到底部，可以看到一串XPath内容
* 记住，这个内容不是绝对OK的，你可能还需要做些修改，因此，你最好还是去学习下XPath语法
* 学习XPath语法的地方：[猛击这里](http://www.w3school.com.cn/xpath/index.asp)

Spiderman Sample | 案例
=======================

* 首先保证你的机器至少可以运行Java程序、也可以执行Maven命令
* 案例程序[spiderman-sample] mvn test
* Spiderman程序将会运行N秒钟，然后到保存抓取数据的文件夹查看对应网站的数据
* 这里有篇文章介绍示例：[http://my.oschina.net/laiweiwei/blog/100866]

这是使用Spiderman的代码：
    
    public class TestSpider {
        
    	private final Object mutex = new Object();
    	
    	@Test
    	public void test() throws Exception {
    		String err = EWeb4JConfig.start();
			if (err != null)
				throw new Exception(err);
			
			SpiderListener listener = new SpiderListenerAdaptor(){
				public void afterScheduleCancel(){
					//调度结束回调
				}
				/**
				 * 每次调度执行前回调此方法
				 * @date 2013-4-1 下午03:33:11
				 * @param theLastTimeScheduledAt 上一次调度时间
				 */
				public void beforeEveryScheduleExecute(Date theLastTimeScheduledAt){
					System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [LAST_SCHEDULE_AT] ~ ");
					System.err.println("at -> " + CommonUtil.formatTime(theLastTimeScheduledAt));
				}
				public void onFetch(Thread thread, Task task, FetchResult result) {
					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [FETCH] ~ ");
					System.out.println("fetch result ->" + result + " from -> " + task.sourceUrl);
				}
				public void onNewUrls(Thread thread, Task task, Collection<String> newUrls) {
					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DIG] ~ ");
					System.out.println(newUrls);
				}
				public void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks) {
	//				for (Task t : validTasks){
	//					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [DUPREMOVE] ~ ");
	//					System.out.println(t.url+" from->"+t.sourceUrl);
	//				}
				}
				public void onTaskSort(Thread currentThread, Task task, Collection<Task> afterSortTasks) {
	//				for (Task t : afterSortTasks){
	//					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [SORT] ~ ");
	//					System.out.println(t.url+" from->"+t.sourceUrl);
	//				}
				}
				public void onNewTasks(Thread thread, Task task, Collection<Task> newTasks) {
	//				for (Task t : newTasks){
	//					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [NEWTASK] ~ ");
	//					System.out.println(t.sort + ",,,," + t.url+" from->"+t.sourceUrl);
	//				}
				}
				public void onTargetPage(Thread thread, Task task, Page page) {
	//				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [TARGET] ~ ");
	//				System.out.println(page.getUrl());
				}
				public void onInfo(Thread thread, Task task, String info) {
					System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
					System.out.println(info);
				}
				
				public void onError(Thread thread, Task task, String err, Throwable e) {
					System.err.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [ERROR] ~ ");
					e.printStackTrace();
				}
				
				public void onParse(Thread thread, Task task, List<Map<String, Object>> models) {
					final String projectRoot = FileUtil.getTopClassPath(TestSpider.class);
					final File dir = new File(projectRoot+"/Data/"+task.site.getName()+"/"+task.target.getName());
					try {
						if (!dir.exists())
							dir.mkdirs();
						
						for (int i = 0; i < models.size(); i++) {
						    Map<String, Object> map = models.get(i);
							String fileName = dir + "/count_" + task.site.counter.getCount() + i;
							StringBuilder sb = new StringBuilder();
							for (Iterator<Entry<String,Object>> it = map.entrySet().iterator(); it.hasNext();){
								Entry<String,Object> e = it.next();
								boolean isBlank = false;
								
								if (e.getValue() == null)
									isBlank = true;
								else if (e.getValue() instanceof String && ((String)e.getValue()).trim().length() == 0)
									isBlank = true;
								else if (e.getValue() instanceof List && ((ArrayList<?>)e.getValue()).isEmpty())
									isBlank = true;
								else if (e.getValue() instanceof List && !((ArrayList<?>)e.getValue()).isEmpty()) {
									if (((ArrayList<?>)e.getValue()).size() == 1 && String.valueOf(((ArrayList<?>)e.getValue()).get(0)).trim().length() == 0)
									isBlank = true;
								}
									
								if (isBlank){
									if (sb.length() > 0)
										sb.append("_");
									sb.append(e.getKey());
								}
							}
							String content = CommonUtil.toJson(map);
							if (sb.length() > 0)
								fileName = fileName + "_no_"+sb.toString()+"_";
							
							File file = new File(fileName+".json");
							FileUtil.writeFile(file, content);
							System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
							System.out.println(fileName + " create finished...");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		
			//启动爬虫
			Spiderman.me()
				.init(listener)//初始化
				.startup()//启动
				.keepStrict("2h");//存活时间，过了存活时间后马上关闭
			
			//启动爬虫 + 调度定时重启
			//Spiderman.me()
				//.listen(listener)//设置监听器
				//.schedule("10s")//调度，爬虫运行10s
				//.delay("2s")//每隔 10 + 2 秒后重启爬虫
				//.times(3)//调度 3 次
				//.startup()//启动
				//.blocking();//阻塞直到所有调度完成
    	}
    }


下面详细看看这个sample的配置文件：

首先有一个初始化配置文件spiderman.properties，它就放在#{ClassPath}目录下
 
    #网站配置文件放置目录
    website.xml.folder=#{ClassPath}/WebSites
    #网站已访问url数据库存储目录
    website.visited.folder=#{ClassPath}/dbEnv
    #http抓取失败重试次数
    http.fetch.retry=3
    #http连接超时，支持单位 s秒 m分 h时 d天，不写单位则表示s秒
    http.fetch.timeout=5s

然后在#{ClassPath}/WebSites目录下有一份oschina.xml

    <?xml version="1.0" encoding="UTF-8"?>
	<!--
	  | Spiderman Java开源垂直网络爬虫 
	  | 项目主页: https://gitcafe.com/laiweiwei/Spiderman
	  | author: l.weiwei@163.com
	  | blog: http://laiweiweihi.iteye.com,http://my.oschina.net/laiweiwei
	  | qq: 493781187
	  | email: l.weiwei@163.com
	  | create: 2013-01-08 16:12
	  | update: 2013-04-10 18:06
	-->
	<beans>
		<!--
		  | name:名称
		  | url:种子链接
		  | skipStatusCode:设置哪些状态码需要忽略，多个用逗号隔开
		  | userAgent:设置爬虫标识
		  | includeHttps:0|1是否抓取https页
		  | isDupRemovalStrict:0|1是否严格去掉重复的TargetUrl，即已访问过一次的TargetUrl不会再被访问，若否，就算是重复的TargetUrl，只要它的来源URL不同，都会被访问
		  | isFollowRedirects:0|1是否递归跟随30X返回的location继续抓取
		  | reqDelay:{n}s|{n}m|{n}h|n每次请求之前延缓时间
		  | enable:0|1是否开启本网站的抓取
		  | charset:网站字符集
		  | schedule:调度时间，每隔多长时间重新从种子链接抓取
		  | thread:分配给本网站爬虫的线程数
		  | waitQueue:当任务队列空的时候爬虫等待多长时间再索取任务
		  | timeout:HTTP请求超时
		-->
		<site name="oschina" includeHttps="1" url="http://www.oschina.net/question?catalog=1&amp;show=&amp;p=1" reqDelay="1s" enable="0" charset="utf-8" schedule="1h" thread="2" waitQueue="10s">
			<!--
			  | 配置多个种子链接
			  | name:种子名称
			  | url:种子链接
			-->
			<!--seeds>
				<seed name="" url="" />
			</seeds-->
			<!--
			  | 告诉爬虫仅抓取以下这些host的链接，多数是应对二级或多级域名的情况
			-->
			<!--validHosts>
				<validHost value="demo.eweb4j.org" />
				<validHost value="wwww.eweb4j.org" />
			</validHosts-->
			<!--
			  | HTTP Header
			<headers>
				<header name="" value="" />
			</headers>-->
			<!--
			  | HTTP Cookie
			<cookies>
				<cookie name="" value="" host="" path="" />
			</cookies>-->
			<!--
			  | 进入任务队列的URL规则
			  | policy:多个rule的策略，and | or
			-->
			<queueRules policy="and">
				<!--
				  | 规则
				  | type:规则类型，包括 regex | equal | start | end | contains 所有规则可以在前面添加 "!" 表示取反
				  | value:值
				-->
				<rule type="!regex" value="^.*\.(jpg|png|gif)$" />
			</queueRules>
			<!--
			  | 抓取目标
			-->
			<targets>
				<!--
				  | 限制目标URL的来源,一般来说，对应的就是网站的频道页，例如某个分类下的新闻列表页
				-->
				<sourceRules policy="and">
					<rule type="regex" value="http://www\.oschina\.net/question\?catalog=1&amp;show=&amp;p=\d+">
						<!--
						  | 定义如何在来源页面上挖掘新的 URL
						  | 这个节点跟 <model> 节点是一样的结构，只不过名称不叫model而是叫做digUrls而已
						-->
						<digUrls>
							<field name="page_url" isArray="1">
								<parsers>
									<parser xpath="//div[@class='QuestionList']//ul[@class='pager']//li[@class='page']//a[@href]" attribute="href" />
									<parser exp="'http://www.oschina.net/question'+$this" />
								</parsers>
							</field>
							<field name="target_url" isArray="1"> 
								<parsers>
									<parser xpath="//div[@class='QuestionList']//ul//li[@class='question']//div[@class='qbody']/h2[1]//a[@href]" attribute="href" />
								</parsers>
							</field>
						</digUrls>
					</rule>
				</sourceRules>
				<!--
				  | name:目标名称	
				-->
				<target name="question">
					<!--
					  | 目标URL的规则
					-->
					<urlRules policy="and">
						<rule type="regex" value="http://www\.oschina\.net/question/\d+_\d+" />
					</urlRules>
					<!--
					  | 目标网页的数据模型
					  | cType: 目标网页的contentType
					  | isForceUseXmlParser:0|1 是否强制使用XML的解析器来解析目标网页，此选项可以让HTML页面支持XPath2.0
					  | isIgnoreComments:0|1 是否忽略注释
					  | isArray:0|1 目标网页是否有多个数据模型，一般一些RSS XML页面上就会有很多个数据模型需要解析，即在一个xml页面上解析多个Model对象
					  | xpath: 搭配 isArray 来使用，可选
					-->
					<model>
						<!--
						  | 目标网页的命名空间配置,一般用于xml页面
						  | prefix: 前缀
						  | uri: 关联的URI
						<namespaces>
							<namespace prefix="" uri="" />
						</namespaces>
						-->
						<!--
						  | 属性的配置
						  | name:属性名称
						  | isArray:0|1 是否是多值
						  | isMergeArray:0|1 是否将多值合并，搭配isArray使用
						  | isParam:0|1 是否作为参数提供给别的field节点使用，如果是，则生命周期不会保持到最后
						  | isFinal:0|1 是否是不可变的参数，搭配isParam使用，如果是，第一次赋值之后不会再被改变
						  | isAlsoParseInNextPage:0|1 是否在分页的下一页里继续解析，用于目标网页有分页的情况
						  | isTrim:0|1 是否去掉前后空格
						-->
						<field name="title">
							<parsers>
								<!--
								  | xpath: XPath规则，如果目标页面是XML，则可以使用2.0语法，否则HTML的话暂时只能1.0
								  | attribute:当使用XPath解析后的内容不是文本而是一个Node节点对象的时候，可以给定一个属性名获取其属性值例如<img src="" />
								  | regex:当使用XPath（包括attribute）规则获取到的文本内容不满足需求时，可以继续设置regex正则表达式进行解析
								  | exp:当使用XPath获取的文本(如果获取的不是文本则会先执行exp而不是regex否则先执行regex)不满足需求时，可以继续这是exp表达式进行解析
								  |     exp表达式有几个内置对象和方法:
								  |     $output(Node): 这个是内置的output函数，作用是输出某个XML节点的结构内容。参数是一个XML节点对象，可以通过XPath获得
								  |     $this: 当使用XPath获取到的是Node节点时，这个表示节点对象，否则表示Java的字符串对象,可以调用Java字符串API进行处理
								  |     $Tags: 这个是内置的用于过滤标签的工具类 
								  |            $Tags.xml($output($this)).rm('p').ok()
								  |            $Tags.xml($this).rm('p').empty().ok()
								  |     $Attrs: 这个是内置的用于过滤属性的工具类
								  |            $Attrs.xml($this).rm('style').ok()
								  |            $Attrs.xml($this).tag('img').rm('src').ok()
								  |     
								  |            $Tags和$Attrs可以一起使用: 
								  |            $Tags.xml($this).rm('p').Attrs().rm('style').ok()
								  |            $Attrs.xml($this).rm('style').Tags().rm('p').ok()
								  | skipErr:0|1 是否忽略错误消息
								  | skipRgxFail:0|1 是否忽略正则匹配失败，如果是，则会取失败前的值
								-->
								<parser xpath="//div[@class='QTitle']/h1/text()"/>
							</parsers>
						</field>
						<field name="content">
							<parsers>
								<parser xpath="//div[@class='Content']//div[@class='detail']" exp="$output($this)" />
								<!--attribute 黑名单-->
								<parser exp="$Attrs.xml($this).rm('class').rm('style').rm('width').rm('height').rm('usemap').rm('align').rm('border').rm('title').rm('alt').ok()" />
								<!--tag 黑名单，去掉内嵌内容-->
								<parser exp="$Tags.xml($this).rm('map').rm('iframe').rm('object').empty().ok()" />
								<!--tag 白名单，保留的标签，除此之外都要删除（不删除其他标签内嵌内容）-->
								<parser exp="$Tags.xml($this).kp('br').kp('h1').kp('h2').kp('h3').kp('h4').kp('h5').kp('h6').kp('table').kp('th').kp('tr').kp('td').kp('img').kp('p').kp('a').kp('ul').kp('ol').kp('li').kp('td').kp('em').kp('i').kp('u').kp('er').kp('b').kp('strong').ok()" />
								<!--其他-->
							</parsers>
						</field>
						<field name="author">
							<parsers>
								<parser xpath="//div[@class='stat']//a[@target='_blank']/text()"/>
							</parsers>
						</field>
						<field name="tags" isArray="1">
							<parsers>
								<parser xpath="//div[@class='Tags']//a/text()"/>
							</parsers>
						</field>
						<field name="answers" isArray="1">
							<parsers>
								<parser xpath="//li[@class='Answer']//div[@class='detail']/text()" />
							</parsers>
						</field>
					</model>
				</target>
			</targets>
			<!--
			  | 插件
			-->
			<plugins>
				<!--
				  | enable:是否开启
				  | name:插件名
				  | version:插件版本
				  | desc:插件描述
				-->
				<plugin enable="1" name="spider_plugin" version="0.0.1" desc="这是一个官方实现的默认插件，实现了所有扩展点。">
					<!--
					  | 每个插件包含了对若干扩展点的实现
					-->
					<extensions>
						<!--
						  | point:扩展点名它们包括  task_poll, begin, fetch, dig, dup_removal, task_sort, task_push, target, parse, pojo, end
						-->
						<extension point="task_poll">
							<!--
							  | 扩展点实现类
							  | type: 如何获取实现类 ,默认通过无参构造器实例化给定的类名，可以设置为ioc，这样就会从EWeb4J的IOC容器里获取
							  | value: 当时type=ioc的时候填写IOC的bean_id，否则填写完整类名
							  | sort: 排序，同一个扩展点有多个实现类，这些实现类会以责任链的方式进行执行，因此它们的执行顺序将变得很重要
							-->
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.TaskPollPointImpl" sort="0"/>
						</extension>
						<extension point="begin">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.BeginPointImpl" sort="0"/>
						</extension>
						<extension point="fetch">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.FetchPointImpl" sort="0"/>
						</extension>
						<extension point="dig">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.DigPointImpl" sort="0"/>
						</extension>
						<extension point="dup_removal">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.DupRemovalPointImpl" sort="0"/>
						</extension>
						<extension point="task_sort">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.TaskSortPointImpl" sort="0"/>
						</extension>
						<extension point="task_push">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.TaskPushPointImpl" sort="0"/>
						</extension>
						<extension point="target">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.TargetPointImpl" sort="0"/>
						</extension>
						<extension point="parse">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.ParsePointImpl" sort="0"/>
						</extension>
						<extension point="end">
							<impl type="" value="org.eweb4j.spiderman.plugin.impl.EndPointImpl" sort="0"/>
						</extension>
					</extensions>
					<providers>
						<provider>
							<orgnization name="CFuture" website="http://lurencun.com" desc="Color your future">
								<author name="weiwei" website="http://laiweiweihi.iteye.com | http://my.oschina.net/laiweiwei" email="l.weiwei@163.com" weibo="http://weibo.com/weiweimiss" desc="一个喜欢自由、音乐、绘画的IT老男孩" />
							</orgnization>
						</provider>
					</providers>
				</plugin>
			</plugins>
		</site>
	</beans>
----
