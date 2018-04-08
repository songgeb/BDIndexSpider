package com.bdindex.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.selenium.BDIndexAction;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			LoginDialog dialog = new LoginDialog();
			dialog.setTitle("dddd");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public LoginDialog() {
		setBounds(100, 100, 332, 170);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 330, 120);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("用户名");
		lblNewLabel.setBounds(39, 27, 61, 16);
		contentPanel.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setBounds(97, 22, 130, 26);
		contentPanel.add(textField);
		textField.setColumns(10);
		{
			JLabel label = new JLabel("密码");
			label.setBounds(39, 55, 61, 16);
			contentPanel.add(label);
		}
		{
			textField_1 = new JTextField();
			textField_1.setColumns(10);
			textField_1.setBounds(97, 50, 130, 26);
			contentPanel.add(textField_1);
		}
		{
			final JButton okButton = new JButton("完成");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BDIndexAction.username = textField.getText();
					BDIndexAction.password = textField_1.getText();
					dispose();
				}
			});
			okButton.setBounds(120, 84, 88, 30);
			contentPanel.add(okButton);
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		
	}
}
