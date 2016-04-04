package com.naks.vk.model.interactor;

import android.content.Context;
import android.util.Log;

import com.naks.vk.view.fragment.NewsTabsFragment;

import javax.inject.Inject;

public class NewsTabInteractorImpl implements NewsTabInteractor {

    private static final String TAG = "NewsTabInteractorImpl";

    @Inject
    Context context;

    public NewsTabInteractorImpl(NewsTabsFragment fragment) {
        fragment.getComponent().inject(this);
    }

    @Override
    public String testAction() {
        Log.d(TAG, context.toString());
        return "ResultTestAction";
    }
}