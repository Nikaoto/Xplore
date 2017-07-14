package com.xplore;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by Nika on 7/8/2017.
 *
 * აღწერა:
 * პიკასოს ბიბლიოთეკას უმატებს საკუთარ CircleTransformation-ს რომელიც იღებს მართკუთხა სურათს და
 * აბრუნებს მის წრიულ ვერსიას. ლამაზად და ნაზად ადიდებს view-ს ზომამდე.
 *
 * Description:
 * Adds a custom CircleTransformation to picasso transformations which takes a rectangular drawable
 * and returns a circular one. Scales smoothly to the view size.
 *
 */

public class CircleTransformation implements Transformation {

    private int viewSize, viewHeight, viewWidth;

    public CircleTransformation(int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
        this.viewSize = Math.min(width, height);
    }

    public CircleTransformation(int size) {
        this.viewWidth = size;
        this.viewHeight = size;
        this.viewSize = size;
    }

    @Override public Bitmap transform(Bitmap source) {
        Bitmap scaledSource = Bitmap.createScaledBitmap(source, viewWidth, viewHeight, true);
        Bitmap bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888);

        int sourceSize = Math.min(scaledSource.getWidth(), scaledSource.getHeight());
        int width = (scaledSource.getWidth() - sourceSize) / 2;
        int height = (scaledSource.getHeight() - sourceSize) / 2;
        /*
        int sourceSize = Math.min(viewWidth, viewHeight);
        int width = (viewWidth - sourceSize) / 2;
        int height = (viewHeight - sourceSize) / 2;
        */

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader =
                new BitmapShader(scaledSource, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (width != 0 || height != 0) {
            // source isn't square, move viewport to center
            Matrix matrix = new Matrix();
            matrix.setTranslate(-width, -height);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = viewSize / 2f;
        canvas.drawCircle(r, r, r, paint);
        scaledSource.recycle();
        source.recycle();

        return bitmap;
    }

    @Override public String key() {
        return "CropCircleTransformation()";
    }
}
