
package com.example.hmsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.hmsdemo.R;

public class BaseActivity extends AppCompatActivity {
    StringBuffer b = new StringBuffer();
    protected String tag ="" ;//GMS or HMS
    public static boolean active = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    public void showLog(String addLog) {
        b.append(tag).append('\n').append(addLog).append('\n');
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View tvView = findViewById(R.id.tv_log);
                View svView = findViewById(R.id.sv_log);
                if (tvView instanceof TextView) {
                    ((TextView) tvView).setText(b.toString());
                    Log.d("msg", b.toString());
                }
                if (svView instanceof ScrollView) {
                    ((ScrollView) svView).fullScroll(View.FOCUS_DOWN);
                }

            }
        });
    }

}
