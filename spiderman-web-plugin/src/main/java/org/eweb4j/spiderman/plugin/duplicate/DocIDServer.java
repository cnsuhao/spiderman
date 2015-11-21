package org.eweb4j.spiderman.plugin.duplicate;
import java.io.File;

import org.eweb4j.spiderman.spider.Settings;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.TaskDbServer;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.FileUtil;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;

public class DocIDServer implements TaskDbServer{
	
	private String name = null;
	public Environment env = null;
	public Database db = null;
//	private final Object mutex = new Object();
	private int lastDocID = 0;
	
	public DocIDServer(String name, SpiderListener listener) {
		this.name = name;
		File _dbEnv = new File(Settings.website_visited_folder());
		if (!_dbEnv.exists()) {
			String error = "dbEnv folder -> " + _dbEnv.getAbsolutePath() + " not found !";
			RuntimeException e = new RuntimeException(error);
			listener.onError(Thread.currentThread(), null, error, e);
			throw e;
		}
		File dir = new File(_dbEnv.getAbsolutePath()+"/"+name);
		if (!dir.exists())
			dir.mkdir();
		
		for (File f : dir.listFiles()){
			boolean flag = FileUtil.deleteFile(f);
			if (!flag) {
				String error = "file -> " + f.getAbsolutePath() + " can not delete !";
				RuntimeException e = new RuntimeException(error);
				listener.onError(Thread.currentThread(), null, error, e);
				throw e;
			}
			listener.onInfo(Thread.currentThread(), null, "file -> " + f.getAbsolutePath() + " delete success !");
		}
		
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		EnvironmentConfig ec = new EnvironmentConfig();
		ec.setAllowCreate(true);
		env = new Environment(dir, ec);
		db = env.openDatabase(null, name, dbConfig);
		lastDocID = 0;
	}
	
	/**
	 * Returns the docid of an already seen url.
	 * 
	 * @param url
	 *            the URL for which the docid is returned.
	 * @return the docid of the url if it is seen before. Otherwise -1 is
	 *         returned.
	 */
	public synchronized int getDocId(String url) {
		OperationStatus result;
		DatabaseEntry value = new DatabaseEntry();
		try {
			DatabaseEntry key = new DatabaseEntry(url.getBytes());
			result = db.get(null, key, value, null);

			if (result == OperationStatus.SUCCESS && value.getData().length > 0) {
				return CommonUtil.byteArray2Int(value.getData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public synchronized int newDocID(String url) {
		try {
			// Make sure that we have not already assigned a docid for this
			// URL
			int docid = getDocId(url);
			if (docid > 0) {
				return docid;
			}

			lastDocID++;
			db.put(null, new DatabaseEntry(url.getBytes()), new DatabaseEntry(CommonUtil.int2ByteArray(lastDocID)));
			return lastDocID;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public synchronized void addUrlAndDocId(String url, int docId) throws Exception {
		if (docId <= lastDocID) {
			throw new Exception("Requested doc id: " + docId + " is not larger than: " + lastDocID);
		}
		
		// Make sure that we have not already assigned a docid for this URL
		int prevDocid = getDocId(url);
		if (prevDocid > 0) {
			if (prevDocid == docId) {
				return;
			}
			throw new Exception("Doc id: " + prevDocid + " is already assigned to URL: " + url);
		}
		
		db.put(null, new DatabaseEntry(url.getBytes()), new DatabaseEntry(CommonUtil.int2ByteArray(docId)));
		lastDocID = docId;
	}
	
	public boolean isSeenBefore(String url) {
		return getDocId(url) != -1;
	}

	public int getDocCount() {
		try {
			return (int) db.count();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void sync() {
		try {
			db.sync();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			db.close();
			env.removeDatabase(null, name);
			env.cleanLog();
			env.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
}
