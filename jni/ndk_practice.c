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
    // ★YUV420toRGB変換
}

