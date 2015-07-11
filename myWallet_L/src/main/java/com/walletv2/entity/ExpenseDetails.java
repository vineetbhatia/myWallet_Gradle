package com.walletv2.entity;

public class ExpenseDetails {
	private String payersList = ""; //used as original payee list for wallet expenses
	private String payeesList = "";
	private String amount = "";
	private String date = "";
	private String description = "";
	private String eventName = ""; // used as original amount for wallet expenses
	
	public String getEventName() {
		return eventName;
	}
	
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public String getPayersList() {
		return payersList;
	}
	
	public void setPayersList(String payerList) {
		this.payersList = payerList;
	}
	
	public void setPayeesList(String payeeList) {
		this.payeesList = payeeList;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getPayeesList() {
		return payeesList;
	}
	
	public String getAmount() {
		return amount;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return "\nAmount: " + amount + "\nDate: " + Utils.getFormattedDateFromMillies(date)
				+ "\nDescription: " + description + "\nEvent Name: " + eventName + "\nPayeesList: " + payeesList
				+ "\nPayersList: " + payersList;
	}
}
