package com.example.naser.thehelper.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.naser.thehelper.HistorySingleActivity;
import com.example.naser.thehelper.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView homeId ;
    public TextView time ;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        homeId=(TextView) itemView.findViewById(R.id.homeId);
        time=(TextView) itemView.findViewById(R.id.time);
    }

    @Override
    public void onClick(View view) {
        Intent intent= new Intent(view.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("homeId" , homeId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);

    }
}
