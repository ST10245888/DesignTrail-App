package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.fkj_consultants.databinding.ActivityChatBinding
import vcmsa.projects.fkj_consultants.ui.ChatAdapter
import vcmsa.projects.fkj_consultants.viewmodel.ChatViewModel
import vcmsa.projects.fkj_consultants.models.ChatMessage

class ChatActivity : AppCompatActivity() {

    private lateinit var b: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private val adapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityChatBinding.inflate(layoutInflater)
        setContentView(b.root)

        val otherUserId = intent.getStringExtra("receiverId")
        if (otherUserId == null) {
            finish()
            return
        }

        // RecyclerView setup
        b.recyclerMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        b.recyclerMessages.adapter = adapter

        // Start listening to messages
        vm.start(otherUserId)

        vm.messages.observe(this, Observer { list: List<ChatMessage> ->
            adapter.submitList(list) {
                if (adapter.itemCount > 0) {
                    b.recyclerMessages.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })

        // Sending messages
        b.btnSend.setOnClickListener {
            val text = b.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                vm.send(otherUserId, text)
                b.etMessage.text?.clear()
            }
        }
    }
}
