package com.bdindex.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 输入model
 **/
public class Model {
	protected String keyword;
	protected Date startDate;
	protected Date endDate;
	protected long time;
	protected String status;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy/MM/dd");

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
		this.keyword = keyword;
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
		// this.endDate = endDate;
		Date date = endDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DAY_OF_MONTH, days - 1);
		date = calendar.getTime();
		this.endDate = date;

	}

	/*
	 * 错误日期检查 
	 * 日期大于当前日期返回1
	 * startDate 大于endDate 返回2
	 * 
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
		return 0;
	}

	public String toString() {
		return "[keyword =" + keyword + "," + "startDate="
				+ dateFormat.format(startDate) + "," + "endDate="
				+ dateFormat.format(endDate) + "," + "time=" + time + ","
				+ "status=" + status + "]" + "\n";
	}

	public Model() {
		this.keyword = "";
		this.startDate = null;
		this.endDate = null;
		this.status = "未运行";
		this.time = 0;
	}

	public Model(String keyword, Date startDate, Date endDate) {
		this.keyword = keyword;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = "未运行";
		this.time = 0;
	}
}
