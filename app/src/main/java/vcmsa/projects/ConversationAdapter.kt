package vcmsa.projects.fkj_consultants.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.databinding.ItemConversationBinding
import vcmsa.projects.fkj_consultants.models.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private var items: List<Conversation>,
    private val clickListener: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Conversation) {
            binding.tvUserId.text = item.getOtherUserId("Mavuso2@gmail.com")
            binding.tvMessage.text = item.lastMessage
            binding.tvTimestamp.text = formatTimestamp(item.lastTimestamp)
            binding.root.setOnClickListener { clickListener(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ConversationViewHolder(ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun submit(list: List<Conversation>) {
        items = list
        notifyDataSetChanged()
    }

    private fun formatTimestamp(ts: Long): String =
        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(ts))
}