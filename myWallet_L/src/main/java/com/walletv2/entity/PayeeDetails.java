package com.walletv2.entity;

public class PayeeDetails {
	private String name = "";
	private String amount = "";
	private int colorCode;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAmount() {
		return amount;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public int getColorCode() {
		return colorCode;
	}

	public void setColorCode(int colorCode) {
		this.colorCode = colorCode;
	}

	@Override
	public String toString() {
		return name + "\t" + amount;
	}
}
