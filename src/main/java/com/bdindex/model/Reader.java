package com.bdindex.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取器接口
 * @author pastqing
 */
public interface Reader {
	
	//bom 标记
	public static char BOM = (char)65279;
	
	// Models 参数
	public Map<String, Object> modelsArgs = new HashMap<String, Object>();
	
	//读取option
	public void read(File file);
	
	public ArrayList<HashMap<String, String>> generate();
}
