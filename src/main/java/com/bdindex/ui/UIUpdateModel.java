package com.bdindex.ui;

public class UIUpdateModel {
	private boolean buttonEnable;
	private String textAreaContent;
	
	@SuppressWarnings("unused")
	private UIUpdateModel() {
	}
	
	public UIUpdateModel(String textAreaContent, boolean buttonEnable) {
		this.textAreaContent = textAreaContent;
		this.buttonEnable = buttonEnable;
	}
	
	public boolean isButtonEnable() {
		return buttonEnable;
	}
	public void setButtonEnable(boolean buttonEnable) {
		this.buttonEnable = buttonEnable;
	}
	public String getTextAreaContent() {
		return this.textAreaContent == null ? "" : this.textAreaContent;
	}
	public void setTextAreaContent(String textAreaContent) {
		this.textAreaContent = textAreaContent;
	}
	
	
}
