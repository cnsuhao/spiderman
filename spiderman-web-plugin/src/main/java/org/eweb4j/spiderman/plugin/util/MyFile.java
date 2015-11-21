package org.eweb4j.spiderman.plugin.util;

import java.io.File;


public class MyFile extends File {

	private static final long serialVersionUID = 1L;
	private static final String class_path = MyFile.class.getResource("/").getPath();

	public MyFile(String pathname) {
		super(class_path + pathname);
	}

}
