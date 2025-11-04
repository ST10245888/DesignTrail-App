package vcmsa.projects.fkj_consultants.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.utils.ChatUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatAdapter - Used in AdminChatViewActivity
 * Enhanced with support for different message types, attachments, and better UI
 */
class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: String,
    private val onMessageClickListener: ((ChatMessage) -> Unit)? = null,
    private val onMessageLongClickListener: ((ChatMessage, View) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "ChatAdapter"
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SYSTEM = 3
        private const val VIEW_TYPE_DATE_HEADER = 4
    }

    private var groupedMessages: List<Any> = emptyList()
    private var showDateHeaders = true

    init {
        groupMessagesByDate()
    }

    /**
     * Adds a single message to the adapter
     */
    fun addMessage(message: ChatMessage) {
        Log.d(TAG, "Adding message: ${message.message} from ${message.senderId}")
        messages.add(message)
        groupMessagesByDate()
        val position = groupedMessages.indexOf(message)
        if (position != -1) {
            notifyItemInserted(position)
        }
    }

    /**
     * Replaces all messages in the adapter
     */
    fun setMessages(newMessages: List<ChatMessage>) {
        Log.d(TAG, "setMessages called. New size: ${newMessages.size}")
        messages.clear()
        messages.addAll(newMessages)
        groupMessagesByDate()
        notifyDataSetChanged()
    }

    /**
     * Adds multiple messages to the adapter
     */
    fun addMessages(newMessages: List<ChatMessage>) {
        messages.addAll(newMessages)
        groupMessagesByDate()
        notifyDataSetChanged()
    }

    /**
     * Removes a message by ID
     */
    fun removeMessage(messageId: String): Boolean {
        val message = messages.find { it.id == messageId }
        return message?.let {
            messages.remove(it)
            groupMessagesByDate()
            notifyDataSetChanged()
            true
        } ?: false
    }

    /**
     * Updates message status
     */
    fun updateMessageStatus(messageId: String, status: String): Boolean {
        val message = messages.find { it.id == messageId }
        return message?.let {
            it.status = status
            if (status == ChatMessage.STATUS_READ) {
                it.read = true
            }
            val position = groupedMessages.indexOf(message)
            if (position != -1) {
                notifyItemChanged(position)
            }
            true
        } ?: false
    }

    /**
     * Marks all messages as read
     */
    fun markAllAsRead() {
        messages.forEach { message ->
            if (!message.isSentByAdmin) {
                message.read = true
                message.status = ChatMessage.STATUS_READ
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Gets the number of unread messages
     */
    fun getUnreadCount(): Int {
        return messages.count { !it.read && !it.isSentByAdmin }
    }

    /**
     * Toggles date headers visibility
     */
    fun toggleDateHeaders(show: Boolean) {
        showDateHeaders = show
        groupMessagesByDate()
        notifyDataSetChanged()
    }

    /**
     * Gets message at specified position
     */
    fun getMessageAt(position: Int): ChatMessage? {
        return if (position in groupedMessages.indices && groupedMessages[position] is ChatMessage) {
            groupedMessages[position] as ChatMessage
        } else {
            null
        }
    }

    /**
     * Clears all messages
     */
    fun clear() {
        messages.clear()
        groupedMessages = emptyList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val item = groupedMessages[position]
        return when {
            item is String -> VIEW_TYPE_DATE_HEADER
            item is ChatMessage && item.isSystemMessage -> VIEW_TYPE_SYSTEM
            item is ChatMessage && item.senderId == currentUserId -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_SYSTEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_system, parent, false)
                SystemMessageViewHolder(view)
            }
            VIEW_TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = groupedMessages[position]

        when (holder) {
            is SentMessageViewHolder -> holder.bind(item as ChatMessage)
            is ReceivedMessageViewHolder -> holder.bind(item as ChatMessage)
            is SystemMessageViewHolder -> holder.bind(item as ChatMessage)
            is DateHeaderViewHolder -> holder.bind(item as String)
        }
    }

    override fun getItemCount(): Int = groupedMessages.size

    /**
     * Groups messages by date and inserts date headers
     */
    private fun groupMessagesByDate() {
        if (messages.isEmpty()) {
            groupedMessages = emptyList()
            return
        }

        if (!showDateHeaders) {
            groupedMessages = messages.sortedBy { it.timestamp }
            return
        }

        val grouped = mutableListOf<Any>()
        val messagesByDate = messages
            .sortedBy { it.timestamp }
            .groupBy { it.formattedDate }

        messagesByDate.forEach { (date, dateMessages) ->
            grouped.add(date)
            grouped.addAll(dateMessages)
        }

        groupedMessages = grouped
    }

    /**
     * ViewHolder for sent messages
     */
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val txtTime: TextView = itemView.findViewById(R.id.tvTime)
        private val txtStatus: TextView? = itemView.findViewById(R.id.tvStatus)
        private val ivAttachment: ImageView? = itemView.findViewById(R.id.ivAttachment)
        private val txtFileName: TextView? = itemView.findViewById(R.id.tvFileName)
        private val txtFileSize: TextView? = itemView.findViewById(R.id.tvFileSize)
        private val progressBar: View? = itemView.findViewById(R.id.progressBar)

        fun bind(message: ChatMessage) {
            bindCommonViews(message, txtMessage, txtTime, ivAttachment, txtFileName, txtFileSize)
            setupMessageStatus(message)
            setupClickListeners(message)
        }

        private fun setupMessageStatus(message: ChatMessage) {
            txtStatus?.text = when (message.status) {
                ChatMessage.STATUS_SENT -> "✓"
                ChatMessage.STATUS_DELIVERED -> "✓✓"
                ChatMessage.STATUS_READ -> "✓✓"
                ChatMessage.STATUS_FAILED -> "!"
                else -> ""
            }

            val statusColor = when (message.status) {
                ChatMessage.STATUS_READ -> R.color.green_500
                ChatMessage.STATUS_FAILED -> R.color.red_500
                else -> R.color.textHint
            }
            txtStatus?.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

            // Show/hide progress bar based on status
            progressBar?.visibility = when (message.status) {
                ChatMessage.STATUS_SENDING -> View.VISIBLE
                else -> View.GONE
            }
        }

        private fun setupClickListeners(message: ChatMessage) {
            itemView.setOnClickListener {
                if (message.hasAttachment) {
                    // Open attachment
                    message.attachmentUri?.let { uri ->
                        Log.d(TAG, "Opening attachment: $uri")
                        ChatUtils.openAttachment(itemView.context, uri)
                    }
                }
                onMessageClickListener?.invoke(message)
            }

            itemView.setOnLongClickListener { view ->
                onMessageLongClickListener?.invoke(message, view)
                true
            }
        }

        private fun bindCommonViews(
            message: ChatMessage,
            txtMessage: TextView,
            txtTime: TextView,
            ivAttachment: ImageView?,
            txtFileName: TextView?,
            txtFileSize: TextView?
        ) {
            // Handle message content
            txtMessage.text = message.message

            // Format timestamp
            txtTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))

            // Handle attachments
            if (message.hasAttachment) {
                setupAttachmentViews(message, ivAttachment, txtFileName, txtFileSize, txtMessage)
            } else {
                hideAttachmentViews(ivAttachment, txtFileName, txtFileSize)
                txtMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                txtMessage.isClickable = false
            }
        }

        private fun setupAttachmentViews(
            message: ChatMessage,
            ivAttachment: ImageView?,
            txtFileName: TextView?,
            txtFileSize: TextView?,
            txtMessage: TextView
        ) {
            ivAttachment?.visibility = View.VISIBLE
            txtFileName?.visibility = View.VISIBLE
            txtFileSize?.visibility = View.VISIBLE

            // Set attachment icon based on file type
            val attachmentIcon = getAttachmentIcon(message)
            ivAttachment?.setImageDrawable(attachmentIcon)

            // Set file info
            txtFileName?.text = message.fileName ?: "Attachment"
            txtFileSize?.text = message.fileSizeFormatted

            // Make message clickable for attachments
            txtMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.blue_500))
            txtMessage.isClickable = true
        }

        private fun hideAttachmentViews(
            ivAttachment: ImageView?,
            txtFileName: TextView?,
            txtFileSize: TextView?
        ) {
            ivAttachment?.visibility = View.GONE
            txtFileName?.visibility = View.GONE
            txtFileSize?.visibility = View.GONE
        }

        private fun getAttachmentIcon(message: ChatMessage) =
            ContextCompat.getDrawable(itemView.context, when {
                message.isQuotationMessage -> R.drawable.ic_quotation
                message.isImageMessage -> R.drawable.ic_image
                message.isFileMessage -> R.drawable.ic_file
                else -> R.drawable.ic_attachment
            })
    }

    /**
     * ViewHolder for received messages
     */
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val txtTime: TextView = itemView.findViewById(R.id.tvTime)
        private val txtSender: TextView? = itemView.findViewById(R.id.tvSender)
        private val ivAttachment: ImageView? = itemView.findViewById(R.id.ivAttachment)
        private val txtFileName: TextView? = itemView.findViewById(R.id.tvFileName)
        private val txtFileSize: TextView? = itemView.findViewById(R.id.tvFileSize)
        private val ivReadIndicator: ImageView? = itemView.findViewById(R.id.ivReadIndicator)

        fun bind(message: ChatMessage) {
            bindCommonViews(message, txtMessage, txtTime, ivAttachment, txtFileName, txtFileSize)
            setupSenderInfo(message)
            setupReadIndicator(message)
            setupClickListeners(message)
        }

        private fun setupSenderInfo(message: ChatMessage) {
            txtSender?.text = if (message.isSentByAdmin) {
                "Admin"
            } else {
                message.getSenderDisplayName()
            }
        }

        private fun setupReadIndicator(message: ChatMessage) {
            ivReadIndicator?.visibility = if (!message.read) View.VISIBLE else View.GONE
        }

        private fun setupClickListeners(message: ChatMessage) {
            itemView.setOnClickListener {
                if (message.hasAttachment) {
                    // Open attachment
                    message.attachmentUri?.let { uri ->
                        Log.d(TAG, "Opening attachment: $uri")
                        ChatUtils.openAttachment(itemView.context, uri)
                    }
                }
                onMessageClickListener?.invoke(message)
            }

            itemView.setOnLongClickListener { view ->
                onMessageLongClickListener?.invoke(message, view)
                true
            }
        }

        private fun bindCommonViews(
            message: ChatMessage,
            txtMessage: TextView,
            txtTime: TextView,
            ivAttachment: ImageView?,
            txtFileName: TextView?,
            txtFileSize: TextView?
        ) {
            // Handle message content
            txtMessage.text = message.message

            // Format timestamp
            txtTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))

            // Handle attachments
            if (message.hasAttachment) {
                setupAttachmentViews(message, ivAttachment, txtFileName, txtFileSize, txtMessage)
            } else {
                hideAttachmentViews(ivAttachment, txtFileName, txtFileSize)
                txtMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                txtMessage.isClickable = false
            }
        }

        private fun setupAttachmentViews(
            message: ChatMessage,
            ivAttachment: ImageView?,
            txtFileName: TextView?,
            txtFileSize: TextView?,
            txtMessage: TextView
        ) {
            ivAttachment?.visibility = View.VISIBLE
            txtFileName?.visibility = View.VISIBLE
            txtFileSize?.visibility = View.VISIBLE

            // Set attachment icon based on file type
            val attachmentIcon = getAttachmentIcon(message)
            ivAttachment?.setImageDrawable(attachmentIcon)

            // Set file info
            txtFileName?.text = message.fileName ?: "Attachment"
            txtFileSize?.text = message.fileSizeFormatted

            // Make message clickable for attachments
            txtMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.blue_500))
            txtMessage.isClickable = true
        }

        private fun hideAttachmentViews(
            ivAttachment: ImageView?,
            txtFileName: TextView?,
            txtFileSize: TextView?
        ) {
            ivAttachment?.visibility = View.GONE
            txtFileName?.visibility = View.GONE
            txtFileSize?.visibility = View.GONE
        }

        private fun getAttachmentIcon(message: ChatMessage) =
            ContextCompat.getDrawable(itemView.context, when {
                message.isQuotationMessage -> R.drawable.ic_quotation
                message.isImageMessage -> R.drawable.ic_image
                message.isFileMessage -> R.drawable.ic_file
                else -> R.drawable.ic_attachment
            })
    }

    /**
     * ViewHolder for system messages
     */
    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val txtTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: ChatMessage) {
            txtMessage.text = message.message
            txtTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))

            // System messages are not interactive
            itemView.setOnClickListener(null)
            itemView.setOnLongClickListener(null)
        }
    }

    /**
     * ViewHolder for date headers
     */
    inner class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(date: String) {
            txtDate.text = date
        }
    }

    /**
     * Finds position of message by ID
     */
    fun findMessagePosition(messageId: String): Int {
        return groupedMessages.indexOfFirst {
            it is ChatMessage && it.id == messageId
        }
    }

    /**
     * Scrolls to specific message
     */
    fun scrollToMessage(messageId: String): Boolean {
        val position = findMessagePosition(messageId)
        return if (position != -1) {
            notifyItemChanged(position)
            true
        } else {
            false
        }
    }

    /**
     * Gets all messages
     */
    fun getMessages(): List<ChatMessage> = messages.toList()
}