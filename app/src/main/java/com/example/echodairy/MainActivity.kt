package com.example.echodairy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.echodairy.data.AppDatabase
import com.example.echodairy.data.JournalRepository
import com.example.echodairy.ui.HistoryScreen
import com.example.echodairy.ui.RecordScreen
import com.example.echodairy.ui.theme.EchoDairyTheme
import com.example.echodairy.vm.HistoryViewModel
import com.example.echodairy.vm.HistoryViewModelFactory
import com.example.echodairy.vm.RecordViewModel
import com.example.echodairy.vm.RecordViewModelFactory
import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = JournalRepository(
            AppDatabase.getInstance(applicationContext).journalDao()
        )

        setContent {
            EchoDairyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EchoDiaryApp(repository = repository)
                }
            }
        }
    }
}

private enum class RootTab { Record, History }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EchoDiaryApp(repository: JournalRepository) {
    var tab by remember { mutableStateOf(RootTab.Record) }

    val recordVm: RecordViewModel = viewModel(factory = RecordViewModelFactory(repository))
    val historyVm: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(text = "EchoDiary") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == RootTab.Record,
                    onClick = { tab = RootTab.Record },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Record") },
                    label = { Text(text = "Record") }
                )
                NavigationBarItem(
                    selected = tab == RootTab.History,
                    onClick = { tab = RootTab.History },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text(text = "History") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            RootTab.Record -> RecordScreen(paddingValues = padding, vm = recordVm)
            RootTab.History -> HistoryScreen(paddingValues = padding, vm = historyVm)
        }
    }
}