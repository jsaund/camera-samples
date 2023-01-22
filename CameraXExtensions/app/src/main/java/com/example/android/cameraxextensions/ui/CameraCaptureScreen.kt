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

import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.ViewPort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import com.example.android.cameraxextensions.viewstate.CameraPermissionsViewState
import com.example.android.cameraxextensions.viewstate.CaptureScreenViewState
import com.example.android.cameraxextensions.viewstate.PostCaptureScreenViewState

@Composable
fun CameraCaptureScreen(
    modifier: Modifier = Modifier,
    state: CaptureScreenViewState,
    cameraPreviewState: CameraPreviewState,
    onRequestPermissionsClick: () -> Unit,
    onTap: (x: Float, y: Float, meteringPointFactory: MeteringPointFactory) -> Unit,
    onZoom: (Float) -> Unit,
    onShutterClick: () -> Unit,
    onSwitchLens: () -> Unit,
    onClosePostCaptureClick: () -> Unit,
    onExtensionSelected: (Int) -> Unit
) {
    if (state.cameraPermissionsViewState is CameraPermissionsViewState.CameraPermissionsRequestViewState) {
        CameraPermissionsScreen(
            modifier = modifier,
            shouldShowRationale = state.cameraPermissionsViewState.showRationale,
            onRequestPermissionsClick = onRequestPermissionsClick
        )
    } else {
        CameraViewFinder(
            modifier = modifier,
            isPreviewVisible = state.cameraPreviewScreenViewState.isVisible,
            isShutterButtonEnabled = state.cameraPreviewScreenViewState.shutterButtonViewState.isEnabled,
            isSwitchLensButtonEnabled = state.cameraPreviewScreenViewState.switchLensButtonViewState.isEnabled,
            isCameraControlsVisible = state.cameraPreviewScreenViewState.shutterButtonViewState.isVisible || state.cameraPreviewScreenViewState.switchLensButtonViewState.isVisible,
            isExtensionPickerVisible = state.cameraPreviewScreenViewState.extensionsSelectorViewState.isVisible,
            isSnapshotVisible = state.cameraPreviewScreenViewState.isSnapshotVisible,
            snapshot = state.cameraPreviewScreenViewState.snapshot,
            extensionPickerOptions = state.cameraPreviewScreenViewState.extensionsSelectorViewState.extensions,
            cameraPreviewState = cameraPreviewState,
            onTap = onTap,
            onZoom = onZoom,
            onShutterClick = onShutterClick,
            onSwitchLens = onSwitchLens,
            onExtensionSelected = onExtensionSelected
        )

        if (state.postCaptureScreenViewState is PostCaptureScreenViewState.PostCaptureScreenVisibleViewState) {
            CameraPostCaptureScreen(
                modifier = modifier,
                snapshot = state.cameraPreviewScreenViewState.snapshot,
                postCapturePhotoUri = state.postCaptureScreenViewState.uri,
                onClosePostCaptureClick
            )
        }
    }
}

