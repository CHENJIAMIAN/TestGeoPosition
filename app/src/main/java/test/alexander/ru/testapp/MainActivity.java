package test.alexander.ru.testapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button ButtonGeo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButtonGeo = (Button) findViewById(R.id.buttonGeo);

        View.OnClickListener onClickButtonGeo = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity(v);
            }
        };

        ButtonGeo.setOnClickListener(onClickButtonGeo);
    }

    public void goToActivity(View v) {
        Intent intent = new Intent(this, Activity_Geo.class);

        startActivity(intent);

    }
}
