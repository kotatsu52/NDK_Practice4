package my.kotatsu.ndk_practice4;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	private CameraPreview mCameraPreview; // カメラプレビュー

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // フルスクリーン・タイトルなし・ディスプレイを常に明るく
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // カメラプレビュー
        mCameraPreview = new CameraPreview(this);
        setContentView(mCameraPreview);
    }
	//-------------------------------//
	// 最初にメニューを開いた時に呼び出される
	//-------------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, 1, Menu.NONE, "JAVA");
    	menu.add(Menu.NONE, 2, Menu.NONE, "NDK");
        return super.onCreateOptionsMenu(menu);
    }
	//-------------------------------//
	// メニューを開く度に呼び出される
	//-------------------------------//
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// JAVAモードの時はNDKだけ表示
    	if(mCameraPreview.mMode == 1){
    		menu.findItem(1).setVisible(false);
    		menu.findItem(2).setVisible(true);
    	}
    	// NDKモードの時はJAVAだけ表示
    	if(mCameraPreview.mMode == 2){
    		menu.findItem(1).setVisible(true);
    		menu.findItem(2).setVisible(false);
    	}

        return super.onPrepareOptionsMenu(menu);
    }
	//-------------------------------//
	// メニューが選択された時に呼び出される
	//-------------------------------//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case 1:
        	// JAVAモードに移行
        	mCameraPreview.mMode = 1;
            ret = true;
            break;
        case 2:
        	// NDKモードに移行
        	mCameraPreview.mMode = 2;
            ret = true;
            break;

        }
        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
        // カメラ復帰処理
        mCameraPreview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // カメラ退避処理
        mCameraPreview.onPuse();
    }

}
