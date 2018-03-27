package com.bdindex.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.selenium.Constant;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class OCRUtil {

	private static ITesseract ocrInstance;
	private static Logger logger = Logger.getLogger(OCRUtil.class);

	static {
		ArrayList<String> configs = new ArrayList<>();
		ocrInstance = new Tesseract();
		configs.add("digits");
		ocrInstance.setDatapath("./tessdata");
		ocrInstance.setConfigs(configs);
		ocrInstance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
		ocrInstance.setLanguage("bdindexlang");
	}

	public static void init() {
		InputStream in = OCRUtil.class.getResourceAsStream("/tessdata/configs/digits");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		try{
			while ((line = br.readLine()) != null) {
				logger.info("TESSDATA: " + line);
			}
		}catch(IOException e) {
			
		}
		
	}

	/**
	 * 进行OCR识别 结果文件为txt文本文件,文件名与目录名一致
	 * 
	 * @param inputDir
	 * @param outputDir
	 */
	public static void doOCR(String inputDir, String outputDir) {
		File dir = new File(inputDir);
		if (dir.exists() && !dir.isDirectory()) {
			logger.error("图像识别输入目录路径有误 : " + inputDir);
			return;
		}
		logger.info("OCR InputDIR: " + inputDir);
		logger.info("OCR OutputDIR: " + outputDir);
		// 开始识别
		File[] fileList = dir.listFiles();
		BufferedWriter writer = null;
		try {
			if (fileList.length > 0) {
				File result = new File(outputDir + dir.getName() + ".txt");
				if (result.exists()) {
					result.delete();
				}
				writer = new BufferedWriter(new FileWriter(result));
			}
			String text = null;
			String extension = null;
			for (File file : fileList) {
				logger.info("OCR PNG NAME: " + file.getName());
				int dot = file.getName().lastIndexOf('.');
				if ((dot > -1) && (dot < (file.getName().length() - 1))) {
					extension = file.getName().substring(dot + 1);
				}
				if (extension == null | !extension.equals("png")) {
					continue;
				}

				writer.write(file.getName().substring(0,
						file.getName().indexOf(".")));
				writer.write(" ");
				text = ocrInstance.doOCR(file).trim().replace(" ", "")
						.replace(",", "");
				logger.info("OCR PNG TEXT: " + text);
				writer.write(text);
				writer.write(Constant.newLineString);
				writer.flush();
			}
			writer.close();

			logger.warn("图像识别结束");
		} catch (Exception e) {
			logger.warn("图像识别发生异常");
		}
	}

	public static void main(String[] args) {
	}
}
