package vcmsa.projects.fkj_consultants.adapters

import android.content.Context
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

/**
 * ChatAdapter - Used in AdminChatViewActivity
 */
class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val TAG = "ChatAdapter"
    }

    fun addMessage(message: ChatMessage) {
        Log.d(TAG, "Adding message: ${message.message} from ${message.senderId}")
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatMessage>) {
        Log.d(TAG, "setMessages called. New size: ${newMessages.size}")
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT)
            R.layout.item_message_sent else R.layout.item_message_received
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MessageViewHolder).bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        private val txtTime: TextView = itemView.findViewById(R.id.txtTime)

        fun bind(message: ChatMessage) {
            val context = itemView.context

            // Handle attachment
            if (!message.attachmentUri.isNullOrEmpty()) {
                val displayText = ChatUtils.getAttachmentDisplayText(
                    message.attachmentUri!!,
                    message.message
                )
                txtMessage.text = displayText
                txtMessage.setTextColor(context.getColor(R.color.purple_700))
                txtMessage.isClickable = true
                txtMessage.isFocusable = true

                txtMessage.setOnClickListener {
                    Log.d(TAG, "Opening attachment: ${message.attachmentUri}")
                    ChatUtils.openAttachment(context, message.attachmentUri!!)
                }
            } else {
                // Regular text message
                txtMessage.text = message.message
                txtMessage.setTextColor(context.getColor(R.color.black))
                txtMessage.isClickable = false
                txtMessage.isFocusable = false
                txtMessage.setOnClickListener(null)
            }

            // Format timestamp
            txtTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))
        }
    }
}