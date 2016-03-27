package com.naks.vk.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.naks.vk.di.component.AppComponent;
import com.naks.vk.di.component.DaggerLoginComponent;
import com.naks.vk.di.module.LoginModule;
import com.naks.vk.presenter.LoginPresenter;
import com.naks.vk.view.LoginView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import javax.inject.Inject;

public class LoginActivity extends BaseActivity implements LoginView {

    private static final String[] sMyScope = new String[]{
            VKScope.FRIENDS,
            VKScope.PHOTOS,
            VKScope.DOCS,
            VKScope.STATUS,
            VKScope.NOTES,
            VKScope.PAGES,
            VKScope.WALL,
            VKScope.GROUPS,
            VKScope.MESSAGES,
            VKScope.EMAIL,
            VKScope.NOTIFICATIONS,
            VKScope.OFFLINE
    };

    @Inject LoginPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.wakeUpSession();
    }

    @Override
    protected void setupComponent(AppComponent appComponent) {
        DaggerLoginComponent.builder()
                .appComponent(appComponent)
                .loginModule(new LoginModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization
                presenter.onUserPassedAuthorization();
            }

            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                finish();
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void showLoginScreen() {
        VKSdk.login(this, sMyScope);
    }

    @Override
    public void navigateToMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
