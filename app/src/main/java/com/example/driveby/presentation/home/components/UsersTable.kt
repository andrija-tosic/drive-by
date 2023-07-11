package com.example.driveby.presentation.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.driveby.components.UserListItem
import com.example.driveby.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersTable(
    users: List<User>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title =
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby users"
                        )
                    }
                })
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
            ) {
                items(users) { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        UserListItem(
                            user,
                            user.name + " " + user.lastName,
                            "${user.score} points"
                        )
                    }
                }
            }
        }
    )
}
