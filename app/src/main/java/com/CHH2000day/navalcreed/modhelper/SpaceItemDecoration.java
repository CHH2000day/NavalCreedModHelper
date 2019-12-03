package com.CHH2000day.navalcreed.modhelper;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int margin = 8;


    public VerticalSpaceItemDecoration(int margin) {
        this.margin = margin;
    }

    public VerticalSpaceItemDecoration() {
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = margin;
        }
    }
}
