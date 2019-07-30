package com.example.imagegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class BlurFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    BlurListener mCallback;
    Button btBlur1X;
    Button btBlur2X;
    Button btBlur3X;
    Button btBlur4X;
    Button btDone;
    int current =0;
    boolean isDone = false;
    Bitmap photo;
    Bitmap preview;

    public BlurFragment()
    {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (BlurListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BlurListener");
        }
        isDone = false;
        current = 0;

    }

    @Override
    public void onDetach() {

        if(isDone == true)
        {
            mCallback.blurCode(current);
        }
        else
        {
            mCallback.blurCode(0);
        }



        mCallback = null; // avoid leaking
        btDone.setEnabled(false);
        btDone.setTextColor(Color.GRAY);


        /*if(photo!=null && !photo.isRecycled()) {
            //photo.recycle();
            //photo = null;
        }
        if(preview!=null && !preview.isRecycled()) {
            //preview.recycle();
            //preview = null;
        }*/

        super.onDetach();
    }

    public interface BlurListener {
        void onBlurChanged(int blur, Bitmap bitmap);
        void blurCode(int code);
    }

    @Override public void onStart() {
        super.onStart();

        Window window = this.getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_blur, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btBlur1X = view.findViewById(R.id.btBlur1X);
        btBlur1X.setOnClickListener(this);

        btBlur2X = view.findViewById(R.id.btBlur2X);
        btBlur2X.setOnClickListener(this);

        btBlur3X = view.findViewById(R.id.btBlur3X);
        btBlur3X.setOnClickListener(this);

        btBlur4X = view.findViewById(R.id.btBlur4X);
        btBlur4X.setOnClickListener(this);

        btDone = view.findViewById(R.id.btDone);
        btDone.setOnClickListener(this);

        btDone.setEnabled(false);
        btDone.setTextColor(Color.GRAY);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        preview = Bitmap.createBitmap(photo);
        switch (id) {
            case R.id.btBlur1X:
                selectBlurButton(1);
                mCallback.onBlurChanged(1, preview);
                current =1;
                break;

            case R.id.btBlur2X:
                selectBlurButton(2);
                mCallback.onBlurChanged(2, preview);
                current = 2;
                break;

            case R.id.btBlur3X:
                selectBlurButton(3);
                mCallback.onBlurChanged(3, preview);
                current =3;
                break;

            case R.id.btBlur4X:
                selectBlurButton(4);
                mCallback.onBlurChanged(4, preview);
                current = 4;
                break;

            case R.id.btDone:
                isDone = true;
                this.dismiss();
                break;
        }
    }

    private void selectBlurButton(int position)
    {
        enableAllButton();
        switch (position)
        {
            case 1:
                btBlur1X.setTextColor(Color.BLUE);
                btBlur1X.setEnabled(false);
                break;

            case 2:
                btBlur2X.setTextColor(Color.BLUE);
                btBlur2X.setEnabled(false);
                break;
            case 3:
                btBlur3X.setTextColor(Color.BLUE);
                btBlur3X.setEnabled(false);
                break;
            case 4:
                btBlur4X.setTextColor(Color.BLUE);
                btBlur4X.setEnabled(false);
                break;
        }

    }

    private void enableAllButton()
    {
        btBlur1X.setEnabled(true);
        btBlur2X.setEnabled(true);
        btBlur3X.setEnabled(true);
        btBlur4X.setEnabled(true);
        btDone.setEnabled(true);

        btBlur1X.setTextColor(Color.BLACK);
        btBlur2X.setTextColor(Color.BLACK);
        btBlur3X.setTextColor(Color.BLACK);
        btBlur4X.setTextColor(Color.BLACK);
        btDone.setTextColor(Color.BLUE);
    }

    public void setBitmap(Bitmap bitmap)
    {
        this.photo = Bitmap.createBitmap(bitmap);
        this.preview = Bitmap.createBitmap(bitmap);
    }

}
