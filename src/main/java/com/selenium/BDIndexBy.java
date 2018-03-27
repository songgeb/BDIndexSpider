package com.selenium;

import org.openqa.selenium.By;

public class BDIndexBy {
	
	private BDIndexBy(){}

	private static String idOfLoginDialog = "TANGRAM_6__";
	private static String idOfUsernameTag = "TANGRAM_12__userName";
	private static String idOfPasswordTag = "TANGRAM_12__password";
	private static String idOfVerifyCodeTag = "TANGRAM_12__verifyCode";
	private static String idOfSearchTextField = "schword";
	private static String idOfBDIndexBlackBox = "viewbox";//整个黑框,包括日期、关键字和指数
	private static String xpathOfBDIndexRect = "//div[@id='trend']/*[name()='svg']/*[name()='rect'][2]";
	private static String xpathOfBDIndexSvg = "//div[@id='trend']/*[name()='svg']";
	private static String xpathOfBDIndexBlackBox = "//*[@id='trendPopTab']/tbody/tr/td[3]";//只包含指数的黑框区域
	private static String xpathOfBDIndexNum = "//*[@id='trendPopTab']//*[@class='imgtxt']";
	private static String xpathOfBDIndexNumStyle = "//*[@id='trendPopTab']/tbody/tr/td[3]/style";
	
	private static String xpathOfTimeSelection = "//*[@class='selectA rangeDate']";
	private static String xpathOfStartYear = "//*[@id='auto_gsid_17']/div[1]/span[2]/span[1]";
	private static String xpathOfStartMonth = "//*[@id='auto_gsid_17']/div[1]/span[2]/span[2]";
	private static String xpathOfEndYear = "//*[@id='auto_gsid_17']/div[2]/span[2]/span[1]";
	private static String xpathOfEndMonth = "//*[@id='auto_gsid_17']/div[2]/span[2]/span[2]";
	private static String xpathOfSubmitTime = "//*[@class='button ml20' and @value='确定']";
	
	private static String xpathOfCurrentTime = "//*[@id='viewbox']/div[1]/div[1]";
	
	//登录部分
	public static By loginDialog = By.id(idOfLoginDialog);
	public static By username = By.id(idOfUsernameTag);
	public static By password = By.id(idOfPasswordTag);
	public static By verifycode = By.id(idOfVerifyCodeTag);
	public static By searchTextField = By.id(idOfSearchTextField);
	
	//数据区域
	public static By bdIndexSvg = By.xpath(xpathOfBDIndexSvg);
	public static By bdIndexRect = By.xpath(xpathOfBDIndexRect);
	public static By bdindexBlackBox = By.id(idOfBDIndexBlackBox);
	public static By bdIndexNumEleInBlackBox = By.xpath(xpathOfBDIndexBlackBox);
	public static By bdIndexNumTxt = By.xpath(xpathOfBDIndexNum);
	public static By bdIndexNumStyle = By.xpath(xpathOfBDIndexNumStyle); 
	
	//自定义时间段
	public static By bdindexTimeSelection = By.xpath(xpathOfTimeSelection);
	public static By bdindexStartYear = By.xpath(xpathOfStartYear);
	public static By bdindexStartMonth = By.xpath(xpathOfStartMonth);
	public static By bdindexEndYear = By.xpath(xpathOfEndYear);
	public static By bdindexEndMonth = By.xpath(xpathOfEndMonth);
	public static By bdindexSubmitTime = By.xpath(xpathOfSubmitTime);
	
	public static By bdindexStartYearClick(String startYear) {
		return By.xpath(xpathOfStartYear + "//*[@class='sltOpt']//*[text()='"+startYear+"年']");
	}
	public static By bdindexStartMothClick(String startMonth) {
		return By.xpath(xpathOfStartMonth + "//*[@class='sltOpt']//a[contains(text(),'"+startMonth+"')]");
	}
	public static By bdindexEndYearClick(String endYear) {
		return By.xpath(xpathOfEndYear + "//*[@class='sltOpt']//*[text()='"+endYear+"年']");
	}
	public static By bdindexEndMonthClick(String endMonth) {
		return By.xpath(xpathOfEndMonth + "//*[@class='sltOpt']//a[contains(text(),'"+endMonth+"')]");
	}
	
	//曲线抽取相关
	public static String idOfTrendYAxisValue = "trendYimg";
	public static By bdindexTrendYAxisValue = By.id(idOfTrendYAxisValue);
	
	//黑框时间
	public static By bdindexCurrentTime = By.xpath(xpathOfCurrentTime);
	
	//处理异常
	public static By bdindexBuyLink = By.linkText("立即购买");
	
}
