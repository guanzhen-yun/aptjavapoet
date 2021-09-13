package com.inke.library;

public interface ViewBinder<T> {

    //绑定初始化控件
    void bind(T target);
}
