package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;

public class RecyclerListView extends RecyclerView {

    private OnItemClickListener onItemClickListener;

    public RecyclerListView(@NonNull Context context) {
        super(context);
        initialize();
    }

    public RecyclerListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public RecyclerListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize(){
        setDivider();

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

                if (onItemClickListener == null){
                    return;
                }

                view.setOnClickListener(v -> {
                    ViewHolder holder = getChildViewHolder(v);
                    onItemClickListener.onItemClick(v, holder.getAdapterPosition(), v.getId());
                });

                view.setOnLongClickListener(v -> {
                    ViewHolder holder = getChildViewHolder(v);
                    onItemClickListener.onItemLongClick(v, holder.getAdapterPosition(), v.getId());
                    return true;
                });
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {

            }
        });
    }

    private void setDivider() {
        DividerItemDecoration divider = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nui_bottom_border_light));

        this.addItemDecoration(divider);
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, long id);

        void onItemLongClick(View view, int position, long id);
    }
}
