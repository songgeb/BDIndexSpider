package com.bdindex.model;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.log4j.Logger;

import com.bdindex.exception.DocumentFormatException;
import com.bdindex.exception.ModelDateException;

/**
 * Model 生成器
 * 
 * @author pastqing
 */

public class ModelGenerator {

	private static Logger logger = Logger.getLogger(ModelGenerator.class);
	private Reader reader;

	/**
	 * @param inputType
	 * @return Model List
	 * @throws ModelDateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * 
	 */
	public ArrayList<Model> get(String inputType, File file)
			throws DocumentFormatException, ModelDateException {
		reader = ReaderFactory.newInstance().create(inputType, file);
		if (reader == null)
			logger.info("Reader null");
		logger.info(reader.getClass().getName());
		return generate(reader);
	}

	/**
	 * 封装一个日期样式枚举
	 */
	public enum DateStyle {
		YYYY_MM_dd("yyyy-MM-dd", false), 
		YYYYpMMpdd("yyyy/MM/dd", false), 
		YYYY_MM("yyyy-MM", false), 
		YYYYpMM("yyyy/MM", false);
		private String value;

		public String value() {
			return this.value;
		}

		DateStyle(String value, boolean isShowOnly) {
			this.value = value;
		}
	}

	/**
	 * 封装一个日期转换器
	 */
	private class DateConver implements Converter {

		/**
		 * 自动解析日期
		 * 
		 * @param date
		 * @throws ModelDateException
		 */
		private java.util.Date transDate(String date) throws ModelDateException {
			// assert date is not null
			
			java.util.Date dateTmp = null;
			for (DateStyle style : DateStyle.values()) {
				ParsePosition pos = new ParsePosition(0);// 从第一个字符开始解析
				// 去除日期中的日
//				String newDate = date.substring(0, date.lastIndexOf("/"));
				int offset = 0;
				for(int i = date.length() - 1; i >= 0; i-- ) {
					if(!Character.isDigit(date.charAt(i))) {
						offset = i;
						break;
					}
				}
				dateTmp = getDateFormat(style.value()).parse(date, pos);
				if (pos.getIndex() == offset) {
					return dateTmp;
				}
			}
			// 没找到已有的DateStyle
			throw new ModelDateException();
		}

		/**
		 * 获取日期的SimpleDateFormat
		 * 
		 * @param style
		 */
		private SimpleDateFormat getDateFormat(String style) {
			SimpleDateFormat format = new SimpleDateFormat(style);
			format.setLenient(false); // 设置严格解析
			format.applyPattern(style);
			return format;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Class<T> type, Object value) {
			if (type != java.util.Date.class) {
				return null;
			}
			if (!(value instanceof String))
				return null;
			// java.util.Date date = format.parse(val);
			// System.out.println(format.format(date));
			try {
				String val = (String) value;
				return (T) transDate(val);
			} catch (ModelDateException e) {
			}
			return null;
		}
	}

	private ArrayList<Model> generate(Reader reader)
			throws DocumentFormatException, ModelDateException {
		ArrayList<Model> modelList = new ArrayList<Model>();
		ConvertUtils.register(new DateConver(), java.util.Date.class);
		int count = 1;
		for (HashMap<String, String> modelMap : reader.generate()) {
			Model model = new Model();
			try {
				BeanUtils.populate(model, modelMap);
				if (model.checkDate() == 1) {
					throw new ModelDateException("第" + String.valueOf(count)
							+ " 行日期不能超过当前日期");
				} else if (model.checkDate() == 2) {
					throw new ModelDateException("第" + String.valueOf(count)
							+ " 行结束日期不能小于开始日期");
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				throw new DocumentFormatException(String.valueOf(count));
			}
			modelList.add(model);
			count++;
		}
		return modelList;
	}

	public static void main(String[] args) throws DocumentFormatException,
			ModelDateException {
		Model model = new Model();
		try {
		HashMap<String, Object> modelMap = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		modelMap.put("startDate", sdf.parse("2015/01/01"));
		modelMap.put("endDate", sdf.parse("2015/02/01"));
		modelMap.put("keyword", "nihao");
			BeanUtils.populate(model, modelMap);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		ModelGenerator m = new ModelGenerator();
//		File file = new File("src/main/resources/test.csv");
//		ArrayList<Model> models = m.get("csv", file);
//		System.out.println(models);
	}
}
