package com.example.com.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private WebView wView; //web展示地图
    private ValueCallback<Uri> mUploadMessage;//回调图片选择，4.4以下
    private ValueCallback<Uri[]> mUploadCallbackAboveL;//回调图片选择，5.0以上

    private final static int FILE_CHOOSER_RESULT_CODE = 4; //图片
    private final static int FILE_CAMERA_RESULT_CODE = 5;//拍照
    private String cameraFielPath; //拍照图片路径
    private static  final  String[] stocks = {"拍照", "相册"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPower();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        readWebMap();
    }





    public void readWebMap(){
        wView = (WebView) findViewById(R.id.web_map);
        wView.loadUrl("file:///android_asset/test.html");
        WebSettings settings = wView.getSettings();

        settings.setJavaScriptEnabled(true);    //设置webview支持javascript
        settings.setLoadsImagesAutomatically(true);    //支持自动加载图片
        settings.setUseWideViewPort(true);    //设置webview推荐使用的窗口，使html界面自适应屏幕
        settings.setLoadWithOverviewMode(true);
        settings.setSaveFormData(true);    //设置webview保存表单数据

        settings.setSupportMultipleWindows(true);
        settings.setAppCacheEnabled(true); //设置APP可以缓存
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);//返回上个界面不刷新  允许本地缓存
        settings.setAllowFileAccess(true);// 设置可以访问文件

        wView.setLongClickable(true);
        wView.setScrollbarFadingEnabled(true);
        wView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wView.setDrawingCacheEnabled(true);


        //添加客户端支持
        wView.setWebChromeClient(new MyWebChromeClient() );

    }

    /**
     *
     *  注入权限
     *  @author: zxf
     *  @time: 2018/9/4 20:06
     */
    private  void initPower(){
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int locations = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int storgeWrite =ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int storgeRead =ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 6);
        }

        if(locations != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 7);
        }
        if(storgeRead != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 8);
        }
        if(storgeWrite != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 8);
        }
    }



    private class MyWebChromeClient extends WebChromeClient {

        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {

            mUploadMessage = uploadMsg;
            openImageChooserActivity();


        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            openImageChooserActivity();
        }

        // For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            openImageChooserActivity();

        }

        // For Android 5.0+
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            mUploadCallbackAboveL = filePathCallback;
            openImageChooserActivity();
            return true;
        }


    }


    ///选择拍照还是相册
    public void openImageChooserActivity() {
        new MaterialDialog.Builder(this)
                .items(stocks)
                .positiveText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mUploadCallbackAboveL != null) {
                            mUploadCallbackAboveL.onReceiveValue(null);
                            mUploadCallbackAboveL = null;
                        }
                        if (mUploadMessage != null) {
                            mUploadMessage.onReceiveValue(null);
                            mUploadMessage = null;
                        }
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 0) {
                            takeCamera();
                        } else if (position == 1) {
                            takePhoto();
                        }
                    }
                }).show();
    }



    //拍照
    private void takeCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        cameraFielPath = Environment.getExternalStorageDirectory() + "//" + System.currentTimeMillis() + ".jpg";
        File outputImage = new File(cameraFielPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputImage));
        startActivityForResult(intent, FILE_CAMERA_RESULT_CODE);
    }


    //选择图片
    private void takePhoto() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  super.onActivityResult(requestCode, resultCode, resultData);
        if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
        if (resultCode != RESULT_OK) {//同上所说需要回调onReceiveValue方法防止下次无法响应js方法
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(null);
                mUploadCallbackAboveL = null;
            }
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
            }
            return;
        }
        Uri result = null;
        if (requestCode == FILE_CAMERA_RESULT_CODE) {
            if (null != data && null != data.getData()) {
                result = data.getData();
            }
            if (result == null && hasFile(cameraFielPath)) {
                result = Uri.fromFile(new File(cameraFielPath));
            }
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{result});
                mUploadCallbackAboveL = null;
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (data != null) {
                result = data.getData();
            }
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }


    /**
     * 判断文件是否存在
     */
    public static boolean hasFile(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            Log.i("error", e.toString());
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(Intent intent) {
        Uri[] results = null;
        if (intent != null) {
            String dataString = intent.getDataString();
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                results = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    results[i] = item.getUri();
                }
            }
            if (dataString != null)
                results = new Uri[]{Uri.parse(dataString)};
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
    }
}
