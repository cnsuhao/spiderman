Spiderman Core - Java开源Web数据抽取工具
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
    		
    		//启动EWeb4J框架
    		String err = EWeb4JConfig.start();
    		if (err != null)
    			throw new Exception(err);
    		
    		SpiderListener listener = new SpiderListenerAdaptor(){
    			public void onInfo(Thread thread, Task task, String info) {
    				System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
    				System.out.println(info);
    			}
    			public void onError(Thread thread, Task task, String err, Exception e) {
    				e.printStackTrace();
    			}
    			
    			public void onParse(Thread thread, Task task, List<Map<String, Object>> models) {
    				synchronized (mutex) {
    					String content = CommonUtil.toJson(models.get(0));
    					
    					try {
    						File dir = new File(FileUtil.getTopClassPath(TestSpider.class) + "/Data/" + task.site.getName());
    						if (!dir.exists())
    							dir.mkdirs();
    						File file = new File(dir+"/count_"+task.site.counter.getCount()+"_"+CommonUtil.getNowTime("yyyy_MM_dd_HH_mm_ss")+".json");
    						FileUtil.writeFile(file, content);
    						System.out.print("[SPIDERMAN] "+CommonUtil.getNowTime("HH:mm:ss")+" [INFO] ~ ");
    						System.out.println(file.getAbsolutePath() + " create finished...");
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    				}
    			}
    		};
    		
    		// 启动爬虫
		Spiderman.me()
				.init(listener)// 初始化
				.startup()// 启动
				.keep("30s");// 存活时间，过了存活时间后马上关闭

		// ------拿到引用后你还可以这样关闭-------------------------
		// spiderman.shutdown();//等待正在活动的线程都死掉再关闭爬虫
		// spiderman.shutdownNow();//马上关闭爬虫
		
		// 定时重启爬虫
		// Spiderman.me()
			// .listen(listener)// 监听
			// .schedule("10s")// 调度，爬虫运行10s
			// .delay("2s")// 每隔 10 + 2 秒后重启爬虫
			// .times(3)// 调度 3 次
			// .startup()// 启动
			// .blocking();// 阻塞当前线程直到所有调度完成
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
      | author: l.weiwei@163.com
      | blog: http://laiweiweihi.iteye.com | http://my.oschina.net/laiweiwei
      | qq: 493781187
    -->
    <beans>
        <!--
    	  | name:名称
    	  | url:种子链接
	  | skipStatusCode:设置哪些状态码需要忽略，多个用逗号隔开
	  | userAgent:设置爬虫标识
	  | includeHttps:是否抓取https页
    	  | reqDelay:{n}s|{n}m|{n}h|n每次请求之前延缓时间
    	  | enable:0|1是否开启本网站的抓取
    	  | charset:网站字符集
    	  | schedule:调度时间，每隔多长时间重新从种子链接抓取
    	  | thread:分配给本网站爬虫的线程数
    	  | waitQueue:当任务队列空的时候爬虫等待多长时间再索取任务
    	-->
    	<site name="oschina" url="http://www.oschina.net/question" reqDelay="1s" enable="1" charset="utf-8" schedule="1h" thread="2" waitQueue="10s">
    		<!--
		  | 配置多个种子链接
		  | url:种子链接
		-->
		<!--seeds>
			<seed url="" />
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
    			<cookie name="" value="" domain="" path="" />
    		</cookies>-->
    		<!--
    		  | 进入任务队列的URL规则
    		  | policy:多个rule的策略，暂时只实现了and，未来会有or
    		-->
    		<queueRules policy="and">
    			<!--
    			  | 规则
    			  | type:规则类型，包括 regex | equal | start | end | contains 所有规则可以在前面添加 "!" 表示取反
    			  | value:值
    			-->
    			<rule type="!regex" value="^.*\.(jpg|png|gif).*$" />
    		</queueRules>
    		<!--
    		  | 抓取目标
    		-->
    		<targets>
    			<!--
    			  | name:目标名
			  | isArray:是否是解析为多个(应付一些feed.xml，一个页面有N个模型数据)
    			-->
    			<target name="deal" isArray="" >
    				<!--
				  | 限制目标URL的来源
				-->
				<!--
				<sourceRules policy="and">
					<rule type="equal" value="">-->
						<!--
						  | 定义如何在来源页面上挖掘新的 URL
						-->
						<!--
						<digUrls isArray="1">
							<parsers>
								<parser xpath="" attribute="" exp="" regex="" />
							</parsers>
						</digUrls>-->
					<!--</rule>
				</sourceRules>-->
				<!--
				  | 目标URL的规则
				-->
				<urlRules policy="and">
					<rule type="regex" value="http://www\.oschina\.net/question/\d+_\d+" />
				</urlRules>
    				<!--
    				  | 目标网页的数据模型
				  | isArray: 1|0 是否是多个model
				  | xpath: XPath解析规则
    				-->
    				<model isArray="" xpath="">
    					<!--
    					  | 属性的配置
    					  | name: 属性名称
					  | isTrim: 1|0 是否去除字符串首尾空格
    					  | parsers: 针对该属性的解析规则，可以有多个，链式执行
    					-->
    					<field name="title" isTrim="">
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
						  | skipErr: 1|0 是否忽略 exp 解析出现的错误
						  | skipRgxFail: 1|0 是否忽略 regex 解析失败的返回值
    						-->
						<parsers>
							<parser xpath="//div[@class='QTitle']/h1/text()" attribute="" regex="" exp="" skipErr="" skipRgxFail="" />
						</parsers>
					</field>
					<field name="content">
						<parsers>
							<parser xpath="//div[@class='Content']//div[@class='detail']" exp="$Tags.xml($output($this)).rm('div').Attrs().rm('style').ok()" />
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
