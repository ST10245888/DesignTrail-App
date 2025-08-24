package vcmsa.projects.fkj_consultants.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.fkj_consultants.databinding.ActivityChatListBinding
import vcmsa.projects.fkj_consultants.ui.ConversationAdapter
import vcmsa.projects.fkj_consultants.viewmodel.ChatListViewModel

class ChatListActivity : AppCompatActivity() {
    private lateinit var b: ActivityChatListBinding
    private val vm: ChatListViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ Setup adapter with click listener
        adapter = ConversationAdapter(emptyList()) { convo ->
            val myId = vm.currentUserId  // expose from FirebaseAuth or ViewModel
            val otherUserId = convo.id.split("_").firstOrNull { it != myId } ?: convo.id

            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("receiverId", otherUserId)
            })
        }

        // ✅ Setup RecyclerView
        b.recyclerConversations.layoutManager = LinearLayoutManager(this)
        b.recyclerConversations.adapter = adapter

        // ✅ Start listening for conversations
        vm.start()

        // ✅ Observe conversations safely
        vm.conversations.observe(this, Observer { list ->
            adapter.submit(list)
        })
    }
}
