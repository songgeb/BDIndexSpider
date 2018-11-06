package com.bdindex.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import com.bdindex.exception.IndexNeedBuyException;
import com.bdindex.exception.IndexNotInServiceException;
import com.bdindex.model.Model;
import com.bdindex.ui.MyTableModel;
import com.bdindex.ui.UIUpdateModel;
import com.bdindex.ui.Util;
import com.selenium.BDIndexAction;
import com.selenium.BDIndexJSExecutor;
import com.selenium.BDIndexUtil;
import com.selenium.Constant;
import com.selenium.Wait;

public class BDIndexCoreWorker extends SwingWorker<Void, UIUpdateModel> {
	private static SimpleDateFormat logDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat imgNameDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static Logger logger = Logger.getLogger(BDIndexCoreWorker.class);
	private DriverService service;
	private WebDriver webdriver;
	private MyTableModel tableModel;
	private ArrayList<AbstractButton> buttons;
	private JTextArea textArea;

	@SuppressWarnings("unused")
	private BDIndexCoreWorker() {
	}

	public BDIndexCoreWorker(MyTableModel myTableModel,
			ArrayList<AbstractButton> buttons, JTextArea textArea) {
		tableModel = myTableModel;
		this.buttons = buttons;
		this.textArea = textArea;
	}

	private void init() throws Exception {
		File ddd = BDIndexUtil.getDriverFileFromJar();
		service = new ChromeDriverService.Builder()
				.usingDriverExecutable(ddd)
				.usingAnyFreePort().build();
		service.start();
		webdriver = new RemoteWebDriver(service.getUrl(),
				new ChromeOptions());
		// 第一次发送请求
		webdriver.get(Constant.url);
		// 处理错误页面
		BDIndexUtil.handleErrorPageBeforeLogin(webdriver, 3);
		// 激活浏览器窗口,将浏览器窗口置顶,并不是真正要截图
//		((TakesScreenshot) webdriver).getScreenshotAs(OutputType.BYTES);
		// 最大化窗口
		BDIndexAction.maximizeBrowser(webdriver);
		// 处理错误页面
		BDIndexUtil.handleErrorPageBeforeLogin(webdriver, 3);
		// 登录
		BDIndexAction.login(webdriver, service, 3);
		BDIndexUtil.handleErrorPage(webdriver);
	}

	/**
	 * 输入关键词进行搜索
	 * 
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */
	private void submitKeyword(String keyword)
			throws Exception {
		// 处理错误页
		BDIndexUtil.handleErrorPage(webdriver);
		// 输入关键字搜索
		BDIndexAction.searchKeyword(webdriver, keyword);
		// 处理关键词需购买的情况
		BDIndexUtil.checkBuyIndexPage(webdriver, service, keyword);
		// 处理关键词不提供服务的情况
		BDIndexUtil.indexNotInServiceCheck(webdriver, service, keyword);
	}

	/**
	 * 精确抓取百度指数
	 */
	private void accurateBDIndex(Model model, String cityID)
			throws Exception {
		//分割时间
		ArrayList<Date[]> list = Util.getDatePairsBetweenDates(model.getStartDate(),
				model.getEndDate());
		String outputFilePath = BDIndexUtil.getOutputFilePath(model);
		File file = new File(outputFilePath);
		if (file.exists()) { file.delete(); }
		
		Calendar tmpCalendar = Calendar.getInstance();
		
		for (int i = 0; i < list.size(); i++) {
			//此处为快速抓取百度指数代码
			//list.get(i)[0]--startDate
			//list.get(i)[1]--endDate
			Date subStartDate = list.get(i)[0];
			Date subEndDate = list.get(i)[1];
			String data = BDIndexJSExecutor.requestTrendIndex(webdriver, model.getKeyword(), cityID, subStartDate, subEndDate);
			//extract uniqid
			String uniqid = data.split("\\*sb\\*")[1];
			String tData = BDIndexJSExecutor.requestPtbk(webdriver, uniqid);
			String aData = data.split("\\*sb\\*")[0];
			//t=t, e=a
			String result = BDIndexJSExecutor.decrypt(webdriver, aData, tData);
			String[] indexes = result.split(",");
			String content = "";
			for (int j = 0; j < indexes.length; j++) {
				tmpCalendar.clear();
				tmpCalendar.setTime(subStartDate);
				tmpCalendar.add(Calendar.DAY_OF_MONTH, j);
				String dateString = imgNameDateFormat.format(tmpCalendar.getTime());
				content += dateString + " " + indexes[j] + Constant.newLineString;
			}
			Util.writeToFile(outputFilePath, content, true);
		}
	}

	private void start() {
		// 防御式编程
		ArrayList<Model> models = tableModel.getValues();
		if (models.size() < 1) {
			logger.warn("关键词数据源0条");
			return;
		}
		// 开始
		publish(new UIUpdateModel(getTextAreaContent(null,
				Constant.Status.Spider_Start), false));
		// 初始化
		try {
			init();
			//输入关键词
			submitKeyword("nba");
			Wait.waitForLoad(webdriver);
		} catch (Exception e) {
			publish(new UIUpdateModel(getTextAreaContent(null,
					Constant.Status.Spider_InitException), true));
			logger.error(Constant.Status.Spider_InitException, e);
			BDIndexUtil.closeSession(webdriver, service);
			return;
		}
		
		Wait.waitForLoad(webdriver);
		// 执行过程
		UIUpdateModel updateModel = null;
		long startTime = 0;
		for (Model model : models) {
			startTime = System.currentTimeMillis();
			model.setStatus(Constant.Status.Model_Start);
			publish(new UIUpdateModel(getTextAreaContent(model.getKeyword(),
					Constant.Status.Model_Start), false));
			try {
				//check city
				String cityID = "0";//0表示全国
				if (model.getCity() == null) {
					if (model.getProvince() != null) {
						cityID = AreaUtil.getProvinceId(model.getProvince());
					}
				} else {
					cityID = AreaUtil.getCityIdByName(model.getCity());
				}
				
				if (cityID == null) {
					model.setStatus(Constant.Status.Model_City_Error);
					updateModel = new UIUpdateModel(getTextAreaContent(model.getKeyword(),
							Constant.Status.Model_City_Error), false);
					continue;
				}
				
				switch (Constant.currentMode) {
				case Estimate:
					break;
				case Accurate:
					accurateBDIndex(model, cityID);
					break;
				default:
					return;
				}
				model.setStatus(Constant.Status.Model_End);
				updateModel = new UIUpdateModel(getTextAreaContent(
						model.getKeyword(), Constant.Status.Model_End), false);
				//稍等一下，降低请求频率
				Thread.sleep(1000);
			} catch (IndexNeedBuyException e) {
				model.setStatus(Constant.Status.Model_IndexNeedBuyException);
				updateModel = new UIUpdateModel(getTextAreaContent(
						model.getKeyword(),
						Constant.Status.Model_IndexNeedBuyException), false);
			} catch (IndexNotInServiceException e) {
				model.setStatus(Constant.Status.Model_IndexNotInServiceException);
				updateModel = new UIUpdateModel(getTextAreaContent(
						model.getKeyword(),
						Constant.Status.Model_IndexNotInServiceException),
						false);
			} catch (Exception e) {
				model.setStatus(Constant.Status.Model_Exception);
				updateModel = new UIUpdateModel(getTextAreaContent(
						model.getKeyword(), Constant.Status.Model_Exception),
						false);
				logger.error(model.getKeyword(), e);
			} finally {
				model.setTime((System.currentTimeMillis() - startTime));
				publish(updateModel);
			}
		}
		publish(new UIUpdateModel(getTextAreaContent(null,
				Constant.Status.Spider_End), true));
		BDIndexUtil.closeSession(webdriver, service);
	}

	/**
	 * 日志日期
	 * 
	 * @return
	 */
	private String logDateString() {
		return "【" + logDateFormat.format(new Date()) + "】";
	}

	/**
	 * 用于在textArea显示的内容
	 * 
	 * @param keyword
	 * @param status
	 * @return
	 */
	private String getTextAreaContent(String keyword, String status) {
		if (keyword == null || keyword.equals("")) {
			return status + logDateString() + "\n";
		}
		return "【" + keyword + "】" + status + logDateString() + "\n";
	}

	@Override
	protected Void doInBackground() throws Exception {
		start();
		return null;
	}

	@Override
	protected void process(List<UIUpdateModel> chunks) {
		super.process(chunks);
		for (int i = 0; i < chunks.size(); i++) {
			UIUpdateModel model = chunks.get(i);
			//model must not be null
			textArea.append(model.getTextAreaContent());
			Util.setButtonsStatus(buttons, model.isButtonEnable());
			tableModel.fireTableDataChanged();
		}
	}
}