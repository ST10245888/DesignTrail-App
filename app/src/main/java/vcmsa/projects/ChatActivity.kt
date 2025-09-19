package vcmsa.projects.fkj_consultants.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vcmsa.projects.fkj_consultants.databinding.ActivityChatBinding
import vcmsa.projects.fkj_consultants.ui.ChatAdapter
import vcmsa.projects.fkj_consultants.viewmodel.ChatViewModel

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val otherUserId = intent.getStringExtra("receiverId") ?: run {
            finish()
            return
        }

        val currentUserId = vm.getCurrentUserId()
        adapter = ChatAdapter(currentUserId)

        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.recyclerMessages.adapter = adapter

        vm.start(otherUserId)

        lifecycleScope.launch {
            vm.messages.collectLatest { list ->
                adapter.submitList(list)
                if (adapter.itemCount > 0)
                    binding.recyclerMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                vm.send(otherUserId, text)
                binding.etMessage.text?.clear()
            }
        }
    }
}
