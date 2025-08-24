package vcmsa.projects.fkj_consultants.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.databinding.ItemConversationBinding
import vcmsa.projects.fkj_consultants.models.Conversation

class ConversationAdapter(
    private var items: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.VH>() {

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemConversationBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, i: Int) {
        val c = items[i]
        h.b.tvTitle.text = c.id
        h.b.tvSubtitle.text = c.lastMessage
        h.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size
    fun submit(list: List<Conversation>) { items = list; notifyDataSetChanged() }

    class VH(val b: ItemConversationBinding): RecyclerView.ViewHolder(b.root)
}
