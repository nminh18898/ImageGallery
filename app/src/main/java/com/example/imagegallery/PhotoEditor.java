package com.example.imagegallery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;

public class PhotoEditor {
    Bitmap bitmapPhoto;

    public PhotoEditor(Bitmap bitmap)
    {
        this.bitmapPhoto = bitmap;
    }

    public PhotoEditor(String imagePath)
    {
        File image = new File(imagePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmapPhoto = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);


    }

    public void setBitmapPhoto(Bitmap bitmap_photo)
    {
        this.bitmapPhoto = bitmap_photo;
    }

    public Bitmap createBitmapFromPath(String image_path)
    {
        File image = new File(image_path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        return bitmap;
    }

    public Bitmap getBitmapPhoto()
    {
        return this.bitmapPhoto;
    }

    public RoundedBitmapDrawable roundConers(Resources res, float cornerRadius)
    {
        RoundedBitmapDrawable dr =  RoundedBitmapDrawableFactory.create(res, bitmapPhoto);
        dr.setCornerRadius(cornerRadius);
        return dr;
    }

    public void freeBitmaps() {
        if(bitmapPhoto!=null && !bitmapPhoto.isRecycled())
        {
            bitmapPhoto.recycle();
            bitmapPhoto = null;
        }

    }

    public Bitmap doGrayscale()
    {
        int width, height;
        height = bitmapPhoto.getHeight();
        width = bitmapPhoto.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmapPhoto, 0, 0, paint);
        return bmpGrayscale;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return  BitmapFactory.decodeFile(path, options);
    }

    public Bitmap blurImage(Context context)
    {
        float BITMAP_SCALE = 0.4f;
        float BLUR_RADIUS = 7.5f;
        int width = Math.round(bitmapPhoto.getWidth() * BITMAP_SCALE);
        int height = Math.round(bitmapPhoto.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmapPhoto, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;

    }


    protected void finalize() {
        freeBitmaps();
    }
}
