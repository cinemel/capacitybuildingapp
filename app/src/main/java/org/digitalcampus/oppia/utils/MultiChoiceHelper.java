package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Helper class to reproduce ListView's modal MultiChoice mode with a RecyclerView.
 * Compatible with API 7+.
 * Declare and use this class from inside your Adapter.
 *
 * https://medium.com/@BladeCoder/implementing-a-modal-selection-helper-for-recyclerview-1e888b4cd5b9
 *
 * @author Christophe Beyls
 */
public class MultiChoiceHelper {

    /**
     * A handy ViewHolder base class which works with the MultiChoiceHelper
     * and reproduces the default behavior of a ListView.
     */
    public abstract static class ViewHolder extends RecyclerView.ViewHolder {

        View.OnClickListener clickListener;
        MultiChoiceHelper multiChoiceHelper;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(view -> {
                if (isMultiChoiceActive()) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        multiChoiceHelper.toggleItemChecked(position, false);
                        updateCheckedState(position);
                    }
                } else {
                    if (clickListener != null) {
                        clickListener.onClick(view);
                    }
                }
            });
            itemView.setOnLongClickListener(view -> {
                if ((multiChoiceHelper == null) || isMultiChoiceActive()) {
                    return false;
                }
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    multiChoiceHelper.setItemChecked(position, true, false);
                    updateCheckedState(position);
                }
                return true;
            });
        }

        public void updateCheckedState(int position) {
            final boolean isChecked = multiChoiceHelper.isItemChecked(position);
            if (itemView instanceof Checkable) {
                ((Checkable) itemView).setChecked(isChecked);
            } else {
                itemView.setActivated(isChecked);
            }
        }

        public void setOnClickListener(View.OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public void bind(MultiChoiceHelper multiChoiceHelper, int position) {
            this.multiChoiceHelper = multiChoiceHelper;
            if (multiChoiceHelper != null) {
                updateCheckedState(position);
            }
        }

        public boolean isMultiChoiceActive() {
            return (multiChoiceHelper != null) && (multiChoiceHelper.getCheckedItemCount() > 0);
        }
    }

    public interface MultiChoiceModeListener extends ActionMode.Callback {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param mode     The {@link ActionMode} providing the selection startSupportActionModemode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  <code>true</code> if the item is now checked, <code>false</code>
         *                 if the item is now unchecked.
         */
        void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked);
    }

    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;

    private final AppCompatActivity activity;
    private final RecyclerView.Adapter adapter;
    private SparseBooleanArray checkStates;
    private LongSparseArray<Integer> checkedIdStates;
    private int checkedItemCount = 0;
    private MultiChoiceModeWrapper multiChoiceModeCallback;
    private ActionMode choiceActionMode;

    /**
     * Make sure this constructor is called before setting the adapter on the RecyclerView
     * so this class will be notified before the RecyclerView in case of data set changes.
     */
    public MultiChoiceHelper(@NonNull AppCompatActivity activity, @NonNull RecyclerView.Adapter adapter) {
        this.activity = activity;
        this.adapter = adapter;
        adapter.registerAdapterDataObserver(new AdapterDataSetObserver());
        checkStates = new SparseBooleanArray(0);
        if (adapter.hasStableIds()) {
            checkedIdStates = new LongSparseArray<>(0);
        }
    }

    public Context getContext() {
        return activity;
    }

    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (listener == null) {
            multiChoiceModeCallback = null;
            return;
        }
        if (multiChoiceModeCallback == null) {
            multiChoiceModeCallback = new MultiChoiceModeWrapper();
        }
        multiChoiceModeCallback.setWrapped(listener);
    }

    public int getCheckedItemCount() {
        return checkedItemCount;
    }

    public boolean isItemChecked(int position) {
        return checkStates.get(position);
    }


    public void clearChoices() {
        if (checkedItemCount > 0) {
            final int start = checkStates.keyAt(0);
            final int end = checkStates.keyAt(checkStates.size() - 1);
            checkStates.clear();
            if (checkedIdStates != null) {
                checkedIdStates.clear();
            }
            checkedItemCount = 0;

            adapter.notifyItemRangeChanged(start, end - start + 1);

            if (choiceActionMode != null) {
                choiceActionMode.finish();
            }

        }
    }

    public void setItemChecked(int position, boolean value, boolean notifyChanged) {
        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value) {
            startSupportActionModeIfNeeded();
        }

        boolean oldValue = checkStates.get(position);
        checkStates.put(position, value);

        if (oldValue != value) {
            final long id = adapter.getItemId(position);

            if (checkedIdStates != null) {
                if (value) {
                    checkedIdStates.put(id, position);
                } else {
                    checkedIdStates.remove(id);
                }
            }

            if (value) {
                checkedItemCount++;
            } else {
                checkedItemCount--;
            }

            if (notifyChanged) {
                adapter.notifyItemChanged(position);
            }

            if (choiceActionMode != null) {
                multiChoiceModeCallback.onItemCheckedStateChanged(choiceActionMode, position, id, value);
                if (checkedItemCount == 0 && choiceActionMode != null) {
                    choiceActionMode.finish();
                }
            }
        }
    }

    public void toggleItemChecked(int position, boolean notifyChanged) {
        setItemChecked(position, !isItemChecked(position), notifyChanged);
    }



    private void startSupportActionModeIfNeeded() {
        if (choiceActionMode == null) {
            if (multiChoiceModeCallback == null) {
                throw new IllegalStateException("No callback set");
            }
            choiceActionMode = activity.startSupportActionMode(multiChoiceModeCallback);
        }
    }

    public static class SavedState implements Parcelable {

        int checkedItemCount;
        SparseBooleanArray checkStates;
        LongSparseArray<Integer> checkedIdStates;

        SavedState(Parcel in) {
            checkedItemCount = in.readInt();
            checkStates = in.readSparseBooleanArray();
            final int n = in.readInt();
            if (n >= 0) {
                checkedIdStates = new LongSparseArray<>(n);
                for (int i = 0; i < n; i++) {
                    final long key = in.readLong();
                    final int value = in.readInt();
                    checkedIdStates.append(key, value);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(checkedItemCount);
            out.writeSparseBooleanArray(checkStates);
            if (checkedIdStates == null){
                out.writeInt(-1);
                return;
            }
            out.writeInt(checkedIdStates.size());
            for (int i = 0; i < checkedIdStates.size(); i++) {
                out.writeLong(checkedIdStates.keyAt(i));
                out.writeInt(checkedIdStates.valueAt(i));
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }



    class AdapterDataSetObserver extends RecyclerView.AdapterDataObserver {

        private void confirmCheckedPositions() {
            if (checkedItemCount == 0) {
                return;
            }

            final int itemCount = adapter.getItemCount();
            boolean checkedCountChanged = false;

            if (itemCount == 0) {
                // Optimized path for empty adapter: remove all items.
                checkStates.clear();
                if (checkedIdStates != null) {
                    checkedIdStates.clear();
                }
                checkedItemCount = 0;
                checkedCountChanged = true;
            } else if (checkedIdStates != null) {
                // Clear out the positional check states, we'll rebuild it below from IDs.
                checkStates.clear();

                for (int checkedIndex = 0; checkedIndex < checkedIdStates.size(); checkedIndex++) {
                    final long id = checkedIdStates.keyAt(checkedIndex);
                    final int lastPos = checkedIdStates.valueAt(checkedIndex);

                    if ((lastPos >= itemCount) || (id != adapter.getItemId(lastPos))) {
                        // Look around to see if the ID is nearby. If not, uncheck it.
                        final int start = Math.max(0, lastPos - CHECK_POSITION_SEARCH_DISTANCE);
                        final int end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, itemCount);
                        boolean found = false;
                        for (int searchPos = start; searchPos < end; searchPos++) {
                            final long searchId = adapter.getItemId(searchPos);
                            if (id == searchId) {
                                found = true;
                                checkStates.put(searchPos, true);
                                checkedIdStates.setValueAt(checkedIndex, searchPos);
                                break;
                            }
                        }

                        if (!found) {
                            checkedIdStates.remove(id);
                            checkedIndex--;
                            checkedItemCount--;
                            checkedCountChanged = true;
                            if (choiceActionMode != null && multiChoiceModeCallback != null) {
                                multiChoiceModeCallback.onItemCheckedStateChanged(choiceActionMode, lastPos, id, false);
                            }
                        }
                    } else {
                        checkStates.put(lastPos, true);
                    }
                }
            } else {
                // If the total number of items decreased, remove all out-of-range check indexes.
                for (int i = checkStates.size() - 1; (i >= 0) && (checkStates.keyAt(i) >= itemCount); i--) {
                    if (checkStates.valueAt(i)) {
                        checkedItemCount--;
                        checkedCountChanged = true;
                    }
                    checkStates.delete(checkStates.keyAt(i));
                }
            }

            if (checkedCountChanged && choiceActionMode != null) {
                if (checkedItemCount == 0) {
                    choiceActionMode.finish();
                } else {
                    choiceActionMode.invalidate();
                }
            }
        }

        @Override
        public void onChanged() {
            confirmCheckedPositions();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            confirmCheckedPositions();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            confirmCheckedPositions();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            confirmCheckedPositions();
        }
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {

        private MultiChoiceModeListener wrapped;

        public void setWrapped(@NonNull MultiChoiceModeListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return wrapped.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return wrapped.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return wrapped.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            wrapped.onDestroyActionMode(mode);
            choiceActionMode = null;
            clearChoices();
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            wrapped.onItemCheckedStateChanged(mode, position, id, checked);
        }
    }
}
