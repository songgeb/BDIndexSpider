package com.bdindex.model;

import java.util.Calendar;
import java.util.Date;

/**
 * 输入model
 **/
public class Model {
	//属性的顺序与txt reader读入文件时中的每个值对应
	protected String keyword;
	protected Date startDate;
	protected Date endDate;
	protected String province;
	protected String city;
	//以下属性来自程序运行过程，不来自txt输入文件
	protected long time;
	protected String status;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword.trim();
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {

		this.startDate = startDate;
	}

	public String getKeyword() {
		return keyword;
	}

	public Date getEndDate() {

		return endDate;
	}

	public void setEndDate(Date endDate) {
		 this.endDate = endDate;
	}

	/*
	 * 错误日期检查 
	 * 日期大于当前日期返回1
	 * startDate 大于endDate 返回2
	 * 如果结束日期大于昨天返回3
	 */
	public int checkDate() {
		Date currentDate = new Date();
		if (startDate.getTime() - currentDate.getTime() > 0
				| endDate.getTime() - currentDate.getTime() > 0) {
			return 1;
		}
		if(startDate.getTime() > endDate.getTime()) {
			return 2;
		}
		//结束日期不能是当前日期的前一天--这是百度指数的约束
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(currentDate);
		cal.add(Calendar.DAY_OF_MONTH, -1);//当前日期前一天
		Calendar yesterday = Calendar.getInstance();
		yesterday.clear();
		yesterday.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		yesterday.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		yesterday.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		if (endDate.getTime() > yesterday.getTime().getTime()) {
			return 3;
		}
		
		return 0;
	}

	public String toString() {
		if (this.startDate == null || this.endDate == null) {
			return "initialisation not finished!";
		}
		return "[keyword =" + keyword + "," + "startDate="
				+ startDate + "," + "endDate="
				+ endDate + "," + "time=" + time + ","
				+ "status=" + status + "]" + "\n";
	}

	public Model() {
		this.keyword = "";
		this.startDate = null;
		this.endDate = null;
		this.status = "未运行";
		this.time = 0;
		this.province = null;
		this.city = null;
	}

	public Model(String keyword, Date startDate, Date endDate) {
		this.keyword = keyword;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = "未运行";
		this.time = 0;
		this.province = null;
		this.city = null;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province.trim();
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city.trim();
	}
}
