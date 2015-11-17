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
package org.centum.android.card;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.google.android.apps.dashclock.ui.SwipeDismissGridViewTouchListener;

import org.centum.android.HelpActivity;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.model.events.StackEvent;
import org.centum.android.model.events.StackListener;
import org.centum.android.presentation.general.PlaySessionSettingsDialogFragment;
import org.centum.android.presentation.learn.LearnActivity;
import org.centum.android.presentation.mixed.MixedActivity;
import org.centum.android.settings.SettingsActivity;
import org.centum.android.sql.StacksDatabaseHelper;
import org.centum.android.stack.R;
import org.centum.android.stats.StatsActivity;
import org.centum.android.utils.Clipboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phani on 1/2/14.
 */
public class CardFragment extends Fragment implements AbsListView.MultiChoiceModeListener,
        SearchView.OnQueryTextListener, StackListener, SwipeDismissGridViewTouchListener.DismissCallbacks, View.OnClickListener {

    private Stack stack;
    private RelativeLayout fragmentContainer;
    private LinearLayout buttonBarLayout;
    private GridView cardList;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private MenuItem searchMenuItem;
    private TextView quizletLink;
    private Button learnButton, testButton;
    private CardListAdapter cardListAdapter;
    private ActionMode actionMode = null;
    private int numSelected = 0;
    private boolean deleteOnSwipe = false;
    private SwipeDismissGridViewTouchListener touchListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_fragment, container, false);

        fragmentContainer = (RelativeLayout) rootView.findViewById(R.id.frag_container);
        emptyTextView = (TextView) rootView.findViewById(R.id.empty_textView);
        emptyImageView = (ImageView) rootView.findViewById(R.id.empty_imageView);
        cardList = (GridView) rootView.findViewById(R.id.card_list);
        quizletLink = (TextView) rootView.findViewById(R.id.attribution_textview);
        buttonBarLayout = (LinearLayout) rootView.findViewById(R.id.button_bar);
        learnButton = (Button) rootView.findViewById(R.id.learn_button);
        testButton = (Button) rootView.findViewById(R.id.test_button);

        learnButton.setOnClickListener(this);
        testButton.setOnClickListener(this);

        cardList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        cardList.setMultiChoiceModeListener(this);
        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() != null && getFragmentManager() != null)
                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(SettingsActivity.KEY_PREF_CARD_TAP_PREVIEW, true)) {
                        CardPreviewDialogFragment fragment = new CardPreviewDialogFragment();
                        fragment.setCard(((CardView) view).getCard());
                        fragment.show(getFragmentManager(), "card_preview");
                    }
            }
        });


        ViewTreeObserver viewTreeObserver = cardList.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateColumns();
                }
            });
        }

        View.OnClickListener newStackListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addActionPerformed();
            }
        };
        emptyTextView.setOnClickListener(newStackListener);
        emptyImageView.setOnClickListener(newStackListener);

        if (savedInstanceState != null) {
            setStack(StackManager.get().getStack(savedInstanceState.getString("stack")));
            cardList.setScrollY(savedInstanceState.getInt("scroll"));
        }

        quizletLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuizlet();
            }
        });

        setStack(stack);
        setHasOptionsMenu(false);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (stack != null)
            savedInstanceState.putString("stack", stack.getName());
        savedInstanceState.putInt("scroll", cardList.getScrollY());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stack != null) {
            if (!StackManager.get().containsStack(stack)) {
                setStack(null);
            } else if (cardListAdapter != null) {
                cardListAdapter.updateStats();
            }
        } else {
            if (StackManager.get().getNumberOfStacks() > 0) {
                setStack(StackManager.get().getStack(0));
            }
        }
        updateEmptyViews();
        updateColumns();
    }

    public void updateColumns() {
        if (getActivity() != null && getView() != null) {
            float density = getActivity().getResources().getDisplayMetrics().density;
            if (getView().getWidth() > Math.round((float) 450 * density)) {
                cardList.setNumColumns(Math.round(getView().getWidth() / (450 * density)));
            } else {
                cardList.setNumColumns(1);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, android.view.MenuInflater inflater) {
        inflater.inflate(R.menu.card, menu);
        setSearchMenuItem(menu.findItem(R.id.action_search_cards));
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
    public void onPrepareOptionsMenu(Menu menu) {
        if (!Clipboard.get().hasContents()) {
            menu.removeItem(R.id.action_paste_card);
        } else if (menu.findItem(R.id.action_paste_card) == null) {
            menu.add(Menu.NONE, R.id.action_paste_card, Menu.NONE, android.R.string.paste);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_card:
                addActionPerformed();
                return true;
            case R.id.action_show_archived_cards:
                showArchiveActionPerformed();
                return true;
            case R.id.action_show_stats:
                showStatsActionPerformed();
                return true;
            case R.id.action_sort_by_name:
                sortByNameActionPerformed();
                return true;
            case R.id.action_shuffle_cards:
                shuffleCardsActionPerformed();
                return true;
            case R.id.action_sort_by_wrong:
                sortByWrongActionPerformed();
                return true;
            case R.id.action_sort_by_correct:
                sortByCorrectActionPerformed();
                return true;
            case R.id.action_paste_card:
                onPasteCardsAction();
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

    private void sortByNameActionPerformed() {
        if (stack != null) {
            stack.sortByName();
        }
    }


    private void sortByCorrectActionPerformed() {
        if (stack != null) {
            stack.sortByCorrect();
        }
    }

    private void sortByWrongActionPerformed() {
        if (stack != null) {
            stack.sortByWrong();
        }
    }

    private void shuffleCardsActionPerformed() {
        if (stack != null) {
            stack.shuffle();
        }
    }

    private void showStatsActionPerformed() {
        if (stack != null) {
            Intent intent = new Intent(getActivity(), StatsActivity.class);
            intent.putExtra("stack", stack.getName());
            startActivity(intent);
        }
    }

    private void showArchiveActionPerformed() {
        if (stack != null) {
            Intent intent = new Intent(getActivity(), ArchivedCardsActivity.class);
            intent.putExtra("stack", stack.getName());
            UndoBarController.clear(getActivity());
            startActivity(intent);
        }
    }

    private void addActionPerformed() {
        if (stack != null) {
            CardDialogFragment fragment = new CardDialogFragment();
            fragment.setStack(stack);
            fragment.show(getFragmentManager(), "new_card");
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        stack.getCard(i).setSelected(b);
        numSelected = cardList.getCheckedItemCount();
        if (actionMode != null) {
            actionMode.setTitle("Select Items");
            actionMode.setSubtitle(numSelected + " item" + (numSelected == 1 ? "" : "s") + " selected");
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.card_context, menu);
        if (!Clipboard.get().hasContents()) {
            menu.removeItem(R.id.action_paste_card);
        }
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
            case R.id.action_select_all_cards:
                onSelectAllCardsAction();
                break;
            case R.id.action_archive_card:
                onArchiveCardsAction();
                actionMode.finish();
                break;
            case R.id.action_swap_elements:
                onSwapElementsAction();
                actionMode.finish();
                break;
            case R.id.action_copy_card:
                onCopyCardsAction();
                actionMode.finish();
                break;
            case R.id.action_paste_card:
                onPasteCardsAction();
                actionMode.finish();
                break;
        }
        return true;
    }

    private void onSelectAllCardsAction() {
        for (int i = 0; i < stack.getNumberOfCards(); i++) {
            cardList.setItemChecked(i, true);
        }
    }

    private void onSwapElementsAction() {
        final List<Card> selectedCards = new ArrayList<Card>();
        for (int i = 0; i < stack.getNumberOfCards(); i++) {
            if (stack.getCard(i).isSelected()) {
                selectedCards.add(stack.getCard(i));
                stack.getCard(i).setSelected(false);
            }
        }

        if (selectedCards.size() > 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Swap Elements")
                    .setMessage("Do you want to swap the title and details of the selected Cards?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    for (Card card : selectedCards) {
                                        String title = card.getTitle();
                                        String details = card.getDetails();

                                        card.setTitle(details);
                                        card.setDetails(title);

                                        card.setAttachmentPartOfDetails(!card.isAttachmentPartOfDetails());
                                    }
                                }
                            };
                            StacksDatabaseHelper.get(null).executeBulkOperation(runnable);

                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
        }
    }

    private void onPasteCardsAction() {
        if (stack != null && Clipboard.get().getContents() != null) {
            final List<Card> cards = Clipboard.get().getContents();
            if (cards.size() > 0) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        for (Card card : cards) {
                            stack.addCard(card);
                        }
                    }
                };
                StacksDatabaseHelper.get(null).executeBulkOperation(runnable);
                Clipboard.get().clearClipboard();
                Toast.makeText(getActivity(), "Cards Pasted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "No Cards Copied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onCopyCardsAction() {
        List<Card> selectedCards = new ArrayList<Card>();
        for (int i = 0; i < stack.getNumberOfCards(); i++) {
            if (stack.getCard(i).isSelected()) {
                stack.getCard(i).setSelected(false);
                selectedCards.add(stack.getCard(i));
            }
        }
        if (selectedCards.size() > 0) {
            Clipboard.get().setContents(selectedCards);
            Toast.makeText(getActivity(), "Cards Copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void onArchiveCardsAction() {
        final List<Card> selectedCards = new ArrayList<Card>();
        for (int i = 0; i < stack.getNumberOfCards(); i++) {
            if (stack.getCard(i).isSelected()) {
                selectedCards.add(stack.getCard(i));
                stack.getCard(i).setSelected(false);
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (Card card : selectedCards) {
                    stack.removeCard(card);
                }
                updateEmptyViews();
            }
        };
        StacksDatabaseHelper.get(null).executeBulkOperation(runnable);
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        for (int i = 0; i < stack.getNumberOfCards(); i++) {
            stack.getCard(i).setSelected(false);
        }
        this.actionMode = null;
        numSelected = 0;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchMenuItem.collapseActionView();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (!TextUtils.isEmpty(s) && searchMenuItem.isActionViewExpanded()) {
            if (stack != null) {
                cardList.post(new Runnable() {
                    @Override
                    public void run() {
                        cardList.setSelection(0);
                    }
                });
                List<Card> newOrder = new ArrayList<Card>();
                for (Card card : stack.getCardList()) {
                    if (card.getTitle().toLowerCase().contains(s.toLowerCase().trim())) {
                        newOrder.add(card);
                    }
                }

                int pos = 0;
                for (Card card : newOrder) {
                    if (stack.getCardPosition(card) != 0) {
                        stack.moveCard(card, pos);
                    }
                    pos++;
                }
            }
        }
        return true;
    }

    public void startActionMode() {
        cardList.startActionMode(this);
    }

    public boolean isActionModeActivated() {
        return actionMode != null;
    }

    @Override
    public void eventFired(StackEvent evt) {
        switch (evt.getEvent()) {
            case Stack.EVENT_CARD_ADDED:
                cardsChanged();
                break;
            case Stack.EVENT_CARD_REMOVED:
                cardsChanged();
                break;
            case Stack.EVENT_CARD_ARCHIVE_STATUS_CHANGED:
                cardsChanged();
                break;
        }
    }

    public void cardsChanged() {
        updateEmptyViews();
        cardList.setFastScrollAlwaysVisible(stack.getNumberOfCards() > 100);
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void updateEmptyViews() {
        if (stack != null) {
            setEmptyViewsVisible(stack.getNumberOfCards() == 0);
            emptyTextView.setText("Add a Card to \"" + stack.getName() + "\"");
        } else {
            setEmptyViewsVisible(true);
            emptyTextView.setText("First select a stack!");
        }
    }

    private void setEmptyViewsVisible(boolean b) {
        emptyImageView.setVisibility(b ? View.VISIBLE : View.GONE);
        emptyTextView.setVisibility(b ? View.VISIBLE : View.GONE);
        buttonBarLayout.setVisibility(b ? View.GONE : View.VISIBLE);
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(final Stack stack) {
        if (this.stack != null) {
            this.stack.removeListener(this);
        }

        if (this.stack != stack) {
            this.stack = stack;

            if (stack != null) {
                quizletLink.setVisibility(View.GONE);
                cardListAdapter = new CardListAdapter(this, stack);
                cardList.setAdapter(cardListAdapter);
                touchListener = new SwipeDismissGridViewTouchListener(cardList, this);
                cardList.setOnTouchListener(touchListener);
                cardList.setOnScrollListener(touchListener.makeScrollListener());
                if (stack.getNumberOfCards() > 100) {
                    cardList.setFastScrollEnabled(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        cardList.setFastScrollAlwaysVisible(true);
                    }
                } else {
                    cardList.setFastScrollEnabled(false);
                    cardList.setFastScrollAlwaysVisible(false);
                }
                stack.addListener(this);
            } else {
                cardList.setAdapter(null);
                quizletLink.setVisibility(View.GONE);
            }
        }
        StackManager.get().setCurrentStack(stack);
        updateEmptyViews();

    }

    @Override
    public boolean canDismiss(int position) {
        return actionMode == null;
    }

    public void onDismiss(GridView gridView, int[] reverseSortedPositions) {
        final List<Card> removedCards = new ArrayList<Card>();

        deleteOnSwipe = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(SettingsActivity.KEY_PREF_CARD_SWIPE_DELETES, false);

        for (int position : reverseSortedPositions) {
            if (position < stack.getNumberOfCards()) {
                removedCards.add(stack.getCard(position));
            }
        }

        for (Card card : removedCards) {
            if (deleteOnSwipe) {
                stack.removeCard(card, false);
            } else {
                stack.removeCard(card);
            }
        }

        if (getActivity() != null) {
            UndoBarController.show(getActivity(), deleteOnSwipe ? "Card deleted." : "Card archived.", new UndoBarController.UndoListener() {
                @Override
                public void onUndo(Parcelable token) {
                    for (Card card : removedCards) {
                        if (deleteOnSwipe) {
                            stack.addCard(card);
                        } else {
                            stack.restoreArchivedCard(card);
                        }
                    }
                }
            });
        }
    }

    private void showQuizlet() {
        String _url = "http://quizlet.com/";
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(_url));
        startActivity(webIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.learn_button:
                startLearnActivity();
                break;
            case R.id.test_button:
                onTestActionPerformed();
                break;
        }
    }

    private void onTestActionPerformed() {
        if (stack != null) {
            if (stack.getNumberOfCards() < 4) {
                new AlertDialog.Builder(getActivity()).setTitle("Oops!")
                        .setMessage("You need at least 4 cards for this").show();
            } else {
                final PlaySessionSettingsDialogFragment dialogFragment = new PlaySessionSettingsDialogFragment();
                dialogFragment.setStack(stack);
                dialogFragment.setOnClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (dialogFragment.getPlaySession() != null) {
                            stack.addPlaySession(dialogFragment.getPlaySession());
                            if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .getBoolean(SettingsActivity.KEY_PREF_PLAY_SHUFFLE_TEST, true)) {
                                stack.shuffle();
                            }
                            Intent intent = new Intent(getActivity(), MixedActivity.class);
                            intent.putExtra("stack", stack.getName());
                            intent.putExtra("playsession", dialogFragment.getPlaySession().getId());
                            startActivity(intent);
                        }
                    }
                });
                dialogFragment.show(getFragmentManager(), "session_settings");
            }
        }
    }

    private void startLearnActivity() {
        Intent intent = new Intent(getActivity(), LearnActivity.class);
        intent.putExtra("stack", stack.getName());
        startActivity(intent);
    }
}
