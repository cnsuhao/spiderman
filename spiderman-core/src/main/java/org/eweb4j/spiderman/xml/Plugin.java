package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

public class Plugin {

	@AttrTag
	private String enable = "1";
	
	@AttrTag
	private String name = "spider_plugin";
	
	@AttrTag
	private String version = "0.0.1";
	
	@AttrTag
	private String desc = "这是官方实现的默认插件，实现了所有扩展点。";

	private Providers providers ;

	private Extensions extensions ;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public void setExtensions(Extensions extensions) {
		this.extensions = extensions;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Providers getProviders() {
		return providers;
	}

	public void setProviders(Providers providers) {
		this.providers = providers;
	}

	@Override
	public String toString() {
		return "Plugin [name=" + name + ", version=" + version + ", desc=" + desc + ", providers=" + providers + "]";
	}
	
}
