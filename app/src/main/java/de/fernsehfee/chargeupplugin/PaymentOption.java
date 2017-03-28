package de.fernsehfee.chargeupplugin;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple POJO for transmitting options for payment back to Charge Up
 */
public class PaymentOption {
	private int mAmount;
	@NonNull
	private String mTitle;
	@NonNull
	private String mDescription;

	/**
	 * Create a new option for payment
	 * @param amount         The Euro index for how much this will cost. Can be 1-20. The index is
	 *                       representative of number of Euros / 5. So if you want to allow for a
	 *                       charge of 60 euros, you would enter 12 in this field.
	 * @param title	         The title should be short and to the point. For example: "100 Points".
	 * @param description	 the description should let the user know what it is <b>exactly</b>
	 *                       that they will get.
	 */
	public PaymentOption(@IntRange(from = 1, to = 20) int amount, @NonNull String title, @NonNull String description) {
		this.mAmount = amount;
		this.mTitle = title;
		this.mDescription = description;
	}

	/**
	 * Turn this POJO into a JSON object for transmitting to Charge Up
	 * @return A JSON representation of this payment option
	 */
	@NonNull
	JSONObject asJson() {
		JSONObject item = new JSONObject();

		try {
			item.put(Constants.PLUGIN_PAYMENT_OPTIONS_AMOUNT, mAmount);
			item.put(Constants.PLUGIN_PAYMENT_OPTIONS_GIVES, mTitle);
			item.put(Constants.PLUGIN_PAYMENT_OPTIONS_DESCRIPTION, mDescription);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return item;
	}

	public int getAmount() {
		return mAmount;
	}

	@NonNull
	public String getTitle() {
		return mTitle;
	}

	@NonNull
	public String getDescription() {
		return mDescription;
	}
}
