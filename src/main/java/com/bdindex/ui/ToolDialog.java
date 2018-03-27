package com.bdindex.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.bdindex.tool.BatchOCRWorker;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;

public class ToolDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;
	
	private String inputDir = null;
	public String getInputDir() {
		return inputDir;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	private String outputDir = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ToolDialog dialog = new ToolDialog();
			dialog.setTitle("抽取数据文件");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ToolDialog() {
		setBounds(100, 100, 332, 144);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 330, 120);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("输入目录");
		lblNewLabel.setBounds(39, 27, 61, 16);
		contentPanel.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(97, 22, 130, 26);
		contentPanel.add(textField);
		textField.setColumns(10);
		{
			JLabel label = new JLabel("输出目录");
			label.setBounds(39, 55, 61, 16);
			contentPanel.add(label);
		}
		{
			textField_1 = new JTextField();
			textField_1.setEditable(false);
			textField_1.setColumns(10);
			textField_1.setBounds(97, 50, 130, 26);
			contentPanel.add(textField_1);
		}
		{
			final JButton okButton = new JButton("开始");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					okButton.setEnabled(false);
					new BatchOCRWorker(getInputDir(), getOutputDir(), okButton).execute();
				}
			});
			okButton.setBounds(120, 84, 88, 30);
			contentPanel.add(okButton);
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		
		JButton button = new JButton("选择");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle("选择输入路径");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = chooser.showOpenDialog(contentPanel);
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setInputDir(file.getAbsolutePath()+"/");
					textField.setText(getInputDir());
				}
			}
		});
		button.setBounds(223, 22, 88, 29);
		contentPanel.add(button);
		
		JButton button_1 = new JButton("选择");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle("选择输出路径");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = chooser.showOpenDialog(contentPanel);
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setOutputDir(file.getAbsolutePath()+"/");
					textField_1.setText(getOutputDir());
				}
			}
		});
		button_1.setBounds(223, 50, 88, 29);
		contentPanel.add(button_1);
	}
}
