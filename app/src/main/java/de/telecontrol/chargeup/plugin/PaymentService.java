package de.telecontrol.chargeup.plugin;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class PaymentService extends IntentService {
	public static final int API_VERSION = 2;

	public PaymentService() {
		super("PaymentService");
	}

	/**
	 * A list of options for payment. Options include the payment index (in multiples of 5 Euros),
	 * the title of the payment (short and to the point) and the description of the payment (should
	 * tell the user exactly what they will get).
	 * @return A list of {@link PaymentOption}s that will be given as options to the user.
	 */
	@NonNull
	public abstract List<PaymentOption> getPaymentOptions();

	/**
	 * This must be a properly formatted URL that will be called by our payment service to tell
	 * your system that money has been paid. Extra parameters will be passed to your service for
	 * you to verify. Contact a representative to find out what parameters will be passed to you.
	 * <b>This should be HTTPS!</b> Users will be warned if you provide an insecure URL.
	 * @return The URL of your payment processing system
	 */
	@NonNull
	public abstract Uri getPaymentUrl();

	/**
	 * The identifier that uniquely identifies every user in your application. This can be an
	 * integer ID or an E-mail for example.
	 * @return The unique user identification string
	 */
	@NonNull
	public abstract String getUserId();

	/**
	 * Perform some pre-payment testing on your service to ensure that your payment will go through
	 * without exceptions. You can test that your service is online and able to take the request,
	 * for example.
	 * @return A pair containing the absolute true / false of whether your service is reachable,
	 * and a string message if it isn't.
	 */
	@NonNull
	public abstract Pair<Boolean, String> isPaymentAllowed();

	@Override
	protected void onHandleIntent(Intent intent) {
		final String command = intent.getStringExtra(Constants.PLUGIN_INTENT_COMMAND);
		final ResultReceiver callback = intent.getParcelableExtra(Constants.PLUGIN_INTENT_CALLBACK);

		if(Constants.PLUGIN_PAYMENT_OPTIONS.equals(command)) {
			Bundle bundle = new Bundle();

			JSONObject root = new JSONObject();
			try {
				JSONArray arr = new JSONArray();

				List<PaymentOption> options = getPaymentOptions();
				for(PaymentOption option : options) {
					arr.put(option.asJson());
				}

				root.put(Constants.PLUGIN_PAYMENT_OPTIONS, arr);
				root.put(Constants.PLUGIN_PAYMENT_URL, getPaymentUrl());
				root.put(Constants.PLUGIN_PAYMENT_USER, getUserId());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			bundle.putString(Constants.PLUGIN_RESPONSE, Constants.PLUGIN_OK);
			bundle.putString(Constants.PLUGIN_PAYMENT_OPTIONS, root.toString());

			callback.send(1, bundle);
		} else if(Constants.PLUGIN_PAYMENT_ALLOWED.equals(command)) {
			Pair<Boolean, String> allowed = isPaymentAllowed();

			Bundle bundle = new Bundle();
			bundle.putString(Constants.PLUGIN_RESPONSE, Constants.PLUGIN_OK);
			bundle.putBoolean(Constants.PLUGIN_PAYMENT_ALLOWED_BOOLEAN, allowed.first);
			bundle.putString(Constants.PLUGIN_PAYMENT_ALLOWED_MESSAGE, allowed.second);
			callback.send(1, bundle);
		} else if(Constants.PLUGIN_VERSION.equals(command)) {
			Bundle bundle = new Bundle();
			bundle.putString(Constants.PLUGIN_RESPONSE, Constants.PLUGIN_OK);
			bundle.putInt(Constants.PLUGIN_VERSION, API_VERSION);
			callback.send(1, bundle);
		} else {
			Bundle bundle = new Bundle();
			bundle.putString(Constants.PLUGIN_RESPONSE, Constants.PLUGIN_UNSUPPORTED);
			bundle.putString(Constants.PLUGIN_INTENT_COMMAND, command);
			callback.send(1, bundle);
		}

		this.stopSelf();
	}
}
