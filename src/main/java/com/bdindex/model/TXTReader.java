package com.bdindex.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import com.bdindex.ui.Util;



@InputType("txt")
public class TXTReader extends ArrayList<String> implements Reader {

	private static final long serialVersionUID = -1648135751263796940L;
	private static final String spiltter = "#";
	private int modelNum;

	public TXTReader(File file) {
		read(file);
	}

	@Override
	public void read(File file) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStreamReader tmpReader = new InputStreamReader(new FileInputStream(file), Util.get_charset(file));
			BufferedReader in = new BufferedReader(tmpReader);
			// 清除bom标记
			String s = in.readLine().trim();
			if (Objects.equals(s.charAt(0), BOM)) {
				s = s.substring(1);
			}
			this.add(s);
			try {
				while ((s = in.readLine()) != null) {
					this.add(s);
					sb.append(s.trim());
					sb.append("\n");
				}
			} finally {
				in.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		modelNum = this.size();
		
	}

	@Override
	/**
	 * 构造参数map
	 * @throws IllegalArgumentException
	 */
	public ArrayList<HashMap<String, String>> generate()
			throws IllegalArgumentException {
		ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < modelNum; i++) {
			HashMap<String, String> modelArgMaps = null;
			String[] args = this.get(i).trim().split(spiltter);
			// 这里还需要改,改为抛出异常？
			if (args.length < 3) { //3是指，输入中至少要包含关键词和开始、结束日期
				int count = i + 1;
				throw new IllegalArgumentException(String.valueOf(count));
			}
			// 生成单个model的参数列表
			modelArgMaps = init(Model.class, args);
			res.add(modelArgMaps);
		}

		return res;
	}

	private HashMap<String, String> init(Class<?> clazz, String... args) {
		HashMap<String, String> argMap = new HashMap<String, String>();

		// init keySet, valueSet
		Field[] fields = clazz.getDeclaredFields();
		// 这里也是有问题的, 需要确定model成员的顺序
		for (int i = 0; i < args.length; i++) {
			Field field = fields[i];
			field.setAccessible(true);
			argMap.put(field.getName(), args[i]);
		}
		return argMap;
	}

}
