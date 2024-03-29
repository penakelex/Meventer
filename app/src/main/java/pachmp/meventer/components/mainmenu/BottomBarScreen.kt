package pachmp.meventer.components.mainmenu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.spec.Direction
import kotlinx.serialization.Serializable
import pachmp.meventer.components.NavGraph
import pachmp.meventer.components.NavGraphs
import pachmp.meventer.components.destinations.ChatsScreenDestination

enum class BottomBarScreens (
    val navGraph: NavGraph,
    val title: String,
    val icon: ImageVector
){
    Profile(
        navGraph = NavGraphs.profile,
        title = "Profile",
        icon = Icons.Default.Person
    ),
    Events(
        navGraph = NavGraphs.events,
        title = "All Events",
        icon = Icons.Default.Search
    ),
    Chats(
        navGraph = NavGraphs.chats,
        title = "Chat",
        icon = Icons.Default.ChatBubble
    )
}