package com.zh.dewuautomationscript;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        initViews();
    }

    private void initViews() {
        TextView tvHelpContent = findViewById(R.id.tvHelpContent);
        
        // 设置帮助内容
        String helpContent = getString(R.string.help_content);
        tvHelpContent.setText(helpContent);
    }
}
