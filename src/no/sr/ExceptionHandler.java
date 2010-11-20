package no.sr;
/*  Copyright (C) 2010  Stefan Rasmusson

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

rasmusson.stefan@gmail.com
*/
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.content.Context;

public class ExceptionHandler implements UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

	private Activity application = null;

	public ExceptionHandler(final Activity app) {
		this.defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		this.application = app;
	}

	public void uncaughtException(final Thread t, final Throwable e) {
		StringBuffer stackTrace = new StringBuffer();
		stackTrace.append(e.toString());
		stackTrace.append("\n\n");
		stackTrace.append("Stack trace:\n\n");
		for (StackTraceElement element : e.getStackTrace()) {
			stackTrace.append("   ");
			stackTrace.append(element.toString());
			stackTrace.append("\n");
		}

		Throwable cause = e.getCause();
		if (cause != null) {
			stackTrace.append("\n\nCause:\n\n");
			stackTrace.append(cause.toString());
			stackTrace.append("\n\n");
			for (StackTraceElement element : cause.getStackTrace()) {
				stackTrace.append("   ");
				stackTrace.append(element.toString());
				stackTrace.append("\n");
			}
		}
		
		try {
			FileOutputStream trace = application.openFileOutput("stacktrace.log", Context.MODE_PRIVATE);
			trace.write(stackTrace.toString().getBytes());
			trace.close();
		} catch (IOException ioe) {
		}

		defaultUncaughtExceptionHandler.uncaughtException(t, e);
	}
}
