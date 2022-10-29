/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.cameraxextensions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberLazyListSnapperLayoutInfo
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.launch

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun HorizontalPicker(
    modifier: Modifier = Modifier,
    options: List<String>,
    onItemSelected: (item: String, itemIndex: Int) -> Unit
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val hapticFeedback = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val layoutInfo = rememberLazyListSnapperLayoutInfo(listState)

    var lastSelectedItemIndex by remember { mutableStateOf(0) }
    var selectedItemIndex by remember { mutableStateOf(0) }

    LaunchedEffect(layoutInfo.currentItem?.offset) {
        val itemIndex = layoutInfo.visibleItems
            .firstOrNull { it.index - 1 == selectedItemIndex }
            ?.let { selectedItem ->
                val midPoint = screenWidth / 2
                if (selectedItem.offset < (midPoint - selectedItem.size)) {
                    selectedItemIndex + 1
                } else if (selectedItem.offset > midPoint) {
                    selectedItemIndex - 1
                } else {
                    selectedItemIndex
                }
            } ?: ((layoutInfo.currentItem?.index ?: 1) - 1)

        if (itemIndex.coerceIn(0, options.lastIndex) != selectedItemIndex) {
            selectedItemIndex = itemIndex
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            if (lastSelectedItemIndex != selectedItemIndex) {
                onItemSelected(options[selectedItemIndex], selectedItemIndex)
                lastSelectedItemIndex = selectedItemIndex
            }
        }
    }

    Box(modifier = modifier) {
        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.Center),
            state = listState,
            flingBehavior = rememberSnapperFlingBehavior(
                lazyListState = listState,
                snapOffsetForItem = SnapOffsets.Center,
                snapIndex = { _, _, targetIndex ->
                    targetIndex.coerceIn(1, options.size)
                }
            )
        ) {
            item {
                SpacerItem(listState = listState, position = 1)
            }

            itemsIndexed(options) { index, option ->
                val isSelected = index == selectedItemIndex

                PickerItem(text = option, isSelected = isSelected) {
                    coroutineScope.launch {
                        listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == index + 1 }
                            ?.let { item ->
                                val x = item.offset + item.size / 2
                                val midPoint = screenWidth / 2
                                val scrollOffset = midPoint - x

                                listState.animateScrollBy(-scrollOffset)

                                selectedItemIndex = item.index - 1

                                if (lastSelectedItemIndex != selectedItemIndex) {
                                    onItemSelected(options[selectedItemIndex], selectedItemIndex)
                                    lastSelectedItemIndex = selectedItemIndex
                                }
                            }
                    }
                }
            }

            item {
                SpacerItem(listState = listState, position = options.size)
            }
        }
    }
}

@Composable
private fun SpacerItem(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    position: Int
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val item = remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == position }
        }
    }

    val width = item.value?.size ?: 0
    val spacerWidthDp = with(density) { ((screenWidth - width) / 2).toDp() + 1.dp }

    Box(
        modifier = modifier.size(width = spacerWidthDp, height = 1.dp)
    )
}

@Composable
private fun PickerItem(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .height(40.dp)
            .background(
                color = if (isSelected) Color(0xFFFFEE44) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp)
            .clickable {
                onClick()
            }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            textAlign = TextAlign.Center,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}