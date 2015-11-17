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
package org.centum.android.integration.communicators;

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.centum.android.integration.GenericSet;
import org.centum.android.model.Card;
import org.centum.android.model.Stack;
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
 * Created by Phani on 3/27/2014.
 */
public class StudyStackCommunicator implements Communicator {

    private static StudyStackCommunicator instance = null;
    private final Context context;
    private final HttpClient client = new DefaultHttpClient();

    private StudyStackCommunicator(Context context) {
        this.context = context;
    }

    public static StudyStackCommunicator get() {
        return instance;
    }

    public static StudyStackCommunicator init(Context context) {
        if (instance == null) {
            instance = new StudyStackCommunicator(context);
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Study Stack";
    }

    @Override
    public String getAttributionText() {
        return "Powered by StudyStack.com";
    }

    @Override
    public String getAttributionURL() {
        return "http://www.studystack.com";
    }

    @Override
    public boolean isStackAccessible(int setID) throws Exception {
        return getJSONObject(getSetURI(setID)) != null;
    }

    @Override
    public Stack getStack(int setID) throws Exception {
        JSONObject set = getJSONObject(getSetURI(setID));
        if (set != null) {
            Stack stack = new Stack(set.getString("name"));
            stack.setDescription(set.getString("description"));

            JSONArray terms = set.getJSONArray("data");
            JSONArray term;
            Card card;
            String string;
            for (int i = 0; i < terms.length(); i++) {
                term = terms.getJSONArray(i);
                if (term.length() > 0) {
                    card = new Card(term.getString(0));
                    string = "";
                    for (int t = 1; t < term.length(); t++) {
                        string += ", " + term.getString(t);
                    }
                    card.setDetails(string.substring(1));

                    stack.quickAddCard(card);
                }
            }
            return stack;
        }

        return null;
    }

    @Override
    public GenericSet[] getKeywordSets(String keyword) throws Exception {
        List<GenericSet> sets = new LinkedList<GenericSet>();

        JSONObject setsObj = getJSONObject(getKeywordURI(keyword));
        if (setsObj != null && setsObj.getInt("matches") > 0) {
            JSONArray setArray = setsObj.getJSONArray("results");

            JSONObject set;
            GenericSet genericSet;
            for (int i = 0; i < setArray.length(); i++) {
                try {
                    set = setArray.getJSONObject(i);
                    genericSet = new GenericSet();
                    genericSet.setTitle(set.getString("title"));
                    genericSet.setDescription(set.getString("description"));
                    genericSet.setId(set.getInt("docid"));
                    genericSet.setTermCount(-1);
                    genericSet.setType("Searched");
                    if (!sets.contains(genericSet))
                        sets.add(genericSet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sets.toArray(new GenericSet[sets.size()]);
    }

    @Override
    public GenericSet[] getUserSets(String username) throws Exception {
        List<GenericSet> sets = new LinkedList<GenericSet>();

        JSONArray userSets = getJSONArray(getUserURI(username));
        JSONObject set;
        if (userSets != null) {
            GenericSet genericSet;
            for (int i = 0; i < userSets.length(); i++) {
                try {
                    set = userSets.getJSONObject(i);
                    genericSet = new GenericSet();
                    genericSet.setTitle(set.getString("stackName"));
                    genericSet.setDescription(set.getString("description"));
                    genericSet.setId(set.getInt("id"));
                    genericSet.setTermCount(set.getInt("numCards"));
                    genericSet.setType("Personal");
                    if (!sets.contains(genericSet))
                        sets.add(genericSet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sets.toArray(new GenericSet[sets.size()]);
    }

    private URI getUserURI(String user) throws URISyntaxException {
        return new URI("https://www.studystack.com/servlet/userStackListJson?userName=" + user + "&strict=Y&appId=PhGa7210");
    }

    private URI getSetURI(int set) throws URISyntaxException {
        return new URI("https://www.studystack.com/servlet/json?studyStackId=" + set + "&strict=Y&appId=PhGa7210");
    }

    private URI getKeywordURI(String keyword) throws URISyntaxException {
        try {
            return new URI("https://www.studystack.com/SearchSets.jsp?q=" + URLEncoder.encode(keyword.trim(), "UTF-8") + "&size=75&page=1");
        } catch (UnsupportedEncodingException e) {
            return new URI("https://www.studystack.com/SearchSets.jsp?q=" + keyword.trim() + "&size=75&page=1");
        }
    }

    private JSONObject getJSONObject(URI uri) throws IOException, JSONException {
        String data = getJSONData(uri);
        if (TextUtils.isEmpty(data)) {
            return null;
        }

        return new JSONObject(data);
    }

    private JSONArray getJSONArray(URI uri) throws IOException, JSONException {
        String data = getJSONData(uri);
        if (TextUtils.isEmpty(data)) {
            return null;
        }

        return new JSONArray(data);
    }

    private String getJSONData(URI uri) throws IOException {
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

        return stringBuffer.toString();
    }
}
