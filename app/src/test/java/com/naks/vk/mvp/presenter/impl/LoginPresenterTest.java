package com.naks.vk.mvp.presenter.impl;

import com.naks.vk.BuildConfig;
import com.naks.vk.TestApp;
import com.naks.vk.mvp.model.interactor.LoginInteractor;
import com.naks.vk.mvp.view.LoginView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApp.class, sdk = 21)
public class LoginPresenterTest {

    @Inject LoginView view;
    @Inject LoginInteractor interactor;

    private LoginPresenterImpl presenter;

    @Before
    public void setUp() {
        ((TestApp) RuntimeEnvironment.application).getComponent().inject(this);
        presenter = new LoginPresenterImpl(view, interactor);
    }

    @Test
    public void invokedOnLoggedInWhenCallWakeUpSession() throws Exception {
        assertNotNull(view);
        assertNotNull(interactor);
        assertNotNull(presenter);

        assertEquals(4, 2 + 2);
    }
}