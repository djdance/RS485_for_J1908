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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConsoleActivity extends SerialPortActivity {

	EditText mReception;
	Button sendbutton,sendbutton2,sendbutton3,sendbutton4;
	Button enableButton;
	Boolean enable = false;
	Process su=null;
	String cmd = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);

		mReception = (EditText) findViewById(R.id.EditTextReception);
		final EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
		final EditText Emission2 = (EditText) findViewById(R.id.EditTextEmission2);
		final EditText Emission3 = (EditText) findViewById(R.id.EditTextEmission3);
		final EditText Emission4 = (EditText) findViewById(R.id.EditTextEmission4);
		sendbutton = (Button) findViewById(R.id.button_send);
		sendbutton2 = (Button) findViewById(R.id.button_send2);
		sendbutton3 = (Button) findViewById(R.id.button_send3);
		sendbutton4 = (Button) findViewById(R.id.button_send4);
		enableButton = (Button) findViewById(R.id.button_enable);
		if (Build.VERSION.SDK_INT>10) {
			try {
			/* Missing read/write permission, trying to chmod the file */
				su = Runtime.getRuntime().exec("su");
				String cmd = "echo 106 > /sys/class/gpio/export" + "\n"
						+ "echo out > /sys/class/gpio/gpio106/direction" + "\n"
						+ "echo 1 > /sys/class/gpio/gpio106/value" + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0)) {
					throw new SecurityException();
				}
				Thread.sleep(100);
				su = Runtime.getRuntime().exec("su");
				Thread.sleep(50);
			} catch (Exception e) {
				sendbutton.setEnabled(false);
				mSerialPort = null;
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		sendbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.14 Run Number
				String t = Emission.getText().toString();
				if (t.length() ==4) {
					//char[] text = new char[5]; //debug
					/*text[0]=44;
					text[1]=23;
					text[2]=61;
					text[3]=114;
					text[4]=62;// 2C173D723E, CS == 0xD0 == 208 * /
					text[0]=128;
					text[1]=95;
					text[2]=23;
					text[3]=45;
					text[4]=123;// CS == 0x62 == 98 */

					char[] text = new char[10];
					text[0]=189;//MID: SIGN
					text[1]=254;//PID
					text[2]=189;//Receiver MID: SIGN MID
					text[3]=6;//data bytes to follow
					text[4]=65;//message type
					text[5]=16;//SSID of sign, 16 to all
					for (int i = 0; i < t.length(); i++) {
						text[6+i] = t.charAt(i);
					}// */
					setWriteToPort(true,0);
					try {
						mOutputStream.write(new String(text).getBytes());
						mOutputStream.write(calculateJ1708_CS(text));//new String(text).getBytes()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					setWriteToPort(false,new String(text).length());
				} else {
					new AlertDialog.Builder(ConsoleActivity.this)
						.setTitle("Error! Must be")
						.setMessage(Emission.getHint().toString())
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
				}
			}
		});

		sendbutton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.12 Route Number
				int n=0;
				try {
					n=Integer.parseInt(Emission2.getText().toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (n>0 && n<=65535) {
					char[] text = new char[10];
					text[0]=189;//MID: SIGN
					text[1]=254;//PID
					text[2]=189;//Receiver MID: SIGN MID
					text[3]=6;//data bytes to follow
					text[4]=52;//message type
					text[5]=16;//SSID of sign, 16 to all
					String t=Integer.toHexString(n);//String.format("#%06X", (0xFFFF & n));
					int tl=t.length();
					if (tl<4)
						for (int i = 0; i < 4-tl; i++) {
							t="0"+t;
						}
					for (int i = 0; i < t.length(); i++) {
						text[6+i] = t.charAt(i);
					}// */
					setWriteToPort(true,0);
					try {
						mOutputStream.write(new String(text).getBytes());
						mOutputStream.write(calculateJ1708_CS(text));//new String(text).getBytes()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					setWriteToPort(false,new String(text).length());
				} else {
					new AlertDialog.Builder(ConsoleActivity.this)
							.setTitle("Error! Must be")
							.setMessage(Emission2.getHint().toString())
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							})
							.show();
				}
			}
		});

		sendbutton3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.11 PR Message
				int n=0;
				try {
					n=Integer.parseInt(Emission2.getText().toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (n>0 && n<=65535) {
					char[] text = new char[10];
					text[0]=189;//MID: SIGN
					text[1]=254;//PID
					text[2]=189;//Receiver MID: SIGN MID
					text[3]=6;//data bytes to follow
					text[4]=51;//message type
					text[5]=16;//SSID of sign, 16 to all
					String t=Integer.toHexString(n);//String.format("#%06X", (0xFFFF & n));
					int tl=t.length();
					if (tl<4)
						for (int i = 0; i < 4-tl; i++) {
							t="0"+t;
						}
					for (int i = 0; i < t.length(); i++) {
						text[6+i] = t.charAt(i);
					}// */
					setWriteToPort(true,0);
					try {
						mOutputStream.write(new String(text).getBytes());
						mOutputStream.write(calculateJ1708_CS(text));//new String(text).getBytes()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					setWriteToPort(false,new String(text).length());
				} else {
					new AlertDialog.Builder(ConsoleActivity.this)
							.setTitle("Error! Must be")
							.setMessage(Emission2.getHint().toString())
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							})
							.show();
				}
			}
		});

		sendbutton4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.10 Destination
				int n=0;
				try {
					n=Integer.parseInt(Emission2.getText().toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (n>0 && n<=65535) {
					char[] text = new char[10];
					text[0]=189;//MID: SIGN
					text[1]=254;//PID
					text[2]=189;//Receiver MID: SIGN MID
					text[3]=6;//data bytes to follow
					text[4]=50;//message type
					text[5]=16;//SSID of sign, 16 to all
					String t=Integer.toHexString(n);//String.format("#%06X", (0xFFFF & n));
					int tl=t.length();
					if (tl<4)
						for (int i = 0; i < 4-tl; i++) {
							t="0"+t;
						}
					for (int i = 0; i < t.length(); i++) {
						text[6+i] = t.charAt(i);
					}// */
					setWriteToPort(true,0);
					try {
						mOutputStream.write(new String(text).getBytes());
						mOutputStream.write(calculateJ1708_CS(text));//new String(text).getBytes()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					setWriteToPort(false,new String(text).length());
				} else {
					new AlertDialog.Builder(ConsoleActivity.this)
							.setTitle("Error! Must be")
							.setMessage(Emission2.getHint().toString())
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							})
							.show();
				}
			}
		});
		////////////////////////////////
		enableButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					/* Missing read/write permission, trying to chmod the file */
					Process su;
					String cmd = "";
					su = Runtime.getRuntime().exec("su");
					if (enable) {
						cmd = echoCom("1") + "exit\n";
					} else {
						cmd = echoCom("0") + "exit\n";
					}
					enable = !enable;
					su.getOutputStream().write(cmd.getBytes());
				} catch (Exception e) {
					e.printStackTrace();
					throw new SecurityException();
				}
				if (enable) {
					enableButton.setText(getString(R.string.enable));
				} else {
					enableButton.setText(getString(R.string.disable));
				}
				sendbutton.setEnabled(!enable);
				sendbutton2.setEnabled(!enable);
				sendbutton3.setEnabled(!enable);
				sendbutton4.setEnabled(!enable);
			}
		});
	}

	void setWriteToPort(boolean write, int length){
		if (Build.VERSION.SDK_INT<=10)
			return;
		//Log.d(TAG,"setWriteToPort START write?"+write);
		try {
			if (!write) {
				Thread.sleep(10*(length+2));//+2 is important!!! +1 is crop send, +3 crop answer of APC. Damn!
				//sendBroadcast(new Intent("PassioServiceDone").putExtra("toast", "" + formatter.format(new Date()) + " write to RS485: in\n"));
			}
			//Log.d(TAG,"setWriteToPort START write?"+write);
			if (su==null) {
				su = Runtime.getRuntime().exec("su");
			}
			if (write) {
				cmd = echoCom("1") + "exit\n";
				enableButton.setText(getString(R.string.disable));
			} else {
				cmd = echoCom("0") + "exit\n";
				enableButton.setText(getString(R.string.enable));
			}
			sendbutton.setEnabled(!write);
			sendbutton2.setEnabled(!write);
			sendbutton3.setEnabled(!write);
			sendbutton4.setEnabled(!write);

			su.getOutputStream().write(cmd.getBytes());
			su.waitFor();
			su.destroy();//???
			su=null;
			if (write) {
				//sendBroadcast(new Intent("PassioServiceDone").putExtra("toast", "\n" + formatter.format(new Date()) + " write to RS485: out\n"));
				//Thread.sleep(100);
				su = Runtime.getRuntime().exec("su");
				//Log.d(TAG,"setWriteToPort DONE");
				Thread.sleep(Build.VERSION.SDK_INT<=10?0:100);
			} else {
			}
		} catch (Exception e) {
			//here may be: java.io.IOException: write failed: EPIPE (Broken pipe)
			Log.e("djd","LEDsendSerive setWriteToPort exception: "+e.toString());
			//sendBroadcast(new Intent("PassioServiceDone").putExtra("toast", "RS485 error: setWriteToPort\n"+e.toString()));
			sendBroadcast(new Intent("PassioServiceDone").putExtra("fromRS485", true).putExtra("info", ""+e.toString()));
		}
		//Log.d(TAG,"setWriteToPort DONE");
	}


	public static byte calculateMyLRC(byte[] bytes) {
		byte LRC = 0;
		for (int i = 0; i < bytes.length; i++) {
			LRC ^= bytes[i];
		}
		LRC= (byte) (((byte)0xFF-LRC) & (byte)0x7f);
		return LRC;
	}
	char calculateJ1708_CS(char[] bytes) {
		//https://www.kvaser.com/about-can/can-standards/j1708/
		//http://wanderlodgegurus.com/database/Theory/Microchip%20J1708%20CanBUS%20interface.pdf
		/*
To compute a checksum:
1. add all the bytes
2. invert the sum
3. add 1, and
4. transmit, as the last byte.
Consider the following data.
What should be inserted at XXX to assure a proper
checksum?
128 + 95 + 23 + 45 + 123 = 158 (assuming an 8-bit
adder with an 8-bit result)
Bitwise inversion of 158 results in 97.
Adding 1 so XXX should be 98.
The receiver will add all the numbers and expects to
see 0.
128 + 95 + 23 + 45 + 123 + 98 = 512 or 0 for an 8-bit
sum with an 8-bit result.
This would be valid.
		 */
		int intCS=0;
		byte CS=0x00;
		for (int i = 0; i < bytes.length; i++) {
			intCS += (int) bytes[i];
			CS += (byte) bytes[i];
			//Log.d("djd","("+i+"): +"+bytes[i]+" = (int)"+intCS+" = (byte)"+CS);
		}
		intCS=intCS%256;
		CS= (byte) (0x100-CS);
		Log.d("djd","intCS="+intCS+", inverted+1="+(255-intCS+1)+". CS+1="+((256+CS)%256));
		//intCS=255-intCS+1;
		return (char) CS;//(char) intCS;
	}
	public String echoCom(String value) {
		String comString = "echo " + value + " > "
				+ "/sys/class/gpio/gpio106/value" + "\n";
		return comString;

	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size)+"\n");
					Log.d("djd"," got buffer:"+(new String(buffer, 0, size)));
				}
			}
		});
	}
}
