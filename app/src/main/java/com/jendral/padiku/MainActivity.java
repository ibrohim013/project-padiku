package com.jendral.padiku;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;
    NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navView.getMenu().findItem(R.id.navigation_home).setVisible(true);

        if(preferences.getLevelData(this).equals("user")){
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(true);
        }else if(preferences.getLevelData(this).equals("admin")){
            navView.getMenu().findItem(R.id.navigation_home).setVisible(false);
            navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(true);
        }


        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    public void logout(MenuItem item) {
        preferences.clearData(this);
        navView.getMenu().findItem(R.id.navigation_home).setVisible(true);
        navView.getMenu().findItem(R.id.navigation_dashboard).setVisible(false);
        navView.getMenu().findItem(R.id.navigation_notifications).setVisible(true);
        navController.navigate(R.id.navigation_notifications);
    }
}
