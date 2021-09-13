package com.inke.aptjavapoet;

import com.inke.library.ViewBinder;

public class MainActivity$$Test implements ViewBinder<MainActivity> {

    @Override
    public void bind(MainActivity target) {
         target.tv = target.findViewById(R.id.tv);
    }
}
