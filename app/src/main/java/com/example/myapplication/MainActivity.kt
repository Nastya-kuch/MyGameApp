package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameApp()
                }
            }
        }
    }
}

data class Game(
    val id: Int,
    val title: String,
    val year: Int,
    val genre: String,
    val time: Int,
    val status: String = "Planned"
)

val sampleGameList = listOf(
    Game(id = 1, title = "Stardew Valley", year = 2016, genre = "indie", time = 150, status = "Planned"),
    Game(id = 2, title = "Portal 2", year = 2011, genre = "Puzzle-platform", time = 15, status = "Planned"),
    Game(id = 3, title = "Cult of the Lamb", year = 2022, genre = "Roguelike", time = 50, status = "Planned"),
    Game(id = 4, title = "Hollow Knight", year = 2017, genre = "Metroidvania", time = 100, status = "Planned"),
    Game(id = 5, title = "The Witcher: Enhanced Edition Director's Cut", year = 2007, genre = "role-play", time = 45, status = "Planned"),
)

class GameStateHolder {
    var searchQuery by mutableStateOf("")
    var filterStatus by mutableStateOf("All")
    var games by mutableStateOf(sampleGameList)

    val filteredList: List<Game>
        get() = games
            .filter { game ->
                when (filterStatus) {
                    "All" -> true
                    else -> game.status == filterStatus
                }
            }
            .filter { game ->
                searchQuery.isBlank() || game.title.contains(searchQuery, ignoreCase = true)
            }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateFilter(filter: String) {
        filterStatus = filter
    }

    fun changeStatus(gameId: Int) {
        games = games.map { game ->
            if (game.id == gameId) {
                val newStatus = when (game.status) {
                    "Planned" -> "Playing"
                    "Playing" -> "Done"
                    "Done" -> "Planned"
                    else -> "Planned"
                }
                game.copy(status = newStatus)
            } else {
                game
            }
        }
    }
}

@Composable
fun GameApp(){
    val stateHolder = remember { GameStateHolder() }

    GameListScreen(
        gameList = stateHolder.filteredList,
        searchQuery = stateHolder.searchQuery,
        onSearchChange = { stateHolder.updateSearchQuery(it) },
        filterStatus = stateHolder.filterStatus,
        onFilterChange = { stateHolder.updateFilter(it) },
        onStatusChange = { gameId -> stateHolder.changeStatus(gameId) }
    )
}

@Composable
fun FilterButton(
    text: String,
    currentFilter: String,
    onFilterChange: (String) -> Unit
) {
    Button(
        onClick = { onFilterChange(text) },
        enabled = currentFilter != text,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    gameList: List<Game>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterStatus: String,
    onFilterChange: (String) -> Unit,
    onStatusChange: (Int) -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text(text = "Game Viewer") }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text( text = "Search by title")},
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterButton("All", filterStatus, onFilterChange)
                FilterButton("Planned", filterStatus, onFilterChange)
                FilterButton("Playing", filterStatus, onFilterChange)
                FilterButton("Done", filterStatus, onFilterChange)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (gameList.isEmpty()) {
                Text(text = "No game")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = gameList,
                        key = { it.id }
                    ) { item ->
                        GameCard(
                            game = item,
                            onStatusChange = { onStatusChange(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    onStatusChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = game.title, fontWeight = FontWeight.Bold)
            Text(text = "${game.year} - ${game.genre}")
            Text(text = "Estimated time: ${game.time} hours")
            Text(text = "Status: ${game.status}", fontWeight = FontWeight.Medium)
        }
        Button(
            onClick = onStatusChange
        ) {
            Text(
                text = when (game.status) {
                    "Planned" -> "Start"
                    "Playing" -> "Done"
                    "Done" -> "Restart"
                    else -> "Change"
                }
            )
        }
    }
}