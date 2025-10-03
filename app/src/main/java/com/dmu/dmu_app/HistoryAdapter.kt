package com.dmu.dmu_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dmu.dmu_app.model.AcrModel

// AcrModel 타입의 데이터를 리스트 형태로 받습니다.
class HistoryAdapter(private val historyList: List<AcrModel>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // item_history.xml 에 있는 뷰들을 담아두는 클래스
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.imageView_album_cover)
        val title: TextView = itemView.findViewById(R.id.textView_title)
        val artist: TextView = itemView.findViewById(R.id.textView_artist)
    }

    // 아이템 뷰(item_history.xml)를 생성하고 ViewHolder를 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩(연결)
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.title.text = currentItem.acr_title
        holder.artist.text = currentItem.artists
        // TODO: Glide나 Picasso 같은 라이브러리를 사용해 앨범 커버 이미지 로딩
    }

    // 리스트에 있는 아이템의 총 개수를 반환합니다.
    override fun getItemCount() = historyList.size
}