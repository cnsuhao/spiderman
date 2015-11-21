/**
 * 
 */
package org.eweb4j.spiderman.container;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author WChao
 *
 */
public class Components {
	
	public final static String site = "site";
	public final static String db = "db";
	public final static String file = "file";
	
	public static String getComponentClassName(String point){
		if (site.equals(point))
			return "org.eweb4j.spiderman.xml.site.Site";
		if (db.equals(point))
			return "org.eweb4j.spiderman.xml.db.Db";
		if (file.equals(point))
			return "org.eweb4j.spiderman.xml.file.File";
		return null;
	}
	public static boolean contains(String name){
		return site.equals(name) || db.equals(name) || file.equals(name) ;
	}
	
	public static String string(){
		return "[" + site + ", "+ db + ", " + file +"]" ;
	}
	public static Collection<String> toArray(String type)
	{
		return Arrays.asList(type);
	}
	public static Collection<String> toArray(){
		return Arrays.asList(site, db, file);
	}
}
