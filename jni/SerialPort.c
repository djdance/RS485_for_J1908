/*
 * Copyright 2009-2011 Cedric Priscal
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


 ATTENTION!!! USe NDK <= 14b only!!! Not work with NDK 15
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>

#include "SerialPort.h"

#include "android/log.h"
static const char *TAG="djd";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static speed_t getBaudrate(jint baudrate)
{
	switch(baudrate) {
	case 0: return B0;
	case 50: return B50;
	case 75: return B75;
	case 110: return B110;
	case 134: return B134;
	case 150: return B150;
	case 200: return B200;
	case 300: return B300;
	case 600: return B600;
	case 1200: return B1200;
	case 1800: return B1800;
	case 2400: return B2400;
	case 4800: return B4800;
	case 9600: return B9600;
	case 19200: return B19200;
	case 38400: return B38400;
	case 57600: return B57600;
	case 115200: return B115200;
	case 230400: return B230400;
	case 460800: return B460800;
	case 500000: return B500000;
	case 576000: return B576000;
	case 921600: return B921600;
	case 1000000: return B1000000;
	case 1152000: return B1152000;
	case 1500000: return B1500000;
	case 2000000: return B2000000;
	case 2500000: return B2500000;
	case 3000000: return B3000000;
	case 3500000: return B3500000;
	case 4000000: return B4000000;
	default: return -1;
	}
}

static int getParity(jint parity)
{
	switch (parity) {
	case 1: return PARENB | PARODD;
	case 2: return PARENB;
	}
	return 0;
}

static int getSize(jint size)
{
	switch (size){
	case 6: return CS6;
	case 7: return CS7;
	case 8: return CS8;
	}
	return 0;
}

static int getStopBit(jint stopb)
{
	switch (stopb){
	case 2: return CSTOPB;
	}
	return 0;
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_android_1serialport_1api_SerialPort_open
  (JNIEnv *env, jclass thiz, jstring path, jint flags,jint baudrate, jint size, jint parity, jint stops)
{
	int fd;
	speed_t speed;
	jobject mFileDescriptor;

	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) {
			/* TODO: throw an exception */
			LOGE("Invalid baudrate");
			return NULL;
		} else {
			LOGD("baudrate %d ok, speed = %d ",baudrate,speed);
         }
    }

	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
		LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
		fd = open(path_utf, O_RDWR | flags);
		LOGD("open() fd = %d", fd);
		(*env)->ReleaseStringUTFChars(env, path, path_utf);
		if (fd == -1)
		{
			/* Throw an exception */
			LOGE("Cannot open port");
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Configure device */
	{
		struct termios cfg;
		LOGD("Configuring serial port");
		if (tcgetattr(fd, &cfg))
		{
			LOGE("tcgetattr() failed");// (fd = %d, err = %d)", fd, errno);
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}

		cfmakeraw(&cfg);
		cfsetispeed(&cfg, speed);
		cfsetospeed(&cfg, speed);

        /*{   //this added by djdance and omitted in 4.3 example, dunno why, I leaved it again.
            //cfg.c_cflag = (cfg.c_cflag & ~CSIZE) | CS8;     // 8-bit chars
            cfg.c_cflag = (cfg.c_cflag & ~CSIZE) | CS7;     // 7-bit chars

            //cfg.c_cflag &= ~(PARENB | PARODD);      // shut off parity
            //no!djd cfg.c_cflag &= ~(IGNPAR | PARMRK); //enable EVEN Parity:
            //no!djd cfg.c_cflag |= INPCK; //enable EVEN Parity:
            cfg.c_cflag |= PARENB; //enable EVEN Parity:
            cfg.c_cflag &= ~PARODD; //enable EVEN Parity:

            //cfg.c_cflag &= ~CSTOPB; //clear stop bit
            cfg.c_cflag |= CSTOPB; //set 2 stop bit
        }*/

		cfg.c_cflag &= ~PARENB;
        cfg.c_cflag &= ~PARODD;
        cfg.c_cflag &= ~CSIZE;
        cfg.c_cflag &= ~CSTOPB;
        cfg.c_cflag |= (CLOCAL | CREAD) | getSize(size) | getParity(parity) | getStopBit(stops);

		if (tcsetattr(fd, TCSANOW, &cfg)){
			LOGE("configureFinal: tcsetattr() failed");// (fd = %d, err = %d)", fd, errno);
			//LOGE("errors: %d %d %d %d %d",EBADF,EINTR,EINVAL,ENOTTY,EIO);
			//22 = [EINVAL]
            //The optional_actions argument is not a supported value,
            //or an attempt was made to change an attribute represented in the termios structure to an unsupported value.
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Create a corresponding file descriptor */
	{
		jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
		mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
		(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
	}

	return mFileDescriptor;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_android_1serialport_1api_SerialPort_close
  (JNIEnv *env, jobject thiz)
{
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}

