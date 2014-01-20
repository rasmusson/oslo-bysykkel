package no.steras.bysykkel.client.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import no.steras.bysykkel.client.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog extends Dialog {
	private static Context mContext = null;
	private String googleMapsAttribution;

	public AboutDialog(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * 
	 * Standard Android on create method that gets called when the activity
	 * initialized.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.about);

		TextView tv = (TextView) findViewById(R.id.info_text);
		tv.setText(Html.fromHtml(readRawTextFile(R.raw.info)));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);

		String legal = readRawTextFile(R.raw.legal) + "\n\n\n\n"
				+ googleMapsAttribution;
		tv = (TextView) findViewById(R.id.legal_text);
		tv.setText(legal);

	}

	public static String readRawTextFile(int id) {
		InputStream inputStream = mContext.getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try {
			while ((line = buf.readLine()) != null)
				text.append(line);
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}

	public void setGoogleMapsAttribution(String googleMapsAttribution) {
		this.googleMapsAttribution = googleMapsAttribution;
	}
}