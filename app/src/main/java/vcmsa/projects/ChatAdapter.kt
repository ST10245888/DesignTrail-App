package vcmsa.projects.fkj_consultants.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.databinding.ItemMessageReceivedBinding
import vcmsa.projects.fkj_consultants.databinding.ItemMessageSentBinding
import vcmsa.projects.fkj_consultants.models.ChatMessage

private const val VIEW_SENT = 1
private const val VIEW_RECEIVED = 2

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(Diff()) {

    private val myId = FirebaseAuth.getInstance().uid

    override fun getItemViewType(position: Int): Int {
        val m = getItem(position)
        return if (m.senderId == myId) VIEW_SENT else VIEW_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_SENT) {
            SentVH(ItemMessageSentBinding.inflate(inf, parent, false))
        } else {
            RecVH(ItemMessageReceivedBinding.inflate(inf, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = getItem(position)
        when (holder) {
            is SentVH -> holder.bind(m)
            is RecVH -> holder.bind(m)
        }
    }

    private class SentVH(private val b: ItemMessageSentBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) { b.tvMessage.text = m.text }
    }
    private class RecVH(private val b: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessage) { b.tvMessage.text = m.text }
    }

    private class Diff : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
    }
}