package org.eweb4j.spiderman.xml.db;

import org.eweb4j.spiderman.xml.Option;
import org.eweb4j.spiderman.xml.Options;

public class Database{
	
	
	private Options options;//一些其他的DB配置信息
	
	private Tables tables;//数据库表;
	
	private Sql sql;//sql语句;

	public String getOption(String name){
		if (options == null)
			return null;
		
		for (Option option: options.getOption()) {
			if (option == null || option.getName() == null || option.getName().trim().length() == 0)
				continue;
			if (!option.getName().equals(name))
				continue;
			
			return option.getValue();
		}
		
		return null;
	}
	
	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public Tables getTables() {
		return tables;
	}

	public void setTables(Tables tables) {
		this.tables = tables;
	}

	public Sql getSql() {
		return sql;
	}

	public void setSql(Sql sql) {
		this.sql = sql;
	}
}
