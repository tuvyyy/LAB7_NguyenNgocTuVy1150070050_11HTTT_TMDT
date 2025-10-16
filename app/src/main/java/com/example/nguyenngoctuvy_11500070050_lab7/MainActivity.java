package com.example.nguyenngoctuvy_11500070050_lab7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText edtUrl;
    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUrl = findViewById(R.id.edtUrl);
        btnDownload = findViewById(R.id.btnDownload);

        btnDownload.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                Intent intent = new Intent(this, DownloadService.class);
                intent.putExtra("url", url);
                startService(intent);
            }
        });
    }
}
