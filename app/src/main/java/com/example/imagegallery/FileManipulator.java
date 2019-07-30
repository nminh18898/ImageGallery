package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileManipulator {
    public static FileManipulator instance = null;
    private Context context;
    private String path;
    private String pathChanged="";


    public FileManipulator(Context context)
    {
        this.context = context;
    }

    public FileManipulator()
    {
    }

    public FileManipulator(Context context, String path)
    {
        this.path = path;
        this.context = context;
    }

    public static FileManipulator GetInstance()
    {
        if (instance == null)
        {
            instance     = new FileManipulator();
        }
        return instance;
    }


    public void copyFile(String inputPath,String fileName, String outputPath)
    {
        String newFileName ="";
        boolean isDuplicate = false;
        if(inputPath.equals(outputPath))
        {
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String name = fileName.replaceFirst("[.][^.]+$", "");
            newFileName = name+ "_copy" +extension;
            isDuplicate = true;
        }

        InputStream inFile;
        OutputStream outFile;
        try
        {
            File file = new File(outputPath);

            //Check if file doesnt exist
            if (!file.exists())
            {
                file.mkdir();//Make a new file directiory
            }

            if(isDuplicate)
            {
                inFile = new FileInputStream(inputPath + fileName);
                outFile = new FileOutputStream(outputPath + newFileName);
            }
            else {
                inFile = new FileInputStream(inputPath + fileName);
                outFile = new FileOutputStream(outputPath + fileName);
            }

            byte buffer[] = new byte[1024];
            int currentRead;
            while ((currentRead = inFile.read(buffer)) != -1)
            {
                outFile.write(buffer,0,currentRead);
            }

            inFile.close();
            inFile = null;


            //File copied
            outFile.flush();
            outFile.close();
            outFile = null;
            if(isDuplicate) {
                refreshGallery(inputPath + fileName);
                refreshGallery(outputPath + newFileName);
                Toast.makeText(context, context.getResources().getString(R.string.duplicate_file_successfully), Toast.LENGTH_SHORT).show();
            }
            else
            {
                refreshGallery(inputPath + fileName);
                refreshGallery(outputPath + fileName);
                Toast.makeText(context, context.getResources().getString(R.string.copy_file_successfully), Toast.LENGTH_SHORT).show();
            }


        }
        catch (FileNotFoundException e)
        {
            Log.e("tag",e.getMessage());
        }
        catch (IOException e)
        {
            Log.e("tag",e.getMessage());
            Toast.makeText(context, "Wrong Wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean moveFile(String inputPath,String fileName, String outputPath)
    {
        boolean isMoved = false;

        InputStream inFile;
        OutputStream outFile;

        try
        {
            File file = new File(outputPath);

            //Check if file doesnt exist
            if (!file.exists())
            {
                file.mkdir();//Make a new file directiory
            }

            inFile = new FileInputStream(inputPath + fileName);
            outFile = new FileOutputStream(outputPath + fileName );

            byte buffer[] = new byte[1024];
            int currentRead;
            while ((currentRead = inFile.read(buffer)) != -1)
            {
                outFile.write(buffer,0,currentRead);
            }

            inFile.close();
            inFile = null;

            outFile.flush();
            outFile.close();
            outFile = null;

            //Delete the original file
            File originalFile =  new File(inputPath + fileName);

            if (originalFile.delete())
            {
                isMoved = true;
            }


            refreshGallery(inputPath + fileName);
            refreshGallery(outputPath + fileName);

        }
        catch (FileNotFoundException e)
        {
            Log.e("Expcetion:",e.getMessage());
        }
        catch (IOException e)
        {
            Log.e("Exception:",e.getMessage());
        }
        return isMoved;
    }


    public void refreshGallery(String PhotoPath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(PhotoPath);
        Uri contentUri = Uri.fromFile(file);
        scanIntent.setData(contentUri);
        context.sendBroadcast(scanIntent);
    }

    public String getDirectoryFromPath()
    {
        File file = new File(path);
        String dir = file.getParent();
        return dir;
    }

    public String getNameFromPath()
    {
        File file = new File(path);
        String name = file.getName();
        return name;
    }

    public String getExtension()
    {
        return path.substring(path.lastIndexOf("."));
    }

    public String getNameWithoutExtension()
    {
        String fileNameWithOutExt = path.replaceFirst("[.][^.]+$", "");
        return fileNameWithOutExt;
    }

}
