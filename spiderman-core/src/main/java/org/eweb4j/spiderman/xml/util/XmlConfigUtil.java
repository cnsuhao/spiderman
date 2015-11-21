package org.eweb4j.spiderman.xml.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eweb4j.spiderman.container.Container;
import org.eweb4j.spiderman.spider.Settings;
import org.eweb4j.spiderman.xml.site.Site;
import org.eweb4j.util.xml.BeanXMLReader;
import org.eweb4j.util.xml.BeanXMLUtil;
import org.eweb4j.util.xml.XMLReader;

public class XmlConfigUtil {
	/**
	 * @param folderPath 站点路径;
	 * 加载所有配置文件;
	 * @throws Exception
	 */
	public static List<Container> loadConfigFiles(String folderPath) throws Exception{
		
		File siteFolder = new File(folderPath);
		if (!siteFolder.exists())
			throw new Exception("can not found Sites Folder -> " + siteFolder.getAbsolutePath());
		
		if (!siteFolder.isDirectory())
			throw new Exception("Sites -> " + siteFolder.getAbsolutePath() + " must be folder !");
		
		File[] files = siteFolder.listFiles();
		
		List<Container> containers = new ArrayList<Container>(files.length);
		for (File file : files){
			if (!file.exists())
				continue;
			if (!file.isFile())
				continue;
			if (!file.getName().endsWith(".xml"))
				continue;
			if(loadConfigFile(file) != null)
			containers.add(loadConfigFile(file));
		}
		return containers;
	}
	/**
	 * 加载所有配置文件;
	 * 配置文件中配置;
	 * @throws Exception
	 */
	public static List<Container> loadConfigFiles() throws Exception{
		
		return loadConfigFiles(Settings.website_xml_folder());
	}
	/**
	 * 加载单个配置文件;
	 * @param file
	 * @return
	 * @throws Exception
	 */
	
	public static Container loadConfigFile(File file) throws Exception {
        Container container = readContainer(file);
        return container;
	}
	/**
	 * 读取容器信息;
	 * @param file
	 * @return
	 */
	@SuppressWarnings("unused")
	public static Container readContainer(File file)throws Exception
	{
		if (!file.exists())
            return null;
        if (!file.isFile())
            return null;
        if (!file.getName().endsWith(".xml"))
            return null;
        XMLReader reader = BeanXMLUtil.getBeanXMLReader(file);
        reader.setBeanName("container");
        reader.setClass("container", Container.class);
        
        Container container = reader.readOne();
        reader.setRootElementName("container");
        container.setReader(reader);
        if (container == null)
            throw new Exception("container xml file error -> " + file.getAbsolutePath());
        container.isStop = true;
        return container;
	}
	/**
	 * 读取站点对象信息;
	 * @param container
	 * @return
	 */
	public static Site readSite(Container container)
	{
		BeanXMLReader reader = (BeanXMLReader)container.getReader();
	    reader.setBeanName("site");
	    reader.setClass("site", Site.class);
	    Site site;
		try {
			site = reader.readOne();
			site.container = container;
		} catch (Exception e) {
			return null;
		}
		return site;
	}
}
