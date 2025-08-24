package vcmsa.projects.fkj_consultants.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// Simple StateFlow bridge if you prefer flows in VM and observe in Activities
fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T) -> Unit) =
    observe(owner, Observer { t -> observer(t) })
