package ru.tlrs.xiphos.adapter;

import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

import ru.tlrs.xiphos.ancestors.AbstractORM;

/**
 * Created by thelongrunsmoke.
 */

public abstract class TableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private boolean mDataValid;

    private AbstractORM mTable;

    public TableAdapter(AbstractORM post) {
        mTable = post;
        mDataValid = mTable != null;
        DataSetObserver mDataSetObserver = new NotifyingDataSetObserver();
        if (mTable.getCount(mTable.getTableName()) != 0) {
            mTable.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mTable != null) {
            return mTable.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mTable != null && mTable.getCount() > position) {
            return position;
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public abstract void onBindViewHolder(VH viewHolder, AbstractORM table);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Something with data.");
        }
        if (mTable.getCount() < position) {
            onBindViewHolder(viewHolder, mTable);
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(viewHolder, mTable);
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
        }
    }
}
