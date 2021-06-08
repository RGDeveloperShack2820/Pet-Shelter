/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int E_PET_LOADER = 0;
    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
    private Uri currentPetUri;
    private PetDbHelper mDbHelper;
    private boolean petHasChanged =false;

    private View.OnTouchListener mTouchListener= new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            petHasChanged =true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        currentPetUri= intent.getData();

        if (currentPetUri==null){

            setTitle(getString(R.string.editor_activity_title_new_pet));
        }
        else{

            setTitle(getString(R.string.editPet));

            LoaderManager.getInstance(this).initLoader(E_PET_LOADER,null,this);
        }



        setupSpinner();
        mDbHelper = new PetDbHelper(this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender =  PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentPetUri==null){

            MenuItem delete= menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // get and insert data
                String str_name=mNameEditText.getText().toString();
                if (TextUtils.isEmpty(str_name)){

                    Toast.makeText(EditorActivity.this, "Pet name can not be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    saveData();
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
               
                showDeleteConformationDailog();


                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home: {
                // Navigate back to parent activity (CatalogActivity)
                if (!petHasChanged) {

                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                showUnsavedChangesDailog();


                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteExistingPet() {

        if (currentPetUri!=null){

            int rowsDeleted= getContentResolver().delete(currentPetUri,null,null);

            if (rowsDeleted==0){

                Toast.makeText(this, getString(R.string.existing_deletion_failed), Toast.LENGTH_SHORT).show();
            }
            else{

                Toast.makeText(this,getString(R.string.existing_deletion_successfull) , Toast.LENGTH_SHORT).show();
            }

            finish();
        }


    }

    private void showUnsavedChangesDailog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.discard_dailog_box_message);
        builder.setPositiveButton(getString(R.string.discard_message), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        });

        builder.setNegativeButton(getString(R.string.keep_edit_message), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.setCancelable(true);
        AlertDialog dailog= builder.create();
        dailog.show();
    }

    private void showDeleteConformationDailog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.delete_conformation_message);
        builder.setPositiveButton(R.string.delete_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                deleteExistingPet();
            }
        });

        builder.setNegativeButton(R.string.cancel_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.setCancelable(true);
        AlertDialog dailog= builder.create();
        dailog.show();
    }


    @Override
    public void onBackPressed() {

        if (!petHasChanged) {

            super.onBackPressed();
        }
        else showUnsavedChangesDailog();


    }

    private void saveData() {

        String str_name=mNameEditText.getText().toString().trim();
        String str_breed=mBreedEditText.getText().toString().trim();

        int int_weight;
        try{
             int_weight=Integer.parseInt(mWeightEditText.getText().toString().trim());

        }catch (Exception e){

            int_weight=0;
        }

        int int_gender=mGender;


        ContentValues values=new ContentValues();
        values.put(PetsEntry.COLUMN_PET_NAME,str_name);
        values.put(PetsEntry.COLUMN_PET_BREED,str_breed);
        values.put(PetsEntry.COLUMN_PET_GENDER,int_gender);
        values.put(PetsEntry.COLUMN_PET_WEIGHT,int_weight);

        if (currentPetUri==null){

            Uri newUri=  getContentResolver().insert(PetsEntry.CONTENT_URI,values);

            if (newUri==null){
                Toast.makeText(this, getString(R.string.Pet_Not_Saved), Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(this, getString(R.string.Pet_Saved), Toast.LENGTH_SHORT).show();
        }
        else{

            int rowsAffected= getContentResolver().update(currentPetUri,values,null,null);

            if (rowsAffected==0){

                Toast.makeText(this, getString(R.string.pet_update_unsuccessful), Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, getString(R.string.pet_update_successful), Toast.LENGTH_SHORT).show();
            }
        }



    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {

        String[] projection= {PetsEntry._ID,
                            PetsEntry.COLUMN_PET_NAME,
                            PetsEntry.COLUMN_PET_BREED,
                            PetsEntry.COLUMN_PET_WEIGHT,
                            PetsEntry.COLUMN_PET_GENDER};



        return new CursorLoader(this,currentPetUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor==null|| cursor.getCount()<1){

            return;

        }

        if (cursor.moveToFirst()){

            int iName= cursor.getColumnIndex(PetsEntry.COLUMN_PET_NAME);
            int iBreed= cursor.getColumnIndex(PetsEntry.COLUMN_PET_BREED);
            int iWeight=cursor.getColumnIndex(PetsEntry.COLUMN_PET_WEIGHT);
            int iGender=cursor.getColumnIndex(PetsEntry.COLUMN_PET_GENDER);

            String str_name= cursor.getString(iName);
            String str_breed=cursor.getString(iBreed);
            int weight=cursor.getInt(iWeight);
            int gender=cursor.getInt(iGender);

            mNameEditText.setText(str_name);
            mBreedEditText.setText(str_breed);
            mWeightEditText.setText(Integer.toString(weight));

            switch (gender){

                case PetsEntry.GENDER_MALE:
                { mGenderSpinner.setSelection(1);
                    break;
                }

                case PetsEntry.GENDER_FEMALE:
                { mGenderSpinner.setSelection(2);
                    break;
                }

                default: {

                    mGenderSpinner.setSelection(0);
                    break;
                }
            }

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);

    }
}