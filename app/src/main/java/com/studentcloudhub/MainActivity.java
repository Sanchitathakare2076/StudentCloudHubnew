package com.studentcloudhub;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

 BottomNavigationView nav;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);

  nav = findViewById(R.id.nav);

  // Do not use nav.inflateMenu() here
  // Menu is already loaded in activity_main.xml

  if (savedInstanceState == null) {
   getSupportFragmentManager()
           .beginTransaction()
           .replace(R.id.container, new HomeFragment())
           .commit();
  }

  nav.setOnItemSelectedListener(item -> {

   int id = item.getItemId();

   if (id == R.id.home) {
    openFragment(new HomeFragment());
    return true;

   } else if (id == R.id.subjects) {
    openFragment(new SubjectsFragment());
    return true;

   } else if (id == R.id.notes) {
    openFragment(new NotesFragment());
    return true;

   }  else if (id == R.id.profile) {
    openFragment(new ProfileFragment());
    return true;
   }

   return false;
  });
 }

 private void openFragment(Fragment fragment) {
  getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.container, fragment)
          .commit();
 }

 public void selectTab(int itemId) {
  if (nav != null) {
   nav.setSelectedItemId(itemId);
  }
 }
}