package pachmp.meventer.components.mainmenu.components.profile.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarConfig
import com.gowtham.ratingbar.RatingBarStyle
import com.ramcosta.composedestinations.annotation.Destination
import pachmp.meventer.R
import pachmp.meventer.components.mainmenu.components.profile.FeedbackModel
import pachmp.meventer.components.mainmenu.components.profile.ProfileViewModel
import pachmp.meventer.data.DTO.UserFeedback
import pachmp.meventer.ui.theme.MeventerTheme
import pachmp.meventer.ui.transitions.BottomTransition
import java.time.LocalDate

@ProfileNavGraph(start = true)
@Destination(style = BottomTransition::class)
@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    with(profileViewModel) {
        if (user == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Загрузка")
            }
        } else {
            MeventerTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Edit button
                            IconButton(
                                onClick = { dropdownMenuExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit account",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            DropdownMenu(expanded = dropdownMenuExpanded, onDismissRequest = { dropdownMenuExpanded=false }) {
                                DropdownMenuItem(text = { Text("Редактировать данные") }, onClick = { navigateToEditData() })
                                DropdownMenuItem(text = { Text("Изменить почту") }, onClick = { navigateToEditEmail() })
                                DropdownMenuItem(text = { Text("Изменить пароль") }, onClick = { navigateToEditPassword() })
                            }

                            // Logout button
                            IconButton(
                                onClick = { logout() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    if (profileViewModel.user != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(25.dp))
                            // Avatar
                            Avatar(model = avatar!!)

                            // Username and ID
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(user!!.name, style = MaterialTheme.typography.headlineLarge)
                            Text("@${user!!.nickname}", style = MaterialTheme.typography.bodySmall)

                            // Details
                            Spacer(modifier = Modifier.height(20.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {

                                ProfileDetail("почта", user!!.email)
                                ProfileDetail(
                                    "возраст",
                                    (LocalDate.now().year - user!!.dateOfBirth.year).toString()
                                )

                                //comments
                                OutlinedCard(
                                    modifier = Modifier
                                        .height(288.dp)
                                        .fillMaxWidth(),
                                    border = BorderStroke(
                                        0.65f.dp,
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    ),

                                    ) {
                                    if (feedbackModels!=null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Отзывы на вас",
                                                modifier = Modifier.padding(12.dp)
                                            )
                                            RatingBar(
                                                value = 4.2f,
                                                config = RatingBarConfig().numStars(5)
                                                    .style(RatingBarStyle.HighLighted).size(24.dp),
                                                onValueChange = {},
                                                onRatingChanged = {})
                                        }
                                        CommentsList(feedbacks = feedbackModels!!)
                                    } else {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text("Загрузка")
                                        }
                                    }

                                }
                            }

                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(74.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(feedback: FeedbackModel) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        var expanded by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            if (feedback.author!=null) {
                AsyncImage(
                    model = feedback.author.avatar,
                    contentDescription = "author avatar",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Text(text = feedback.author.name, fontWeight = FontWeight.Bold)
            } else {
                Text(text = "Не удалось загрузить пользователя")
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${feedback.rating}")
                RatingBar(
                    value = feedback.rating,
                    config = RatingBarConfig().numStars(5).style(RatingBarStyle.HighLighted)
                        .size(20.dp),
                    onValueChange = {},
                    onRatingChanged = {})
            }
            Column {
                if(feedback.comment.length < 55){
                    Text(text = feedback.comment)
                }else{
                    if (expanded) {
                        Text(text = feedback.comment)
                    } else {
                        Text(
                            text = feedback.comment.take(55) + "..."
                        )
                    }

                    if (feedback.comment.length > 55) {
                        OutlinedButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "Скрыть" else "Показать весь")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentsList(feedbacks: List<FeedbackModel>) {
    LazyColumn {
        items(feedbacks) {
            FeedbackCard(feedback = it)
        }
    }
}

@Composable
fun ProfileDetail(label: String, value: String) {
    OutlinedCard(
        border = BorderStroke(0.55f.dp, MaterialTheme.colorScheme.onSecondaryContainer),
        modifier = Modifier.height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                maxLines = 1
            )
            Text(
                value, modifier = Modifier
                    .weight(3f)
                    .horizontalScroll(rememberScrollState()),
                fontSize = 17.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun Avatar(model: String) {
    AsyncImage(
        model = model,
        contentDescription = "avatar",
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
    )
}