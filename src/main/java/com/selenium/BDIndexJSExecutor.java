package com.selenium;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import com.bdindex.exception.IndexImgRequestFailException;

public class BDIndexJSExecutor {
	
	private static Logger logger = Logger.getLogger(BDIndexJSExecutor.class);
	
	/**
	 * 执行js代码
	 * @param webdriver
	 * @param jsCode
	 */
	public static void executeJS(WebDriver webdriver, String jsCode) {
		((JavascriptExecutor) webdriver)
		.executeScript(jsCode);
	}
	
	/**
	 * 请求百度指数图片
	 * @throws IndexImgRequestFailException 
	 */
	public static void requestBDIndexNumImg(WebDriver webdriver, String urlString) throws Exception {
		webdriver.manage().timeouts().setScriptTimeout(8, TimeUnit.SECONDS);
		
		Object response = ((JavascriptExecutor) webdriver).executeAsyncScript(
						"var callback = arguments[arguments.length - 1];" +
						"var xhr = new XMLHttpRequest();" +
						"xhr.open('GET', '"+urlString+"', true);" +
						"xhr.timeout = 6000;" +
						"xhr.responseType = 'blob';" +
						"xhr.onload = function() {" +
						"  if (xhr.status == 200) {" +
						"		var viewbox = document.getElementById('viewbox');" + 
						"		var imgtxts = viewbox.getElementsByClassName('imgtxt');" +
						"		var styleNode;" +
						" 		if (imgtxts != undefined) {"+
						"			styleNode = document.createElement('style');" +
						"			styleNode.innerHTML = '.view-value .imgval .imgtxt{background:url(\\'"+urlString+"\\')}';" +
						"			document.getElementsByClassName('view-value')[0].appendChild(styleNode);" +
						"		}" +
						"    	callback(true);" +
						"  	} else {" +
						"		callback(false);" +
						"	}" +
						"};" +
						"xhr.ontimeout = function() {"+ 
						"	callback(false)" +
						"};" +
						"xhr.send();", "function (args){ return args; }"
			  );
		if ((Boolean)response == false) {
			logger.warn("js请求图片失败,准备重试");
			throw new IndexImgRequestFailException();
		}
	}
	
	/**
	 * 该方法弃用,经测试无效
	 * 向html中注入监测刷新相关js代码
	 * @param webdriver
	 */
	public static void injectMonitorRefreshJS(WebDriver webdriver) {
//		String tmp = "document.onreadystatechange = function() { if(document.readyState = \\'complete\\') { console.warn(\\'complete\\'); } else { console.warn(\\'others\\'); }}";
//		String tmp = "window.onbeforeunload = function(){alert(\\'nihao\\'); if(event.clientX>document.body.clientWidth && event.clientY < 0 || event.altKey){}else{console.warn(\\'lalala\\');};";
		String uniqueTag = 
				"var script = document.createElement(\\'script\\');" +
				"script.setAttribute(\\'text\\', \\'songgeb\\');" +
				"document.body.appendChild(script);" +
				"console.warn(\\'append songgeb success!\\');";
		String tmp = "window.onbeforeunload = function(e){"+uniqueTag+"};";
		
		((JavascriptExecutor)webdriver).executeScript(
				"var script = document.createElement(\'script\');" +
				"script.setAttribute(\'type\', \'text\\/javascript\');" +
				"script.innerHTML = \'"+tmp+"\';" +
				"document.body.appendChild(script);"
//				"document.write('<script>document.onreadystatechange = function() { if(document.readyState = \\'complete\\') { console.warn(\\'complete\\'); } else { console.warn(\\'others\\'); }}</script\\>');"
				);
	}
	
}
