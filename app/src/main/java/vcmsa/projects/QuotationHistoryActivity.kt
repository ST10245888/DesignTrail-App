package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import java.text.SimpleDateFormat
import java.util.*

data class LocalPdf(val displayName: String, val uri: Uri, val sizeBytes: Long, val dateAddedSec: Long)

class QuotationHistoryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private val items = mutableListOf<LocalPdf>()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_history)
        recycler = findViewById(R.id.recyclerHistory)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = HistoryAdapter(items) { pdf ->
            val i = Intent(this, QuotationPreviewActivity::class.java)
            i.putExtra("pdf_uri", pdf.uri.toString())
            startActivity(i)
        }
        loadPdfs()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadPdfs() {
        items.clear()
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.DATE_ADDED
        )
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
        val args = arrayOf("Quotation_%.pdf")
        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, args,
            "${MediaStore.Downloads.DATE_ADDED} DESC")
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Downloads.DATE_ADDED)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol)
                val size = it.getLong(sizeCol)
                val date = it.getLong(dateCol)
                val contentUri = Uri.withAppendedPath(uri, id.toString())
                items.add(LocalPdf(name, contentUri, size, date))
            }
        }
        recycler.adapter?.notifyDataSetChanged()
    }

    class HistoryAdapter(
        private val data: List<LocalPdf>,
        private val onOpen: (LocalPdf) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.tvFileName)
            val meta: TextView = v.findViewById(R.id.tvMeta)
            val btnOpen: Button = v.findViewById(R.id.btnOpenHistory)
        }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
            val v = LayoutInflater.from(p.context).inflate(R.layout.item_history_quote, p, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, i: Int) {
            val it = data[i]
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val added = if (Build.VERSION.SDK_INT >= 29) sdf.format(Date(it.dateAddedSec * 1000)) else ""
            h.name.text = it.displayName
            h.meta.text = "Size: ${"%.2f".format(it.sizeBytes/1024f/1024f)} MB  •  $added"
            h.btnOpen.setOnClickListener { onOpen(it) }
        }

        private fun onOpen(it: View?) {
            TODO("Not yet implemented")
        }

        override fun getItemCount() = data.size
    }
}
