package vcmsa.projects.fkj_consultants.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.databinding.ItemChatMessageBinding
import vcmsa.projects.fkj_consultants.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val currentUserId: String) :
    ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DiffCallback()) {

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.tvMessage.text = item.text
            binding.tvTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.timestamp))

            binding.tvMessage.textAlignment =
                if (item.senderId == currentUserId) android.view.View.TEXT_ALIGNMENT_VIEW_END
                else android.view.View.TEXT_ALIGNMENT_VIEW_START
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatViewHolder(ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
    }
}
