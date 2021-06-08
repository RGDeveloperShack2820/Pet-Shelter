package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetsContract;


public class PetCursorAdapter extends CursorAdapter {

    PetCursorAdapter(Context context,Cursor c){
        super(context,c,0);

    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tv_name=view.findViewById(R.id.tv_name);
        TextView tv_summary=view.findViewById(R.id.tv_summary);

        int nameColumnIndex= cursor.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_NAME);
        int breedColumnIndex=cursor.getColumnIndex(PetsContract.PetsEntry.COLUMN_PET_BREED);

        String str_name= cursor.getString(nameColumnIndex);
        String str_breed= cursor.getString(breedColumnIndex);

        tv_name.setText(str_name);
        if (str_breed.isEmpty()){
            tv_summary.setText("Unknown Breed");
        }
        else {
            tv_summary.setText(str_breed);
        }
    }

}
