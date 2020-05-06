package com.dreamfish.com.autocalc.item.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.dreamfish.com.autocalc.MainActivity;
import com.dreamfish.com.autocalc.R;
import com.dreamfish.com.autocalc.item.FunctionsListItem;
import com.dreamfish.com.autocalc.item.FunctionsListViewHolder;
import com.dreamfish.com.autocalc.utils.PixelTool;

import java.util.List;

import androidx.annotation.NonNull;

public class FunctionsListAdapter extends ArrayAdapter<FunctionsListItem> {

  private int layoytId;
  private Context context;

  public FunctionsListAdapter(Context context, int layoutId, List<FunctionsListItem> list) {
    super(context, layoutId, list);
    this.layoytId = layoutId;
    this.context = context;
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    final FunctionsListItem item = getItem(position);

    FunctionsListViewHolder viewHolder;

    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(layoytId, parent, false);
      viewHolder = new FunctionsListViewHolder();
      viewHolder.textViewTitle = convertView.findViewById(R.id.text_title);
      viewHolder.textViewExplain = convertView.findViewById(R.id.text_explan);

      convertView.setTag(viewHolder);
    } else {
      viewHolder = (FunctionsListViewHolder) convertView.getTag();
    }

    if(item != null) {
      viewHolder.textViewExplain.setText(item.explain);
      viewHolder.textViewTitle.setText(item.title);

      if(item.isHeader) {
        viewHolder.textViewTitle.setTextSize(12);
        viewHolder.textViewTitle.setTextColor(Color.GRAY);
      }else {
        viewHolder.textViewTitle.setTextSize(14);
        viewHolder.textViewTitle.setTextColor(Color.BLACK);
      }
    }


    return convertView;
  }
}
