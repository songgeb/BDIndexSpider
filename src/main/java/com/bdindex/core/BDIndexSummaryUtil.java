package com.bdindex.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.bdindex.model.Model;
import com.bdindex.ui.Util;
import com.selenium.BDIndexUtil;
import com.selenium.Constant;
import com.selenium.Constant.ExecutionMode;

public class BDIndexSummaryUtil {
	
	private static Logger logger = Logger.getLogger(BDIndexSummaryUtil.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
	
	//1. 抽取有效数据
	//2. 产生无服务数据
	//3. 产生未完整抓取数据
	
	public static void summary(Model model) {
		
		//数据是否无法抓取
		if (isNotAvailable(model.getStatus())) {
			try {
				writeNotAvailableSummary(model);
			} catch (Exception e) {
				logger.error(model.getKeyword() + " : 写入无法抓取数据信息错误", e);
				return;
			}
			return;
		}
		
		//数据抓取是否发生异常
		if (isOtherException(model.getStatus())) {
			try {
				writeOtherExceptionIndexSummary(model);
			} catch (Exception e) {
				logger.error(model.getKeyword() + " : 写入其他抓取异常关键词时发生错误", e);
				return;
			}
			return;
		}
		
		//判断图片抓取是否完整(只针对精确模式)
		if (Constant.currentMode == ExecutionMode.Accurate && isDataIncomplete(model)) {
			try {
				writeIncompleteSummary(model);
			} catch (Exception e) {
				logger.error(model.getKeyword() + " : 写入未完整抓取信息错误", e);
				return;
			}
			return;
		}
		
		//抽取有效数据
		extractValidData(model);
	}
	
	/**
	 * 检查是否发生其他异常
	 * @param status
	 * @return
	 */
	private static boolean isOtherException (String status) {
		return status.equals(Constant.Status.Model_Exception) ? true : false;
	}
	
	/**
	 * 根据status判断数据是否提供服务
	 */
	private static boolean isNotAvailable(String status) {
		return (status.equals(Constant.Status.Model_IndexNeedBuyException) 
				|| status.equals(Constant.Status.Model_IndexNotInServiceException)) 
				? true : false;
	}
	
	/**
	 * 写入发生其他异常的关键词
	 * @param model
	 * @throws IOException 
	 */
	private static void writeOtherExceptionIndexSummary(Model model) throws IOException {
		String filePath = Constant.getCurrentOtherExceptionFilePath();
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(model.getKeyword());
		writer.write(" " + dateFormat.format(model.getStartDate()));
		writer.write(" " + dateFormat.format(model.getEndDate()));
		writer.write(Constant.newLineString);
		writer.close();
	}
	
	private static void writeNotAvailableSummary(Model model) throws Exception {
		String filePath = Constant.getCurrentNotAvaliableDataFilePath();
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(model.getKeyword() + " " + model.getStatus());
		writer.write(Constant.newLineString);
		writer.close();
	}
	
	/**
	 * 判断抓取数据是否完整
	 * @param model
	 * @return
	 */
	private static boolean isDataIncomplete(Model model) {
		long days = Util.daysBetweenTwoDates(model.getStartDate(), model.getEndDate());
		return days == picCountInAccurateMode(model) ? false : true;
	}
	
	private static int picCountInAccurateMode(Model model) {
		File dir = new File(BDIndexUtil.getOutputDir(model));
		int picCount = 0;
		for (File  subFile : dir.listFiles()) {
			if (subFile.getName().endsWith(".png")) picCount++;
		}
		return picCount;
	}
	
	private static void writeIncompleteSummary(Model model) throws Exception {
		String filePath = Constant.accurateInCompleteDataFilePath;
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(model.getKeyword());
		writer.write(Constant.newLineString);
		writer.close();
	}
	
	/**
	 * 
	 * @param model
	 */
	private static void extractValidData(Model model) {
		File inputDir = new File(Constant.getCurrentOutputDir());
		
		File[] subFiles = inputDir.listFiles();
		if (subFiles.length < 1) {
			System.out.println("根目录下没有文件");
			return;
		}
		
		File targetDir = null;
		String targetDirPath = Constant.getCurrentValidDataDir();
		targetDir = new File(targetDirPath);
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		
		for (File dir : subFiles) {
			if (dir.isDirectory()) {
				//抽取有效数据文件
				//判断条件很弱
				for (File file2 : dir.listFiles()) {
					if (file2.getName().equals(dir.getName()+".txt")) {
						copyFile2(file2, targetDir, file2.getName());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 复制文件(以超快的速度复制文件)
	 * 
	 * @param srcFile
	 *            源文件File
	 * @param destDir
	 *            目标目录File
	 * @param newFileName
	 *            新文件名
	 * @return 实际复制的字节数，如果文件、目录不存在、文件为null或者发生IO异常，返回-1
	 */
	@SuppressWarnings("resource")
	private static long copyFile2(File srcFile, File destDir, String newFileName) {
		long copySizes = 0;
		if (!srcFile.exists()) {
			System.out.println("源文件不存在");
			copySizes = -1;
		} else if (!destDir.exists()) {
			System.out.println("目标目录不存在");
			copySizes = -1;
		} else if (newFileName == null) {
			System.out.println("文件名为null");
			copySizes = -1;
		} else {
			FileChannel fcin = null;
			FileChannel fcout = null;
			try {
				fcin = new FileInputStream(srcFile).getChannel();
				fcout = new FileOutputStream(new File(destDir, newFileName)).getChannel();
				long size = fcin.size();
				fcin.transferTo(0, fcin.size(), fcout);
				copySizes = size;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fcin.close();
					fcout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return copySizes;
	}

}
