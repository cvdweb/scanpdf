package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.DocumentRepository
import com.example.ui.DocumentViewModel
import com.example.ui.DocumentViewModelFactory
import com.example.ui.ScanAppUi
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Database, Repository, and ViewModel using native ViewModelProvider
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = DocumentRepository(database.documentDao())
    val viewModel = ViewModelProvider(
        this,
        DocumentViewModelFactory(repository)
    )[DocumentViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          ScanAppUi(
              viewModel = viewModel,
              modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}
