package my.kotatsu.ndk_practice4;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;          // サーフェースホルダー
	private Context mContext;               // コンテクスト
	private Camera mCamera;                 // カメラ
	private Camera.Parameters mCamparam;    // カメラ設定
	private boolean mHasSurface;            // サーフェースの存在フラグ
	private boolean mCameraPreviewing;      // プレビュー実行フラグ
	private Bitmap mBitmap;                 // 撮影されたビットマップ
    private int mWidth;						// 画面サイズ
    private int mHeight;					// 画面サイズ
    private int[] rgb;						// RGB配列
    public  int mMode=1;					// 1(JAVAモード) 2(NDKモード)

    //-------------------------------//
	//コンストラクタ
	//@param context
	//-------------------------------//
	public CameraPreview(Context context) {
	    super(context);
	    mContext = context;
	    mHolder = getHolder();
	    initSurface();

	}
	//-------------------------------//
	// サーフェース作成後のコールバック
	//-------------------------------//
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("NDK_PRACTICE4","PreviewFormat:"+mCamera.getParameters().getPreviewFormat());
	    mCamparam = mCamera.getParameters();
		if(Build.MODEL.equals("SO-01C")){
			//Xperia arc はなぜかwidthを864にしないと落ちる
			//参考：http://d.hatena.ne.jp/Superdry/20110407/1302130513
			width=864;
		}
	    mCamparam.setPreviewSize(width, height);
	    mCamera.setParameters(mCamparam);
	    startCameraPreview();
	    mWidth = width;
	    mHeight = height;
	    rgb = new int[(mWidth * mHeight)];
	    mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

	}
	//-------------------------------//
	// 画面の作成コールバック
	//-------------------------------//
	public void surfaceCreated(SurfaceHolder holder) {
	    mCamera = Camera.open();
	    mCamera.setPreviewCallback(previewCallback);
	    mCamparam = mCamera.getParameters();
	    mHasSurface = true;
	}
	//-------------------------------//
	// 画面の破棄コールバック
	//-------------------------------//
	public void surfaceDestroyed(SurfaceHolder holder) {
	    cameraDestroy();
	    mHasSurface = false;
	}
	//-------------------------------//
	// 再開
	//-------------------------------//
	public void onResume() {
	    if (mHasSurface) {
	        surfaceCreated(mHolder);
	        startCameraPreview();
	    } else {
	        initSurface();
	    }
	}
	//-------------------------------//
	// 一時停止
	//-------------------------------//
	public void onPuse() {
	    cameraDestroy();
	}
	//-------------------------------//
	// サーフェースの初期化
	//-------------------------------//
	private void initSurface() {
	    mHolder.addCallback(this);
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
	}

	//-------------------------------//
	// プレビューの開始
	//-------------------------------//
	private void startCameraPreview() {
	    if (!mCameraPreviewing) {
	        mCamera.startPreview();
	        mCameraPreviewing = true;
	    }
	}
	//-------------------------------//
	// プレビューの停止
	//-------------------------------//
	private void stopCameraPreview() {
	    if (mCameraPreviewing) {
	        mCamera.stopPreview();
	        mCameraPreviewing = false;
	    }
	}

	//-------------------------------//
	// カメラの開放
	//-------------------------------//
	private void cameraDestroy() {
	    if (mCamera != null) {
	    	mCamera.setPreviewCallback(null);
	        stopCameraPreview();
	        mCamera.release();
	        mCamera = null;
	    }
	}

	//-------------------------------//
	// プレビューコールバック
	//-------------------------------//
	private final Camera.PreviewCallback previewCallback =
	    new Camera.PreviewCallback() {
	        public void onPreviewFrame(byte[] data, Camera camera) {

	        	long start = System.currentTimeMillis();
	        	//JAVAモード
	        	if(mMode==1){
	        		// YUV420からBitmapに変換
	        		decodeYUV420SP(rgb, data, mWidth, mHeight);
	        	}
	        	//NDKモード
	        	if(mMode==2){
	        		// YUV420からBitmapに変換
	        		decodeYUV420SP_Native(rgb, data, mWidth, mHeight);
	        	}
	        	long stop = System.currentTimeMillis();
	        	Log.d("NDK_PRACTICE3", "処理時間：" + (stop - start) + " msec");

	        	//byte配列からBitmapに変換
    	        mBitmap.setPixels(rgb, 0, mWidth, 0, 0, mWidth, mHeight);

    	        // 描画
    	        Canvas canv = mHolder.lockCanvas();
    	        canv.drawBitmap(mBitmap, 0, 0, null);
    	        mHolder.unlockCanvasAndPost(canv);
	        }
	    };

	//-------------------------------//
	// YUV420データをBitmapに変換する
	// @param rgb
	// @param yuv420sp
	// @param width
	// @param height
	//-------------------------------//
	// YUV420 to BMP
	public static final void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
	    final int frameSize = width * height;

	    for (int j = 0, yp = 0; j < height; j++) {
	        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	        for (int i = 0; i < width; i++, yp++) {
	            int y = (0xff & ((int) yuv420sp[yp])) - 16;
	            if (y < 0) y = 0;
	            if ((i & 1) == 0) {
	                    v = (0xff & yuv420sp[uvp++]) - 128;
	                    u = (0xff & yuv420sp[uvp++]) - 128;
	            }

	            int y1192 = 1192 * y;
	            int r = (y1192 + 1634 * v);
	            int g = (y1192 - 833 * v - 400 * u);
	            int b = (y1192 + 2066 * u);

	            if (r < 0) r = 0; else if (r > 262143) r = 262143;
	            if (g < 0) g = 0; else if (g > 262143) g = 262143;
	            if (b < 0) b = 0; else if (b > 262143) b = 262143;

	            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
	        }
	    }
	}

	//-------------------------------//
	// NDK
	//-------------------------------//
    public native void decodeYUV420SP_Native( int[] rgb,byte[] data,int width,int height);
    static {
        System.loadLibrary("ndk_practice");
    }

}



