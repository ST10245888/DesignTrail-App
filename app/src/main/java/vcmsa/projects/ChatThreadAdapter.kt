package vcmsa.projects.fkj_consultants.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.ChatThread
import java.text.SimpleDateFormat
import java.util.*

class ChatThreadAdapter(
    private var threads: MutableList<ChatThread>,
    private val onItemClick: (ChatThread) -> Unit
) : RecyclerView.Adapter<ChatThreadAdapter.ThreadViewHolder>() {

    private val TAG = "ChatThreadAdapter"

    /** Replace the entire thread list and sort by lastTimestamp descending */
    fun updateThreads(newThreads: List<ChatThread>) {
        threads.clear()
        threads.addAll(newThreads.sortedByDescending { it.lastTimestamp })
        notifyDataSetChanged()
        Log.d(TAG, "updateThreads: Thread list refreshed (${threads.size} threads)")
    }

    /** Add or update a thread, move it to top if updated or new */
    fun addOrUpdateThread(thread: ChatThread) {
        val existingIndex = threads.indexOfFirst { it.chatId == thread.chatId }
        if (existingIndex != -1) {
            threads.removeAt(existingIndex)
        }
        threads.add(0, thread) // newest on top
        notifyDataSetChanged()
        Log.d(TAG, "addOrUpdateThread: Added/Updated thread chatId=${thread.chatId}, unread=${thread.unreadCount}")
    }

    /** Sort all threads by timestamp descending (optional helper) */
    fun sortByTimestampDesc() {
        threads.sortByDescending { it.lastTimestamp }
        notifyDataSetChanged()
        Log.d(TAG, "sortByTimestampDesc: Threads sorted by latest timestamp")
    }

    /** Remove a thread by chatId */
    fun removeThread(chatId: String) {
        val index = threads.indexOfFirst { it.chatId == chatId }
        if (index != -1) {
            threads.removeAt(index)
            notifyItemRemoved(index)
            Log.d(TAG, "removeThread: Removed thread chatId=$chatId")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_thread, parent, false)
        return ThreadViewHolder(view)
    }

    override fun getItemCount(): Int = threads.size

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val thread = threads[position]
        holder.bind(thread)
        holder.itemView.setOnClickListener { onItemClick(thread) }
    }

    inner class ThreadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtUser: TextView = view.findViewById(R.id.txtUser)
        private val txtMessage: TextView = view.findViewById(R.id.txtLastMessage)
        private val txtTime: TextView = view.findViewById(R.id.txtTime)
        private val txtUnread: TextView = view.findViewById(R.id.txtUnread)

        fun bind(thread: ChatThread) {
            txtUser.text = "User: ${thread.userEmail}"
            txtMessage.text = thread.lastMessage.ifEmpty { "(No messages yet)" }
            txtTime.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(thread.lastTimestamp))

            // Show admin-specific unread count
            if (thread.unreadCount > 0) {
                txtUnread.visibility = View.VISIBLE
                txtUnread.text = thread.unreadCount.toString()
            } else {
                txtUnread.visibility = View.GONE
            }
        }
    }
}
