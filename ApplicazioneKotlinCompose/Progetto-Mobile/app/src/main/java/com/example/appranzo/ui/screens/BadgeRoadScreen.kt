package com.example.appranzo.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appranzo.BadgeDetailActivity
import com.example.appranzo.communication.remote.loginDtos.UserDto
import org.koin.androidx.compose.koinViewModel
import com.example.appranzo.viewmodel.BadgeRoadViewModel


@Composable
fun BadgeRoadScreen(
    userDto: UserDto,
    viewModel: BadgeRoadViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val ctx = LocalContext.current
    val itemHeight: Dp = 150.dp
    val linecolor = MaterialTheme.colorScheme.primary

    LaunchedEffect(userDto) {
        viewModel.loadBadgeData(userDto)
    }

    LaunchedEffect(Unit) {
        if (uiState.badges.isNotEmpty()) {
            val scrollToIndex = uiState.badges.lastIndex - uiState.lastUnlockedIndex
            listState.animateScrollToItem(scrollToIndex)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    Text(
        text = "I badge di ${userDto.username}",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
    Scaffold { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            reverseLayout = true

        ) {
            itemsIndexed(uiState.badges) { index, badge ->
                val unlocked = uiState.points >= badge.threshold
                val currentIcon = badge.icona

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val baseIconSize = 80.dp
                val iconSize by animateDpAsState(
                    targetValue = if (isPressed) 100.dp else 80.dp,
                    label = "sizeAnimation"
                )


                val circleColor = if (isPressed) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(itemHeight)
                        .drawBehind {
                            if (index < uiState.badges.lastIndex) {
                                val staticIconDiameter = baseIconSize.toPx()

                                val isCurrentStartAligned = index % 2 == 0
                                val isNextStartAligned = (index + 1) % 2 == 0

                                val startX = if (isCurrentStartAligned) staticIconDiameter
                                else size.width - staticIconDiameter
                                val startY = size.height / 2f

                                val endX = if (isNextStartAligned) staticIconDiameter
                                else size.width - staticIconDiameter
                                val endY = startY - itemHeight.toPx()

                                val controlPoint1X =
                                    if (isCurrentStartAligned) startX + size.width / 3f else startX - size.width / 3f
                                val controlPoint1Y = startY - itemHeight.toPx() / 2f

                                val controlPoint2X =
                                    if (isNextStartAligned) endX + size.width / 3f else endX - size.width / 3f
                                val controlPoint2Y = endY + itemHeight.toPx() / 2f


                                val path = Path().apply {
                                    moveTo(startX, startY)
                                    cubicTo(
                                        controlPoint1X,
                                        controlPoint1Y,
                                        controlPoint2X,
                                        controlPoint2Y,
                                        endX,
                                        endY
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = linecolor,
                                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                                )
                            }
                        }
                ) {

                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .align(if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if(!unlocked){
                                    val duration = Toast.LENGTH_LONG
                                    val toast = Toast.makeText(ctx, "Per sbloccare questo badge ci vogliono ${badge.threshold} punti", duration)
                                    toast.show()
                                }
                                else {
                                    val intent =
                                        Intent(ctx, BadgeDetailActivity::class.java).apply {
                                            putExtra("BADGE_ID", badge.nome)
                                        }
                                    ctx.startActivity(intent)
                                }
                            }
                            .background(
                                color = if (unlocked)
                                    circleColor
                                else
                                    Color.Gray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentIcon,
                            modifier = Modifier.size(48.dp),
                            contentDescription = "Badge ${index + 1}",
                            tint = if (unlocked)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}}

