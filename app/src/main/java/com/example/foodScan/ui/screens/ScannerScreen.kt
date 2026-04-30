package com.example.foodScan.ui.screens

import android.Manifest
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.foodScan.viewmodel.ProductViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@kotlin.OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(viewModel: ProductViewModel, modifier: Modifier = Modifier) {

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    when {
        cameraPermission.status.isGranted -> {
            ScannerContent(viewModel = viewModel, modifier = modifier)
        }
        cameraPermission.status.shouldShowRationale -> {
            PermissionRationaleContent(
                onRequest = { cameraPermission.launchPermissionRequest() },
                modifier = modifier
            )
        }
        else -> {
            LaunchedEffect(Unit) { cameraPermission.launchPermissionRequest() }
            PermissionRationaleContent(
                onRequest = { cameraPermission.launchPermissionRequest() },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ScannerContent(viewModel: ProductViewModel, modifier: Modifier = Modifier) {

    var detectedBarcode by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentProduct by viewModel.currentProduct.collectAsState()

    // When product loads successfully, show confirmation
    LaunchedEffect(currentProduct) {
        if (currentProduct != null) isScanning = false
    }

    Box(modifier = modifier.fillMaxSize()) {

        // Camera preview — always mounted, paused via isScanning flag
        CameraPreview(
            isScanning = isScanning,
            onBarcodeDetected = { barcode ->
                if (isScanning && detectedBarcode == null) {
                    detectedBarcode = barcode
                    isScanning = false
                }
            }
        )

        // Viewfinder overlay
        ScannerOverlay()

        // Bottom panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                // Idle — waiting to scan
                detectedBarcode == null && !isLoading -> {
                    Text(
                        text = "Pointez la caméra vers un code-barres",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                // Barcode detected — confirm before loading
                detectedBarcode != null && currentProduct == null && !isLoading && error == null -> {
                    Text(
                        text = "Code détecté",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = detectedBarcode!!,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                detectedBarcode = null
                                isScanning = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Rescanner")
                        }
                        Button(
                            onClick = { viewModel.loadProduct(detectedBarcode!!) }
                        ) {
                            Text("Rechercher")
                        }
                    }
                }

                // Loading
                isLoading -> {
                    CircularProgressIndicator(color = Color.White)
                    Text(
                        text = "Recherche en cours…",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Error
                error != null -> {
                    Text(
                        text = "Produit introuvable",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = {
                            viewModel.clearCurrentProduct()
                            detectedBarcode = null
                            isScanning = true
                        }
                    ) {
                        Text("Réessayer")
                    }
                }

                // Product found
                currentProduct != null -> {
                    Text(
                        text = "✓ Produit enregistré",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentProduct!!.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            viewModel.clearCurrentProduct()
                            detectedBarcode = null
                            isScanning = true
                        }
                    ) {
                        Text("Scanner un autre produit")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Semi-transparent background with a cut-out feel via border
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp, 160.dp)
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

@Composable
private fun CameraPreview(
    isScanning: Boolean,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            if (!isScanning) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull {
                                            it.valueType == Barcode.TYPE_PRODUCT ||
                                                    it.valueType == Barcode.TYPE_TEXT
                                        }?.rawValue?.let { onBarcodeDetected(it) }
                                    }
                                    .addOnFailureListener { Log.e("Scanner", "Scan failed", it) }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("Scanner", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun PermissionRationaleContent(
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Accès à la caméra requis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "L'application a besoin de la caméra pour scanner les codes-barres des produits.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRequest) {
                Text("Autoriser la caméra")
            }
        }
    }
}