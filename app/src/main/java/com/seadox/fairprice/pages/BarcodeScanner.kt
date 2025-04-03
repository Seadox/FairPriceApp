package com.seadox.fairprice.pages

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.seadox.fairprice.R

@Composable
fun BarcodeScannerScreen(onBarcodeScanned: (String) -> Unit, onBackPress: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scannerBeepSound: MediaPlayer = MediaPlayer.create(context, R.raw.scanner_beep)

    BackHandler(onBack = onBackPress)

    StartCameraX(context, lifecycleOwner, {
        scannerBeepSound.setOnCompletionListener { it.release() }
        scannerBeepSound.start()
    }, onBarcodeScanned)
}

@Composable
fun StartCameraX(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    playSound: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

            val imageAnalysis = ImageAnalysis.Builder().build()
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                processImageBarcode(imageProxy, barcodeScanner, playSound, onBarcodeScanned)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
fun processImageBarcode(
    imageProxy: ImageProxy,
    barcodeScanner: BarcodeScanner,
    playSound: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    playSound()
                }
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        onBarcodeScanned(rawValue)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CameraX", "Barcode scanning failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}