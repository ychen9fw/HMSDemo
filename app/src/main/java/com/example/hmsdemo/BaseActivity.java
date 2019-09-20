
package com.example.hmsdemo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.hmsdemo.R;

public class BaseActivity extends AppCompatActivity {
    StringBuffer b = new StringBuffer();



    protected void showLog(String addLog) {
        b.append(addLog).append('\n');
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
