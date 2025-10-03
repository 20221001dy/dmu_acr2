package com.dmu.dmu_app.ui.helper

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dmu.dmu_app.*
import com.dmu.dmu_app.util.AuthManager
import com.google.android.material.navigation.NavigationView

object NavigationDrawerHelper {

    fun setup(
        activity: Activity,
        drawerLayout: DrawerLayout,
        navView: NavigationView,
        toolbar: Toolbar
    ) {
        if (activity is AppCompatActivity) {
            activity.setSupportActionBar(toolbar)

            val toggle = ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
        }

        if (navView.headerCount == 0) {
            navView.inflateHeaderView(R.layout.nav_header)
        }

        updateHeader(activity, navView)
        updateMenu(navView, activity)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_login -> {
                    activity.startActivity(Intent(activity, LoginActivity::class.java))
                    true
                }
                R.id.nav_signup -> {
                    activity.startActivity(Intent(activity, SignupActivity::class.java))
                    true
                }
                R.id.nav_logout -> {
                    AuthManager.logout(activity)
                    Toast.makeText(activity, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                    activity.recreate()
                    true
                }
                R.id.nav_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // ✅ '마이페이지' 클릭 시 동작 추가
                R.id.nav_mypage -> {
                    activity.startActivity(Intent(activity, MyPageActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    fun updateHeader(activity: Activity, navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val textUserName = headerView.findViewById<TextView>(R.id.textUserName)
        if (AuthManager.isLoggedIn(activity)) {
            val userName = AuthManager.getUserName(activity) ?: "사용자"
            textUserName.text = "$userName 님"
        } else {
            textUserName.text = "로그인 필요"
        }
    }

    fun updateMenu(navView: NavigationView, activity: Activity) {
        val menu = navView.menu
        val loggedIn = AuthManager.isLoggedIn(activity)

        menu.findItem(R.id.nav_login)?.isVisible = !loggedIn
        menu.findItem(R.id.nav_signup)?.isVisible = !loggedIn
        menu.findItem(R.id.nav_logout)?.isVisible = loggedIn

        // ✅ 로그인 상태일 때만 '마이페이지' 메뉴가 보이도록 설정
        menu.findItem(R.id.nav_mypage)?.isVisible = loggedIn
    }
}