package com.HIVFactsheet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Brian on 7/2/2017.
 */

public class abortion extends AppCompatActivity  implements Animation.AnimationListener{
    private AlphaAnimation buttonClick = new AlphaAnimation(1F,0.8F);
    private WebView mWebView;
    Animation animZoomOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abortion);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tool = (TextView) toolbar.findViewById(R.id.title);
        tool.setText("All about Abortion");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        final LinearLayoutCompat mlinear = findViewById(R.id.line2);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mlinear.startAnimation(animZoomOut);
            }
        }, 0);
        Intent intent= new Intent(this, Aimportance.class);
        Button button= findViewById(R.id.button104);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intent= new Intent(abortion.this,Aimportance.class);
                startActivity(intent);
            }
        });
        Intent intent1= new Intent(this,Aunsafe.class);
        Button button1= findViewById(R.id.button105);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intent1= new Intent(abortion.this,Aunsafe.class);
                startActivity(intent1);
            }
        });
        Intent intent2= new Intent(this,Adefination.class);
        Button button2= findViewById(R.id.button1000);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intent2= new Intent(abortion.this,Adefination.class);
                startActivity(intent2);
            }
        });

        Intent intentttt= new Intent(this,Acompli.class);
        Button buttttn= findViewById(R.id.button107);
        buttttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intentttt= new Intent(abortion.this,Acompli.class);
                startActivity(intentttt);
            }
        });


        Intent intents= new Intent(this,Alaws.class);
        Button butto= findViewById(R.id.button108);
        butto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intents= new Intent(abortion.this,Alaws.class);
                startActivity(intents);
            }
        });

        Intent intentt= new Intent(this,Aquiz.class);
        Button buttn= findViewById(R.id.button21);
        buttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent intentt= new Intent(abortion.this,Aquiz.class);
                startActivity(intentt);
            }
        });

        // load the animation
        animZoomOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_out);

        // set animation listener
        animZoomOut.setAnimationListener(this);

        // button click event


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onAnimationEnd(Animation animation) {
        // Take any action after completing the animation

        // check for zoom in animation
        if (animation == animZoomOut) {
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }



}
