package com.sdf.seetademo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sdf.seetademo.jni.SeetaHandle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "Main";

    private TextView tv;
    private Button btnAgePredict;
    private String strExternalCache;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        tv = findViewById(R.id.sample_text);
        tv.setText(SeetaHandle.getInstance().stringFromJNI());
        btnAgePredict = findViewById(R.id.btn_age_predict);
        btnAgePredict.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_age_predict:
                String imageUrl = strExternalCache + "/sdf.jpg";
                tv.setText("age: " + SeetaHandle.getInstance().predictAge(imageUrl));
                break;
        }
    }

    private void initModel() {
        strExternalCache = getExternalCacheDir().getAbsolutePath();
        try {
            new File(strExternalCache + "/test").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
