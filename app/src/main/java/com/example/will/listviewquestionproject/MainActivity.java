package com.example.will.listviewquestionproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.list_view);
//        ImageAdapter adapter = new ImageAdapter(this, 0, Images.imageUrls);
//        ImageAdapter_1 adapter = new ImageAdapter_1(this, 0, Images.imageUrls);
        ImageAdapter_2 adapter = new ImageAdapter_2(this, 0, Images.imageUrls);
        listView.setAdapter(adapter);
    }
}
