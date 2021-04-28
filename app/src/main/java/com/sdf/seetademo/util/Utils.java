package com.sdf.seetademo.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.sdf.seetademo.jni.AgePredictInfo;

import java.util.List;

public class Utils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap drawAgeInfo(List<AgePredictInfo> agePredictInfos, int imageWidth, int imageHeight) {
        if (agePredictInfos == null || agePredictInfos.size() <= 0){
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setTextSize(27);
        canvas.drawRect(0, 0, imageWidth, imageHeight, paint);
        Rect rect;
        for (int i=0; i<agePredictInfos.size(); i++) {
            rect = agePredictInfos.get(i).getFaceRect();
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(4f);
            canvas.drawRect(rect, paint);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(2.5f);
            canvas.drawText("age:" + agePredictInfos.get(i).getAge(), rect.left, rect.top, paint);
        }
        return bitmap;
    }
}
