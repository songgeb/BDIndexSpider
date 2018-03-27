package com.bdindex.tool;

import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.bdindex.core.OCRUtil;

public class BatchOCRWorker extends SwingWorker<Boolean, Void> {

	private String inputDir;
	private String outputFilePath;
	private JButton button;
	
	public JButton getButton() {
		return button;
	}
	public void setButton(JButton button) {
		this.button = button;
	}

	private Logger logger = Logger.getLogger(BatchOCRWorker.class);
	
	public String getInputDir() {
		return inputDir;
	}
	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}
	public String getOutputFilePath() {
		return outputFilePath;
	}
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
	
	@SuppressWarnings("unused")
	private BatchOCRWorker() {}
	
	public BatchOCRWorker(String inputDir, String outputFilePath, JButton button) {
		this.inputDir = inputDir;
		this.outputFilePath = outputFilePath;
		this.button = button;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		OCRUtil.doOCR(inputDir, outputFilePath);
		return true;
	}
	
	@Override
	protected void done() {
		super.done();
		try {
			if (get()) {
				button.setEnabled(true);
				JOptionPane.showMessageDialog(button, "操作完成!");
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			logger.error("批量OCR错误",e);
			JOptionPane.showMessageDialog(button, "操作失败,请重试!");
		}
	}

}
