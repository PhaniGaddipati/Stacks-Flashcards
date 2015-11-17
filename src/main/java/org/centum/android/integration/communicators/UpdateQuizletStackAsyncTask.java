package org.centum.android.integration.communicators;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;

/**
 * Created by Phani on 5/10/2014.
 */
public class UpdateQuizletStackAsyncTask extends AsyncTask<Stack, Void, Boolean> {

    private final Context context;
    private final ProgressDialog progressDialog;

    public UpdateQuizletStackAsyncTask(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    public void onPreExecute() {
        progressDialog.setTitle("Updating Cards");
        progressDialog.setMessage("Please wait.");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Stack... stacks) {
        try {
            for (Stack stack : stacks) {
                if (stack.isQuizletStack()) {
                    Stack newStack = QuizletCommunicator.init(context).getStack(stack.getQuizletID());
                    if (newStack != null) {
                        for (Card newCard : newStack.getCardList()) {
                            boolean alreadyInStack = false;
                            for (Card card : stack.getCardList()) {
                                if (card.getQuizletTermID() == newCard.getQuizletTermID()) {
                                    alreadyInStack = true;
                                    card.setTitle(newCard.getTitle());
                                    card.setDetails(newCard.getDetails());
                                    card.setAttachment(newCard.getAttachment());
                                }
                            }
                            for (Card card : stack.getArchivedCards()) {
                                if (card.getQuizletTermID() == newCard.getQuizletTermID()) {
                                    alreadyInStack = true;
                                    card.setTitle(newCard.getTitle());
                                    card.setDetails(newCard.getDetails());
                                    card.setAttachment(newCard.getAttachment());
                                }
                            }
                            if (!alreadyInStack) {
                                stack.addCard(newCard.clone());
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPostExecute(Boolean ok) {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
