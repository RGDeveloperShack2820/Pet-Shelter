package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.R;


public class PetProvider extends ContentProvider {

     private PetDbHelper mDbhelper;

        public static final String LOG_TAG = PetProvider.class.getSimpleName();
        public static final UriMatcher sUriMatcher= new UriMatcher(UriMatcher.NO_MATCH);
        public static final int PETS = 100;
        public static final int PET_ID = 101;

    static{

         sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS, PETS);
         sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS+"/#", PET_ID);
     }

    @Override
    public boolean onCreate() {

        mDbhelper= new PetDbHelper(getContext());
        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbhelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {

            case PETS:


                cursor =database.query(PetsContract.PetsEntry.TABLE_NAME,
                                        projection,
                                        selection,
                                        selectionArgs,
                                        null,
                                        null,
                                        sortOrder);
                break;


            case PET_ID:

                selection = PetsContract.PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetsContract.PetsEntry.TABLE_NAME,
                                        projection,
                                        selection,
                                        selectionArgs,
                                        null,
                                        null,
                                        sortOrder);

                break;

            default:
                throw new IllegalArgumentException("Cannot Query Unknown uri" + uri);


        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Override
    public String getType( Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match){

            case PETS:
            {
                return PetsContract.PetsEntry.CONTENT_LIST_TYPE;
            }

            case PET_ID:
            {
                return PetsContract.PetsEntry.CONTENT_ITEM_TYPE;
            }


            default: throw new IllegalStateException("Unknown uri "+uri+ "with match"+match);
        }
    }

    @Override
    public Uri insert( Uri uri,  ContentValues values) {

        int match = sUriMatcher.match(uri);

        switch (match){

            case PETS:
            {
                return insertPet(uri,values);
            }

            default: throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }

    }

    private Uri insertPet(Uri uri, ContentValues values){

        String name = values.getAsString(PetsContract.PetsEntry.COLUMN_PET_NAME);

        if (name==null){

            throw new IllegalArgumentException("Pet name can not be null");
        }

        Integer gender= values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_GENDER);
        if (gender==null|| !PetsContract.PetsEntry.isValidGender(gender)){

            throw new IllegalArgumentException("Pet gender is invalid");

        }

        Integer weight=values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0){

            throw new IllegalArgumentException("Pet weight can not be null");
        }


        SQLiteDatabase database= mDbhelper.getWritableDatabase();

        long newRowId=  database.insert(PetsContract.PetsEntry.TABLE_NAME,null,values);
        if (newRowId==-1){
            Log.v(LOG_TAG,"failed to insert row for "+uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,newRowId);

    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {

        int deletedRows;
        SQLiteDatabase database = mDbhelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        switch (match){

            case PETS:
            {
                deletedRows= database.delete(PetsContract.PetsEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }

            case PET_ID:
            {
                selection = PetsContract.PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
               deletedRows= database.delete(PetsContract.PetsEntry.TABLE_NAME,selection,selectionArgs);
               break;
            }

            default: throw new IllegalArgumentException("Deletion is not supported for "+uri);
        }

        if (deletedRows!=0){

            getContext().getContentResolver().notifyChange(uri,null);
        }

        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int match = sUriMatcher.match(uri);

        switch (match){

            case PETS:
            {

                return updatePet(uri,values,selection,selectionArgs);

            }

            case PET_ID:
            {
                selection = PetsContract.PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,values,selection,selectionArgs);

            }

            default: throw new IllegalArgumentException("Updation is not supported for "+uri);
        }

    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(PetsContract.PetsEntry.COLUMN_PET_NAME)){

            String name = values.getAsString(PetsContract.PetsEntry.COLUMN_PET_NAME);

            if (name==null){

                throw new IllegalArgumentException("Pet name can not be null");
            }
        }

        if (values.containsKey(PetsContract.PetsEntry.COLUMN_PET_GENDER)){

            Integer gender= values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_GENDER);
            if (gender==null|| !PetsContract.PetsEntry.isValidGender(gender)){

                throw new IllegalArgumentException("Pet gender is invalid");

            }
        }

        if (values.containsKey(PetsContract.PetsEntry.COLUMN_PET_WEIGHT)){

            Integer weight=values.getAsInteger(PetsContract.PetsEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0){

                throw new IllegalArgumentException("Pet weight can not be null");
            }

        }

        if (values.size()==0){
            return 0;
        }

        SQLiteDatabase database = mDbhelper.getWritableDatabase();

        int updatedRows= database.update(PetsContract.PetsEntry.TABLE_NAME,values,selection,selectionArgs);

        if (updatedRows!=0){

            getContext().getContentResolver().notifyChange(uri,null);
        }

        return updatedRows;

    }
}
