package no.steras.bysykkel.client.data;

import android.content.Context;
import android.database.Cursor;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class StationsOpenHelper extends SQLiteAssetHelper {

	private static final int DATABASE_VERSION = 5;
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