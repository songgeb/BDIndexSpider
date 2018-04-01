package com.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Wait {
	
	
	private static int TIMEOUT_PAGE_LOAD = 10;
	private static int TIMEOUT_LOGINDIALOG_DISPOSE = 10;
	
	/**
	 * 等待页面加载的条件
	 */
	private static ExpectedCondition<Boolean> pageLoad = new ExpectedCondition<Boolean>() {
		@Override
		public Boolean apply(WebDriver driver) {
			return
			((JavascriptExecutor)driver)
			.executeScript("return document.readyState;")
			.equals("complete");
		}
	};
	
	/**
	 * 判断登录框是否消失的条件
	 */
	private static ExpectedCondition<Boolean> loginDialogDispose = new ExpectedCondition<Boolean>() {
		
		@Override
		public Boolean apply(WebDriver driver) {
			List<WebElement> list = driver.findElements(BDIndexBy.loginDialog);
			if (list.size() > 0 && list.get(0).isDisplayed()) {
				return false;
			}
			return true;
		}
	};
	
	
	/**
	 * 等待页面加载完毕
	 * @param driver
	 */
	public static void waitForLoad(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT_PAGE_LOAD);
		wait.until(pageLoad);
	}
	
	/**
	 * 等待登录框消失
	 */
	public static void waitForLoginDialogDispose(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT_LOGINDIALOG_DISPOSE);
		wait.until(loginDialogDispose);
	}
	
	/**
	 * 等待by路径的webelement在html中出现但并不一定显示出来
	 * @param timeout / seconds
	 */
	public static void waitForElementPresence(WebDriver webdriver, By by, long timeout) {
		WebDriverWait wait = new WebDriverWait(webdriver, timeout);
		wait.until(ExpectedConditions.presenceOfElementLocated(by));
	}
	
	/**
	 * 等待元素显示
	 * @param webdriver
	 * @param by
	 * @param timeout
	 */
	public static void waitForElementVisible(WebDriver webdriver, By by, long timeout) {
		WebDriverWait wait = new WebDriverWait(webdriver, timeout);
		wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	
	public static void waitForElementClickable(WebDriver webdriver, By by, long timeout) {
		WebDriverWait wait = new WebDriverWait(webdriver, timeout);
		wait.until(ExpectedConditions.elementToBeClickable(by));
	}
}
