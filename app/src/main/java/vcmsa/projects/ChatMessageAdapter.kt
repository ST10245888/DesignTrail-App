package vcmsa.projects.fkj_consultants.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.utils.ChatUtils
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageAdapter(
    private var messages: MutableList<ChatMessage>,
    private val currentUserEmail: String,
    private val onAdminMessageReceived: ((ChatMessage) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "ChatMessageAdapter"
    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2
    private val processedAdminMessages = mutableSetOf<Long>()

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserEmail) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TYPE_SENT) R.layout.item_chat_sent else R.layout.item_chat_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        (holder as MessageViewHolder).bind(message)

        // Trigger callback for admin messages only once
        if (message.senderId != currentUserEmail && !processedAdminMessages.contains(message.timestamp)) {
            processedAdminMessages.add(message.timestamp)
            Log.d(TAG, "Admin message received: ${message.message} from ${message.senderId}")
            onAdminMessageReceived?.invoke(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<ChatMessage>, notify: Boolean = true) {
        messages.clear()
        messages.addAll(newMessages)
        processedAdminMessages.clear()
        if (notify) notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage, notify: Boolean = true) {
        messages.add(message)
        if (notify) notifyItemInserted(messages.size - 1)
    }

    fun getItem(position: Int): ChatMessage = messages[position]

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView? = itemView.findViewById(R.id.tvTime)

        fun bind(msg: ChatMessage) {
            val context = itemView.context

            // Handle attachment
            if (!msg.attachmentUri.isNullOrEmpty()) {
                val displayText = ChatUtils.getAttachmentDisplayText(
                    msg.attachmentUri!!,
                    msg.message
                )
                tvMessage.text = displayText
                tvMessage.setTextColor(context.getColor(R.color.purple_700))
                tvMessage.isClickable = true
                tvMessage.isFocusable = true

                tvMessage.setOnClickListener {
                    Log.d(TAG, "Opening attachment: ${msg.attachmentUri}")
                    ChatUtils.openAttachment(context, msg.attachmentUri!!)
                }
            } else {
                // Regular text message
                tvMessage.text = msg.message
                tvMessage.setTextColor(context.getColor(R.color.black))
                tvMessage.isClickable = false
                tvMessage.isFocusable = false
                tvMessage.setOnClickListener(null)
            }

            // Format timestamp
            tvTime?.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(msg.timestamp))
        }
    }
}
// (AbhiAndroid, n.d.).