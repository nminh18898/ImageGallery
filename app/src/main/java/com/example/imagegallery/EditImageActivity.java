package com.example.imagegallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagegallery.adapter.EditingToolsAdapter;
import com.example.imagegallery.adapter.FilterListener;
import com.example.imagegallery.adapter.FilterViewAdapter;
import com.example.imagegallery.adapter.ToolType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;



public class EditImageActivity extends BaseActivity implements OnPhotoEditorListener,
        PropertiesBSFragment.Properties,
        EmojiBSFragment.EmojiListener,
        View.OnClickListener,
        EditingToolsAdapter.OnItemSelected,
        FilterListener,
        BrightnessFragment.BrightnessListener, BlurFragment.BlurListener {

    private static final String TAG = EditImageActivity.class.getSimpleName();
    public static final String EXTRA_IMAGE_PATHS = "extra_image_paths";
    private static final int CAMERA_REQUEST = 52;
    private static final int PICK_REQUEST = 53;
    private static final int PIC_CROP = 2;
    private static final int SIMPLE_CROP_TOOL = 0;
    public static final String OVERWRITE_FILE = "overwrite_file";

    private int currentBlur = 0;
    private static final float BLUR_RADIUS_1X = 2.5f;
    private static final float BLUR_RADIUS_2X = 4.5f;
    private static final float BLUR_RADIUS_3X = 6.5f;
    private static final float BLUR_RADIUS_4X = 8.5f;

    private static final int FLIP_HORIZONTAL = 378;
    private static final int FLIP_VERTICAL = 379;

    private static final int ADD_VIEW = 450;
    private static final int CHANGE_SOURCE = 451;
    private static final int CHANGE_FILTER = 452;
    private static final int CHANGE_PHOTO_FILTER = 453;

    private ColorFilter originalColorFilter = new ColorFilter();

    private String tempPath;

    private PhotoEditor mPhotoEditor;
    private PhotoEditorView mPhotoEditorView;
    private PropertiesBSFragment mPropertiesBSFragment;
    private EmojiBSFragment mEmojiBSFragment;

    private BrightnessFragment mBrightnessFragment;
    private BlurFragment mBlurFragment;
    private TextView mTxtCurrentTool;
    private Typeface mWonderFont;
    private RecyclerView mRvTools, mRvFilters;
    private EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);
    private FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(this);
    private ConstraintLayout mRootView;
    private ConstraintSet mConstraintSet = new ConstraintSet();
    private boolean mIsFilterVisible;
    private String imagePath;
    Bitmap previewBitmap;
    Bitmap blurBitmap;

    PhotoFilter currentPhotoFilter;
    boolean isFilterAdded = false;

    ArrayList<ActionInfo> actionList = new ArrayList<>();
    ArrayList<ActionInfo> redoActionList = new ArrayList<>();

    ArrayList<Bitmap> bitmapList = new ArrayList<>();
    ArrayList<Bitmap>  redoList = new ArrayList<>();

    int currentAction = 0;
    boolean isClose = false;
    ArrayList<ColorFilter> colorFilter = new ArrayList<>();
    ArrayList<ColorFilter> redoColorFilter = new ArrayList<>();

    ArrayList<PhotoFilter> photoFilter = new ArrayList<>();
    ArrayList<PhotoFilter> redoPhotoFilter = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image);
        imagePath = getIntent().getExtras().getString("imagePath");
        initViews();
        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf");
        mPropertiesBSFragment = new PropertiesBSFragment();
        mPropertiesBSFragment.setPropertiesChangeListener(this);
        mEmojiBSFragment = new EmojiBSFragment();
        /*mStickerBSFragment = new StickerBSFragment();
        mStickerBSFragment.setStickerListener(this);*/
        mEmojiBSFragment.setEmojiListener(this);

        mBrightnessFragment = new BrightnessFragment();
        mBlurFragment = new BlurFragment();

        LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvTools.setLayoutManager(llmTools);
        mRvTools.setAdapter(mEditingToolsAdapter);

        LinearLayoutManager llmFilters = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvFilters.setLayoutManager(llmFilters);
        mRvFilters.setAdapter(mFilterViewAdapter);

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk
        mPhotoEditor.setOnPhotoEditorListener(this);

        mPhotoEditorView.getSource().setScaleType(ImageView.ScaleType.FIT_CENTER);
        mPhotoEditorView.getSource().setImageURI(Uri.parse(imagePath));
        previewBitmap = ((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap();
        originalColorFilter =  mPhotoEditorView.getSource().getColorFilter();

        currentPhotoFilter = PhotoFilter.NONE;

        mPhotoEditorView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                //Toast.makeText(EditImageActivity.this, "added" + currentView, Toast.LENGTH_SHORT).show();
                int id;
                try{
                    id = (int) child.getTag();
                }
                catch (ClassCastException e){
                    id =-1;
                }

                if(id == -1) {
                    child.setTag(currentAction);
                    addToActionList(ADD_VIEW, currentAction);
                }
                else{
                    addToActionList(ADD_VIEW, (int)child.getTag());
                    //Toast.makeText(EditImageActivity.this, "view add", Toast.LENGTH_SHORT).show();
                    //check current action
                }


            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
               // Toast.makeText(EditImageActivity.this, "removed" + child.getTag().toString(), Toast.LENGTH_SHORT).show();
                int viewID = (int) child.getTag();
                for(int i=0;i<actionList.size();i++)
                {
                    if(actionList.get(i).ID == viewID)
                    {
                        redoActionList.add(0,actionList.get(i));
                        actionList.remove(i);
                        return;
                    }
                }

            }
        });

        //bitmapList.add(previewBitmap);
    }

    private void initViews() {
        ImageView imgUndo;
        ImageView imgRedo;
        /*ImageView imgCamera;
        ImageView imgGallery;*/
        ImageView imgSave;
        ImageView imgClose;

        mPhotoEditorView = findViewById(R.id.photoEditorView);
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool);
        mRvTools = findViewById(R.id.rvConstraintTools);
        mRvFilters = findViewById(R.id.rvFilterView);
        mRootView = findViewById(R.id.rootView);

        imgUndo = findViewById(R.id.imgUndo);
        imgUndo.setOnClickListener(this);

        imgRedo = findViewById(R.id.imgRedo);
        imgRedo.setOnClickListener(this);

        /*imgCamera = findViewById(R.id.imgCamera);
        imgCamera.setOnClickListener(this);

        imgGallery = findViewById(R.id.imgGallery);
        imgGallery.setOnClickListener(this);*/

        imgSave = findViewById(R.id.imgSave);
        imgSave.setOnClickListener(this);

        imgClose = findViewById(R.id.imgClose);
        imgClose.setOnClickListener(this);

    }

    void showFilter(boolean isVisible) {
        mIsFilterVisible = isVisible;
        mConstraintSet.clone(mRootView);

        if (isVisible) {
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
        }

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(350);
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        TransitionManager.beginDelayedTransition(mRootView, changeBounds);

        mConstraintSet.applyTo(mRootView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgUndo:
                //mPhotoEditor.undo();
                if(actionList.size()<=0)
                {
                    return;
                }

                if(actionList.get(actionList.size()-1).changeCode == ADD_VIEW)
                {
                    mPhotoEditor.undo();
                }
                else if(actionList.get(actionList.size()-1).changeCode == CHANGE_SOURCE)
                {
                    undoChangeSource();

                }
                else if(actionList.get(actionList.size()-1).changeCode == CHANGE_FILTER)
                {
                    undoChangeFilter();
                }
                else if(actionList.get(actionList.size()-1).changeCode == CHANGE_PHOTO_FILTER)
                {
                    undoChangePhotoFilter();
                }

                /*if(actionList.size()>0) {
                    redoActionList.add(actionList.get(actionList.size() - 1));
                }*/
                if(isFilterAdded)
                {
                    handleFilterAdded();
                }

                break;

            case R.id.imgRedo:
                //mPhotoEditor.redo();
                if(redoActionList.size()<=0)
                {
                   return;
                }

                if(redoActionList.get(0).changeCode == ADD_VIEW)
                {
                    mPhotoEditor.redo();
                    redoActionList.remove(0);
                    //Toast.makeText(this, "redo", Toast.LENGTH_SHORT).show();
                }
                else if(redoActionList.get(0).changeCode == CHANGE_SOURCE)
                {
                    redoChangeSource();
                }
                else if(redoActionList.get(0).changeCode == CHANGE_FILTER)
                {
                    redoChangeFilter();
                }
                else if(redoActionList.get(0).changeCode == CHANGE_PHOTO_FILTER)
                {
                    redoChangePhotoFilter();
                }
                if(isFilterAdded)
                {
                    handleFilterAdded();
                }
                break;

            case R.id.imgSave:
                showDialogSaveFile();
                break;

            case R.id.imgClose:
                isClose = true;
                onBackPressed();
                break;
        }
    }

    public void actionBlurPhoto(float blurRadius, Bitmap blurBitmap) {
        blurBitmap = blurImage(this, blurBitmap, blurRadius);
        mPhotoEditorView.getSource().setImageBitmap(blurBitmap);
    }

    public Bitmap blurImage(Context context, Bitmap bitmapPhoto, float blurRadius) {
        float BITMAP_SCALE = 0.4f;
        int width = Math.round(bitmapPhoto.getWidth() * BITMAP_SCALE);
        int height = Math.round(bitmapPhoto.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmapPhoto, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(blurRadius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    @Override
    public void onToolSelected(ToolType toolType) {

        switch (toolType) {
            case DEFAULT:
                mPhotoEditorView.getSource().setImageURI(Uri.parse(imagePath));
                mPhotoEditor.clearAllViews();

                actionList.clear();
                redoActionList.clear();
                colorFilter.clear();
                redoColorFilter.clear();
                photoFilter.clear();
                redoPhotoFilter.clear();

                recycleBitmapList(bitmapList);
                recycleBitmapList(redoList);
                bitmapList = new ArrayList<>();
                redoList = new ArrayList<>();
                currentAction=0;
                currentPhotoFilter = PhotoFilter.NONE;
                break;

            case FILTER:
                mTxtCurrentTool.setText(R.string.label_filter);
                showFilter(true);
                break;

            case BRUSH:
                mPhotoEditor.setBrushDrawingMode(true);
                mTxtCurrentTool.setText(R.string.label_brush);
                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
                break;

            case EMOJI:
                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
                break;

            case TEXT:
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
                    @Override
                    public void onDone(String inputText, int colorCode) {
                        mPhotoEditor.addText(inputText, colorCode);
                        mTxtCurrentTool.setText(R.string.label_text);

                        //Toast.makeText(EditImageActivity.this, "test text", Toast.LENGTH_SHORT).show();
                    }
                });

                break;

            case ROTATE:
                //mPhotoEditor.brushEraser();
                //mTxtCurrentTool.setText(R.string.label_eraser);
                rotatePhoto();
                addToActionList(CHANGE_SOURCE,currentAction);
                if(isFilterAdded)
                {
                    handleFilterAdded();
                }
                break;

            case FLIP_HORIZONTAL:
                flipImage(FLIP_HORIZONTAL);
                addToActionList(CHANGE_SOURCE,currentAction);
                if(isFilterAdded)
                {
                    handleFilterAdded();
                }
                break;

            case FLIP_VERTICAL:
                flipImage(FLIP_VERTICAL);
                addToActionList(CHANGE_SOURCE,currentAction);
                if(isFilterAdded)
                {
                    handleFilterAdded();
                }
                break;

            case BLUR:
                previewBitmap = Bitmap.createBitmap(((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap());
                mBlurFragment.setBitmap(previewBitmap);
                mBlurFragment.show(getSupportFragmentManager(), mBlurFragment.getTag());
                break;

            case BRIGHTNESS:
                mBrightnessFragment.show(getSupportFragmentManager(), mBrightnessFragment.getTag());
                break;

            case CROP:
                CropImage(imagePath);
                addToActionList(CHANGE_SOURCE, currentAction);
                break;
        }
    }

    private void handleFilterAdded()
    {
        mPhotoEditor.setFilterEffect(currentPhotoFilter);
    }


    private void addToActionList(int changeCode, int id) {
        actionList.add(new ActionInfo(changeCode, id));
        if(changeCode == CHANGE_SOURCE)
        {
            bitmapList.add(((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap());
        }
        else if(changeCode == CHANGE_FILTER)
        {
            colorFilter.add(mPhotoEditorView.getSource().getColorFilter());
        }
        else if(changeCode == CHANGE_PHOTO_FILTER)
        {
            photoFilter.add(currentPhotoFilter);
        }

        currentAction++;
    }

    private void undoChangePhotoFilter()
    {
        redoPhotoFilter.add(0, currentPhotoFilter);
        redoActionList.add(0,actionList.get(actionList.size()-1));

        actionList.remove(actionList.size()-1);
        photoFilter.remove(photoFilter.size()-1);

        if(photoFilter.size() == 0)
        {
            mPhotoEditor.setFilterEffect(PhotoFilter.NONE);
            currentPhotoFilter = PhotoFilter.NONE;
        }
        else
        {
            mPhotoEditor.setFilterEffect(photoFilter.get(photoFilter.size() - 1));
            currentPhotoFilter = photoFilter.get(photoFilter.size() - 1);
        }
    }

    private void  undoChangeFilter()
    {
        redoColorFilter.add(0, mPhotoEditorView.getSource().getColorFilter());
        redoActionList.add(0,actionList.get(actionList.size()-1));

        actionList.remove(actionList.size()-1);
        colorFilter.remove(colorFilter.size()-1);

        if(colorFilter.size() ==0)
        {
            mPhotoEditorView.getSource().setColorFilter(originalColorFilter);
        }
        else
        {
            mPhotoEditorView.getSource().setColorFilter(colorFilter.get(colorFilter.size() - 1));
        }


    }

    private void undoChangeSource()
    {
        //actionList.add(new ActionInfo(false,id));
        redoList.add(0,((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap());
        redoActionList.add(0,actionList.get(actionList.size()-1));

        actionList.remove(actionList.size()-1);
        bitmapList.remove(bitmapList.size()-1);

        if(bitmapList.size() == 0)
        {
            mPhotoEditorView.getSource().setImageURI(Uri.parse(imagePath));
        }
        else {
            mPhotoEditorView.getSource().setImageBitmap(bitmapList.get(bitmapList.size() - 1));
        }
    }

    private void redoChangeFilter()
    {
        colorFilter.add(redoColorFilter.get(0));
        actionList.add(redoActionList.get(0));

        redoColorFilter.remove(0);
        redoActionList.remove(0);
        mPhotoEditorView.getSource().setColorFilter(colorFilter.get(colorFilter.size()-1));
    }

    private void redoChangePhotoFilter()
    {
        photoFilter.add(redoPhotoFilter.get(0));
        actionList.add(redoActionList.get(0));

        redoPhotoFilter.remove(0);
        redoActionList.remove(0);
        mPhotoEditor.setFilterEffect(photoFilter.get(photoFilter.size()-1));
        currentPhotoFilter = photoFilter.get(photoFilter.size()-1);
    }

    private void redoChangeSource()
    {
        bitmapList.add(redoList.get(0));
        actionList.add(redoActionList.get(0));

        redoList.remove(0);
        redoActionList.remove(0);
        mPhotoEditorView.getSource().setImageBitmap(bitmapList.get(bitmapList.size()-1));
    }

    private void CropImage(String imagePath) {
        try {
            //Call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");


           /* Uri imageUri = FileProvider.getUriForFile(this,
                    this.getApplicationContext().getPackageName() + ".provider",
                    new File(imagePath));*/
            Uri imageUri = getUriFromBitmap(((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap());

            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //Indicate image type and Uri
            cropIntent.setDataAndType(imageUri, "image/*");


            //Call the simple crop tool on device without creating intent chooser
            //First get all the app can be call from intent
            List<ResolveInfo> availableApps = this.getPackageManager().
                    queryIntentActivities(cropIntent, 0);
            if (availableApps.isEmpty()) {
                Toast.makeText(this, "Can not find any crop tool", Toast.LENGTH_SHORT).show();
                return;
            }

            //Set crop properties
            cropIntent.putExtra("crop", "true");

            //Set aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);

            //Indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);

            //Retrieve data on return
            cropIntent.putExtra("return-data", true);

            //Then get the Crop app and set to the intent(is the zero index of available apps)
            cropIntent.setPackage(availableApps.get(SIMPLE_CROP_TOOL).activityInfo.packageName);
            //Start activty for retreving the result
            startActivityForResult(cropIntent, PIC_CROP);
        } catch (Exception e) {
            Toast.makeText(this, "Your device doesn't support the crop action!",
                    Toast.LENGTH_SHORT).show();

        }

    }

    private static int calculateInSampleSize(
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

    private Uri getUriFromBitmap(Bitmap bitmap) {

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            tempPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
            return Uri.parse(tempPath);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_CROP ) {
            if (resultCode == RESULT_OK) {
                Bundle extra = data.getExtras();
                previewBitmap = extra.getParcelable("data");
                mPhotoEditorView.getSource().setImageBitmap(previewBitmap);
            }
            getContentResolver().delete(Uri.parse(tempPath),null, null);
                /*File originalFile =  new File(tempPath);
                originalFile.delete();*/
            MediaScannerConnection.scanFile(this, new String[] { Environment.getExternalStorageDirectory().toString() }, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri)
                {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
            if(isFilterAdded)
            {
                handleFilterAdded();
            }
        }

    }

    @Override
    public void onFilterSelected(PhotoFilter photoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter);
        currentPhotoFilter = photoFilter;
        isFilterAdded = true;
        addToActionList(CHANGE_PHOTO_FILTER, currentAction);
    }

    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
            @Override
            public void onDone(String inputText, int colorCode) {
                mPhotoEditor.editText(rootView, inputText, colorCode);
                mTxtCurrentTool.setText(R.string.label_text);
            }
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

        //Toast.makeText(this, mPhotoEditorView.getChildCount()+" add", Toast.LENGTH_SHORT).show();



    }



    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {
       // Toast.makeText(this, numberOfAddedViews+" remove", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {


    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false);
            mTxtCurrentTool.setText(R.string.app_name);
        } else if (!mPhotoEditor.isCacheEmpty()) {
            showDialogSaveFile();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onColorChanged(int colorCode) {
        mPhotoEditor.setBrushColor(colorCode);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onOpacityChanged(int opacity) {
        mPhotoEditor.setOpacity(opacity);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onBrushSizeChanged(int brushSize) {
        mPhotoEditor.setBrushSize(brushSize);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onEmojiClick(String emojiUnicode) {
        mPhotoEditor.addEmoji(emojiUnicode);
        mTxtCurrentTool.setText(R.string.label_emoji);
    }

    private void showDialogSaveFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final FileManipulator fm = new FileManipulator(this, imagePath);

        final View view = inflater.inflate(R.layout.dialog_save_file, null);
        final EditText etName = view.findViewById(R.id.dialog_tvNewName);
        final RadioGroup radSaveFile = view.findViewById(R.id.radSaveFile);
        final RadioButton radMakeNew = view.findViewById(R.id.radMakeNew);
        final RadioButton radOverwrite = view.findViewById(R.id.radOverwrite);
        radOverwrite.setChecked(true);
        etName.setEnabled(false);

        radSaveFile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == radMakeNew.getId()) {
                    etName.setEnabled(true);
                } else {
                    etName.setEnabled(false);
                }
            }
        });

        final String fileNameWithOutExt = fm.getNameWithoutExtension();
        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.save_file));
        builder.setIcon(R.drawable.ic_photo);

        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    // new name equals to current name
                    if (etName.getText().toString().equals(fileNameWithOutExt) || radOverwrite.isChecked()) {
                        saveImage(imagePath);
                        File photo = new File(imagePath);
                        Uri uri = Uri.fromFile(photo);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                         updateThumbnail(photo.getAbsolutePath());
                        //Toast.makeText(EditImageActivity.this, getResources().getString(R.string.file_overwritten), Toast.LENGTH_SHORT).show();
                    } else {
                        String pathChanged = etName.getText().toString();
                        String extension = fm.getExtension();
                        pathChanged = fm.getDirectoryFromPath() + "/" + pathChanged + extension;
                        saveImage(pathChanged);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(pathChanged))));
                        //Toast.makeText(EditImageActivity.this, pathChanged, Toast.LENGTH_SHORT).show();
                    }

                    if(isClose)
                    {
                        EditImageActivity.super.onBackPressed();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isClose)
                {
                    EditImageActivity.super.onBackPressed();
                }
            }
        });

        final AlertDialog mydialog = builder.create();
        mydialog.show();
        ((AlertDialog) mydialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

        TextWatcher textWatcher = new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    ((AlertDialog) mydialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) mydialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        };

        etName.addTextChangedListener(textWatcher);

    }



    private void saveImage(String path) {

        showLoading("Saving...");


        File file = new File(path);

        try {
            file.createNewFile();

            SaveSettings saveSettings = new SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(true)
                    .build();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mPhotoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    hideLoading();
                    showSnackbar("Image Saved Successfully");
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    hideLoading();
                    showSnackbar("Failed to save Image");
                }
            });
            Intent intent = new Intent(EditImageActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
            hideLoading();
            showSnackbar(e.getMessage());
        }
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        Log.e("brightness", brightness + "");
        mPhotoEditorView.getSource().setColorFilter(GetContrastBrightnessFilter((float) brightness / 30f, 2));

    }

    @Override
    public void isBrightnessChanged(boolean isChanged) {
        if(isChanged)
        {
            //Toast.makeText(this, "Changed", Toast.LENGTH_SHORT).show();
            addToActionList(CHANGE_FILTER,currentAction);

        }
    }


    private ColorMatrixColorFilter GetContrastBrightnessFilter(float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        return new ColorMatrixColorFilter(cm);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recycleBitmap(previewBitmap);
        recycleBitmap(blurBitmap);
        recycleBitmapList(bitmapList);
        recycleBitmapList(redoList);
    }

    void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = null;
    }

    void recycleBitmapList(ArrayList<Bitmap> bitmapForRecycle)
    {
        for(int i=0;i<bitmapForRecycle.size();i++)
        {
            recycleBitmap(bitmapForRecycle.get(i));
        }
        if(bitmapForRecycle!=null)
        {
            bitmapForRecycle.clear();
            bitmapForRecycle=null;
        }
    }

    @Override
    public void onBlurChanged(int blur, Bitmap photo) {
        blurBitmap = Bitmap.createBitmap(photo);
        switch (blur)
        {
            case 1:
                actionBlurPhoto(BLUR_RADIUS_1X, blurBitmap);
                break;

            case 2:
                actionBlurPhoto(BLUR_RADIUS_2X, blurBitmap);
                break;

            case 3:
                actionBlurPhoto(BLUR_RADIUS_3X, blurBitmap);
                break;

            case 4:
                actionBlurPhoto(BLUR_RADIUS_4X, blurBitmap);
                break;
        }
    }

    @Override
    public void blurCode(int code) {
        if(code == 0)
        {
            mPhotoEditorView.getSource().setImageBitmap(previewBitmap);
            //mPhotoEditorView.getSource().setImageURI(Uri.parse(imagePath));
        }
        else {
            addToActionList(CHANGE_SOURCE, currentAction);
        }
        if(isFilterAdded)
        {
            handleFilterAdded();
        }

    }

    private void rotatePhoto() {
        previewBitmap = ((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        //degrees+=90;
        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(previewBitmap,  previewBitmap.getWidth(),previewBitmap.getHeight(),true);
        previewBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        mPhotoEditorView.getSource().setImageBitmap(previewBitmap);
        recycleBitmap(scaledBitmap);
    }

    private void flipPhotoHorizontal()
    {
        previewBitmap = ((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.setScale(-1.0f, 1.0f);
        previewBitmap = Bitmap.createBitmap(previewBitmap, 0, 0,
                previewBitmap.getWidth(), previewBitmap.getHeight(), matrix, false);
        mPhotoEditorView.getSource().setImageBitmap(previewBitmap);
    }

    private void flipImage(int direction)
    {
        previewBitmap = ((BitmapDrawable) mPhotoEditorView.getSource().getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        if (direction == FLIP_HORIZONTAL) {
            matrix.setScale(-1.0f, 1.0f);
        }
        else if (direction == FLIP_VERTICAL)
        {
            matrix.setScale(1.0f, -1.0f);
        }
        previewBitmap = Bitmap.createBitmap(previewBitmap, 0, 0,
                previewBitmap.getWidth(), previewBitmap.getHeight(), matrix, false);
        mPhotoEditorView.getSource().setImageBitmap(previewBitmap);
    }

    private static void removeThumbnails(ContentResolver contentResolver, long photoId) {
        Cursor thumbnails = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Thumbnails.IMAGE_ID + "=?", new String[]{String.valueOf(photoId)}, null);
        for (thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()) {

            long thumbnailId = thumbnails.getLong(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails._ID));
            String path = thumbnails.getString(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            File file = new File(path);
            if (file.delete()) {
                contentResolver.delete(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, MediaStore.Images.Thumbnails._ID + "=?", new String[]{String.valueOf(thumbnailId)});
            }

        }
    }

    private void updateThumbnail(String photoName)
    {
        final String[] columns = {
                BaseColumns._ID, MediaStore.MediaColumns.DATA
        };

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,columns, null, null, null);

        boolean cancel = false;
        if(null != cursor){
            while(cursor.moveToNext() && !cancel){
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                int imageId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));

                if(fileName.equals(photoName)){
                    removeThumbnails(getContentResolver(), imageId);
                    cancel = true;
                }
            }
        }

        MediaScannerConnection.scanFile(EditImageActivity.this,
                new String[] { photoName }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    private class ActionInfo{
        public int changeCode;
        public int ID;

        public ActionInfo()
        {

        }

        public ActionInfo(int changeCode, int id)
        {
            this.changeCode = changeCode;
            this.ID = id;
        }


    }
}
