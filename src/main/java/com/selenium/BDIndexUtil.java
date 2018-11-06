package com.selenium;

import java.awt.Robot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.remote.service.DriverService;
import com.bdindex.exception.ErrorPageException;
import com.bdindex.exception.IndexNeedBuyException;
import com.bdindex.exception.IndexNotInServiceException;
import com.bdindex.exception.StyleDisplayNoneException;
import com.bdindex.model.Model;
import com.bdindex.ui.Util;

public class BDIndexUtil {
	private static Logger logger = Logger.getLogger(BDIndexAction.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMdd");
	// 暂未用到
	private static int pageY_viewportY = -1;
	// 鼠标纵向位置比例
	private static float scaleMouseY = 0.05f;
	// 配置驱动文件
	private static String driverDir = "drivers/";
	private static String driverFilePath = null;
	private static String osName = System.getProperty("os.name");
	private static String osArch = System.getProperty("os.arch");
	// 躲避特殊节点
	private static Random random = new Random(System.currentTimeMillis());
	// 错误页处理相关
	private static String errorpageURL = "http://www.baidu.com/search/error.html";
	private static Date startDate = null;
	private static Date endDate = null;
	private static String currentKeyword = "null";
	private static String currentDateString = "null";

	public static String getCurrentDateString() {
		return currentDateString;
	}

	public static void setCurrentDateString(String dateString) {
		currentDateString = dateString;
	}

	public static String getCurrenKeyword() {
		return currentKeyword;
	}

	public static void setCurrentKeyword(String keyword) {
		currentKeyword = keyword;
	}

	public static Date getStartDate() {
		return startDate;
	}

	public static void setStartDate(Date date) {
		startDate = date;
	}

	public static Date getEndDate() {
		return endDate;
	}

	public static void setEndDate(Date date) {
		endDate = date;
	}

	private static// ip范围
	int[][] range = { { 607649792, 608174079 }, // 36.56.0.0-36.63.255.255
			{ 1038614528, 1039007743 }, // 61.232.0.0-61.237.255.255
			{ 1783627776, 1784676351 }, // 106.80.0.0-106.95.255.255
			{ 2035023872, 2035154943 }, // 121.76.0.0-121.77.255.255
			{ 2078801920, 2079064063 }, // 123.232.0.0-123.235.255.255
			{ -1950089216, -1948778497 }, // 139.196.0.0-139.215.255.255
			{ -1425539072, -1425014785 }, // 171.8.0.0-171.15.255.255
			{ -1236271104, -1235419137 }, // 182.80.0.0-182.92.255.255
			{ -770113536, -768606209 }, // 210.25.0.0-210.47.255.255
			{ -569376768, -564133889 }, // 222.16.0.0-222.95.255.255
	};
	static {
		if (isWindows()) {
			driverFilePath = (driverDir + "chromedriver.exe");
		} else if (isMacOS()) {
			driverFilePath = (driverDir + "chromedriver_mac");
		} else if (isLinux()) {
			if (osArch.contains("x86") || osArch.contains("i386")) {
				driverFilePath = (driverDir + "chromedriver_32");
			} else {
				driverFilePath = (driverDir + "chromedriver_64");
			}
		}
	}

	/**
	 * 系统类型相关
	 */
	public static boolean isWindows() {
		return osName.contains("Windows");
	}

	public static boolean isLinux() {
		return osName.contains("Linux");
	}

	public static boolean isMacOS() {
		return osName.contains("Mac OS");
	}

	public static File getDriverFileFromJar() throws Exception {
		try {
			logger.info("DriverFilePath: " + driverFilePath);
			File outputFile = new File(driverFilePath);
			if (outputFile.exists()) {
				logger.info("Driver folder exist");
				outputFile.setExecutable(true);
				return outputFile;
			}
			logger.info("Driver Path: " + driverFilePath);
			InputStream is = BDIndexUtil.class.getResourceAsStream("/"
					+ driverFilePath);
			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			// 写的过程可以去掉，直接用maven打包时完成
			outputFile.createNewFile();
			OutputStream os = new FileOutputStream(outputFile.getAbsolutePath());
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			is.close();
			outputFile.setExecutable(true);
			return outputFile;
		} catch (Exception e) {
			logger.error("DriverFilePath: " + driverFilePath);
			logger.error("jar中抽取driver文件出错", e);
			throw e;
		}
	}

	/**
	 * 获取数据输出路径
	 * 
	 * @param mode
	 * @param keyword
	 * @return
	 */
	public static String getOutputFilePath(Model model) {
		return Constant.getCurrentOutputDir()
				+ getOutputFileName(model) + ".txt";
	}

	private static String getOutputFileName(Model model) {
		String area = model.getCity() == null ? (model.getProvince() == null ? "全国" : model.getProvince()) : model.getCity();
		if (area.length() > 0) {
			area = "-"+area+"-";
		}
		return model.getKeyword() + area + "(" + dateFormat.format(model.getStartDate()) + "-"
				+ dateFormat.format(model.getEndDate()) + ")";
	}
	
	public static int differentDays(Date date1,Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
       int day1= cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);
        
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if(year1 != year2)   //同一年
        {
            int timeDistance = 0 ;
            for(int i = year1 ; i < year2 ; i ++)
            {
                if(i%4==0 && i%100!=0 || i%400==0)    //闰年            
                {
                    timeDistance += 366;
                }
                else    //不是闰年
                {
                    timeDistance += 365;
                }
            }
            
            return timeDistance + (day2-day1) ;
        }
        else    //不同年
        {
            return day2-day1;
        }
    }

	/**
	 * 提取URL中时间区域,计算出两个日期间相差天数 time=12;表示7天 time=13;表示30天
	 * 
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	public static long getDaysFromURL(String url) throws ParseException,
			UnsupportedEncodingException {
		String regex = "&time=(\\w+%\\w+)|&time=(\\d+\\|\\d+)|&time=(\\d+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(url);
		long days = 30;// default
		if (matcher.find()) {
			if (matcher.group(1) != null | matcher.group(2) != null) {
				String result = matcher.group(1) == null ? matcher.group(2)
						: matcher.group(1);
				String date[];
				Date date1 = null, date2 = null;
				try {
					date = URLDecoder.decode(result, "UTF-8").split("\\|");
					date1 = dateFormat.parse(date[0]);
					date2 = dateFormat.parse(date[1]);
				} catch (UnsupportedEncodingException e) {
					logger.error(getCurrenKeyword() + " : 通过URL进行日期格式化出错", e);
					throw e;
				} catch (ParseException e) {
					logger.error(getCurrenKeyword() + " : 通过URL进行日期格式化出错", e);
					throw e;
				}
				days = Util.daysBetweenTwoDates(date1, date2);
			} else {
				String result = matcher.group(3);
				days = result.equals("12") ? 7 : (result.equals("13") ? 30 : 0);
			}
		}
		return days;
	}

	/**
	 * 获取当前正在抓取数据的日期
	 * 
	 * @param webdriver
	 * @return
	 * @throws Exception
	 */
	public static String getCurrentDateString(WebDriver webdriver,
			DriverService service, Robot robot, int mouseX, int mouseY,
			float step, int dayIndex, long waitTimeInSeconds, int retryCount)
			throws Exception {
		Exception e = null;
		String timeString = null;
		while (retryCount > 0) {
			try {
				Wait.waitForElementPresence(webdriver,
						BDIndexBy.bdindexCurrentTime, 5);
				timeString = webdriver
						.findElement(BDIndexBy.bdindexCurrentTime).getText();
				break;
			} catch (Exception e2) {
				logger.warn(getCurrenKeyword() + " : 获取日期出现异常,正在重试...", e2);
				Wait.waitForLoad(webdriver);
				retryWaitRectElement(webdriver, service, 3, 3);
				BDIndexAction.adjustMouseToAvoidSpecialNode(webdriver, robot,
						mouseX, mouseY, step, dayIndex);
				retryWaitBlackBoxDisplay(webdriver, service, robot, mouseX,
						mouseY, step, dayIndex, waitTimeInSeconds, retryCount);
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error(getCurrenKeyword() + " : 获取日期,重试失败", e);
			throw e;
		}
		return timeString.substring(0, 10);
	}

	/**
	 * 对登录前进入错误页的情况处理
	 * 
	 * @param webdriver
	 * @param retryCount
	 * @throws ErrorPageException
	 */
	public static void handleErrorPageBeforeLogin(WebDriver webdriver,
			int retryCount) throws ErrorPageException {
		Wait.waitForLoad(webdriver);
		while (retryCount > 0) {
			if (!BDIndexUtil.isEnterErrorPage(webdriver)) {
				break;
			}
			logger.error("进入错误页");
			webdriver.navigate().back();
			webdriver.get(Constant.url);
			retryCount--;
		}
		if (retryCount == 0) {
			throw new ErrorPageException();
		}
	}

	/**
	 * 检查是否出现错误页，如果有则返回后退
	 * 
	 * @param webdriver
	 */
	public static void handleErrorPage(WebDriver webdriver) {
		Wait.waitForLoad(webdriver);
		if (BDIndexUtil.isEnterErrorPage(webdriver)) {
			logger.error("进入错误页面");
			webdriver.navigate().back();
			BDIndexAction.refreshPage(webdriver);
		}
		Wait.waitForLoad(webdriver);
	}

	/**
	 * 计算鼠标每次移动的X坐标值
	 */
	public static int getMouseX(int rectWidth, Point pointInViewport,
			long days, float step, int i) {
		int x = 0;
		if (i == 0) {
			x = pointInViewport.x + 2;
		} else if (i == days - 1) {
			x = (int) (pointInViewport.x + rectWidth - 2);
		} else {
			x = Math.round(pointInViewport.x + (step * i) + 2);
		}
		return x;
	}

	public static int getMouseY(WebDriver webdriver, WebElement rectElement,
			Point pointInViewport) {
		long browserToolbarHeight = (Long) ((JavascriptExecutor) webdriver)
				.executeScript("return window.outerHeight-window.innerHeight");
		float y = 0.0f;
		if (isMacOS()) {
			//mac系统中，浏览器的地址栏和工具栏等高度以及顶部状态栏
			y = pointInViewport.y + browserToolbarHeight
					+ Constant.MAC_TOP_BAR_HEIGHT
					+ (rectElement.getSize().height * scaleMouseY);
		} else {
			//Windows系统无顶部状态，但其他linux系统还未验证
			y = pointInViewport.y + browserToolbarHeight
					+ (rectElement.getSize().height * scaleMouseY);
		}
		int mouseY = Math.round(y);
		return mouseY;
	}

	public static void closeSession(WebDriver webdriver, DriverService service) {
		webdriver.quit();
		if (service.isRunning()) {
			service.stop();
		}
	}

	/**
	 * rect区域等待+重试
	 * 
	 * @param webdriver
	 * @param service
	 * @param by
	 * @param waitTimeInSeconds
	 * @param retryCount
	 * @throws Exception
	 */
	public static void retryWaitRectElement(WebDriver webdriver,
			DriverService service, long waitTimeInSeconds, int retryCount)
			throws Exception {
		Exception e = null;
		while (retryCount > 0) {
			try {
				Wait.waitForElementPresence(webdriver, BDIndexBy.bdIndexRect,
						waitTimeInSeconds);
				break;
			} catch (Exception e2) {
				logger.warn(getCurrenKeyword() + " : 找不到rect区域,正在重试...", e2);
				// 处理进入错误页面情况
				if (isEnterErrorPage(webdriver)) {
					webdriver.navigate().back();
					Wait.waitForLoad(webdriver);
				} else {
					BDIndexAction.refreshPage(webdriver);
				}
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error(getCurrenKeyword() + " : 找不到rect区域,重试失败", e);
			throw e;
		}
	}

	/**
	 * 等待整个黑框显示出来 主要功能是防止遇到重要事件的节点,这些节点会妨碍黑框显示 黑框不出来主要两种情况:1. 遇到特殊节点,本来就不显示黑框.
	 * 2. 页面自动刷新,这时需要刷新页面了. 对于情况1来说,解决办法就是移动鼠标寻找合适位置. 3. 就是不显示,这种情况也是有可能的
	 * 
	 * @param webdriver
	 * @param service
	 * @param by
	 * @param robot
	 * @param mouseX
	 * @param mouseY
	 * @param waitTimeInSeconds
	 * @param retryCount
	 * @throws Exception
	 */
	public static void retryWaitBlackBoxDisplay(WebDriver webdriver,
			DriverService service, Robot robot, int mouseX, int mouseY,
			float step, int dayIndex, long waitTimeInSeconds, int retryCount)
			throws Exception {

		Exception e = null;
		while (retryCount > 0) {
			try {
				Wait.waitForElementPresence(webdriver,
						BDIndexBy.bdindexBlackBox, waitTimeInSeconds);
				if (!webdriver.findElement(BDIndexBy.bdindexBlackBox)
						.isDisplayed()) {
					throw new StyleDisplayNoneException();
				}
				break;
			} catch (StyleDisplayNoneException e1) {
				logger.warn(getCurrenKeyword() + " : 找不到黑框区域,正在重试...", e1);
				// 此处之所以要刷新页面是应对页面数据缓存过期的情况
				BDIndexAction.refreshPage(webdriver);
				retryWaitRectElement(webdriver, service, 3, 3);
				BDIndexAction.adjustMouseToAvoidSpecialNode(webdriver, robot,
						mouseX, mouseY, step, dayIndex);
				e = e1;
				retryCount--;
			} catch (Exception e2) {
				logger.warn(getCurrenKeyword() + " : 找不到黑框区域,正在重试...", e2);
				Wait.waitForLoad(webdriver);
				retryWaitRectElement(webdriver, service, 3, 3);
				BDIndexAction.adjustMouseToAvoidSpecialNode(webdriver, robot,
						mouseX, mouseY, step, dayIndex);
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error(getCurrenKeyword() + " : 找不到黑框区域,重试失败", e);
			throw e;
		}
	}

	/**
	 * 等待指数区域出现 并不能完全确保指数显示出来,只能确定指数区域的参数(如width是否已经设置)
	 * 
	 * @param webdriver
	 * @param service
	 * @param by
	 * @param robot
	 * @param mouseX
	 * @param mouseY
	 * @param waitTimeInSeconds
	 * @param retryCount
	 * @throws Exception
	 */
	public static void retryWaitIndexNumElement(WebDriver webdriver,
			DriverService service, Robot robot, int mouseX, int mouseY,
			float step, int dayIndex, int waitTimeInSeconds, int retryCount)
			throws Exception {
		Exception e = null;
		while (retryCount > 0) {
			try {
				Wait.waitForElementPresence(webdriver, BDIndexBy.bdIndexNumTxt,
						waitTimeInSeconds);
				WebElement element = webdriver
						.findElement(BDIndexBy.bdIndexNumTxt);
				BDIndexJSExecutor.requestBDIndexNumImg(webdriver,
						getURLStringFromStyleText(element
								.getCssValue("background")));
				break;
			} catch (Exception e2) {
				logger.warn(getCurrenKeyword() + "--" + currentDateString
						+ " : 数字未显示,正在重试...", e2);
				Wait.waitForLoad(webdriver);
				retryWaitRectElement(webdriver, service, 3, 3);
				BDIndexAction.adjustMouseToAvoidSpecialNode(webdriver, robot,
						mouseX, mouseY, step, dayIndex);
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error(getCurrenKeyword() + "--" + currentDateString
					+ " : 找不到指数区域,重试失败", e);
			// closeSession(webdriver, service);
			throw e;
		}
	}

	/**
	 * 等待黑框部分是否更新,防止stale element not attached 异常
	 * 
	 * @param webdriver
	 * @param service
	 * @param by
	 */
	public static void retryWaitElementChanged(WebDriver webdriver,
			DriverService service, WebElement element, By by, int retryCount)
			throws Exception {
		Exception e = null;
		while (retryCount > 0) {
			try {
				// 不是获取真正位置,只是为了验证element是否attached在DOM上
				element.getLocation();
				break;
			} catch (Exception e2) {
				logger.warn("元素还未更新,正在重试...", e2);
				Thread.sleep(500);
				element = webdriver.findElement(by);
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error("元素已删除仍未更新成功,重试失败", e);
			throw e;
		}
	}

	/**
	 * 检查是否进入错误页面
	 */
	public static boolean isEnterErrorPage(WebDriver webdriver) {
		return webdriver.getCurrentUrl().trim().equals(errorpageURL) ? true
				: false;
	}

	/**
	 * 检查当前所查指数是否需要购买
	 * 
	 * @param webdriver
	 * @param service
	 * @param by
	 * @param retryCount
	 * @throws Exception
	 */
	public static void checkBuyIndexPage(WebDriver webdriver,
			DriverService service, String keyword) throws IndexNeedBuyException {
		Wait.waitForLoad(webdriver);
		if (webdriver.findElements(BDIndexBy.bdindexBuyLink).size() > 0) {
			logger.warn(keyword + " : 所查关键词百度指数需要购买");
			throw new IndexNeedBuyException();
		}
	}

	/**
	 * 检查百度指数不提供查词服务的关键词
	 * 
	 * @param webdriver
	 * @param service
	 * @param keyword
	 * @throws IndexNotInServiceException
	 */
	public static void indexNotInServiceCheck(WebDriver webdriver,
			DriverService service, String keyword)
			throws IndexNotInServiceException {
		Wait.waitForLoad(webdriver);
		if (webdriver.getPageSource().contains(Constant.IndexNotInService)) {
			logger.warn(keyword + " : 百度指数不提供对该关键词的查询服务");
			throw new IndexNotInServiceException();
		}
	}

	/**
	 * 截屏操作的重试实现
	 * 
	 * @param webdriver
	 * @param service
	 * @param keyword
	 * @param robot
	 * @param mouseX
	 * @param mouseY
	 * @param step
	 * @param dayIndex
	 * @param waitTimeInSeconds
	 * @param retryCount
	 */
	public static void retryScreenShot(WebDriver webdriver,
			DriverService service, String keyword, Robot robot, int mouseX,
			int mouseY, float step, int dayIndex, int waitTimeInSeconds,
			int retryCount, String outputDir) throws Exception {
		WebElement numEleInBlackBox = null;
		Exception e = null;
		while (retryCount > 0) {
			try {
				numEleInBlackBox = webdriver
						.findElement(BDIndexBy.bdIndexNumEleInBlackBox);
				Thread.sleep(1000);
				ScreenShot.capturePicForAccurateMode(
						(TakesScreenshot) webdriver, numEleInBlackBox,
						currentDateString + ".png", outputDir);
				break;
			} catch (Exception e2) {
				logger.warn(getCurrenKeyword() + "--" + getCurrentDateString()
						+ " : 截图出现异常,正在重试...", e2);
				Wait.waitForLoad(webdriver);
				retryWaitRectElement(webdriver, service, 4, 4);
				BDIndexAction.adjustMouseToAvoidSpecialNode(webdriver, robot,
						mouseX, mouseY, step, dayIndex);
				Thread.sleep(500);
				BDIndexUtil.retryWaitBlackBoxDisplay(webdriver, service, robot,
						mouseX, mouseY, step, dayIndex, 5, 4);
				BDIndexUtil.retryWaitIndexNumElement(webdriver, service, robot,
						mouseX, mouseY, step, dayIndex, 5, 8);
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error(getCurrenKeyword() + "--" + getCurrentDateString()
					+ " : 截图异常重试失败", e);
			// closeSession(webdriver, service);
			throw e;
		}
	}

	/**
	 * 从style值中提取出图片的URL
	 * 
	 * @param styleText
	 * @return
	 */
	public static String getURLStringFromStyleText(String styleText) {
		// styleText格式:rgba(x,x,x,x) url(http://xxxxx)xxxx
		String tmp = styleText.split("url")[1].split("\\)")[0];
		return tmp.substring(2, tmp.length() - 1);
	}

	/*
	 * 随机生成国内IP地址
	 */
	public static String getRandomIp() {
		int index = random.nextInt(10);
		return num2ip(range[index][0]
				+ new Random().nextInt(range[index][1] - range[index][0]));
	}

	/*
	 * 将十进制转换成ip地址
	 */
	private static String num2ip(int ip) {
		int[] b = new int[4];
		String x = "";
		b[0] = (int) ((ip >> 24) & 0xff);
		b[1] = (int) ((ip >> 16) & 0xff);
		b[2] = (int) ((ip >> 8) & 0xff);
		b[3] = (int) (ip & 0xff);
		x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "."
				+ Integer.toString(b[2]) + "." + Integer.toString(b[3]);
		return x;
	}

	/**
	 * 该方法暂时未用到 返回相对于浏览器窗口(不包含标题栏和地址栏)的坐标(X坐标不变,默认横向滚动条不移动)
	 * 由于coordinates的inViewPort方法会让屏幕滚动,造成一些不便,故重定义此方法。
	 * 原理:根据在coordinates的onPage和inViewport方法获取两种相对坐标,计算差值,可以确定viewPort使页面滚动的距离
	 * 
	 * 重要:此方法需要本类的静态变量支持,静态变量需要提前设置,因为需要调用inViewport方法,
	 */
	public static Point getLocationInViewport(WebElement element) {
		if (pageY_viewportY < 0)
			return null;
		int newY = ((Locatable) element).getCoordinates().onPage().y
				- pageY_viewportY;
		return new Point(((Locatable) element).getCoordinates().onPage().x,
				newY);
	}

	public static void scrollEleToViewport(WebElement element) {
		Point pointInPage = ((Locatable) element).getCoordinates().onPage();
		Point pointInViewport = ((Locatable) element).getCoordinates()
				.inViewPort();
		pageY_viewportY = pointInPage.y - pointInViewport.y;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(new URL("jar!:"
				+ BDIndexUtil.class.getResource(driverDir + "chromedriver.exe")
						.getFile()));
	}
}