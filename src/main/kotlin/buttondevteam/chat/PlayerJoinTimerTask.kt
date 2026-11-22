package buttondevteam.chat

import java.util.*

abstract class PlayerJoinTimerTask : TimerTask() {
    abstract override fun run()

    var mp: ChatPlayer? = null
}
