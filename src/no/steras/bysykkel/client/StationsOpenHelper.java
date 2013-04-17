package no.steras.bysykkel.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StationsOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String STATION_TABLE_NAME = "station";

	private static final String DB_PATH = "/data/data/no.steras.bysykkel.client/databases/";

	private static final String DB_NAME = "db";

	private SQLiteDatabase stationsDB;

	private Context context;

	StationsOpenHelper(Context context) {
		super(context, DB_NAME, null, 3);
		this.context = context;
	}

	public Cursor getStations() {
		return stationsDB.rawQuery("select * from station;", null);
	}

	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (!dbExist) {
			Log.e("Sykkelkoll-" + "IO", "Creating DB");
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {
				Log.e("Sykkelkoll-" + "IO", e.getMessage(), e);
			}
		} else {
			Log.e("Sykkelkoll-" + "IO", "else");
		}

	}

	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			Log.e("Sykkelkoll-" + "IO", e.getMessage(), e);
		}

		if (checkDB != null) {

			checkDB.close();

		}
		Log.e("Sykkelkoll-" + "IO", "Creating DB");
		return checkDB != null ? true : false;
	}

	private void copyDataBase() throws IOException {

		InputStream myInput = context.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;

		OutputStream myOutput = new FileOutputStream(outFileName);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		myOutput.flush();
		myOutput.close();
		myInput.close();

	}
	

	public void openDataBase() throws SQLException {
		String myPath = DB_PATH + DB_NAME;
		stationsDB = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	@Override
	public synchronized void close() {

		if (stationsDB != null)
			stationsDB.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
		String myPath = DB_PATH + DB_NAME;
		new File(myPath).delete();
		try {
			copyDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}

}