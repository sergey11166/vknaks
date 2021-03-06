package com.naks.vk.di.component;

import com.naks.vk.di.anotation.PerFragment;
import com.naks.vk.di.module.NewsTabModule;
import com.naks.vk.ui.fragment.NewsTabsFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = NewsTabModule.class)
public interface NewsTabComponent {
    void inject(NewsTabsFragment fragment);
}
