package com.selenium;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Locatable;
public  class ScreenShot {
	
	private static Logger logger = Logger.getLogger(ScreenShot.class);
	
	
	//getScreenshotAs方法按照以下的偏好顺序返回截图
	//Entire page
	//Current window
	//Visible portion of the current frame
	//The screenshot of the entire display containing the browser
	
	/**
	 * 截屏
	 * @param drivername
	 * @param filename
	 * @throws IOException
	 */
	public static void captureScreen(TakesScreenshot drivername, String filePath) throws IOException {
		File logScreen = new File(filePath);
		if (!logScreen.getParentFile().exists()) {
			logScreen.getParentFile().mkdirs();
		}
		
		ByteArrayInputStream by = new ByteArrayInputStream(drivername.getScreenshotAs(OutputType.BYTES));
		BufferedImage originImage = null;
		try {
			originImage = ImageIO.read(by);
			ImageIO.write(originImage, "png", logScreen);
		} catch (IOException e) {
			logger.error(filePath + " : 日志截图读写错误",e);
			throw e;
		}
	}
	
	/**
	 * 针对推算模式下趋势曲线的二值化
	 * @param image
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage binaryForTrendPicInEstimateModel(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);// 重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
		int r, g, b, top = 0;
		String str = "";
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				str = Integer.toHexString(image.getRGB(i, j));
				r = Integer.parseInt(str.substring(2, 4), 16);
				g = Integer.parseInt(str.substring(4, 6), 16);
				b = Integer.parseInt(str.substring(6, 8), 16);
				top = (r + g + b) / 3;
				if ((150 < top && top < 200) && r < 100) {
					binaryImage.setRGB(i, j, new Color(0, 0, 0).getRGB());
				} else {
					binaryImage.setRGB(i, j, new Color(255, 255, 255).getRGB());
				}
			}
		}
		return binaryImage;
	}
	
	/**
	 * 精确模式下图像采集过程
	 * @param drivername
	 * @param element
	 * @param filename
	 * @param directory
	 * @throws Exception
	 */
	public static void capturePicForAccurateMode(TakesScreenshot drivername
			, WebElement element
			, String filename
			, String directory) throws Exception {
		// 2. 截屏、裁剪
		BufferedImage image = screenShot(drivername, element, filename);
		// 3. 预处理
		image = binaryImage(image);
		image = zoomPicture(image, 2.0);
		// 4. 持久化
		savePic(image, directory, filename);
	}
	
	/**
	 * 推算模式下趋势曲线的图像采集
	 * @param drivername
	 * @param element
	 * @param filename
	 * @param directory
	 * @throws Exception
	 */
	public static void captureTrendPicForEstimatedMode(TakesScreenshot drivername
			, WebElement element
			, String filename
			, String directory) throws Exception {
		// 2. 截屏、裁剪
		BufferedImage image = screenShot(drivername, element, filename);
		// 3. 预处理
		image = binaryForTrendPicInEstimateModel(image);
		// 4. 持久化
		savePic(image, directory, filename);
	}
	
	/**
	 * 推算模式下刻度值图像采集
	 * @param drivername
	 * @param element
	 * @param filename
	 * @param directory
	 * @throws Exception
	 */
	public static void captureYAxisPicForEstimatedMode(TakesScreenshot drivername
			, WebElement element
			, String filename
			, String directory) throws Exception {
		// 2. 截屏、裁剪
		BufferedImage image = screenShot(drivername, element, filename);
		// 3. 预处理
		image = zoomPicture(image, 2.0f);
		// 4. 持久化
		savePic(image, directory, filename);
	}
	
	/**
	 * 持久化图片
	 * @param image
	 * @param directory
	 * @param filename
	 * @throws IOException
	 */
	private static void savePic(BufferedImage image, String directory, String filename) throws IOException {
		try {
			File dirFile = new File(directory);
			if(!dirFile.exists()) dirFile.mkdirs();
			ImageIO.write(image, "png", new File(directory + filename));
		} catch (IOException e) {
			logger.error(filename + " : 图片写入出错",e);
			throw e;
		}
	}
	
	
	private static BufferedImage screenShot(TakesScreenshot drivername
			, WebElement element, String filename) throws Exception{
		Point point = ((Locatable)element).getCoordinates().inViewPort();
		//截屏
		ByteArrayInputStream by = new ByteArrayInputStream(drivername.getScreenshotAs(OutputType.BYTES));
		BufferedImage originImage = null;
		try {
			originImage = ImageIO.read(by);
		} catch (IOException e) {
			logger.error(filename + " : 截图读入失败",e);
			throw e;
		}
		
		//裁剪
		long windowInnerHeight = (Long)((JavascriptExecutor)drivername)
				.executeScript("return window.innerHeight");
		long windowInnerWidth = (Long)((JavascriptExecutor)drivername)
				.executeScript("return window.innerWidth");
		float scaleY = (originImage.getHeight()*1.0f) 
				/ windowInnerHeight;
		float scaleX = (originImage.getWidth() * 1.0f) 
				/ windowInnerWidth;
		int x = Math.round(point.x * scaleX);
		int y = Math.round(point.y * scaleY);
		int width = Math.round(element.getSize().width * scaleX);
		int height = Math.round(element.getSize().height * scaleY);
		BufferedImage targetImage = originImage
				.getSubimage(x, y
						, Math.min(originImage.getWidth() - x, width)
						, Math.min(originImage.getHeight() - y, height));
		return targetImage;
	}
	
	/**
	 * 图片缩放
	 * @param inputFile
	 * @param outputPicName
	 *            文件全名，后缀为.jpg
	 * @param max
	 * @author wenc
	 */
	private static BufferedImage zoomPicture(BufferedImage image, double ratio) {
		try {
			int widthdist = (int) Math.floor(image.getWidth() * ratio),
					heightdist = (int) Math.floor(image.getHeight() * ratio);
			BufferedImage tag = new BufferedImage(widthdist, heightdist, BufferedImage.TYPE_INT_RGB);
			tag.getGraphics().drawImage(image.getScaledInstance(widthdist, heightdist, BufferedImage.SCALE_SMOOTH), 0, 0,
					null);
			return tag;
		} catch (Exception e) {
			logger.error("图片缩放失败");
			throw e;
		}
	}
	
	/**
	 * 普通二值化--突出黑白对比
	 * @param image
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage binaryImage(BufferedImage image) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);// 重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				binaryImage.setRGB(i, j, rgb);
			}
		}
		return binaryImage;
	}
	
	/**
	 * 灰度化
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static BufferedImage grayImage(BufferedImage image) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);// 重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		return grayImage;
	}
}