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

package com.example.android.cameraxextensions.model

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

data class CameraPreviewAttachDelegate(
    private val cameraProvider: ProcessCameraProvider,
    private val useCaseGroup: UseCaseGroup,
    private val cameraSelector: CameraSelector,
    private val onAttached: () -> Unit
) {

    /**
     * Starts the preview stream. The camera state should be in the READY or PREVIEW_STOPPED state
     * when calling this operation.
     * This process will bind the preview and image capture uses cases to the camera provider.
     */
    fun startStreaming(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val useCaseGroup = addViewPort(useCaseGroup, previewView.viewPort)
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            useCaseGroup
        )
        useCaseGroup.useCases
            .filterIsInstance(Preview::class.java)
            .firstOrNull()
            ?.setSurfaceProvider(previewView.surfaceProvider)

        onAttached()
    }

    private fun addViewPort(useCaseGroup: UseCaseGroup, viewPort: ViewPort?): UseCaseGroup {
        if (viewPort == null) return useCaseGroup
        val useCaseGroupBuilder = UseCaseGroup.Builder()
        useCaseGroupBuilder.setViewPort(viewPort)
        useCaseGroup.useCases.forEach { useCaseGroupBuilder.addUseCase(it) }
        return useCaseGroupBuilder.build()
    }
}