package com.selenium;

public class Constant {

	//url
	public static String url = "http://index.baidu.com";
	//错误处理
	public static String unloadURL = "data:,";
	
	//输出路径
	
	//数据输出路径
	private static String accurateModeOutputDir = "accuratePics/";
	private static String estimatedModeOutputDir = "estimatedPics/";
	public static String logOutputDir = "logs/";
	public static String spiderinfoFilename = "spiderinfo.txt";
	//汇总结果输出路径
	private static String summaryOutputDir = "结果集/";
	private static String accurateModeSummaryOutputDir = accurateModeOutputDir + summaryOutputDir;
	private static String estimatedModeSummaryOutputDir = estimatedModeOutputDir + summaryOutputDir;
	
	private static String validDataDir = "有效数据/";
	private static String notAvaliableDataFilename = "无法抓取数据.txt";
	private static String incompleteDataFilename = "未完整抓取数据.txt";
	private static String otherExceptionDataFilename = "抓取异常数据集.txt";
	
	private static String accurateValidDataDir = accurateModeSummaryOutputDir + validDataDir;
	private static String estimatedValidDataDir = estimatedModeSummaryOutputDir + validDataDir;
	private static String accurateNotAvaliableDataFilePath = accurateModeSummaryOutputDir + notAvaliableDataFilename;
	private static String estimatedNotAvaliableDataFilePath = estimatedModeSummaryOutputDir + notAvaliableDataFilename;
	private static String estimatedOEDataFilePath = estimatedModeSummaryOutputDir + otherExceptionDataFilename;
	private static String accurateOEDataFilePath = accurateModeSummaryOutputDir + otherExceptionDataFilename;
	public static String accurateInCompleteDataFilePath = accurateModeSummaryOutputDir + incompleteDataFilename;
	
	public static String getCurrentOutputDir() {
		String filePath = null;
		switch (Constant.currentMode) {
		case Accurate:
			filePath = accurateModeOutputDir;
			break;
		case Estimate:
			filePath = estimatedModeOutputDir;
			break;
		default:
			break;
		}
		return filePath;
	}
	
	public static String getCurrentNotAvaliableDataFilePath() {
		String filePath = null;
		switch (Constant.currentMode) {
		case Accurate:
			filePath = Constant.accurateNotAvaliableDataFilePath;
			break;
		case Estimate:
			filePath = Constant.estimatedNotAvaliableDataFilePath;
			break;
		default:
			break;
		}
		return filePath;
	}
	
	public static String getCurrentValidDataDir() {
		String filePath = null;
		switch (Constant.currentMode) {
		case Accurate:
			filePath = Constant.accurateValidDataDir;
			break;
		case Estimate:
			filePath = Constant.estimatedValidDataDir;
			break;
		default:
			break;
		}
		return filePath;
	}
	
	public static String getCurrentOtherExceptionFilePath() {
		String filePath = null;
		switch (Constant.currentMode) {
		case Accurate:
			filePath = Constant.accurateOEDataFilePath;
			break;
		case Estimate:
			filePath = Constant.estimatedOEDataFilePath;
			break;
		default:
			break;
		}
		return filePath;
	}
	
	//操作系统状态栏
	public static int MAC_TOP_BAR_HEIGHT = 22;//mac系统状态栏在顶部
	
	//兼容相关
	public static String newLineString = null;
	static {
		if (BDIndexUtil.isWindows()) {
			newLineString = "\r\n";
		} else if (BDIndexUtil.isMacOS()) {
			newLineString = "\r";
		} else {
			newLineString = "\n";
		}
	}
	
	//模式
	public enum ExecutionMode {
		Estimate,//已弃用
		Accurate
	}
	public static ExecutionMode currentMode;
	
	//异常判定条件
	public static String IndexNotInService = "不提供数据";
	public static String IndexNotBeCreated = "未被收录";
	
	//状态
	public static class Status {
		public static String Model_Start = "开始"; 
		public static String Model_End = "结束"; 
		public static String Model_City_Error = "地区不存在"; 
		
		public static String  Model_Exception = "发生异常"; 
		public static String  Model_IndexNeedBuyException = "数据服务需购买"; 
		public static String  Model_IndexNotInServiceException = "不提供数据服务"; 
		public static String  Spider_Exception = "发生异常"; 
		public static String  Spider_InitException = "初始化失败"; 
		
		public static String  Spider_Start = "爬虫启动"; 
		public static String  Spider_End = "爬虫结束"; 
	}
}
