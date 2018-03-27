package com.bdindex.ui;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractButton;

import org.apache.log4j.Logger;

import com.bdindex.model.Model;
import com.selenium.Constant;

public class Util {

	private static int dateSplitStepInAccurateMode = 10;
	private static int dateSplitStepInEstimatedMode = 1;

	private static Logger logger = Logger.getLogger(Util.class);

	/**
	 * 获取文件编码格式,防止读入关键字文件时出现中文乱码
	 * 
	 * @param file
	 * @return
	 */
	public static String get_charset(File file) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try {
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1) {
				bis.close();
				return charset;
			}
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked) {
				while ((read = bis.read()) != -1) {
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
															// (0x80
															// - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					} else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else
								break;
						} else
							break;
					}
				}
			}

			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return charset;
	}

	/**
	 * 获取以dateSplitStep个月为单位,将开始日期和结束日期之间分组信息 注意:为了和百度指数日期同步01.01-01.21算是一个月
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static ArrayList<Date[]> getDatePairsBetweenDates(Date startDate, Date endDate) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.clear();
		startCalendar.setTime(startDate);
		int startYear = startCalendar.get(Calendar.YEAR);
		int startMonth = startCalendar.get(Calendar.MONTH);

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.clear();
		endCalendar.setTime(endDate);
		int endYear = endCalendar.get(Calendar.YEAR);
		int endMonth = endCalendar.get(Calendar.MONTH);

		int months = (endMonth - startMonth) + 12 * (endYear - startYear) + 1;

		int dateSplitStep = 1;
		switch (Constant.currentMode) {
		case Estimate:
			dateSplitStep = dateSplitStepInEstimatedMode;
			break;
		case Accurate:
			dateSplitStep = dateSplitStepInAccurateMode;
			break;
		default:
			break;
		}
		
		int result = (int) Math.ceil(months * 1.0f / dateSplitStep);
		Date subStartDate = null;
		Date subEndDate = null;
		Calendar tmpCalendar = Calendar.getInstance();
		tmpCalendar.clear();
//threshold
		ArrayList<Date[]> datePairs = new ArrayList<>();
		for (int i = 1; i <= result; i++) {
			startCalendar.setTime(startDate);
			startCalendar.add(Calendar.MONTH, (i - 1) * dateSplitStep);
			subStartDate = startCalendar.getTime();
			tmpCalendar.setTime(subStartDate);
			tmpCalendar.add(Calendar.MONTH, dateSplitStep - 1);
			if (tmpCalendar.compareTo(endCalendar) >= 0) {
				subEndDate = new Date(endCalendar.getTimeInMillis());
			} else {
				subEndDate = new Date(tmpCalendar.getTimeInMillis());
			}
			Date[] datePair = { subStartDate, subEndDate };
			datePairs.add(datePair);
		}
		return datePairs;
	}

	/**
	 * 将爬虫爬去每次关键词的信息写入指定路径文件中
	 * 
	 * @param filePath
	 * @param model
	 * @throws IOException
	 */
	public static void writeSpiderInfoToFile(String filePath, Model model) {
		if (filePath == null) {
			return;
		}
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(model.getKeyword());
			writer.write(Constant.newLineString);
			writer.write("数据抓取状态 : " + model.getStatus());
			writer.write(" 耗时 : " + model.getTime() + " 秒");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error("【"+model.getKeyword()+"】"+"向写入文件中爬虫信息出错", e);
		}
	}
	
	/**
	 * 控制主界面的按钮状态
	 * @param buttons
	 */
	public static void setButtonsStatus(ArrayList<AbstractButton> buttons, boolean status) {
		for (AbstractButton abstractButton : buttons) {
			abstractButton.setEnabled(status);
		}
	}
	
	/**
	 * 计算两个日期间的天数
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long daysBetweenTwoDates(Date startDate, Date endDate) {
		long startTime = startDate.getTime();
		long endTime = endDate.getTime();
		if (endTime < startTime) {
			return 0;
		}
		return ((endTime - startTime) / (3600 * 24 * 1000)) + 1;
	}

	public static void main(String[] args) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.clear();

		calendar1.set(2011, 1, 1);
		Date date1 = calendar1.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(2011, 1, 1);
		Date date2 = calendar2.getTime();

		getDatePairsBetweenDates(date1, date2);
	}
}
