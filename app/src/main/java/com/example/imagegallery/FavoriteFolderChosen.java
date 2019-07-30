package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagegallery.adapter.FavoriteFoldersChosenAdapter;
import com.example.imagegallery.database.GalleryDatabase;

import java.util.ArrayList;

public class FavoriteFolderChosen extends AppCompatActivity {

    GridView gvFavoriteFolder;
    String imagePath;
    ArrayList<ImageFolder> imageFavoriteFolderList = new ArrayList<>();
    GalleryDatabase db = new GalleryDatabase(this);
    Toolbar toolbar;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_folder_chosen);
        toolbar = findViewById(R.id.favorite_folder_chosen_toolbar);
        gvFavoriteFolder = findViewById(R.id.gv_imageFavoriteFolders);
        title = findViewById(R.id.favorite_folder_chosen_tvTitle);
        title.setText(getResources().getString(R.string.add_to_favorites));

        imagePath =  getIntent().getExtras().getString("imagePath");

        updateGallery();

        //toolbar's creation
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gvFavoriteFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)
                {
                    createNewAlbum();
                }
                else {
                    for (int i = 0; i < imageFavoriteFolderList.get(position).getImagePath().size(); i++) {
                        if (imagePath.equals(imageFavoriteFolderList.get(position).getImagePath().get(i))) {
                            Toast.makeText(FavoriteFolderChosen.this, getResources().getString(R.string.this_album_has_already_contained_this_photo), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    db.insertImageFavorite(imageFavoriteFolderList.get(position).getFolderName(), imagePath);
                    updateGallery();
                }
            }
        });

    }


    private void createNewAlbum()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(FavoriteFolderChosen.this);
        LayoutInflater inflater = getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_create_new_favorite_album,null);
        final EditText etName = view.findViewById(R.id.dialog_tvNewName);
        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.create_new_album));
        builder.setIcon(R.drawable.ic_photo);
        builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.insertImageFavorite(etName.getText().toString(),imagePath);
                updateGallery();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // builder.show();
        final AlertDialog mydialog = builder.create();
        mydialog.show();
        ((AlertDialog) mydialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


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

    private void updateGallery()
    {
        imageFavoriteFolderList = db.getFolderAndImage();

        //create dummy item for create new album function
        ImageFolder dummy=null;
        imageFavoriteFolderList.add(0,dummy);

        FavoriteFoldersChosenAdapter adapter = new FavoriteFoldersChosenAdapter(this,imageFavoriteFolderList);
        gvFavoriteFolder.setAdapter(adapter);

    }
}
