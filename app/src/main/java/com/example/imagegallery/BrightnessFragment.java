package com.example.imagegallery;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagegallery.adapter.ColorPickerAdapter;

public class BrightnessFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    BrightnessListener mCallback;
    int currentBrightness = 50;
    SeekBar sbBrightness;

    public BrightnessFragment()
    {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (BrightnessListener) context;
            if(sbBrightness!=null)
            {
                currentBrightness = sbBrightness.getProgress();
            }

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BrightnessListener");
        }
    }

    @Override
    public void onDetach() {
        if(currentBrightness != sbBrightness.getProgress()) {
            mCallback.isBrightnessChanged(true);
        }
        mCallback = null; // avoid leaking
        super.onDetach();
    }

    public interface BrightnessListener {
        void onBrightnessChanged(int brightness);
        void isBrightnessChanged(boolean isChanged);

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_seekbar_and_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sbBrightness = view.findViewById(R.id.seekBar);
        TextView tvTitle = view.findViewById(R.id.tvTitle);

        sbBrightness.setProgress(50);
        sbBrightness.setOnSeekBarChangeListener(this);
        tvTitle.setText(getResources().getString(R.string.brightness));
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBar:
                mCallback.onBrightnessChanged(progress);
                break;

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
