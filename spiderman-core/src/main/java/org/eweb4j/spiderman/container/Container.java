/**
 * 
 */
package org.eweb4j.spiderman.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eweb4j.spiderman.spider.Settings;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.spider.SpiderListenerAdaptor;
import org.eweb4j.util.xml.AttrTag;
import org.eweb4j.util.xml.Skip;
import org.eweb4j.util.xml.XMLReader;

/**
 * @author yangc
 * @param <T>
 *
 */
public class Container {
	@AttrTag
	private String id;//容器Id;
	@AttrTag
	private String name;//容器名称;
	@AttrTag
	private String enable = "1";//是否开启本容器
	@Skip
	public Boolean isStop = false;//每个容器都有属于自己的一个停止信号，用来标识该容器的状态是否停止完全
	
	private XMLReader reader;//所属配置文件;
	
	private ExecutorService pool = null;//线程池;
	
	//容器包含组件
	private Collection<Component> components = new ArrayList<Component>();
	
	private SpiderListener listener = null;
	
	
	
	public Container(){}
	public Container(String id)
	{
		this.id = id;
	}
	
	public Container init(SpiderListener listener) throws Exception{
		this.listener = listener;
        if (this.listener == null)
            this.listener = new SpiderListenerAdaptor();
        Collection<String> modules = null;
        if(Settings.modules() == null){//默认只加载站点组件;提高性能;
        	modules = Components.toArray(Components.site);
        }else{
        	modules = Arrays.asList(Settings.modules());
        }
        for(String module : modules){
        	Component component = getModuleComponent(module);
        	if(component != null)
        	{
        		component = component.init(this, listener);
        		if(component != null)
        		components.add(component);
        	}
        }
        //初始化容器线程池;
        initPool();
        return this;
    }
	
	public void initPool(){
		if (pool == null){
			int size = components.size();
			if (size == 0)
				throw new RuntimeException("there is no component to load...");
			pool = new ThreadPoolExecutor(size, size, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			listener.onInfo(Thread.currentThread(), null, "init container["+this.id+"] thread pool size->"+size+" success ");
		}
	}
	
	public Container startup() {
		for(Component component : components)
		{
			pool.execute(new Container._Executor(component));
		}
		this.isStop = false;
	    return this;
	}
	
	private class _Executor implements Runnable{
		private Component component = null;
		
		public _Executor(Component component){
			this.component = component;
		}
		
		public void run() {
			component.startup();
		}
	} 
	
	public void destroy(SpiderListener listener, boolean isShutdownNow,Object... args)
	{
		for(Component component : components)
		{
			component.destroy(listener, isShutdownNow);
			listener.onInfo(Thread.currentThread(), null, component.getClass().getSimpleName()+"[" + component.getName() + "] of the Container["+this.id+"] destroy... ");
			listener.onAfterShutdown(component,args);
		}
		if (isShutdownNow)
			this.pool.shutdownNow();
		else
			this.pool.shutdown();
		if(this.pool != null)
			this.pool = null;
		components.clear();
		this.isStop = true;
	}
	
	public Container addComponent(Component component)
	{
		this.components.add(component);
		return this;
	}
	
	public Container removeComponent(Component component)
	{
		this.components.remove(component);
		return this;
	}
	
	public Component getComponent(String componentName)
	{
		for(Component component : components)
		{
			String simpleName = component.getClass().getSimpleName().toLowerCase();
			if(componentName.toLowerCase().equals(simpleName)){
				return component;
			}
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	private Component getModuleComponent(final String componentName){
		if (!Components.contains(componentName))
			return null;
		String value = Components.getComponentClassName(componentName);
		Component t = null;
		try {
			Class<Component> cls = (Class<Component>) Thread.currentThread().getContextClassLoader().loadClass(value);
			t = cls.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Component class -> " + value + " of Component["+componentName+"] not found !", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Component class -> " + value + " of Component["+componentName+"] instaniation fail !", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Component class -> " + value + " of Component["+componentName+"] illegal access !", e);
		}
		
		return t;
	}
	
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Collection<Component> getComponents() {
		return components;
	}
	public void setComponents(Collection<Component> components) {
		this.components = components;
	}
	public XMLReader getReader() {
		return reader;
	}
	public void setReader(XMLReader reader) {
		this.reader = reader;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
