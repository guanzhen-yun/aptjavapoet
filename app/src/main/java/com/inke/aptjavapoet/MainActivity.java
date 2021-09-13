package com.inke.aptjavapoet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.inke.annotation.ARouter;
import com.inke.annotation.BindView;
import com.inke.annotation.HelloWorld;
import com.inke.library.ButterKnife;

@ARouter(path="/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        tv.setText("Hello APT + JavaPoet");
    }

    @HelloWorld
    public void hello() {

    }
}