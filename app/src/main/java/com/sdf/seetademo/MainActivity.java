package com.sdf.seetademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sdf.seetademo.jni.SeetaAgeCallback;
import com.sdf.seetademo.jni.SeetaHandle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback{
    private static final String TAG = "Main";
    public static final int MSG_PREDICT_AGE = 1;

    private TextView tv;
    private Button btnAgePredict;
    private String strExternalCache;
    private Handler threadHandler;
    private HandlerThread handlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        handlerThread = new HandlerThread("work-thread");
        handlerThread.start();
        threadHandler = new Handler(handlerThread.getLooper(), this);

        tv = findViewById(R.id.sample_text);
        tv.setText(SeetaHandle.getInstance().stringFromJNI());
        btnAgePredict = findViewById(R.id.btn_age_predict);
        btnAgePredict.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_age_predict:
                threadHandler.sendEmptyMessage(MSG_PREDICT_AGE);
                break;
        }
    }

    private SeetaAgeCallback seetaAgeCallback = new SeetaAgeCallback() {
        @Override
        public void onAgePredict(int face_index, int age) {
            Log.d(TAG, "onAgePredict, face_index:" + face_index + " ,age:" + age);
        }

        @Override
        public void onDetected(int size, int[] score, int[] face_x, int[] face_y, int[] face_width, int[] face_height) {
            Log.d(TAG, "onDetected, size:" + size);
            for (int i=0; i<size; i++) {
                Log.d(TAG, "onDetected, score:" + score[i] + " facex:" + face_x[i] + " facey:" + face_y[i] + " faceWidth:" + face_width[i] + " faceHeight:" + face_height[i]);

            }
        }

        @Override
        public void onLandmarked(int face_index, int[] mark_x, int[] mark_y) {
            Log.d(TAG, "onLandmarked, face_index:" + face_index);
        }
    };

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

    private void initModel() {
        strExternalCache = getExternalCacheDir().getAbsolutePath();

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
