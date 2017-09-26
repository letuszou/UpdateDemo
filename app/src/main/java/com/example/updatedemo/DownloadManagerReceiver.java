package com.example.updatedemo;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

public class DownloadManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            Log.e("error", "用户点击了通知");

        } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//            Log.e("error", "下载完成 ");
            installApk(context);
        }
    }

    private void installApk(Context context) {
        long downloadId = PreferenceUtil.getLongValue("download_id", 0, context);
        String localFilename = PreferenceUtil.getStringValue("download_path", null, context);
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        // 根据 下载ID 过滤结果
        query.setFilterById(downloadId);
        // 执行查询, 返回一个 Cursor (相当于查询数据库)
        Cursor cursor = manager.query(query);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        // 下载ID
        long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
        // 下载请求的状态
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        cursor.close();

        if (status == DownloadManager.STATUS_SUCCESSFUL && downloadId == id) {
            File apk_file = new File(localFilename);
            if (apk_file.exists()) {
                // 通过Intent安装APK文件
                Intent intentFile = new Intent(Intent.ACTION_VIEW);
                intentFile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentFile.setDataAndType(Uri.parse("file://" + apk_file.toString()), "application/vnd.android.package-archive");
                context.startActivity(intentFile);
            } else {
                Toast.makeText(context, "下载成功,请手动安装", Toast.LENGTH_SHORT).show();
            }

        }

    }


}
