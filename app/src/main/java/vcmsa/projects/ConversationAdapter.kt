package vcmsa.projects.fkj_consultants.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.databinding.ItemConversationBinding
import vcmsa.projects.fkj_consultants.models.Conversation

class ConversationAdapter(
    private var items: List<Conversation> = emptyList(),
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val convo = items[position]
        holder.bind(convo, onClick)
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<Conversation>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(private val b: ItemConversationBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(convo: Conversation, onClick: (Conversation) -> Unit) {
            b.tvTitle.text = convo.id
            b.tvSubtitle.text = convo.lastMessage
            itemView.setOnClickListener { onClick(convo) }
        }
    }
}
