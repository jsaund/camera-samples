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

import android.os.SystemClock
import androidx.camera.core.MeteringPointFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.dynamicanimation.animation.SpringForce
import com.example.android.cameraxextensions.R
import com.example.android.cameraxextensions.adapter.CameraExtensionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TapToFocusPoint(val x: Float = 0f, val y: Float = 0f)

@Composable
fun CameraViewFinder(
    modifier: Modifier = Modifier,
    isShutterButtonEnabled: Boolean,
    isSwitchLensButtonEnabled: Boolean,
    isCameraControlsVisible: Boolean,
    isExtensionPickerVisible: Boolean,
    extensionPickerOptions: List<CameraExtensionItem>,
    cameraPreviewState: CameraPreviewState,
    onTap: (x: Float, y: Float, meteringPointFactory: MeteringPointFactory) -> Unit,
    onZoom: (Float) -> Unit,
    onShutterClick: () -> Unit,
    onSwitchLens: () -> Unit,
    onExtensionSelected: (Int) -> Unit
) {
    var focusPoint: TapToFocusPoint? by remember { mutableStateOf(null) }
    var focusPointId: Long by remember { mutableStateOf(0L) }

    Box(modifier) {
        CameraPreview(
            modifier = Modifier
                .fillMaxSize()
                .then(Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { onSwitchLens() }
                        )
                    }
                ),
            cameraPreviewState,
            onTap = { x, y, meteringPointFactory ->
                focusPoint = TapToFocusPoint(x, y)
                focusPointId = SystemClock.elapsedRealtimeNanos()
                onTap(x, y, meteringPointFactory)
            },
            onZoom
        )

        if (isCameraControlsVisible) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom
            ) {

                if (isExtensionPickerVisible) {
                    val options = remember { extensionPickerOptions.map { it.name } }

                    HorizontalPicker(
                        options = options,
                        onItemSelected = { _, itemIndex ->
                            onExtensionSelected(extensionPickerOptions[itemIndex].extensionMode)
                        })

                    Spacer(modifier = Modifier.size(width = 1.dp, height = 16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    SwitchLensButton(
                        modifier = Modifier.size(width = 60.dp, height = 96.dp),
                        isEnabled = isSwitchLensButtonEnabled,
                        onClick = onSwitchLens
                    )

                    ShutterButton(
                        modifier = Modifier.size(96.dp),
                        isEnabled = isShutterButtonEnabled,
                        onClick = onShutterClick
                    )

                    Spacer(modifier = Modifier.size(60.dp))
                }
            }
        }

        focusPoint?.let {
            TapToFocus(
                modifier = Modifier
                    .size(64.dp)
                    .offset {
                        IntOffset(
                            x = it.x.roundToInt() - 32.dp.roundToPx(),
                            y = it.y.roundToInt() - 32.dp.roundToPx()
                        )
                    },
                id = focusPointId
            )
        }
    }
}

@Composable
fun TapToFocus(modifier: Modifier = Modifier, id: Long) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(id) {
        alpha.snapTo(0f)
        scale.snapTo(1f)

        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY,
                    stiffness = SpringForce.STIFFNESS_VERY_LOW
                )
            )
        }

        launch {
            scale.animateTo(
                targetValue = 0.75f,
                animationSpec = spring(
                    dampingRatio = 0.35f,
                    stiffness = SpringForce.STIFFNESS_LOW
                )
            )
            delay(200)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY,
                    stiffness = SpringForce.STIFFNESS_LOW
                )
            )
        }
    }

    Box(
        modifier
            .scale(scale.value)
            .alpha(alpha.value)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(width = (1.5).dp, shape = CircleShape, color = Color.White)
        )
    }
}

@Composable
private fun ShutterButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val transition = updateTransition(targetState = isPressed, label = "animation")

    val scale: Float by transition.animateFloat(
        transitionSpec = {
            spring(
                stiffness = 500f,
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            )
        },
        label = ""
    ) { state ->
        if (state) {
            0.65f
        } else {
            1f
        }
    }

    Box(modifier = modifier.scale(scale)) {
        val color = if (isEnabled) Color(0xFFFFEE44) else Color(0xFFFFEE44).copy(alpha = 0.65f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 4.dp, shape = CircleShape, color = color)
                .padding(9.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        isPressed = true
                        if (tryAwaitRelease()) {
                            onClick()
                        }
                        isPressed = false
                    })
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun SwitchLensButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var isAnimating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(!isAnimating && isEnabled) {
                    isAnimating = true
                    onClick()
                    coroutineScope.launch {
                        rotation.animateTo(
                            targetValue = 180f,
                            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                        )
                        rotation.snapTo(0f)
                        isAnimating = false
                    }
                }
                .rotate(rotation.value)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                painter = painterResource(id = R.drawable.ic_flip_camera_android),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "Switch Lens"
            )
        }
    }
}