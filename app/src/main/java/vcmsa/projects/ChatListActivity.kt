package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vcmsa.projects.fkj_consultants.data.ChatRepository
import vcmsa.projects.fkj_consultants.databinding.ActivityChatListBinding
import vcmsa.projects.fkj_consultants.ui.ConversationAdapter

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private val repo = ChatRepository()
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ConversationAdapter(emptyList()) { convo ->
            val otherUserId = convo.getOtherUserId(repo.auth.currentUser?.uid ?: "")
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("receiverId", otherUserId)
            })
        }

        binding.recyclerConversations.layoutManager = LinearLayoutManager(this)
        binding.recyclerConversations.adapter = adapter

        lifecycleScope.launch {
            repo.observeConversations().collectLatest { list ->
                adapter.submit(list)
            }
        }
    }
}