package com.example.android.pets.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.example.android.pets.CatalogActivity;

public class PetDbHelper extends SQLiteOpenHelper {


    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="Pets.db";

    public PetDbHelper(Context context) {

        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         String CREATE_TABLE="CREATE TABLE "+PetsContract.PetsEntry.TABLE_NAME+
                " ("+ PetsContract.PetsEntry.ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                PetsContract.PetsEntry.COLUMN_PET_NAME+" TEXT NOT NULL, "+
                PetsContract.PetsEntry.COLUMN_PET_BREED+" TEXT, "+
                PetsContract.PetsEntry.COLUMN_PET_GENDER+" INTEGER NOT NULL, "+
                PetsContract.PetsEntry.COLUMN_PET_WEIGHT+" INTEGER NOT NULL DEFAULT 0);";


         db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
