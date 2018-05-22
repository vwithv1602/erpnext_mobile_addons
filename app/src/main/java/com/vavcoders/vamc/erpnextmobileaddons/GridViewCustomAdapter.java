package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import static com.vavcoders.vamc.erpnextmobileaddons.GlobalVariables.*;
/**
 * Created by vamc on 12/13/17.
 */

public class GridViewCustomAdapter extends ArrayAdapter{
    Context context;
    public GridViewCustomAdapter(Context context)
    {
        super(context, 0);
        this.context=context;

    }
    public int getCount()
    {
        return menu.length;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.grid_row, parent, false);


            TextView textViewTitle = (TextView) row.findViewById(R.id.textViewMenuItem);

            textViewTitle.setText(menu[position][1]);
        }



        return row;

    }
}
