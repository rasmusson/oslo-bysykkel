package no.steras.bysykkel.client.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import no.steras.bysykkel.client.Timer;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class StationsOpenHelper extends SQLiteAssetHelper {

	private static final int DATABASE_VERSION = 4;
	private static final String STATION_TABLE_NAME = "station";

	private static final String DB_PATH = "/data/data/no.steras.bysykkel.client/databases/";

	private static final String DB_NAME = "db";

	public StationsOpenHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		setForcedUpgrade();
	}
	
	public Cursor getStations() {
		return getReadableDatabase().rawQuery("select * from station;", null);
	}
	
	public void initDb() {
		getReadableDatabase();
	}
}