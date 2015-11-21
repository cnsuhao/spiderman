/**
 * 
 */
package org.eweb4j.spiderman.xml.db;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangc
 *
 */
public class Tables {
	
	//数据库表
	private List<Table> table = new ArrayList<Table>();

	public List<Table> getTable() {
		return table;
	}

	public void setTable(List<Table> table) {
		this.table = table;
	}
	
}
