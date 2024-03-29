package pachmp.meventer.components.mainmenu.components.events.components.display

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pachmp.meventer.Nav
import pachmp.meventer.Navigator
import pachmp.meventer.RootNav
import pachmp.meventer.components.destinations.EventScreenDestination
import pachmp.meventer.components.mainmenu.BottomViewModel
import pachmp.meventer.components.mainmenu.components.events.components.Rank
import pachmp.meventer.components.mainmenu.components.events.components.getUserRank
import pachmp.meventer.components.mainmenu.components.profile.FeedbackModel
import pachmp.meventer.data.DTO.Event
import pachmp.meventer.data.DTO.EventOrganizer
import pachmp.meventer.data.DTO.NullableUserID
import pachmp.meventer.data.DTO.User
import pachmp.meventer.data.DTO.UserFeedbackCreate
import pachmp.meventer.data.DTO.UserFeedbackUpdate
import pachmp.meventer.data.repository.Repositories
import javax.inject.Inject

@HiltViewModel
class EventScreenViewModel @Inject constructor(
    @RootNav rootNavigator: Navigator,
    @Nav navigator: Navigator,
    repositories: Repositories,
) : BottomViewModel(rootNavigator, navigator, repositories) {
    var parentSnackbarHostState = snackBarHostState

    var allMembers by mutableStateOf<List<UserModel>?>(null)

    var originatorFeedbacks by mutableStateOf<List<FeedbackModel>?>(null)

    var originatorRating by mutableStateOf(0f)
        private set

    var ready by mutableStateOf<Boolean?>(null)
    var event by mutableStateOf<Event?>(null)
    var appUser by mutableStateOf<UserModel?>(null)
    var originatorUser  by mutableStateOf<User?>(null)
    var originator by mutableStateOf<UserModel?>(null)
    var organizers by mutableStateOf<List<UserModel>?>(null)
    var participants by mutableStateOf<List<UserModel>?>(null)

    var rating by mutableStateOf(0f)
    var comment by mutableStateOf("")

    fun init(eventID: Int, appUserID: Int) {
        viewModelScope.launch {
            val eventResponse = repositories.eventRepository.getEvent(eventID)
            if (checkResponse(eventResponse)) {
                event = fixEventImages(eventResponse!!.data!!)
            } else {
                navigator.clearNavigate(EventScreenDestination)
                parentSnackbarHostState.showSnackbar("Не удалось загрузить мероприятие")
                return@launch
            }

            val userResponse = repositories.userRepository.getUserData(appUserID)
            if (checkResponse(userResponse)) {
                appUser = UserModel(
                    id = appUserID,
                    avatar = repositories.fileRepository.getFileURL(fixUserAvatar(userResponse!!.data!!).avatar),
                    name = userResponse.data!!.name,
                    rank = getUserRank(event!!, userResponse.data)
                )
            } else {
                navigator.clearNavigate(EventScreenDestination)
                parentSnackbarHostState.showSnackbar("Не удалось загрузить пользователя")
                return@launch
            }

            originator = userModelFromUserID(event!!.originator, Rank.ORIGINATOR) {
                originatorUser = it
            }

            updateFeedbacks()

            organizers = List(event!!.organizers.size) {
                userModelFromUserID(event!!.organizers[it], Rank.ORGANIZER)
            }


            participants = List(event!!.participants.size) {
                userModelFromUserID(event!!.participants[it], Rank.PARTICIPANT)
            }

            allMembers = organizers!!+participants!!+originator!!
            ready = true
        }
    }

    suspend fun updateFeedbacks() {
        originatorRating = 0f
        val feedbacksResponse = repositories.userRepository.getFeedbacks(NullableUserID(id = originator!!.id))
        if (checkResponse(feedbacksResponse) {
                if (it.value==404) { originatorFeedbacks = emptyList(); false } else {  null } }) {
            originatorFeedbacks = List(feedbacksResponse!!.data!!.size) {
                val authorResponse = repositories.userRepository.getUserData(feedbacksResponse.data!![it].fromUserID)
                originatorRating += feedbacksResponse.data[it].rating
                if (checkResponse(authorResponse)) {
                    if (authorResponse!!.data!!.id==appUser!!.id) {
                        rating = feedbacksResponse.data[it].rating
                        comment = feedbacksResponse.data[it].comment
                    }
                    FeedbackModel(
                        id = feedbacksResponse.data[it].id,
                        author = authorResponse.data!!,
                        rating = feedbacksResponse.data[it].rating,
                        comment = feedbacksResponse.data[it].comment
                    )
                } else {
                    FeedbackModel(
                        id = feedbacksResponse.data[it].id,
                        author = null,
                        rating = feedbacksResponse.data[it].rating,
                        comment = feedbacksResponse.data[it].comment
                    )
                }
            }
            originatorRating /= originatorFeedbacks!!.size
        }
    }

    fun changeUserOrganizer(userModel: UserModel) {
        viewModelScope.launch {
            val response = repositories.eventRepository.changeUserOrganizer(
                EventOrganizer(event!!.id, userModel.id)
            )
            if (checkResponse(response)) {
                val fr = organizers!!.find { userModel.id==it.id }
                if (fr!=null) {
                    organizers = organizers!!-userModel
                } else {
                    organizers = organizers!!+userModel
                }
            }
        }
    }

    fun changeUserParticipant() {
        viewModelScope.launch {
            val response = repositories.eventRepository.changeUserParticipant(event!!.id)
            if (checkResponse(response)) {
                val fr = allMembers!!.find { appUser!!.id==it.id }
                if (fr!=null) {
                    participants = participants!!-fr
                    allMembers = participants!!-fr
                } else {
                    participants = participants!!+appUser!!
                    allMembers = participants!!+appUser!!
                }
            }
        }
    }

    suspend fun userModelFromUserID(userID: Int, rank: Rank, onUserGet: (User) -> Unit = {}): UserModel {
        val userRequest = repositories.userRepository.getUserData(userID)
        if (checkResponse(userRequest)) {
            val user = userRequest!!.data!!
            onUserGet(user)
            return UserModel(
                id = userID,
                avatar = fixUserAvatar(user).avatar,
                name = user.name,
                rank = rank
            )
        } else {
            return UserModel(id = userID, avatar = null, name = null, rank = rank)
        }

    }

    fun createFeedback() {
        viewModelScope.launch {
            val response = repositories.userRepository.createFeedback(UserFeedbackCreate(originatorUser!!.id, rating, comment))
            if (checkResponse(response)) {
                updateFeedbacks()
                parentSnackbarHostState.showSnackbar("Отзыв отправлен")
            }
        }
    }

    fun updateFeedback() {
        viewModelScope.launch {
            val feedback = originatorFeedbacks!!.find { it.author?.id==appUser!!.id }
            if (feedback!=null) {
                val response = repositories.userRepository.updateFeedback(UserFeedbackUpdate(feedback!!.id, rating, comment))
                if (checkResponse(response)) {
                    updateFeedbacks()
                }
            } else {
                parentSnackbarHostState.showSnackbar("Отзыв не найден")
            }
        }
    }

    fun deleteFeedback() {
        viewModelScope.launch {
            val feedback = originatorFeedbacks!!.find { it.author?.id==appUser!!.id }
            if (feedback!=null) {
                val response = repositories.userRepository.deleteFeedback(feedback.id)
                if (checkResponse(response)) {
                    rating = 0f
                    comment = ""
                    updateFeedbacks()
                }
            } else {
                parentSnackbarHostState.showSnackbar("Отзыв не найден")
            }
        }
    }
}

data class UserModel(
    val id: Int,
    val avatar: String?,
    val name: String?,
    val rank: Rank,
)
