package com.homeaway.homeawayplaces.droid.placelist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homeaway.homeawayplaces.domain.Place;
import com.homeaway.homeawayplaces.droid.R;
import com.jvanila.core.collection.IGenericList;
import com.jvanila.core.io.DataObject;
import com.jvanila.mobile.mvp.IBindableView;
import com.jvanila.mobile.util.BinderUtil;
import com.jvanila.mobile.util.IBindableViewVisitor;

/**
 * Created by pavan on 27/05/18.
 *
 */
public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListItemViewHolder> implements
        IBindableViewVisitor {

    private final LayoutInflater mInflater;
    private final View.OnClickListener mClickListener;

    private IGenericList<Place> mResultList;

    PlaceListAdapter(LayoutInflater inflater, View.OnClickListener clickListener) {
        mClickListener = clickListener;
        mInflater = inflater;
    }

    @NonNull
    @Override
    public PlaceListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.place_list_item_view, parent, false);
        view.setOnClickListener(mClickListener);
        return new PlaceListItemViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceListItemViewHolder holder, int position) {
        holder.onBind(mResultList.get(position));
    }

    @Override
    public int getItemCount() {
        return mResultList == null ? 0 : mResultList.size();
    }

    void notifyDataSetChanged(IGenericList<Place> results) {
        mResultList = results;
        notifyDataSetChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void release() {
        if (mResultList != null) {
            for (Place item : mResultList) {
                BinderUtil.traverse(item, this);
            }
        }
    }

    @Override
    public void visit(IBindableView bindableView, DataObject dataObject) {
        if (bindableView instanceof PlaceListItemViewHolder) {
            unbindReferences((PlaceListItemViewHolder) bindableView);
        }
    }

    private void unbindReferences(PlaceListItemViewHolder holder) {
        holder.onDestroy();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String stringify() {
        return toString();
    }
}
