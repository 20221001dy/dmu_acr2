package com.dmu.dmu_app

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmu.dmu_app.network.SupabaseClient
import com.dmu.dmu_app.util.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPageActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // UI 요소들을 코드와 연결합니다.
        toolbar = findViewById(R.id.mypage_toolbar)
        recyclerView = findViewById(R.id.recyclerView_history)

        // 툴바에 뒤로가기 버튼 등을 설정합니다.
        setupToolbar()

        // RecyclerView를 어떤 형태로 보여줄지 결정합니다. (세로 목록)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DB에서 데이터를 불러오는 함수를 실행합니다.
        loadHistory()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        supportActionBar?.title = "내 검색 기록" // 툴바 제목 설정
    }

    private fun loadHistory() {
        // 1. 현재 로그인한 사용자의 usercode를 가져옵니다.
        val userCode = AuthManager.getUserCode(this)
        if (userCode == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 코루틴을 사용해 백그라운드에서 네트워크 작업을 실행합니다.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 3. Supabase API를 호출해 검색 기록을 요청합니다.
                val response = SupabaseClient.instance.getHistoryByUserCode("eq.$userCode")

                // 4. UI 작업은 메인 스레드에서 처리합니다.
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val historyList = response.body()
                        if (!historyList.isNullOrEmpty()) {
                            // 5. 성공적으로 데이터를 받으면 어댑터에 넣고 RecyclerView에 연결!
                            val historyAdapter = HistoryAdapter(historyList)
                            recyclerView.adapter = historyAdapter
                        } else {
                            // 목록이 비어있을 경우
                            Toast.makeText(this@MyPageActivity, "검색 기록이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // API 호출 실패 시
                        Toast.makeText(this@MyPageActivity, "기록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // 기타 오류 발생 시
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPageActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 툴바의 뒤로가기 버튼이 눌렸을 때의 동작을 처리합니다.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // 현재 화면을 종료하고 이전 화면으로 돌아갑니다.
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}