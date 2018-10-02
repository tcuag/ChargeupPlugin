package de.telecontrol.chargeup.plugin;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;

public abstract class PaymentProvider extends ContentProvider {
	public static final int API_VERSION = 3;

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
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] args, String sort) {
		final String command = selection;

		if(Constants.PLUGIN_PAYMENT_OPTIONS.equals(command)) {
			MatrixCursor cursor = new MatrixCursor(new String[] {
					Constants.PLUGIN_RESPONSE, Constants.PLUGIN_PAYMENT_OPTIONS, Constants.PLUGIN_PAYMENT_URL, Constants.PLUGIN_PAYMENT_USER
			});

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

			cursor.addRow(new Object[] {
					Constants.PLUGIN_OK, root.toString(), getPaymentUrl(), getUserId()
			});

			return cursor;
		} else if(Constants.PLUGIN_PAYMENT_ALLOWED.equals(command)) {
			Pair<Boolean, String> allowed = isPaymentAllowed();

			MatrixCursor cursor = new MatrixCursor(new String[] {
					Constants.PLUGIN_RESPONSE, Constants.PLUGIN_PAYMENT_ALLOWED_BOOLEAN, Constants.PLUGIN_PAYMENT_ALLOWED_MESSAGE
			});

			cursor.addRow(new Object[] {
					Constants.PLUGIN_OK, allowed.first ? 1 : 0, allowed.second
			});

			return cursor;
		} else if(Constants.PLUGIN_VERSION.equals(command)) {
			MatrixCursor cursor = new MatrixCursor(new String[] {
					Constants.PLUGIN_RESPONSE, Constants.PLUGIN_VERSION
			});

			cursor.addRow(new Object[] {
					Constants.PLUGIN_OK, API_VERSION
			});

			return cursor;
		} else {
			MatrixCursor cursor = new MatrixCursor(new String[] {
					Constants.PLUGIN_RESPONSE, Constants.PLUGIN_COMMAND
			});

			cursor.addRow(new Object[] {
					Constants.PLUGIN_UNSUPPORTED, command
			});

			return cursor;
		}
	}

	@Override
	public String getType(Uri uri) {
		return "payment-option";
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		throw new RuntimeException("Insert is not implemented");
	}

	@Override
	public int delete(Uri uri, String s, String[] strings) {
		throw new RuntimeException("Insert is not implemented");
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
		throw new RuntimeException("Insert is not implemented");
	}
}