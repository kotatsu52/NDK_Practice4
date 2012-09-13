#include <string.h>
#include <time.h>
#include <jni.h>
#include <android/log.h>

char TAG[]="NDK_PRACTICE4";

// YUV420toRGB
void
Java_my_kotatsu_ndk_1practice4_CameraPreview_decodeYUV420SP_1Native( JNIEnv* env, jobject thiz,
                                                                    jintArray jrgb, jbyteArray jdata, int width, int height)
{
	jint *rgb;
	jbyte *yuv420sp;
	int i;
	int j;
	int yp;
	int frameSize;
	int uvp;
	int u;
	int v;
	int y;
	int y1192;
	int r;
	int g;
	int b;
    
	// 配列変換
	rgb      = (*env)->GetIntArrayElements(env, jrgb, 0);
	yuv420sp = (*env)->GetByteArrayElements(env, jdata, 0);
    
	frameSize = width * height;
    for ( j = 0, yp = 0; j < height; j++) {
        uvp = frameSize + (j >> 1) * width;
        u = 0;
        v = 0;
        for ( i = 0; i < width; i++, yp++) {
            y = (0xff & ((int) yuv420sp[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & yuv420sp[uvp++]) - 128;
                u = (0xff & yuv420sp[uvp++]) - 128;
            }
            
            y1192 = 1192 * y;
            r = (y1192 + 1634 * v);
            g = (y1192 - 833 * v - 400 * u);
            b = (y1192 + 2066 * u);
            
            if (r < 0) r = 0; else if (r > 262143) r = 262143;
            if (g < 0) g = 0; else if (g > 262143) g = 262143;
            if (b < 0) b = 0; else if (b > 262143) b = 262143;
            
            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }
    
	// 解放
	(*env)->ReleaseIntArrayElements( env, jrgb, rgb, 0);
	(*env)->ReleaseByteArrayElements( env, jdata, yuv420sp, 0);
}

