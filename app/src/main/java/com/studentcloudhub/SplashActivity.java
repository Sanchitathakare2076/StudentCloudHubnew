package com.studentcloudhub;
import android.content.Intent; import android.os.Bundle; import android.os.Handler; import androidx.appcompat.app.AppCompatActivity; import com.google.firebase.auth.FirebaseAuth;
public class SplashActivity extends AppCompatActivity {
 protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_splash);new Handler().postDelayed(()->{Intent i=new Intent(this,FirebaseAuth.getInstance().getCurrentUser()==null?AuthActivity.class:MainActivity.class);startActivity(i);finish();},1200);}
}