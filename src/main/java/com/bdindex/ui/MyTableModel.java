package com.bdindex.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.bdindex.model.Model;

public class MyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private String[] names = {"关键词", "起始日期" ,"结束日期", "状态", "省份", "城市", "用时(秒)"};
	private ArrayList<Model> values = new ArrayList<Model>();

	public void setValues(ArrayList<Model> values) {
		this.values = values;
	}

	public ArrayList<Model> getValues() {
		return values;
	}

	@Override
	public int getRowCount() {
		return values.size();
	}

	@Override
	public int getColumnCount() {
		return names.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Model model = values.get(rowIndex);
		Object obj = null;
		switch (columnIndex) {
		case 0://关键词
			obj = model.getKeyword();
			break;
		case 1://起始时间
			obj = dateFormat.format(model.getStartDate());
			break;
		case 2://结束时间
			obj = dateFormat.format(model.getEndDate());
			break;
		case 3://状态
			obj = model.getStatus();
			break;
		case 4:
			obj = model.getProvince() == null ? "全国" : model.getProvince();
			break;
		case 5:
			obj = model.getCity() == null ? "所有地区" : model.getCity();
			break;
		case 6://用时
			obj = model.getTime();
			break;
		default:
			break;
		}
		return obj;
	}
	
	public String getColumnName(int col) {
		return names[col];
	}

}
