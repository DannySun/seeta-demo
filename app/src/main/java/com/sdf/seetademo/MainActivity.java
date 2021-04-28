package com.sdf.seetademo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sdf.seetademo.jni.AgePredictInfo;
import com.sdf.seetademo.jni.SeetaAgeCallback;
import com.sdf.seetademo.jni.SeetaHandle;
import com.sdf.seetademo.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback{
    private static final String TAG = "Main";
    public static final int MSG_PREDICT_AGE = 1;

    private TextView tv;
    private Button btnAgePredict;
    private ImageView imageView, imageViewForeground;

    private String strExternalCache;
    private Handler threadHandler;
    private HandlerThread handlerThread;
    private Handler mainHandler;
    private List<AgePredictInfo> agePredictInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        strExternalCache = getExternalCacheDir().getAbsolutePath();
        checkPermission();
        handlerThread = new HandlerThread("work-thread");
        handlerThread.start();
        threadHandler = new Handler(handlerThread.getLooper(), this);
        mainHandler = new Handler(getMainLooper());
        tv = findViewById(R.id.sample_text);
        tv.setText(SeetaHandle.getInstance().stringFromJNI());
        btnAgePredict = findViewById(R.id.btn_age_predict);
        btnAgePredict.setOnClickListener(this);
        imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(BitmapFactory.decodeFile(strExternalCache + "/face-test.jpg"));
        imageViewForeground = findViewById(R.id.image_view_foreground);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_age_predict:
                threadHandler.sendEmptyMessage(MSG_PREDICT_AGE);
                break;
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case MSG_PREDICT_AGE:
                String imageUrl = strExternalCache + "/face-test.jpg";
                SeetaHandle.getInstance().predictAge(imageUrl, seetaAgeCallback);
                return true;
        }
        return false;
    }

    private SeetaAgeCallback seetaAgeCallback = new SeetaAgeCallback() {
        @Override
        public void onAgePredict(int face_index, int age) {
            Log.d(TAG, "onAgePredict, face_index:" + face_index + " ,age:" + age);
            if (face_index >= agePredictInfoList.size()) {
                return;
            }
            AgePredictInfo agePredictInfo = agePredictInfoList.get(face_index);
            if (agePredictInfo != null) {
                agePredictInfo.setAge(age);
            }
            mainHandler.removeCallbacks(drawFaceInfoRunnable);
            mainHandler.post(drawFaceInfoRunnable);
        }

        @Override
        public void onDetected(int bitmap_width, int bitmap_height, int size, int[] score, int[] face_x, int[] face_y, int[] face_width, int[] face_height) {
            Log.d(TAG, "onDetected, size:" + size);
            agePredictInfoList.clear();
            for (int i=0; i<size; i++) {
                Log.d(TAG, "onDetected, score:" + score[i] + " facex:" + face_x[i] + " facey:" + face_y[i] + " faceWidth:" + face_width[i] + " faceHeight:" + face_height[i]);
                AgePredictInfo agePredictInfo = new AgePredictInfo();
                agePredictInfo.setBitmapWidth(bitmap_width);
                agePredictInfo.setBitmapHeight(bitmap_height);
                agePredictInfo.setFaceRect(new Rect(face_x[i], face_y[i],face_x[i] + face_width[i], face_y[i] + face_height[i]));
                agePredictInfoList.add(agePredictInfo);
            }
        }

        @Override
        public void onLandmarked(int face_index, int[] mark_x, int[] mark_y) {
            Log.d(TAG, "onLandmarked, face_index:" + face_index);
        }
    };

    private Runnable drawFaceInfoRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            int bitmapWidth = 0;
            int bitmapHeight = 0;
            if (agePredictInfoList.size() > 0) {
                bitmapWidth = agePredictInfoList.get(0).getBitmapWidth();
                bitmapHeight = agePredictInfoList.get(0).getBitmapHeight();
            }
//            Log.d(TAG, "drawFaceInfoRunnable, face_width:" + bitmapWidth + " face_height:" + bitmapHeight);
            Bitmap bitmap = Utils.drawAgeInfo(agePredictInfoList, bitmapWidth, bitmapHeight);
            if (bitmap != null) {
                imageViewForeground.setImageBitmap(bitmap);
            }
        }
    };

    private void initModel() {

        String faceDetector = strExternalCache + "/model/face_detector.csta";
        String landMarker = strExternalCache + "/model/face_landmarker_pts5.csta";
        String agePredictor = strExternalCache + "/model/age_predictor.csta";
        SeetaHandle.getInstance().initModels(faceDetector, landMarker, agePredictor);
    }

    private void checkPermission() {
        ArrayList permissionItems = new ArrayList<PermissionItem>();
        permissionItems.add(
                new PermissionItem(
                        Manifest.permission.CAMERA,
                        getString(R.string.permission_camera), R.drawable.permission_ic_camera
                )
        );
        permissionItems.add(
                new PermissionItem(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.permission_storage), R.drawable.permission_ic_storage
                )
        );
        permissionItems.add(
                new PermissionItem(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.permission_storage), R.drawable.permission_ic_storage
                )
        );
        HiPermission.create(this)
                .msg(getString(R.string.open_permission_to_use))
                .permissions(permissionItems)
                .style(R.style.PermissionDefaultBlueStyle)
                .animStyle(R.style.PermissionAnimScale)
                .checkMutiPermission(new  PermissionCallback(){
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "checkPermission onFinish");
                        initModel();
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        Toast.makeText(MainActivity.this, R.string.permission_fail, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
    }
}
