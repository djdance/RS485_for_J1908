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

package android_serialport_api.sample43_8n1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android_serialport_api.SerialPort;

import static android.os.Build.VERSION.SDK;

public abstract class SerialPortActivity extends Activity {

	protected Application mApplication;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	byte[] buffer_sum = new byte[0];

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					//sendBroadcast(new Intent("PassioServiceDone").putExtra("toast","RS485 waiting..."));
					//Log.d(TAG,"RS485 waiting...");
					if (mInputStream == null) return;
					byte[] buffer = new byte[64];
					//onDataReceived(buffer, mInputStream.read(buffer));
					size = mInputStream.read(buffer);
					mHandler485.removeMessages(5);
					if (size > 0) {
						//sendBroadcast(new Intent("PassioServiceDone").putExtra("toast", ""+formatter.format(new Date())+" RS485 pure ("+size+"): "+(new String(buffer, 0, size))+"\n"));
						//Log.w("djd","comport size="+size+", buffer="+new String(buffer, 0, size));
						if (Build.VERSION.SDK_INT<=10){
							onDataReceived(buffer, size);
						} else {
							byte[] c = new byte[size + buffer_sum.length];
							if (buffer_sum.length > 0)
								System.arraycopy(buffer_sum, 0, c, 0, buffer_sum.length);
							System.arraycopy(buffer, 0, c, buffer_sum.length, size);
							buffer_sum = c;

							if (buffer_sum.length > 30) {
								//Log.d("djd","bufferS.length()>30!");
								onDataReceived(buffer_sum, buffer_sum.length);
								buffer_sum = new byte[0];
							} else
								mHandler485.sendMessageDelayed(Message.obtain(mHandler485, 5, ""), 100);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	private Handler mHandler485 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 5) {
				//Log.d("djd", "mHandler: onDataReceived()");
				if (buffer_sum.length>0){
					onDataReceived(buffer_sum,buffer_sum.length);
					buffer_sum=new byte[0];
				}
			}
		}
	};

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SerialPortActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (Application) getApplication();
		try {
			mSerialPort = mApplication.getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);

	@Override
	protected void onDestroy() {
		if (mReadThread != null)
			mReadThread.interrupt();
		mApplication.closeSerialPort();
		mSerialPort = null;
		super.onDestroy();
	}
}
