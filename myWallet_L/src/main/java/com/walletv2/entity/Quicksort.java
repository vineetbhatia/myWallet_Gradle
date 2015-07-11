package com.walletv2.entity;

import java.util.ArrayList;

public class Quicksort {
	private final int[] numbers;
	private final String[] names;
	private final int[] colors;

	public Quicksort(ArrayList<PayeeDetails> payeesList) {
		names = new String[payeesList.size()];
		numbers = new int[payeesList.size()];
		colors = new int[payeesList.size()];
		for (int i = 0; i < payeesList.size(); i++) {
			PayeeDetails payeeDetails = payeesList.get(i);
			names[i] = payeeDetails.getName();
			numbers[i] = Integer.parseInt(payeeDetails.getAmount());
			colors[i] = payeeDetails.getColorCode();
		}
	}

	public ArrayList<PayeeDetails> sort() {
		// Check for empty or null array
		int number = numbers.length;
		quicksort(0, number - 1);
		ArrayList<PayeeDetails> payeesList = new ArrayList<>();
		for (int i = 0; i < numbers.length; i++) {
			PayeeDetails payeeDetails = new PayeeDetails();
			payeeDetails.setName(names[i]);
			payeeDetails.setAmount("" + numbers[i]);
			payeeDetails.setColorCode(colors[i]);
			payeesList.add(payeeDetails);
		}
		return payeesList;
	}

	private void quicksort(int low, int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		int pivot = numbers[low + (high - low) / 2];
		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (numbers[i] > pivot) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (numbers[j] < pivot) {
				j--;
			}
			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j);
		if (i < high)
			quicksort(i, high);
	}

	private void exchange(int i, int j) {
		int temp = numbers[i];
		numbers[i] = numbers[j];
		numbers[j] = temp;
		String tempString = names[i];
		names[i] = names[j];
		names[j] = tempString;
	}
}
