package com.naks.vk.model.interactor;

import android.content.Context;

import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import javax.inject.Inject;

public class LoginInteractorImpl implements LoginInteractor {

    private Context context;

    @Inject public LoginInteractorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void wakeUpSession(final OnLoginFinishedListener listener) {
        VKSdk.wakeUpSession(context, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                switch (res) {
                    case LoggedOut:
                        listener.onLoggedOut();
                        break;
                    case LoggedIn:
                        listener.onLoggedIn();
                        break;
                    case Pending:
                        listener.onPending();
                        break;
                    case Unknown:
                        listener.onUnknown();
                        break;
                }
            }

            @Override
            public void onError(VKError error) {
            }
        });
    }
}
