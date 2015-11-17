/**
 Stacks Flashcards - A flashcards application for Android devices 4.0+
 Copyright (C) 2014  Phani Gaddipati

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.centum.android.stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.cocosw.undobar.UndoBarController;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

import org.centum.android.HelpActivity;
import org.centum.android.MainActivity;
import org.centum.android.integration.ImportDialogFragment;
import org.centum.android.integration.communicators.QuizletCommunicator;
import org.centum.android.integration.communicators.StudyStackCommunicator;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.events.StackManagerEvent;
import org.centum.android.model.events.StackManagerListener;
import org.centum.android.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phani on 1/1/14.
 */
public class StackFragment extends Fragment implements AbsListView.MultiChoiceModeListener,
        SearchView.OnQueryTextListener, StackManagerListener, SwipeDismissListViewTouchListener.DismissCallbacks {

    private ListView stackList;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private MenuItem searchMenuItem;
    private StackListAdapter stackListAdapter;
    private ActionMode actionMode = null;
    private int numSelected = 0;
    private boolean deleteOnSwipe = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getString(R.string.stacks));
        View rootView = inflater.inflate(R.layout.stack_fragment, container, false);
        emptyTextView = (TextView) rootView.findViewById(R.id.empty_textView);
        emptyImageView = (ImageView) rootView.findViewById(R.id.empty_imageView);
        stackList = (ListView) rootView.findViewById(R.id.stack_list);
        stackListAdapter = new StackListAdapter(getActivity(), R.layout.stack_list_item);
        stackList.setAdapter(stackListAdapter);
        //stackList.setDivider(null);
        stackList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        stackList.setMultiChoiceModeListener(this);
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(stackList, this);
        stackList.setOnTouchListener(touchListener);
        stackList.setOnScrollListener(touchListener.makeScrollListener());
        stackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MainActivity) getActivity()).getCardFragment().setStack(((StackView) view).getStack());
                ((MainActivity) getActivity()).showCards();
            }
        });

        View.OnClickListener newStackListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addActionPerformed();
            }
        };
        emptyTextView.setOnClickListener(newStackListener);
        emptyImageView.setOnClickListener(newStackListener);
        StackManager.get().addListener(this);
        setHasOptionsMenu(false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEmptyViews();
    }

    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, android.view.MenuInflater inflater) {
        inflater.inflate(R.menu.stack, menu);
        setSearchMenuItem(menu.findItem(R.id.action_search_stacks));
    }

    public void setSearchMenuItem(MenuItem item) {
        searchMenuItem = item;
        if (searchMenuItem != null) {
            SearchView searchView = (SearchView) searchMenuItem.getActionView();
            if (searchView != null) {
                searchView.setOnQueryTextListener(this);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_stack:
                addActionPerformed();
                return true;
            case R.id.action_show_archived_stacks:
                showArchivedActionPerformed();
                return true;
            case R.id.action_import_quizlet:
                importQuizletActionPerformed();
                return true;
            case R.id.action_import_studystack:
                importStudyStackActionPerformed();
                return true;
            case R.id.action_show_settings:
                showSettings();
                return true;
            case R.id.action_show_help:
                showHelp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelp() {
        startActivity(new Intent(getActivity(), HelpActivity.class));
    }

    private void showSettings() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
    }

    private void restartActivity() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
            activity.startActivity(new Intent(activity, activity.getClass()));
        }
    }

    private void importQuizletActionPerformed() {
        ImportDialogFragment importDialogFragment = new ImportDialogFragment();
        importDialogFragment.setCommunicator(QuizletCommunicator.get());
        importDialogFragment.show(getFragmentManager(), "import_fragment");
    }

    private void importStudyStackActionPerformed() {
        ImportDialogFragment importDialogFragment = new ImportDialogFragment();
        importDialogFragment.setCommunicator(StudyStackCommunicator.get());
        importDialogFragment.show(getFragmentManager(), "import_fragment");
    }

    public void showArchivedActionPerformed() {
        Intent intent = new Intent(getActivity(), ArchivedStacksActivity.class);
        UndoBarController.clear(getActivity());
        startActivity(intent);
    }

    public void addActionPerformed() {
        new StackDialogFragment().show(getFragmentManager(), "new_stack");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        StackManager.get().getStack(i).setSelected(b);
        numSelected = stackList.getCheckedItemCount();
        if (actionMode != null) {
            actionMode.setTitle("Select Items");
            actionMode.setSubtitle(numSelected + " item" + (numSelected == 1 ? "" : "s") + " selected");
        }
    }


    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.stack_context, menu);
        this.actionMode = actionMode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_archive_stack:
                List<Stack> selectedStacks = new ArrayList<Stack>();
                for (int i = 0; i < StackManager.get().getNumberOfStacks(); i++) {
                    if (StackManager.get().getStack(i).isSelected()) {
                        selectedStacks.add(StackManager.get().getStack(i));
                        StackManager.get().getStack(i).setSelected(false);
                    }
                }
                for (Stack stack : selectedStacks) {
                    StackManager.get().removeStack(stack);
                }
                break;
            case R.id.action_merge_stacks:
                onMergeAction();
                break;
        }
        actionMode.finish();
        return true;
    }

    private void onMergeAction() {
        List<Stack> selectedStacks = new ArrayList<Stack>();
        for (int i = 0; i < StackManager.get().getNumberOfStacks(); i++) {
            if (StackManager.get().getStack(i).isSelected()) {
                selectedStacks.add(StackManager.get().getStack(i));
            }
        }
        if (selectedStacks.size() > 1) {
            MergeDialogFragment fragment = new MergeDialogFragment();
            fragment.setStacks(selectedStacks);
            fragment.show(getFragmentManager(), "merge_stacks");
        } else {
            new AlertDialog.Builder(getActivity()).setTitle("Oops!").setMessage("You must select more than 1 stack to do this!").show();
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        for (int i = 0; i < StackManager.get().getNumberOfStacks(); i++) {
            StackManager.get().getStack(i).setSelected(false);
        }
        this.actionMode = null;
        numSelected = 0;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchMenuItem.collapseActionView();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (!TextUtils.isEmpty(s) && searchMenuItem.isActionViewExpanded()) {
            stackList.post(new Runnable() {
                @Override
                public void run() {
                    stackList.setSelection(0);
                }
            });
            List<Stack> newOrder = new ArrayList<Stack>();
            for (Stack stack : StackManager.get().getStackList()) {
                if (stack.getName().toLowerCase().contains(s.toLowerCase().trim())) {
                    newOrder.add(stack);
                }
            }

            int pos = 0;
            for (Stack stack : newOrder) {
                if (StackManager.get().getStackPosition(stack) != 0) {
                    StackManager.get().moveStack(stack, pos);
                }
                pos++;
            }
        }
        return true;

    }

    public void startActionMode() {
        stackList.startActionMode(this);
    }

    public boolean isActionModeActivated() {
        return actionMode != null;
    }

    public ListView getStackList() {
        return stackList;
    }

    public void updateEmptyViews() {
        setEmptyViewsVisible(StackManager.get().getNumberOfStacks() == 0);
        stackListAdapter.notifyDataSetChanged();
    }

    private void setEmptyViewsVisible(final boolean b) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    emptyImageView.setVisibility(b ? View.VISIBLE : View.GONE);
                    emptyTextView.setVisibility(b ? View.VISIBLE : View.GONE);
                    emptyTextView.setText(StackManager.get().getNumberOfArchivedStacks() > 0 ? (getString(R.string.empty_stack_text) + "\n You have archived Stacks") : getString(R.string.empty_stack_text));
                }
            });
        }
    }

    @Override
    public boolean canDismiss(int position) {
        return actionMode == null;
    }

    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
        final List<Stack> removedStacks = new ArrayList<Stack>();

        deleteOnSwipe = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(SettingsActivity.KEY_PREF_STACK_SWIPE_DELETES, false);

        for (int position : reverseSortedPositions) {
            if (position < StackManager.get().getNumberOfStacks()) {
                removedStacks.add(StackManager.get().getStack(position));
            }
        }
        for (Stack stack : removedStacks) {
            if (deleteOnSwipe) {
                StackManager.get().removeStack(stack, false);
            } else {
                StackManager.get().removeStack(stack);
            }
        }

        if (getActivity() != null) {
            UndoBarController.show(getActivity(), deleteOnSwipe ? "Stack deleted." : "Stack archived.", new UndoBarController.UndoListener() {
                @Override
                public void onUndo(Parcelable token) {
                    for (Stack stack : removedStacks) {
                        if (deleteOnSwipe) {
                            StackManager.get().addStack(stack);
                        } else {
                            StackManager.get().restoreArchivedStack(stack);
                        }
                    }
                }
            }, UndoBarController.UNDOSTYLE);
        }
    }

    @Override
    public void eventFired(StackManagerEvent evt) {
        switch (evt.getEvent()) {
            case StackManager.EVENT_STACK_ADDED:
                stackAdded(evt.getTarget());
                break;
            case StackManager.EVENT_STACK_REMOVED:
                stackRemoved(evt.getTarget());
                break;
            case StackManager.EVENT_STACK_ARCHIVE_STATUS_CHANGED:
                if (StackManager.get().getStack(evt.getTarget().getName()) == null) {
                    stackRemoved(evt.getTarget());
                } else {
                    stackAdded(evt.getTarget());
                }
                break;
        }
    }

    public void stackAdded(Stack stack) {
        updateEmptyViews();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
        if (StackManager.get().getNumberOfStacks() == 1 && getActivity() != null) {
            ((MainActivity) getActivity()).getCardFragment().setStack(stack);
        }
    }

    public void stackRemoved(Stack stack) {
        updateEmptyViews();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }

        if (getActivity() != null) {
            if (((MainActivity) getActivity()).getCardFragment().getStack() == stack) {
                ((MainActivity) getActivity()).getCardFragment().setStack(null);
                if (StackManager.get().getNumberOfStacks() > 0 && getActivity() != null) {
                    ((MainActivity) getActivity()).getCardFragment().setStack(StackManager.get().getStack(0));
                }
            }
        }
    }
}
