package com.bdindex.core;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import com.bdindex.exception.IndexNeedBuyException;
import com.bdindex.exception.IndexNotInServiceException;
import com.bdindex.model.Model;
import com.bdindex.ui.MyTableModel;
import com.bdindex.ui.UIUpdateModel;
import com.bdindex.ui.Util;
import com.selenium.BDIndexAction;
import com.selenium.BDIndexBy;
import com.selenium.BDIndexUtil;
import com.selenium.Constant;
import com.selenium.ScreenShot;

public class BDIndexCoreWorker extends SwingWorker<Void, UIUpdateModel> {
	private static SimpleDateFormat logDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat yearMonthDateFormat = new SimpleDateFormat(
			"yyyyMM");
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
		service = new ChromeDriverService.Builder()
				.usingDriverExecutable(BDIndexUtil.getDriverFileFromJar())
				.usingAnyFreePort().build();
		service.start();
		webdriver = new RemoteWebDriver(service.getUrl(),
				DesiredCapabilities.chrome());
		// 第一次发送请求
		webdriver.get(Constant.url);
		// 处理错误页面
		BDIndexUtil.handleErrorPageBeforeLogin(webdriver, 3);
		// 激活浏览器窗口,将浏览器窗口置顶,并不是真正要截图
		((TakesScreenshot) webdriver).getScreenshotAs(OutputType.BYTES);
		// 最大化窗口
		BDIndexAction.maximizeBrowser(webdriver);
		// 处理错误页面
		BDIndexUtil.handleErrorPageBeforeLogin(webdriver, 3);
		// 登录
		BDIndexAction.login(webdriver, service, 3);
		BDIndexUtil.handleErrorPage(webdriver);
	}

	/**
	 * 推算百度指数
	 * 
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */
	private void estimateBDIndex(String keyword, Date startDate, Date endDate)
			throws Exception {
		ArrayList<Date[]> list = Util.getDatePairsBetweenDates(startDate,
				endDate);
		String outputDir = BDIndexUtil
				.getOutputDir(keyword, startDate, endDate);
		String outputBDIndexFilePath = BDIndexUtil.getBDIndexDataFilePath(
				keyword, startDate, endDate);
		for (int i = 0; i < list.size(); i++) {
			estimatedAction(keyword, list.get(i)[0], list.get(i)[1], outputDir,
					outputBDIndexFilePath);
		}
	}

	/**
	 * 推算百度指数核心操作
	 * 
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */
	private void estimatedAction(String keyword, Date startDate, Date endDate,
			String outputDir, String outputBDIndexFilePath) throws Exception {
		// 这很重要
		BDIndexUtil.setStartDate(startDate);
		BDIndexUtil.setEndDate(endDate);
		BDIndexUtil.setCurrentKeyword(keyword);
		submitKeyword(keyword, startDate, endDate);
		// 找到trend/svg/rect区域
		WebElement rectElement = null;
		BDIndexAction.retryCustomizeDate(webdriver, startDate, endDate, 3);
		BDIndexUtil.retryWaitRectElement(webdriver, service, 5, 4);

		rectElement = webdriver.findElement(BDIndexBy.bdIndexRect);
		// 使操作区域进入视野范围
		Point pointInViewport = ((Locatable) rectElement).getCoordinates()
				.inViewPort();
		// 将鼠标移动到截图内容区域以外
		int mouseY = BDIndexUtil.getMouseY(webdriver, rectElement,
				pointInViewport);
		// 鼠标移动事件
		Robot robot = new Robot();
		// 鼠标点击用以激活鼠标所在的浏览器窗口
		robot.mouseMove(pointInViewport.x - 20, mouseY);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		// 截图
		String trendFilename = getTrendFileName(keyword, startDate, endDate);
		ScreenShot.captureTrendPicForEstimatedMode((TakesScreenshot) webdriver,
				rectElement, trendFilename, outputDir);
		long days = BDIndexUtil.getDaysFromURL(webdriver.getCurrentUrl());
		// 截取纵向刻度值
		String yAxisFilename = getYAxisFileName(startDate, endDate);
		BDIndexEstimateUtil.extractYAxisValue(webdriver, service,
				yAxisFilename, outputDir);
		// 计算估算值
		int bdindexs[] = BDIndexEstimateUtil.doEstimatedValue(outputDir
				+ yAxisFilename, outputDir + trendFilename, days);
		BDIndexEstimateUtil.writeEstimateBDIndexToFile(startDate, bdindexs,
				outputBDIndexFilePath);
	}

	/**
	 * 输入关键词进行搜索
	 * 
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 * @throws Exception
	 */
	private void submitKeyword(String keyword, Date startDate, Date endDate)
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
	private void accurateBDIndex(String keyword, Date startDate, Date endDate)
			throws Exception {
		ArrayList<Date[]> list = Util.getDatePairsBetweenDates(startDate,
				endDate);
		String outputDir = BDIndexUtil
				.getOutputDir(keyword, startDate, endDate);
		for (int i = 0; i < list.size(); i++) {
			accurateAction(keyword, list.get(i)[0], list.get(i)[1], outputDir);
		}
		OCRUtil.doOCR(outputDir, outputDir);
	}

	/**
	 * 精确抓取核心操作
	 * 
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 */
	private void accurateAction(String keyword, Date startDate, Date endDate,
			String outputDir) throws Exception {
		// 这很重要
		BDIndexUtil.setStartDate(startDate);
		BDIndexUtil.setEndDate(endDate);
		BDIndexUtil.setCurrentKeyword(keyword);
		submitKeyword(keyword, startDate, endDate);
		// 找到trend/svg/rect区域
		WebElement rectElement = null;
		BDIndexAction.retryCustomizeDate(webdriver, startDate, endDate, 3);
		BDIndexUtil.retryWaitRectElement(webdriver, service, 5, 4);
		rectElement = webdriver.findElement(BDIndexBy.bdIndexRect);
		// 使操作区域进入视野范围
		Point pointInViewport = ((Locatable) rectElement).getCoordinates()
				.inViewPort();
		// 计算鼠标应设置的Y值
		int mouseY = BDIndexUtil.getMouseY(webdriver, rectElement,
				pointInViewport);
		// 计算鼠标X轴单步移动距离(读取当前url的时间参数,计算步长,默认30天)
		String currentURLString = webdriver.getCurrentUrl();
		long days = BDIndexUtil.getDaysFromURL(currentURLString);
		int rectWidth = rectElement.getSize().width;
		float step = (rectWidth * 1.0f) / (days - 1);
		// 鼠标移动事件
		Robot robot = new Robot();
		// 一边移动鼠标一边截图
		// 鼠标点击用以激活鼠标所在的浏览器窗口
		robot.mouseMove(pointInViewport.x - 20, mouseY);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		Thread.sleep(500);
		// 开始移动
		for (int i = 0; i < days; i++) {
			int mouseX = BDIndexUtil.getMouseX(rectWidth, pointInViewport,
					days, step, i);
			robot.mouseMove(mouseX, mouseY);
			Thread.sleep(500);
			try {
				// 第一层过滤:1. 特殊节点如重要事件 2. 页面自动刷新 3. 黑框不出现
				BDIndexUtil.retryWaitBlackBoxDisplay(webdriver, service, robot,
						mouseX, mouseY, step, i, 5, 4);
				// 保留当前时间
				BDIndexUtil.setCurrentDateString(BDIndexUtil
						.getCurrentDateString(webdriver, service, robot,
								mouseX, mouseY, step, i, 4, 4));
				// 第二层过滤: 1. 页面自动刷新 2. 数字不显示(不能保证百分之百)
				BDIndexUtil.retryWaitIndexNumElement(webdriver, service, robot,
						mouseX, mouseY, step, i, 5, 8);
				// 截图工作
				BDIndexUtil.retryScreenShot(webdriver, service, keyword, robot,
						mouseX, mouseY, step, i, 4, 4, outputDir);
			} catch (Exception e) {
				logger.error(keyword + " : 发生异常,跳过当前节点", e);
				continue;
			}
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
		} catch (Exception e) {
			publish(new UIUpdateModel(getTextAreaContent(null,
					Constant.Status.Spider_InitException), true));
			logger.error(Constant.Status.Spider_InitException, e);
			BDIndexUtil.closeSession(webdriver, service);
			return;
		}
		// 执行过程
		UIUpdateModel updateModel = null;
		long startTime = 0;
		for (Model model : models) {
			startTime = System.currentTimeMillis();
			model.setStatus(Constant.Status.Model_Start);
			publish(new UIUpdateModel(getTextAreaContent(model.getKeyword(),
					Constant.Status.Model_Start), false));
			try {
				switch (Constant.currentMode) {
				case Estimate:
					estimateBDIndex(model.getKeyword(), model.getStartDate(),
							model.getEndDate());
					break;
				case Accurate:
					accurateBDIndex(model.getKeyword(), model.getStartDate(),
							model.getEndDate());
					break;
				default:
					return;
				}
				model.setStatus(Constant.Status.Model_End);
				updateModel = new UIUpdateModel(getTextAreaContent(
						model.getKeyword(), Constant.Status.Model_End), false);
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
				// 删除当前关键词的结果文件,防止不必要麻烦
				BDIndexUtil.deleteIndexFile(model);
			} finally {
				model.setTime((System.currentTimeMillis() - startTime) / 1000);
				publish(updateModel);
				// 数据汇总统计
				BDIndexSummaryUtil.summary(model);
			}
			// 记录爬虫信息
			String spiderInfoFilePath = BDIndexUtil.getOutputDir(
					model.getKeyword(), model.getStartDate(),
					model.getEndDate())
					+ Constant.spiderinfoFilename;
			Util.writeSpiderInfoToFile(spiderInfoFilePath, model);
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
	 * 刻度图片文件名
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private String getYAxisFileName(Date startDate, Date endDate) {
		return yearMonthDateFormat.format(startDate) + "-"
				+ yearMonthDateFormat.format(endDate) + ".png";
	}

	/**
	 * 曲线图文件名
	 * 
	 * @param keyword
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private String getTrendFileName(String keyword, Date startDate, Date endDate) {
		return keyword + "(" + yearMonthDateFormat.format(startDate) + "-"
				+ yearMonthDateFormat.format(endDate) + ")" + ".png";
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
			System.out.println(model.getTextAreaContent());
			textArea.append(model.getTextAreaContent());
			Util.setButtonsStatus(buttons, model.isButtonEnable());
			tableModel.fireTableDataChanged();
		}
	}
}