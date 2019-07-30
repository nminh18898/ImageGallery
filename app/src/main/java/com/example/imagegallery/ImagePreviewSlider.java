package com.example.imagegallery;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagegallery.adapter.ImageSliderAdapter;
import com.example.imagegallery.database.GalleryDatabase;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.rongi.rotate_layout.layout.RotateLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.content.FileProvider;

public class ImagePreviewSlider extends DialogFragment  {

    private static final int SCALE_HEIGHT = 100;
    private static final long MEGABYTE = 1000000;
    private static final int SELECT_CONTACT = 1;
    private static final int PIC_CROP = 2;
    private static final int SIMPLE_CROP_TOOL = 0;

    ViewPager viewPager;
    TextView tvCount;
    ImageFolder imageFolder;
    int selectedPosition;
    Toolbar toolbar, bottomToolbar;

    ShareDialog shareDialog;
    WallpaperManager wallpaperManager;

    Bitmap bmFacebook;
    Bitmap bmBefore, bmAfter;
    boolean isReflect = false;
    ImageSliderAdapter adapterForSlider = new ImageSliderAdapter();
    int pictureDegrees=0;
    RotateLayout rotateLayout;

    boolean isVideo = false;

    CountDownTimer timer = new CountDownTimer(4000, 1000)
    {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            adapterForSlider.slideUp(toolbar, true);
            toolbar.setVisibility(View.INVISIBLE);

            adapterForSlider.slideDown(bottomToolbar, true);
            bottomToolbar.setVisibility(View.INVISIBLE);
        }
    };


    // constructor
    public static ImagePreviewSlider newInstance() {
        ImagePreviewSlider fragment = new ImagePreviewSlider();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_slider, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        tvCount = (TextView) v.findViewById(R.id.tvCount);
        imageFolder = (ImageFolder) getArguments().getSerializable("imageFolder");
        selectedPosition = getArguments().getInt("position");

        isVideo = imageFolder.isVideo();

        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        bottomToolbar = (Toolbar) v.findViewById(R.id.bottomToolbar);

        rotateLayout = v.findViewById(R.id.rotateLayout);

        createToolbar();
        //setHasOptionsMenu(true);

        updateGalleryLayout();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {

            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            public void onPageSelected(int position) {
                displayCount(position);
                selectedPosition = position;
            }
        });

        //v.setOnTouchListener(this);

        wallpaperManager = WallpaperManager.getInstance(getActivity());
        //shareDialog = new ShareDialog(getActivity());

        timer.start();
        return v;
    }



    private void createToolbar() {
        toolbar.inflateMenu(R.menu.image_menu);


        if(isVideo)
        {
            bottomToolbar.inflateMenu(R.menu.bottom_video_toolbar);
        }
        else {
            bottomToolbar.inflateMenu(R.menu.bottom_image_toolbar);
        }



        Menu menu = toolbar.getMenu();
        MenuItem item = menu.findItem(R.id.action_favorite);

        if(isVideo)
        {
            menu.removeItem(item.getItemId());
        }
        else
        {
            if (imageFolder.isFavorite && item != null) {
                item.setIcon(R.drawable.ic_action_unfavorite);
            }
        }

        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_favorite:
                        doActionFavorite();
                        break;
                }
                return true;
            }
        });

        bottomToolbar.setOverflowIcon(getActivity().getDrawable(R.drawable.ic_action_more_horiz));

        bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_edit:
                        Intent intent = new Intent(getActivity(), EditImageActivity.class);
                        intent.putExtra("imagePath", imageFolder.getImagePath().get(selectedPosition));
                        startActivity(intent);
                        break;

                    case R.id.action_set_wallpaper:
                        SetHomeScreen(imageFolder.getImagePath().get(selectedPosition));
                        //xuLyXoayAnh(imageFolder.getImagePath().get(selectedPosition));
                        break;

                    case R.id.action_set_lockscreen:
                        SetLockScreen(imageFolder.getImagePath().get(selectedPosition));
                        break;

                    case R.id.action_share:
                        //ShareFacebook(imageFolder.getImagePath().get(selectedPosition));
                        //PrintKeyHash();
                        sharePicture(imageFolder.getImagePath().get(selectedPosition));

                        break;
                    case R.id.action_set_contact_picture:
                        OpenContactPicture();
                        break;

                    case R.id.action_rename:
                        renamePictureAction();
                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageFolder.getImagePath().get(selectedPosition)))));
                        break;

                    case R.id.action_delete:
                        deletePhoto(imageFolder.getImagePath().get(selectedPosition));
                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(imageFolder.getImagePath().get(selectedPosition)))));
                        imageFolder.getImagePath().remove(selectedPosition);
                        updateGalleryLayout();
                        break;

                    case R.id.action_details:
                        getInfo(imageFolder.getImagePath().get(selectedPosition));
                        break;

                    case R.id.action_rotate_right_90:
                        pictureDegrees = rotateLayout.getAngle() + 90;
                        rotateLayout.setAngle(pictureDegrees);
                        break;

                    case R.id.action_flip_horizontal:
                        rotateLayout.setScaleX(rotateLayout.getScaleX()*-1);
                        break;

                    case R.id.action_flip_vertical:
                        rotateLayout.setScaleY(rotateLayout.getScaleY()*-1);
                        break;

                    case R.id.action_copy:
                        doActionCopyFile();
                        break;

                    case R.id.action_cut:
                        doActionCutFile();
                        break;

                }
                return true;
            }
        });

    }



    private void doActionCopyFile()
    {
        Intent intentChooseDestination = new Intent(getActivity(), DestinationFolderChosen.class);
        intentChooseDestination.putExtra("imagePath", imageFolder.getImagePath().get(selectedPosition));
        intentChooseDestination.putExtra("isCopy", true);
        intentChooseDestination.putExtra("isVideo", isVideo);
        startActivity(intentChooseDestination);

    }

    private void doActionCutFile()
    {
        Intent intentChooseDestination = new Intent(getActivity(), DestinationFolderChosen.class);
        intentChooseDestination.putExtra("imagePath", imageFolder.getImagePath().get(selectedPosition));
        intentChooseDestination.putExtra("isCopy", false);
        intentChooseDestination.putExtra("isVideo", isVideo);
        startActivity(intentChooseDestination);

    }



    private void sharePicture(String imagePath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        if(isVideo)
        {
            share.setType("video/*");
        }
        else
        {
            share.setType("image/*");
        }


        File imageFileToShare = new File(imagePath);

        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share, "Share via"));
    }

    private String getDirectoryFromPath(String path)
    {
        File file = new File(path);
        String dir = file.getParent();
        return dir;
    }

    private String getNameFromPath(String path)
    {
        File file = new File(path);
        String name = file.getName();
        return name;
    }

    private void renamePictureAction()
    {
        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_create_new_favorite_album,null);
        final EditText etName = view.findViewById(R.id.dialog_tvNewName);
        final String fileNameWithOutExt = imageFolder.getImagePath().get(selectedPosition).replaceFirst("[.][^.]+$", "");
        etName.setText(getNameFromPath(fileNameWithOutExt));

        //etName.requestFocus();
        etName.setSelectAllOnFocus(true);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        TextView tvInstruction = view.findViewById(R.id.dialog_tvInstruction);
        tvInstruction.setVisibility(View.INVISIBLE);
        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.rename));
        builder.setIcon(R.drawable.ic_photo);

        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    // new name equals to current name
                    if(etName.getText().toString().equals(getNameFromPath(fileNameWithOutExt)))
                    {

                    }else {
                        changePictureName(imageFolder.getImagePath().get(selectedPosition), etName.getText().toString());
                    }
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    dialog.cancel();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //InputMethodManager im = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dialog.cancel();

            }
        });

        // builder.show();
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


    private void changePictureName(String imagePath, String newName) {

        String extension = imagePath.substring(imagePath.lastIndexOf("."));
        File originalFile =  new File(imagePath);
        File fileChanged = new File(getDirectoryFromPath(imagePath) +"/" + newName + extension);


        if (fileChanged.exists()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.file_name_already_exists), Toast.LENGTH_SHORT).show();
           return;
        }

        //Check if file exist
        if (!originalFile.exists())
        {
            Toast.makeText(getActivity(),getResources().getString(R.string.file_not_found),Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(originalFile.renameTo(fileChanged)) {
                Toast.makeText(getActivity(), getResources().getString(R.string.rename_successfully), Toast.LENGTH_SHORT).show();
                ContentResolver resolver = getActivity().getContentResolver();
                resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?",new String[] { originalFile.getAbsolutePath() });
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(fileChanged)));

                ArrayList<String> listPath = imageFolder.getImagePath();
                listPath.set(selectedPosition, fileChanged.getAbsolutePath());
                updateGalleryLayout();
            }
        }



    }

    public void deletePhoto(String imagePath)
    {
        File originalFile =  new File(imagePath);

        //Check if file exist
        if (!originalFile.exists())
        {
            Toast.makeText(getActivity(),getResources().getString(R.string.file_not_found),Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Delete the original file
            originalFile.delete();
            Toast.makeText(getActivity(),getResources().getString(R.string.delete_successfully),Toast.LENGTH_SHORT).show();


        }
    }

    private void getInfo(String path) {

        File originalFile =  new File(path);
        String exif = "";
        exif+="\nFile name: " + originalFile.getName();

        if (!originalFile.exists())
        {
            Toast.makeText(getActivity(),getResources().getString(R.string.file_not_found),Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                ExifInterface exifInterface = new ExifInterface(path);
                float size = (float) (originalFile.length()) / 1024;
                float tieuCu=0;
                if(exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)!= null)
                {
                    String temp[] = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH).split("/");
                    tieuCu = Float.parseFloat(temp[0]) / Float.parseFloat(temp[1]);
                }



                exif += "\nResolution: " +
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) + "x" +
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);

                exif += "\nSize: " + size + "MB";

                exif += "\nTime: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME);

                exif += "\nManufacturer: " + exifInterface.getAttribute(ExifInterface.TAG_MAKE);

                if( exifInterface.getAttribute(ExifInterface.TAG_MODEL)!=null)
                    exif += "\n Model: " + exifInterface.getAttribute(ExifInterface.TAG_MODEL);

                exif += "\nOrientation: " + exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                if(tieuCu!=0) {
                    exif += "\nFocal length " + tieuCu + "mm";
                }


                if (exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) != null) {
                    exif += "\nGPS related:";
                    exif += "\nGPS_DATESTAMP: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                    exif += "\nGPS_TIMESTAMP: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                    exif += "\nGPS_LATITUDE: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                    exif += "\nGPS_LATITUDE_REF: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                    exif += "\nGPS_LONGITUDE: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                    exif += "\nGPS_LONGITUDE_REF: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                    exif += "\nGPS_PROCESSING_METHOD: " +
                            exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
                }

                exif+="\nFile path: " + originalFile.getAbsolutePath();
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Picture information");
                alertDialog.setMessage(exif);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }


    }

    /*private void PrintKeyHash()
    {
        try
        {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo("com.example.imagegallery",
                    PackageManager.GET_SIGNATURES);

            for (Signature signature: info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }*/

    private void OpenContactPicture()
    {
        Intent contactIntent = new Intent(Intent.ACTION_PICK,
                android.provider.ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactIntent,SELECT_CONTACT);
    }

    private void SetContactImage(Intent data, String imagePath)
    {
        //Get the raw contact uri
        Uri contactData = data.getData();
        Uri rawContactUri = GetRawContactUri(contactData);

        //Scale down bimap and Get the Bitmap picture from image Path
        byte[] byteArrayOfBitmap = FromImagePathToByteArray(imagePath);

        //Get the photo row
        int photoRow = GetContactPhotoRow(rawContactUri);

        //Set the bitmap to the raw data contact by using content value
        //Toast.makeText(context, Integer.toString(photoRow), Toast.LENGTH_SHORT).show();
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.RAW_CONTACT_ID,
                ContentUris.parseId(rawContactUri));
        values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArrayOfBitmap);
        values.put(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        if (photoRow >= 0) {
            try {
                getActivity().getContentResolver().update(
                        ContactsContract.Data.CONTENT_URI,
                        values,
                        ContactsContract.Data._ID + " = " + photoRow, null);
            }
            catch (Exception e)
            {
                Toast.makeText(getActivity(), getResources().getString(R.string.can_not_set_contact_picture), Toast.LENGTH_SHORT).show();
            }
        } else {
            getActivity().getContentResolver().insert(
                    ContactsContract.Data.CONTENT_URI,
                    values);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == getActivity().RESULT_OK)
        {
            if (requestCode == SELECT_CONTACT)
            {
                SetContactImage(data, imageFolder.getImagePath().get(selectedPosition));

            }

        }
    }

    private byte[] FromImagePathToByteArray(String imagePath)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        //Scale down the bitmap if the size of file is greater than 1 MB
        if (new File(imagePath).length() >= MEGABYTE)
        {
            final float densityMultiplier = getActivity().getResources().getDisplayMetrics().density;

            int h= (int) (SCALE_HEIGHT*densityMultiplier);
            int w= (int) (h * bitmap.getWidth()/((double) bitmap.getHeight()));

            bitmap  = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }

        //Change to byte array
        ByteArrayOutputStream streamy = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, streamy);
        byte[] byteArrayOfBitmap = streamy.toByteArray();

        return byteArrayOfBitmap;
    }

    private int GetContactPhotoRow(Uri rawContactUri)
    {
        int photoRow = -1;
        String where = ContactsContract.Data.RAW_CONTACT_ID + " == " +
                ContentUris.parseId(rawContactUri) + " AND " + ContactsContract.Data.MIMETYPE + "=='" +
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";

        Cursor cursor = getActivity().getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                where,
                null,
                null);
        int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
        if (cursor.moveToFirst()) {
            photoRow = cursor.getInt(idIdx);
        }
        cursor.close();
        return  photoRow;
    }

    private Uri GetRawContactUri(Uri contactData)
    {
        Uri rawContactUri = null;
        Cursor rawContactCursor =  getActivity().getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + " = " + contactData.getLastPathSegment(),
                null,
                null);

        if (!rawContactCursor.isAfterLast()) {
            rawContactCursor.moveToFirst();
            rawContactUri =
                    ContactsContract.RawContacts.CONTENT_URI.buildUpon().
                            appendPath("" + rawContactCursor.getLong(0)).build();
        }
        rawContactCursor.close();
        return rawContactUri;
    }

    private void SetHomeScreen(String imagePath)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            wallpaperManager.setBitmap(bitmap);
            Toast.makeText(getActivity(), getResources().getString(R.string.set_home_screen_successfully), Toast.LENGTH_SHORT).show();

        }
        catch (IOException e)
        {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        recycleBitmap(bitmap);

    }

    private void SetLockScreen(String imagePath)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        try{
            wallpaperManager.setBitmap(bitmap,null,true,WallpaperManager.FLAG_LOCK);
            Toast.makeText(getActivity(),getResources().getString(R.string.set_lock_screen_successfully),Toast.LENGTH_SHORT).show();
        }
        catch  (Exception e)
        {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        recycleBitmap(bitmap);

    }

    private void ShareFacebook(String imagePath)
    {
        bmFacebook = BitmapFactory.decodeFile(imagePath);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bmFacebook)
                .build();

        SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        shareDialog.show(sharePhotoContent);
        //recycleBitmap(bitmap);
    }


    void recycleBitmap(Bitmap bitmap)
    {
        if(bitmap != null)
        {
            bitmap.recycle();
        }
        bitmap=null;
    }

    private void doActionFavorite()
    {
        if(!imageFolder.isFavorite) {
            Intent intent = new Intent(getActivity(), FavoriteFolderChosen.class);
            intent.putExtra("imagePath", imageFolder.getImagePath().get(selectedPosition));
            startActivity(intent);
            return;
        }
        else
        {
            GalleryDatabase db = new GalleryDatabase(getActivity());
            if(db.deleteImageFavorite(imageFolder.getFolderName(),imageFolder.getImagePath().get(selectedPosition)))
            {
                Toast.makeText(getActivity(), getResources().getString(R.string.remove_from_favorite_album_successfully), Toast.LENGTH_SHORT).show();
                removeImagePath(selectedPosition);
                checkSelectedPosition();
                updateGalleryLayout();
            }
        }
    }

    private void checkSelectedPosition()
    {
        if(selectedPosition>imageFolder.getImagePath().size())
        {
            selectedPosition=imageFolder.getImagePath().size();
        }

        if(imageFolder.getImagePath().size() == 0)
        {
            selectedPosition=-1;
        }

    }

   private void updateGalleryLayout()
   {
       if(selectedPosition==-1 || imageFolder.getImagePath().size() <= 0)
       {
           getActivity().onBackPressed();
           return;
       }

       if(selectedPosition > imageFolder.getImagePath().size())
       {
           selectedPosition = imageFolder.getImagePath().size();
       }

       ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageFolder, toolbar, bottomToolbar, timer);
       viewPager.setAdapter(adapter);
       setCurrentItem(selectedPosition);
       displayCount(selectedPosition);
       adapterForSlider = adapter;
   }


    private void displayCount(int position) {
        tvCount.setText((position + 1) + "/" + imageFolder.getImagePath().size());
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayCount(position);
    }

    private void removeImagePath(int position)
    {
        imageFolder.getImagePath().remove(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recycleBitmap(bmFacebook);
        recycleBitmap(bmBefore);
        recycleBitmap(bmAfter);
    }
}
