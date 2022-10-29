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

import android.util.Log
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.ViewPort
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnAttach
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

private const val TAG = "CameraPreview"

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    state: CameraPreviewState? = null,
    onTap: (x: Float, y: Float, meteringPointFactory: MeteringPointFactory) -> Unit,
    onZoom: (Float) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val transformableState = rememberTransformableState(onTransformation = { zoomChange, _, _ ->
        onZoom(zoomChange)
    })

    state?.lifecycleOwnerDeferred?.complete(lifecycleOwner)

    DisposableEffect(lifecycleOwner) {
        onDispose {
            Log.d(TAG, "onDispose")
            state?.clear()
        }
    }

    AndroidView(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        Log.d(TAG, "onTap $offset")
                        state?.let {
                            coroutineScope.launch {
                                onTap(offset.x, offset.y, it.meteringPointFactory())
                            }
                        }
                    }
                )
            }
            .transformable(state = transformableState),
        factory = { context ->
            PreviewView(context).apply {
                keepScreenOn = true
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { view ->
            state?.previewViewDeferred?.complete(view)
        }
    )
}

class CameraPreviewState {
    private companion object {
        private const val TAG = "CameraPreviewState"
    }

    internal var previewViewDeferred: CompletableDeferred<PreviewView> = CompletableDeferred()
    internal var lifecycleOwnerDeferred: CompletableDeferred<LifecycleOwner> = CompletableDeferred()

    init {
        Log.d(TAG, "Initializing PreviewState")
    }

    internal fun clear() {
        previewViewDeferred.cancel()
        lifecycleOwnerDeferred.cancel()
        previewViewDeferred = CompletableDeferred()
        lifecycleOwnerDeferred = CompletableDeferred()
    }

    suspend fun bitmap() = previewViewDeferred.await().bitmap

    suspend fun meteringPointFactory() = previewViewDeferred.await().meteringPointFactory

    @TransformExperimental
    suspend fun outputTransform() = previewViewDeferred.await().outputTransform

    suspend fun previewStreamState() = previewViewDeferred.await().previewStreamState

    suspend fun surfaceProvider() = previewViewDeferred.await().surfaceProvider

    suspend fun viewPort() = previewViewDeferred.await().viewPortOnAttach()

    suspend fun getController() = previewViewDeferred.await().controller

    suspend fun setController(controller: CameraController) {
        previewViewDeferred.await().controller = controller
    }

    suspend fun setImplementationMode(mode: PreviewView.ImplementationMode) {
        previewViewDeferred.await().implementationMode = mode
    }

    suspend fun getImplementationMode() = previewViewDeferred.await().implementationMode

    suspend fun setScaleType(scaleType: PreviewView.ScaleType) {
        previewViewDeferred.await().scaleType = scaleType
    }

    suspend fun getScaleType() = previewViewDeferred.await().scaleType

    suspend fun lifecycleOwner() = lifecycleOwnerDeferred.await()
}

suspend fun PreviewView.viewPortOnAttach(): ViewPort {
    val viewPortDeferred = CompletableDeferred<ViewPort>()
    doOnAttach {
        viewPortDeferred.complete(viewPort!!)
    }
    return viewPortDeferred.await()
}