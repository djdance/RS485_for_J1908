/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class SerialPort {
	public final static int SERIALPORT_NOPARITY		= 0;
	public final static int SERIALPORT_ODDPARITY	= 1;
	public final static int SERIALPORT_EVENPARITY	= 2;
	public final static int SERIALPORT_SIZE5		= 5;
	public final static int SERIALPORT_SIZE6		= 6;
	public final static int SERIALPORT_SIZE7		= 7;
	public final static int SERIALPORT_SIZE8		= 8;
	public final static int SERIALPORT_1STOPBIT		= 1;
	public final static int SERIALPORT_2STOPBIT		= 2;

	String TAG = "djd";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public SerialPort(File device, int flags,int baudrate, int size, int parity, int stopb) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("su");
				//String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
				String cmd = "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}
		mFd = open(device.getAbsolutePath(), flags,baudrate, size, parity, stopb);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int flags,int baudrate, int size, int parity, int stopb);
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}
}
