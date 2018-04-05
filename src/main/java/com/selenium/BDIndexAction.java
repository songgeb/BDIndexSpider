package com.selenium;

import java.awt.Robot;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.service.DriverService;

import com.bdindex.exception.VerifyCodeException;

public class BDIndexAction {

	private static Logger logger = Logger.getLogger(BDIndexAction.class);

	public static String username = "";//input your username
	public static String password = "";//input your password

	private static SimpleDateFormat formattor = new SimpleDateFormat("yyyyMMdd");

	private static Random random = new Random(System.currentTimeMillis());

	/**
	 * 选择最近7天数据
	 */
	public static void clickCurrentSevenDaysData(WebDriver webdriver, Actions builder) {
		WebElement currElement = webdriver.findElement(By.linkText("7天"));
		builder.moveToElement(currElement).click().build().perform();
		Wait.waitForLoad(webdriver);
	}

	/**
	 * 自定义时间段获取数据
	 * @throws InterruptedException 
	 */
	public static void customizeDate(WebDriver webdriver, Date startDate, Date endDate) throws InterruptedException {
		//处理错误页
		Wait.waitForLoad(webdriver);
		BDIndexUtil.handleErrorPage(webdriver);
		
		//设置时间
		String url = webdriver.getCurrentUrl();
		if (url.contains("time=")) {
			return;
		}
		String newtimeUrl = url + "&time=" + formattor.format(startDate) + "|" + formattor.format(endDate);
		webdriver.get(newtimeUrl);
		Wait.waitForLoad(webdriver);
	}

	/**
	 * 加入重试机制的自定义时间段
	 * 
	 * @param webdriver
	 * @param startDate
	 * @param endDate
	 * @param retryCount
	 */
	public static void retryCustomizeDate(WebDriver webdriver, Date startDate, Date endDate, int retryCount) throws Exception {
		Exception e = null;
		while (retryCount > 0) {
			try {
				customizeDate(webdriver, startDate, endDate);
				break;
			} catch (Exception e2) {
				BDIndexAction.refreshPage(webdriver);
				e = e2;
				retryCount--;
			}
		}
		if (retryCount == 0) {
			logger.error(BDIndexUtil.getCurrenKeyword()+" : 设置日期失败" ,e);
			//记录截屏以备debug
			String filePath = Constant.logOutputDir + BDIndexUtil.getCurrenKeyword() + "("+formattor.format(startDate)+"-"+formattor.format(endDate)+")"+".png";
			ScreenShot.captureScreen((TakesScreenshot)webdriver, filePath);
			throw e;
		}
	}

	/**
	 * 输入关键词搜索
	 * 
	 * @param webdriver
	 */
	public static void searchKeyword(WebDriver webdriver, String keyword) {
		Wait.waitForLoad(webdriver);
		if (webdriver.getCurrentUrl().contains(Constant.unloadURL)) {
			webdriver.get(Constant.url);
			Wait.waitForLoad(webdriver);
		}
		webdriver.findElement(BDIndexBy.searchTextField).clear();
		webdriver.findElement(BDIndexBy.searchTextField).sendKeys(keyword);
		webdriver.findElement(BDIndexBy.searchTextField).submit();
		if (BDIndexUtil.isEnterErrorPage(webdriver)) {
			webdriver.navigate().back();
			webdriver.findElement(BDIndexBy.searchTextField).sendKeys(keyword);
			webdriver.findElement(BDIndexBy.searchTextField).submit();
		}
	}

	/**
	 * 执行登录 retryCount : 当登录出错导致登录框无法关闭时需要重试的次数
	 * 
	 * 
	 */
	public static void login(WebDriver webdriver, DriverService service, int retryCount) throws Exception {
		Exception e = null;
		while (retryCount > 0) {
			try {
				Wait.waitForElementVisible(webdriver, By.linkText("登录"), 8);
				webdriver.findElement(By.linkText("登录")).click();
				// 输入用户名密码
				Wait.waitForElementVisible(webdriver, BDIndexBy.username, 3);
				webdriver.findElement(BDIndexBy.username).sendKeys(username);
				webdriver.findElement(BDIndexBy.password).sendKeys(password);
				// 登录
				webdriver.findElement(BDIndexBy.username).submit();
				// 判断是否需要输入验证码
				WebElement verifyCodeElement = webdriver.findElement(BDIndexBy.verifycode);
				if (verifyCodeElement.isDisplayed()) {
					throw new VerifyCodeException();
				}
				Wait.waitForLoginDialogDispose(webdriver);
				break;
			} catch (VerifyCodeException e3) {
//				BDIndexUtil.closeSession(webdriver, service);
				logger.warn("需要输入验证码");
				try {
					Wait.waitForLoginDialogDispose(webdriver);
					break;
				} catch (Exception e2) {
					e = e2;
					retryCount --;
				}
			} catch (Exception e2) {
				e = e2;
				retryCount--;
				BDIndexAction.refreshPage(webdriver);
			}
		}
		if (retryCount == 0) {
			throw e;
		}
	}

	/**
	 * 配置Cookies
	 */
	public static WebDriver configCookie(WebDriver webdriver) {
		// 清空服务器Cookies
		webdriver.manage().deleteAllCookies();
		// 配置新Cookies
		Cookie cookie1 = new Cookie("BAEID", "3C3CA29081B76DABEE3447E825DBE543:FG=1");
		Cookie cookie2 = new Cookie("BAIDUID", "D1567126BF8B1EFF9732B04FB720F6A1:FG=1");
		Cookie cookie3 = new Cookie("BDUSS",
				"mlqbnlVQTR2MzJJRmNTdnFvUUltWWdHSklNaEJVazcyeXVFT2V1YzZ5R0dTMFZYQUFBQUFBJCQAAAAAAAAAAAEAAAC~gLmRc29uZ2dlYmEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIa-HVeGvh1XT");
		Cookie cookie4 = new Cookie("CHKFORREG", "194c5460e31c2ba154ac9bf9bed3dedd");
		Cookie cookie5 = new Cookie("Hm_lvt_d101ea4d2a5c67dab98251f0b5de24dc",
				"1459386463,1459671596,1460218410,1461305068");
		Cookie cookie6 = new Cookie("Hm_lpvt_d101ea4d2a5c67dab98251f0b5de24dc", "1461566817");
		webdriver.manage().addCookie(cookie1);
		webdriver.manage().addCookie(cookie2);
		webdriver.manage().addCookie(cookie3);
		webdriver.manage().addCookie(cookie4);
		webdriver.manage().addCookie(cookie5);
		webdriver.manage().addCookie(cookie6);
		return webdriver;
	}

	/**
	 * 最大化浏览器窗口
	 */
	public static void maximizeBrowser(WebDriver webdriver) {
		if (BDIndexUtil.isMacOS()) {
			// 最大化
			Long width = (Long) ((JavascriptExecutor) webdriver).executeScript("return window.screen.availWidth;");
			Long height = (Long) ((JavascriptExecutor) webdriver).executeScript("return window.screen.availHeight;");
			webdriver.manage().window().setPosition(new Point(0, 0));
			webdriver.manage().window().setSize(new Dimension(width.intValue(), height.intValue()));
		} else {
			webdriver.manage().window().maximize();
		}
	}

	/**
	 * 刷新页面并等待页面加载结束
	 */
	public static void refreshPage(WebDriver webdriver) {
		webdriver.navigate().refresh();
		Wait.waitForLoad(webdriver);
	}

	/**
	 * 躲避特殊节点
	 * 
	 * @param webdriver
	 * @param robot
	 * @param currentX
	 * @param currentY
	 * @param step
	 * @param dayIndex
	 * @throws Exception
	 */
	public static void adjustMouseToAvoidSpecialNode(WebDriver webdriver, Robot robot, int currentX, int currentY,
			float step, int dayIndex) throws Exception {
		long days = BDIndexUtil.getDaysFromURL(webdriver.getCurrentUrl());
		if (dayIndex == days - 1) {
			robot.mouseMove(Math.round(currentX - step), currentY);
		} else {
			robot.mouseMove(Math.round(currentX + step), currentY);
		}
		Thread.sleep(500);
		robot.mouseMove(currentX, currentY + (15 * random.nextInt(5)) + 20);// 因为鼠标位置比较高,所以加
	}
	
	/**
	 * 测试
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.setProperty("webdriver.chrome.driver", "drivers/chromedriver_mac");
		WebDriver chromeDriver = new ChromeDriver();
		chromeDriver.get("https://www.baidu.com");
//		String filePath = Constant.logOutputDir + BDIndexUtil.getCurrenKeyword() + "(2222)"+".png";
//		ScreenShot.captureScreen((TakesScreenshot)chromeDriver, filePath);
	}
}
