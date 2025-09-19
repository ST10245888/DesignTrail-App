package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.fkj_consultants.databinding.ActivityChatListBinding
import vcmsa.projects.fkj_consultants.ui.ConversationAdapter

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private val vm: ChatListViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ConversationAdapter(emptyList()) { convo ->
            val users = listOf(convo.userA, convo.userB)
            val adminUid = "ADMIN_FIREBASE_UID" // Replace with actual admin UID
            val otherUserId = users.first { it != adminUid }

            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("receiverId", otherUserId)
            })
        }

        binding.recyclerConversations.layoutManager = LinearLayoutManager(this)
        binding.recyclerConversations.adapter = adapter

        // Fetch conversations
        vm.startAdmin()

        // Observe LiveData
        vm.conversations.observe(this) { list ->
            adapter.submit(list)
        }
    }
}
