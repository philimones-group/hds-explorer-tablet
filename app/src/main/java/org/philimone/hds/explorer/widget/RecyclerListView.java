package org.philimone.hds.explorer.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerListView extends RecyclerView {

    private List<OnItemClickListener> onItemClickListeners = new ArrayList<>();
    private boolean itemsSelectable = true;

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

                if (onItemClickListeners.size() == 0 || itemsSelectable == false){
                    return;
                }

                view.setOnClickListener(v -> {
                    ViewHolder holder = getChildViewHolder(v);
                    Log.d("click rec", ""+v);
                    fireOnItemClick(v, holder.getAdapterPosition(), v.getId());
                });

                view.setOnLongClickListener(v -> {
                    ViewHolder holder = getChildViewHolder(v);
                    fireOnItemLongClick(v, holder.getAdapterPosition(), v.getId());
                    return true;
                });
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {

            }
        });
    }

    public void setItemsSelectable(boolean selectable) {
        this.itemsSelectable = selectable;
    }

    private void setDivider() {
        DividerItemDecoration divider = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nui_bottom_border_light));

        this.addItemDecoration(divider);
    }

    public List<OnItemClickListener> getOnItemClickListeners() {
        return onItemClickListeners;
    }

    public void addOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListeners.add(onItemClickListener);
    }

    public void fireOnItemClick(View view, int position, long id) {
        if (onItemClickListeners.size() > 0) {
            for (OnItemClickListener listener : this.onItemClickListeners) {
                listener.onItemClick(view, position, id);
            }
        }
    }

    public void fireOnItemLongClick(View view, int position, long id) {
        if (onItemClickListeners.size() > 0) {
            for (OnItemClickListener listener : this.onItemClickListeners) {
                listener.onItemLongClick(view, position, id);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, long id);

        void onItemLongClick(View view, int position, long id);
    }
}
