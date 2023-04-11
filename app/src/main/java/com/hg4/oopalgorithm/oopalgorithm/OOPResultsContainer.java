package com.hg4.oopalgorithm.oopalgorithm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/* loaded from: classes.dex */
public class OOPResultsContainer {
    String message;
    OOPResults[] oOPResultsArray = new OOPResults[0];
    int version = 1;

    /* JADX INFO: Access modifiers changed from: package-private */
    public String toGson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
