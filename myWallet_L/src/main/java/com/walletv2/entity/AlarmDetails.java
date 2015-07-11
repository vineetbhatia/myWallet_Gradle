package com.walletv2.entity;

import java.util.Calendar;

public class AlarmDetails {
	private String notificationTitle;
	private String notificationContent;
	private Calendar calender;
	private String expensePayee;
	
	public String getExpensePayee() {
		return expensePayee;
	}
	
	public void setExpensePayee(String expensePayee) {
		this.expensePayee = expensePayee;
	}
	
	public String getNotificationTitle() {
		return notificationTitle;
	}
	
	public void setNotificationTitle(String notificationTitle) {
		this.notificationTitle = notificationTitle;
	}
	
	public String getNotificationContent() {
		return notificationContent;
	}
	
	public void setNotificationContent(String notificationContent) {
		this.notificationContent = notificationContent;
	}
	
	public Calendar getCalender() {
		return calender;
	}
	
	public void setCalender(Calendar calender) {
		this.calender = calender;
	}
	
	@Override
	public String toString() {
		return "Notification Title: " + notificationTitle + " Notification Content: "
				+ notificationContent;
	}
}
