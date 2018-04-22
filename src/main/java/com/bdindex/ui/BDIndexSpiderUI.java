package com.bdindex.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import com.bdindex.core.BDIndexCoreWorker;
import com.bdindex.exception.DocumentFormatException;
import com.bdindex.exception.ModelDateException;
import com.bdindex.model.Model;
import com.bdindex.model.ModelGenerator;
import com.selenium.Constant;
import com.selenium.Constant.ExecutionMode;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenuBar;
import javax.swing.JMenu;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BDIndexSpiderUI {

	private static Logger logger = Logger.getLogger(BDIndexSpiderUI.class);
	// private static SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yyyy-MM");
	private JFrame frame;
	private JTable table_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BDIndexSpiderUI window = new BDIndexSpiderUI();
					window.frame.setVisible(true);
					window.frame.setResizable(false);
					window.frame.setTitle("百度指数Spider");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BDIndexSpiderUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 617, 429);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(0, 22, 611, 48);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.LIGHT_GRAY);
		menuBar.setBounds(0, 0, 611, 22);
		frame.getContentPane().add(menuBar);

		JMenu menu = new JMenu("初始化");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("从文件导入");
		menuItem.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				int result = chooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					// 截取文件后缀名
					int dot = file.getName().lastIndexOf(".");
					String fileType = file.getName().substring(dot + 1);
					// getModels
					ArrayList<Model> values;
					try {
						values = new ModelGenerator().get(fileType, file);
					} catch (IllegalArgumentException e2) {
						JOptionPane.showMessageDialog(frame,
								"关键词文件第 " + e2.getMessage()
										+ " 行格式有误.\n正确格式:关键词 起始时间 结束时间");
						return;
					} catch (DocumentFormatException e2) {
						JOptionPane.showMessageDialog(frame,
								"第 " + e2.getMessage()
										+ " 行日期格式有问题.\n正确格式:1990/01/01");
						return;
					} catch (ModelDateException e2) {
						JOptionPane.showMessageDialog(frame, e2.getMessage());
						return;
					} catch(Exception e2) {
						JOptionPane.showMessageDialog(frame, "error: " + e2.getClass().getName());
						return ;
					}

					try {
						if (values.size() > 0) {
							((MyTableModel) table_1.getModel())
									.setValues(values);
							((MyTableModel) table_1.getModel())
									.fireTableDataChanged();
						}
					} catch (Exception e1) {
						logger.error(e1);
					}
				}
			}
		});
		menu.add(menuItem);
		
		JMenuItem loginItem = new JMenuItem("配置账户");
		loginItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				LoginDialog dialog = new LoginDialog();
				dialog.setTitle("配置账户");
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		});
		menu.add(loginItem);

		JMenu mnNewMenu = new JMenu("工具");
		menuBar.add(mnNewMenu);

		JMenuItem mntmocr = new JMenuItem("批量OCR");
		mntmocr.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				ToolDialog dialog = new ToolDialog();
				dialog.setTitle("批量OCR");
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		});
		mnNewMenu.add(mntmocr);

		JMenu menu_1 = new JMenu("帮助");
		menuBar.add(menu_1);

		JMenuItem menuItem_2 = new JMenuItem("使用说明");
		menuItem_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JOptionPane.showMessageDialog(frame,
						"启动爬虫后会在jar文件相同目录下产生几个文件夹.\n"
								+ "estimatedPics目录: 用于存放推算模式下的抓取数据.\n"
								+ "accuratePics目录: 用于存放精确模式下的抓取数据.\n"
								+ "        logs目录: 用于记录软件运行日志.\n"
								+ "    drivers目录: 用于存放软件运行必要的驱动文件.\n");
			}
		});
		menu_1.add(menuItem_2);

		JMenuItem menuItem_1 = new JMenuItem("关于");
		menuItem_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JOptionPane.showMessageDialog(frame,
						"本软件由songgeb开发,用于科研工作,版权所有,侵权必究.\nQQ:肆壹玖壹伍壹叁叁零");
			}
		});
		menu_1.add(menuItem_1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 77, 601, 183);
		frame.getContentPane().add(scrollPane);

		table_1 = new JTable(new MyTableModel());
		table_1.setBackground(Color.WHITE);
		scrollPane.setViewportView(table_1);

		final JTextArea textArea = new JTextArea();
		textArea.setBackground(new Color(255, 255, 255));
		textArea.setEditable(false);

		JScrollPane scrollPane_1 = new JScrollPane(textArea);
		scrollPane_1.setBounds(10, 264, 601, 137);
		frame.getContentPane().add(scrollPane_1);

		final JRadioButton radioButton = new JRadioButton("曲线推算");
		radioButton.setActionCommand("estimate");
		radioButton.setBounds(6, 13, 84, 23);
		radioButton.setEnabled(false);
		panel.add(radioButton);

		final JRadioButton radioButton_1 = new JRadioButton("精确抓取");
		radioButton_1.setActionCommand("accurate");
		radioButton_1.setSelected(true);
		radioButton_1.setBounds(104, 13, 84, 23);
		panel.add(radioButton_1);

		ActionListener radioBtnListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("estimate")) {
				} else if (e.getActionCommand().equals("accurate")) {
				}
			}
		};
		radioButton.addActionListener(radioBtnListener);
		radioButton_1.addActionListener(radioBtnListener);

		final ButtonGroup group = new ButtonGroup();
		group.add(radioButton);
		group.add(radioButton_1);

		final JButton button_1 = new JButton("启动");
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 关键词处理
				ArrayList<Model> models = ((MyTableModel) table_1.getModel())
						.getValues();
				if (models.size() < 1) {
					JOptionPane.showMessageDialog(frame, "请添加关键词");
					return;
				}

				// 启动爬虫
				String selectedActionCommand = group.getSelection()
						.getActionCommand();
				ArrayList<AbstractButton> buttons = new ArrayList<>();
				buttons.add(button_1);
				buttons.add(radioButton_1);
				buttons.add(radioButton);

				textArea.setText("");
				if (selectedActionCommand.equals("estimate")) {
					Constant.currentMode = ExecutionMode.Estimate;
				} else if (selectedActionCommand.equals("accurate")) {
					Constant.currentMode = ExecutionMode.Accurate;
				}
				new BDIndexCoreWorker((MyTableModel) table_1.getModel(),
						buttons, textArea).execute();
			}
		});
		button_1.setBounds(544, 7, 61, 29);
		panel.add(button_1);
	}
}
