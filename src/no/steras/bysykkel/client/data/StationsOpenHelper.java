package no.steras.bysykkel.client.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.analytics.tracking.android.EasyTracker;

import no.steras.bysykkel.client.Timer;
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

	private Timer stopWatch;
	
	public StationsOpenHelper(Context context) {
		super(context, DB_NAME, null, 3);
		this.context = context;
		stopWatch = new Timer();
	}

	public Cursor getStations() {
		return stationsDB.rawQuery("select * from station;", null);
	}

	public void createDataBase() throws IOException {
		stopWatch.start();
		
		boolean dbExist = checkDataBase();

		if (!dbExist) {
			Log.i("Sykkelkoll-" + "IO", "Creating DB");
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {
				Log.i("Sykkelkoll-" + "IO", e.getMessage(), e);
			}
		} else {
			Log.i("Sykkelkoll-" + "IO", "else");
		}
		
		EasyTracker.getTracker().sendTiming("Initial loading time", stopWatch.getElapsedTime(), "createDataBase", null);

	}

	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
			
			
		} catch (SQLiteException e) {
			Log.i("Sykkelkoll-" + "IO", e.getMessage(), e);
		}

		if (checkDB != null) {		
			boolean isTableExists = isTableExists("station", checkDB);
			checkDB.close();
			return isTableExists;

		}
		Log.i("Sykkelkoll-" + "IO", "Creating DB");
		return false;
	}

	public boolean isTableExists(String tableName, SQLiteDatabase db) {
	    Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if(cursor!=null) {
	        if(cursor.getCount()>0) {
	                            cursor.close();
	            return true;
	        }
	                    cursor.close();
	    }
	    return false;
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
		stopWatch.start();
		String myPath = DB_PATH + DB_NAME;
		stationsDB = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);
		EasyTracker.getTracker().sendTiming("Initial loading time", stopWatch.getElapsedTime(), "openDataBase", null);
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