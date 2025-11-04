package vcmsa.projects.fkj_consultants.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.fkj_consultants.R
import vcmsa.projects.fkj_consultants.models.ChatMessage
import vcmsa.projects.fkj_consultants.utils.ChatUtils
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageAdapter(
    private var messages: MutableList<ChatMessage>,
    private val currentUserId: String,
    private val onAdminMessageReceived: ((ChatMessage) -> Unit)? = null,
    private val onMessageClick: ((ChatMessage) -> Unit)? = null,
    private val onMessageLongClick: ((ChatMessage, View) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "ChatMessageAdapter"
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
        private const val TYPE_SYSTEM = 3
        private const val TYPE_DATE_HEADER = 4
    }

    private val processedAdminMessages = mutableSetOf<String>()
    private var groupedMessages: List<Any> = emptyList()
    private var showDateHeaders = true

    init {
        groupMessagesByDate()
    }

    override fun getItemViewType(position: Int): Int {
        val item = groupedMessages[position]
        return when {
            item is String -> TYPE_DATE_HEADER
            item is ChatMessage && item.isSystemMessage -> TYPE_SYSTEM
            item is ChatMessage && item.senderId == currentUserId -> TYPE_SENT
            else -> TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_sent, parent, false)
                SentMessageViewHolder(view)
            }
            TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            TYPE_SYSTEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_system, parent, false)
                SystemMessageViewHolder(view)
            }
            TYPE_DATE_HEADER -> {
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

        // Handle admin message callbacks
        if (item is ChatMessage && !item.isSentByAdmin && !processedAdminMessages.contains(item.id)) {
            processedAdminMessages.add(item.id)
            Log.d(TAG, "Admin message received: ${item.message} from ${item.senderId}")
            onAdminMessageReceived?.invoke(item)
        }
    }

    override fun getItemCount(): Int = groupedMessages.size

    fun getMessageAt(position: Int): ChatMessage? {
        return if (position in groupedMessages.indices && groupedMessages[position] is ChatMessage) {
            groupedMessages[position] as ChatMessage
        } else {
            null
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>, notify: Boolean = true) {
        messages.clear()
        messages.addAll(newMessages)
        processedAdminMessages.clear()
        groupMessagesByDate()
        if (notify) notifyDataSetChanged()
    }

    fun addMessage(message: ChatMessage, notify: Boolean = true) {
        messages.add(message)
        groupMessagesByDate()
        if (notify) {
            val position = groupedMessages.indexOf(message)
            if (position != -1) notifyItemInserted(position)
        }
    }

    fun addMessages(newMessages: List<ChatMessage>, notify: Boolean = true) {
        messages.addAll(newMessages)
        groupMessagesByDate()
        if (notify) notifyDataSetChanged()
    }

    fun removeMessage(messageId: String, notify: Boolean = true): Boolean {
        val message = messages.find { it.id == messageId }
        return message?.let {
            messages.remove(it)
            processedAdminMessages.remove(messageId)
            groupMessagesByDate()
            if (notify) notifyDataSetChanged()
            true
        } ?: false
    }

    fun updateMessageStatus(messageId: String, status: String, notify: Boolean = true): Boolean {
        val message = messages.find { it.id == messageId }
        return message?.let {
            it.status = status
            if (status == ChatMessage.STATUS_READ) {
                it.read = true
            }
            groupMessagesByDate()
            if (notify) {
                val position = groupedMessages.indexOf(message)
                if (position != -1) notifyItemChanged(position)
            }
            true
        } ?: false
    }

    fun markAllAsRead(notify: Boolean = true) {
        messages.forEach { message ->
            if (!message.isSentByAdmin) {
                message.read = true
                message.status = ChatMessage.STATUS_READ
            }
        }
        if (notify) notifyDataSetChanged()
    }

    fun getUnreadCount(): Int {
        return messages.count { !it.read && !it.isSentByAdmin }
    }

    fun toggleDateHeaders(show: Boolean) {
        showDateHeaders = show
        groupMessagesByDate()
        notifyDataSetChanged()
    }

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

    // ViewHolder Classes
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView? = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView? = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView? = itemView.findViewById(R.id.tvStatus)
        private val ivAttachment: ImageView? = itemView.findViewById(R.id.ivAttachment)
        private val progressBar: ProgressBar? = itemView.findViewById(R.id.progressBar)
        private val tvFileName: TextView? = itemView.findViewById(R.id.tvFileName)
        private val tvFileSize: TextView? = itemView.findViewById(R.id.tvFileSize)

        fun bind(message: ChatMessage) {
            bindCommonViews(message, itemView.context, itemView)
            setupMessageStatus(message)
            setupClickListeners(message, itemView)
        }

        private fun setupMessageStatus(message: ChatMessage) {
            val (statusText, statusColor) = when (message.status) {
                ChatMessage.STATUS_SENT -> Pair("✓", R.color.textHint)
                ChatMessage.STATUS_DELIVERED -> Pair("✓✓", R.color.textSecondary)
                ChatMessage.STATUS_READ -> Pair("✓✓", R.color.primary)
                ChatMessage.STATUS_FAILED -> Pair("!", R.color.error)
                else -> Pair("", R.color.textHint)
            }

            tvStatus?.text = statusText
            tvStatus?.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

            // Show progress bar for sending messages
            progressBar?.visibility = if (message.status == ChatMessage.STATUS_SENT) View.VISIBLE else View.GONE
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView? = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView? = itemView.findViewById(R.id.tvTime)
        private val tvSender: TextView? = itemView.findViewById(R.id.tvSender)
        private val ivAttachment: ImageView? = itemView.findViewById(R.id.ivAttachment)
        private val tvFileName: TextView? = itemView.findViewById(R.id.tvFileName)
        private val tvFileSize: TextView? = itemView.findViewById(R.id.tvFileSize)
        private val ivReadIndicator: ImageView? = itemView.findViewById(R.id.ivReadIndicator)

        fun bind(message: ChatMessage) {
            bindCommonViews(message, itemView.context, itemView)
            setupSenderInfo(message)
            setupReadIndicator(message)
            setupClickListeners(message, itemView)
        }

        private fun setupSenderInfo(message: ChatMessage) {
            tvSender?.text = if (message.isSentByAdmin) {
                "Admin"
            } else {
                message.getSenderDisplayName()
            }
        }

        private fun setupReadIndicator(message: ChatMessage) {
            ivReadIndicator?.visibility = if (!message.read) View.VISIBLE else View.GONE
        }
    }

    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView? = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView? = itemView.findViewById(R.id.tvTime)

        fun bind(message: ChatMessage) {
            tvMessage?.text = message.message
            tvTime?.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp))

            // System messages are not interactive
            itemView.setOnClickListener(null)
            itemView.setOnLongClickListener(null)
        }
    }

    inner class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView? = itemView.findViewById(R.id.tvDate)

        fun bind(date: String) {
            tvDate?.text = date
        }
    }

    // Common binding logic with safe view handling
    private fun bindCommonViews(message: ChatMessage, context: Context, itemView: View) {
        val tvMessage: TextView? = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView? = itemView.findViewById(R.id.tvTime)
        val ivAttachment: ImageView? = itemView.findViewById(R.id.ivAttachment)
        val tvFileName: TextView? = itemView.findViewById(R.id.tvFileName)
        val tvFileSize: TextView? = itemView.findViewById(R.id.tvFileSize)

        // Handle message content
        tvMessage?.text = message.message

        // Format timestamp
        tvTime?.text = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(message.timestamp))

        // Handle attachments only if the views exist
        if (message.hasAttachment && ivAttachment != null && tvFileName != null && tvFileSize != null) {
            setupAttachmentViews(message, context, ivAttachment, tvFileName, tvFileSize, tvMessage)
        } else {
            hideAttachmentViews(ivAttachment, tvFileName, tvFileSize)
        }
    }

    private fun setupAttachmentViews(
        message: ChatMessage,
        context: Context,
        ivAttachment: ImageView,
        tvFileName: TextView,
        tvFileSize: TextView,
        tvMessage: TextView?
    ) {
        ivAttachment.visibility = View.VISIBLE
        tvFileName.visibility = View.VISIBLE
        tvFileSize.visibility = View.VISIBLE

        // Set attachment icon based on file type
        val attachmentIcon = getAttachmentIcon(message, context)
        ivAttachment.setImageDrawable(attachmentIcon)

        // Set file info
        tvFileName.text = message.fileName ?: "Attachment"
        tvFileSize.text = message.fileSizeFormatted

        // Make message clickable for attachments
        tvMessage?.setTextColor(ContextCompat.getColor(context, R.color.primary))
        tvMessage?.isClickable = true
    }

    private fun hideAttachmentViews(
        ivAttachment: ImageView?,
        tvFileName: TextView?,
        tvFileSize: TextView?
    ) {
        ivAttachment?.visibility = View.GONE
        tvFileName?.visibility = View.GONE
        tvFileSize?.visibility = View.GONE
    }

    private fun getAttachmentIcon(message: ChatMessage, context: Context): Drawable? {
        val iconRes = when {
            message.isQuotationMessage -> R.drawable.ic_quotation
            message.isImageMessage -> R.drawable.ic_image
            message.isFileMessage -> R.drawable.ic_file
            else -> R.drawable.ic_attachment
        }
        return ContextCompat.getDrawable(context, iconRes)
    }

    private fun setupClickListeners(message: ChatMessage, itemView: View) {
        itemView.setOnClickListener {
            if (message.hasAttachment) {
                // Open attachment
                message.attachmentUri?.let { uri ->
                    ChatUtils.openAttachment(itemView.context, uri)
                }
            }
            onMessageClick?.invoke(message)
        }

        itemView.setOnLongClickListener { view ->
            onMessageLongClick?.invoke(message, view)
            true
        }
    }

    fun clear() {
        messages.clear()
        processedAdminMessages.clear()
        groupedMessages = emptyList()
        notifyDataSetChanged()
    }

    fun getMessages(): List<ChatMessage> = messages.toList()

    fun findMessagePosition(messageId: String): Int {
        return groupedMessages.indexOfFirst {
            it is ChatMessage && it.id == messageId
        }
    }

    fun scrollToMessage(messageId: String): Boolean {
        val position = findMessagePosition(messageId)
        return if (position != -1) {
            // Notify the RecyclerView to scroll to this position
            notifyItemChanged(position)
            true
        } else {
            false
        }
    }
}
// (AbhiAndroid, n.d.).