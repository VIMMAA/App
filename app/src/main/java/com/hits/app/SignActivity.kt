package com.hits.app

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hits.moviecatalog.ui.welcomescreen.WelcomeFragment

class SignActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign)

        supportActionBar?.hide();
        val welcomeFragment = WelcomeFragment()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.root_container, welcomeFragment)
                .commitAllowingStateLoss()
        }
    }
}