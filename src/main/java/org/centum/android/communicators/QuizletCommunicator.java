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
package org.centum.android.communicators;

import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.centum.android.integration.Communicator;
import org.centum.android.integration.GenericSet;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.utils.AttachmentHandler;
import org.centum.android.utils.KEYS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Phani on 2/18/14.
 */
public class QuizletCommunicator implements Communicator {

    private static final String SUFFIX = "?client_id=" + KEYS.QUIZLET_ACCESS_TOKEN;
    private static QuizletCommunicator instance = null;
    private Context context;

    private QuizletCommunicator(Context context) {
        this.context = context;
    }

    public static QuizletCommunicator getInstance(Context context) {
        if (instance == null) {
            instance = new QuizletCommunicator(context);
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Quizlet";
    }

    @Override
    public String getAttributionText() {
        return "Powered by Quizlet.com";
    }

    @Override
    public String getAttributionURL() {
        return "http://www.quizlet.com/";
    }

    public boolean isStackAccessible(int setID) {
        JSONObject set = null;
        try {
            set = getSetJSONObject(setID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (set != null && !set.has("error"));
    }

    public Stack getStack(int setID) throws JSONException, IOException, URISyntaxException {
        JSONObject set = getSetJSONObject(setID);
        if (set != null && !set.has("error")) {
            Stack stack = new Stack(set.getString("title"));
            stack.setDescription(set.getString("description"));
            stack.setQuizletStack(true);
            stack.setQuizletID(setID);

            JSONArray terms = set.getJSONArray("terms");
            JSONObject term;
            Card card;
            for (int i = 0; i < terms.length(); i++) {
                term = terms.getJSONObject(i);
                card = new Card(term.getString("term"), term.getString("definition"));
                try {
                    if (term.has("image") && !term.isNull("image")) {
                        card.setAttachment(AttachmentHandler.get(context).loadBitmapFromURL(term.getJSONObject("image").getString("url").replace("https", "http")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stack.quickAddCard(card);
            }
            return stack;
        }

        return null;
    }

    private JSONObject getSetJSONObject(int id) throws URISyntaxException, IOException, JSONException {
        return getJSONObject(getSetURI(id));
    }

    public GenericSet[] getKeywordSets(String keyword) throws JSONException, IOException, URISyntaxException {
        List<GenericSet> sets = new LinkedList<GenericSet>();

        JSONObject setsObj = getKeywordJSONObject(keyword);
        if (setsObj != null && !setsObj.has("error")) {
            JSONArray setArray = setsObj.getJSONArray("sets");

            JSONObject set;
            GenericSet genericSet;
            for (int i = 0; i < setArray.length(); i++) {
                try {
                    set = setArray.getJSONObject(i);
                    genericSet = new GenericSet();
                    genericSet.setTitle(set.getString("title"));
                    genericSet.setDescription(set.getString("description"));
                    genericSet.setId(set.getInt("id"));
                    genericSet.setTermCount(set.getInt("term_count"));
                    genericSet.setType("Searched");
                    if (!sets.contains(genericSet))
                        sets.add(genericSet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sets.toArray(new GenericSet[0]);
    }

    private JSONObject getKeywordJSONObject(String keyword) throws URISyntaxException, IOException, JSONException {
        return getJSONObject(getKeywordURI(keyword));
    }

    public GenericSet[] getUserSets(String username) throws URISyntaxException, IOException, JSONException {
        List<GenericSet> sets = new LinkedList<GenericSet>();

        JSONObject user = getUserJSONObject(username);
        if (user != null && !user.has("error")) {
            JSONArray setArray = user.getJSONArray("sets");

            JSONObject set;
            GenericSet genericSet;
            for (int i = 0; i < setArray.length(); i++) {
                try {
                    set = setArray.getJSONObject(i);
                    genericSet = new GenericSet();
                    genericSet.setTitle(set.getString("title"));
                    genericSet.setDescription(set.getString("description"));
                    genericSet.setId(set.getInt("id"));
                    genericSet.setTermCount(set.getInt("term_count"));
                    genericSet.setType("Personal");
                    if (!sets.contains(genericSet))
                        sets.add(genericSet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            setArray = user.getJSONArray("studied");
            for (int i = 0; i < setArray.length(); i++) {
                try {
                    set = setArray.getJSONObject(i).getJSONObject("set");
                    genericSet = new GenericSet();
                    genericSet.setTitle(set.getString("title"));
                    genericSet.setDescription(set.getString("description"));
                    genericSet.setId(set.getInt("id"));
                    genericSet.setTermCount(set.getInt("term_count"));
                    genericSet.setType("Studied");
                    if (!sets.contains(genericSet))
                        sets.add(genericSet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sets.toArray(new GenericSet[0]);
    }

    private JSONObject getUserJSONObject(String username) throws URISyntaxException, IOException, JSONException {
        return getJSONObject(getUserURI(username));
    }

    private JSONObject getJSONObject(URI uri) throws IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
        HttpGet request = new HttpGet();
        request.setHeader("Content-Type", "text/plain; charset=utf-8");
        request.setURI(uri);
        HttpResponse response = client.execute(request);
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer stringBuffer = new StringBuffer("");
        String line;

        String NL = System.getProperty("line.separator");

        while ((line = in.readLine()) != null) {
            stringBuffer.append(line + NL);
        }
        in.close();

        return new JSONObject(stringBuffer.toString());
    }

    private URI getKeywordURI(String keyword) throws URISyntaxException {
        try {
            return new URI("https://api.quizlet.com/2.0/search/sets?q=" + URLEncoder.encode(keyword, "UTF-8") + SUFFIX.replace("?", "&"));
        } catch (UnsupportedEncodingException e) {
            return new URI("https://api.quizlet.com/2.0/search/sets?q=" + keyword.trim() + SUFFIX.replace("?", "&"));
        }
    }

    private URI getUserURI(String username) throws URISyntaxException {
        return new URI("https://api.quizlet.com/2.0/users/" + username.trim() + SUFFIX);
    }

    private URI getSetURI(int set) throws URISyntaxException {
        return new URI("https://api.quizlet.com/2.0/sets/" + set + SUFFIX);
    }
}
