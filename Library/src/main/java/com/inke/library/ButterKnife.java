package com.inke.library;

public class ButterKnife {

    // target.tv = target.findViewById(R.id.tv)
    public static void bind(Object object) {
        //类名: MainActivity$$ViewBinder
        String className = object.getClass().getName() + "$$ViewBinder";

        try {
            //类加载
            Class<?> clazz = Class.forName(className);

            //初始化APT生成的类
            ViewBinder viewBinder = (ViewBinder) clazz.newInstance();

            //执行其中的bind方法
            viewBinder.bind(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
