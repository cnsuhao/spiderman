package org.eweb4j.spiderman.xml.db;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

public class Table {
	//表名称;
	@AttrTag
	public String name;
	private Database database;
	public List<Column> columns = new ArrayList<Column>();
	public Table(){};
	public Table(Database database)
	{
		this.database = database;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Database getDatabase() {
		return database;
	}
	public void setDatabase(Database database) {
		this.database = database;
	}
}
