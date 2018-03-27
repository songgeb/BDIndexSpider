package com.bdindex.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.service.DriverService;

import com.selenium.BDIndexAction;
import com.selenium.BDIndexBy;
import com.selenium.BDIndexUtil;
import com.selenium.Constant;
import com.selenium.ScreenShot;
import com.selenium.Wait;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class BDIndexEstimateUtil {

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private static ITesseract ocrInstance = new Tesseract();
	private static Logger logger = Logger.getLogger(BDIndexEstimateUtil.class);
	static {
		ArrayList<String> configs = new ArrayList<>();
		configs.add("digits");
		ocrInstance.setConfigs(configs);
		ocrInstance.setDatapath("./tessdata");
		ocrInstance.setLanguage("yaxislang");
	}

	/**
	 * JS加载Y坐标刻度,将图片写入本地
	 * 
	 * @param webdriver
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 * @param retryCount
	 * @throws Exception
	 */
	public static void extractYAxisValue(WebDriver webdriver,
			DriverService service, String filename, String directory)
			throws Exception {
		retryWaitTrendYAxis(webdriver, service, 4);
		String trendYAxisValueURL = webdriver.findElement(
				BDIndexBy.bdindexTrendYAxisValue).getAttribute("src");
		webdriver.get(trendYAxisValueURL);
		Wait.waitForLoad(webdriver);
		WebElement trendYAxisElement = null;
		// 异常捕获,进行回退。避免后面的关键词发生异常
		try {
			trendYAxisElement = webdriver.findElement(By.tagName("img"));
		} catch (Exception e) {
			webdriver.navigate().back();
			Wait.waitForLoad(webdriver);
			throw e;
		}

		ScreenShot.captureYAxisPicForEstimatedMode((TakesScreenshot) webdriver,
				trendYAxisElement, filename, directory);
		webdriver.navigate().back();
	}

	/**
	 * 计算估算值
	 * 
	 * @param yAxisFilePath
	 * @param trendFilePath
	 * @throws Exception
	 */
	public static int[] doEstimatedValue(String yAxisFilePath,
			String trendFilePath, long days) throws Exception {
		File file = new File(yAxisFilePath);
		String[] result = ocrInstance.doOCR(file).trim().replace(",", "")
				.replace(" ", "").split("\n");
		// 过滤掉无用的换行符或空串
		ArrayList<String> array = new ArrayList<>();
		for (int i = 0; i < result.length; i++) {
			if (!result[i].trim().equals("")) {
				array.add(result[i]);
			}
		}
		BufferedImage image = ImageIO.read(new File(trendFilePath));
		long min = Long.parseLong(array.get(array.size() - 1))
				- (Long.parseLong(array.get(0)) - Long.parseLong(array.get(1)));
		long max = Long.parseLong(array.get(0));

		float step = image.getWidth() / ((days - 1) * 1.0f);
		float perPixelValue = ((max - min) * 1.0f) / image.getHeight();
		int[] bdindexs = new int[(int) days];

		int mouseX = 0;
		ArrayList<Integer> yArray = new ArrayList<>();
		for (int i = 0; i < days; i++) {
			mouseX = Math.min(Math.round(i * step), image.getWidth() - 1);

			for (int j = image.getHeight() - 1; j > -1; j--) {
				if (image.getRGB(mouseX, j) == Color.black.getRGB()) {
					yArray.add(j);

					if (!((j - 1) >= 0 && image.getRGB(mouseX, j - 1) == Color.black
							.getRGB())) {
						break;
					}
				}
			}
			// 按比例计算指数数据
			float bdindex = 0.f;
			if (yArray.size() == 0) {
				// 值和min接近导致曲线数据特征不明显
				bdindex = min * 1.0f;
			} else {
				// 计算均值
				// int sum = 0;
				// int actualLength = 0;
				// for (int j = 0; j < y.length; j++) {
				// if (y[j] == 0) {
				// actualLength = j;
				// break;
				// }
				// sum += y[j];
				// }
				// float avargeY = actualLength == 0 ? 0 : ((sum * 1.0f) /
				// actualLength);
				// bdindex = ((image.getHeight() - 1 - avargeY) * perPixelValue)
				// + min;

				// 选择最大的
				int minY = 0;
				minY = yArray.get(yArray.size() - 1);

				bdindex = ((image.getHeight() - minY) * perPixelValue) + min;
			}
			bdindexs[i] = Math.round(bdindex);
			// 及时清空yArray
			yArray.clear();

		}
		return bdindexs;

	}

	/**
	 * 将推算的百度指数与日期对应写入文件中
	 * 
	 * @param startDate
	 * @param bdindexs
	 * @param directory
	 * @param filename
	 * @throws Exception
	 */
	public static void writeEstimateBDIndexToFile(Date startDate,
			int[] bdindexs, String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 将日期中的day设置为1
		String lineString = "";
		for (int i = 0; i < bdindexs.length; i++) {
			lineString = format.format(calendar.getTime()) + " " + bdindexs[i];
			logger.info(lineString);
			writer.write(lineString);
			writer.write(Constant.newLineString);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		writer.close();
	}

	/**
	 * 给刻度值元素加入重试
	 */
	private static void retryWaitTrendYAxis(WebDriver webdriver,
			DriverService service, int retryCount) throws Exception {
		Exception e = null;
		while (retryCount > 0) {
			try {
				Wait.waitForElementPresence(webdriver,
						BDIndexBy.bdindexTrendYAxisValue, 6);
				break;
			} catch (Exception e2) {
				e = e2;
				retryCount--;
				BDIndexAction.refreshPage(webdriver);
				BDIndexUtil.retryWaitRectElement(webdriver, service, 5, 4);
				((Locatable) webdriver.findElement(BDIndexBy.bdIndexRect))
						.getCoordinates().inViewPort();
			}
		}
		if (retryCount == 0) {
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		// System.out.println(ocrInstance.doOCR(new File("3.png")));
		// String result[] = ocrInstance.doOCR(new
		// File("3.png")).trim().replace(",", "").replace(" ", "").split("\n");
		// System.out.println("处理前:" + result.length);
		// ArrayList<String> array = new ArrayList<>();
		// for (int i = 0; i < result.length; i++) {
		// if (!result[i].trim().equals("")) {
		// array.add(result[i]);
		// }
		// }
		// System.out.println("处理后:" + array.size());

		// System.out.println(result[1].length());
		System.out.println(ocrInstance.doOCR(new File(
				"estimatedPics/nba(20160325-20160524)/201603-201603.png")));
	}
}
