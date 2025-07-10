package com.example.appranzo.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appranzo.ReviewDetailActivity
import com.example.appranzo.viewmodel.ProfileReviewsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileReviewsScreen(
    navController: NavController,
    viewModel: ProfileReviewsViewModel = koinViewModel()
) {
    val reviews by viewModel.reviews.collectAsState()
    val context        = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Le mie recensioni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (reviews.isEmpty()) {
                item {
                    Text(
                        "Nessuna recensione ancora inserita",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(reviews) { r ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = r.placeName,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        ReviewItem(
                            review = Review(
                                author = r.username,
                                date = r.creationDate,
                                rating = r.rating.toInt(),
                                content = r.comment?:""
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Vedi in dettaglio →",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .clickable {
                                    Intent(context, ReviewDetailActivity::class.java).also { intent ->
                                        intent.putExtra(
                                            ReviewDetailActivity.EXTRA_RESTAURANT_ID,
                                            r.placeId
                                        )
                                        intent.putExtra(
                                            ReviewDetailActivity.EXTRA_REVIEW_ID,
                                            r.id
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                        )
                    }
                    Divider()
                }
            }
        }
    }
}
