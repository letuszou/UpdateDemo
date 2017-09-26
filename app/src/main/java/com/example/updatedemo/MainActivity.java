package com.example.updatedemo;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String XmlUrl = "http://ocf5858b1.bkt.clouddn.com/update_demo.xml";
    String JsonUrl = "http://ocf5858b1.bkt.clouddn.com/update_demo.json";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap<String, String> hashMap = (HashMap) msg.obj;
            String desc = hashMap.get("desc");
            final String downloadUrl = hashMap.get("url");

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("新版本提示框")
                    .setMessage(desc)
                    .setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            downloadFromXml(downloadUrl);
                        }
                    })
                    .setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        initVew();

    }


    private void initVew() {
        Button btn_json_update1 = (Button) findViewById(R.id.btn_json_update1);
        btn_json_update1.setOnClickListener(this);
        Button btn_json_update2 = (Button) findViewById(R.id.btn_json_update2);
        btn_json_update2.setOnClickListener(this);
    }

    private void updateOne() {
        OkGo.<String>get(JsonUrl).tag(this).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    int code = response.code();
                    if (200 == code) {
                        String backBody = response.body();
                        JSONObject jsonObject = new JSONObject(backBody);
                        int version = jsonObject.optInt("version");
                        final String url = jsonObject.optString("url");
                        String desc = jsonObject.optString("desc");
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("新版本提示框")
                                .setMessage(desc)
                                .setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        downloadFromJson(url);
                                    }
                                })
                                .setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    Log.e("error", "解析json异常：" + e.toString());
                }

            }
        });
    }

    private void downloadFromJson(String url) {
        String downloadPath = Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".apk";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知的标题和描述
        request.setTitle(getString(R.string.app_name));
        request.setDescription("下载测试");
        request.setDestinationUri(Uri.fromFile(new File(downloadPath)));
        //2. 获取下载管理器服务的实例, 添加下载任务
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        // 将下载请求加入下载队列, 返回一个下载ID
        long downloadId = manager.enqueue(request);
        PreferenceUtil.setLongValue("download_id", downloadId, MainActivity.this);
        PreferenceUtil.setStringValue("download_path", downloadPath, MainActivity.this);
    }

    private void updateTwo() {

        try {
            URL url = new URL(XmlUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            InputStream inStream = urlConn.getInputStream();
            ParseXmlService parseXmlService = new ParseXmlService();
            HashMap<String, String> hashMap = parseXmlService.parseXml(inStream);
            String desc = hashMap.get("desc");
            final String downloadUrl = hashMap.get("url");
            Message message = handler.obtainMessage();
            message.obj = hashMap;
            handler.sendMessage(message);
        } catch (Exception e) {
            Log.e("error", "error：" + e.toString());
        }

    }

    private void downloadFromXml(String url) {
        OkGo.<File>get(url).tag(this).execute(new FileCallback() {
                                                  @Override
                                                  public void downloadProgress(Progress progress) {
                                                      //,"已下载"+(int)(progress.fraction*100)
                                                      Log.e("error","已下载"+(int)(progress.fraction*100));
                                                  }

                                                  @Override
                                                  public void onSuccess(Response<File> response) {
                                                      String path = "" + response.body();
                                                      install(path);
                                                  }
                                              }
        );

    }


    public void install(String mUrl) {
        // 核心是下面几句代码
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(mUrl)), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_json_update1:
                updateOne();
                break;
            case R.id.btn_json_update2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 写子线程中的操作
                        Looper.prepare();
                        updateTwo();
                    }
                }).start();

                break;
        }

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
