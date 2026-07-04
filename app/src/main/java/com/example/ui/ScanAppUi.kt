package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Crop
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ScannedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalDensity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// High quality Color Matrix filters for Compose live previews
private val GrayscaleColorMatrix = ColorMatrix(
    floatArrayOf(
        0.299f, 0.587f, 0.114f, 0f, 0f,
        0.299f, 0.587f, 0.114f, 0f, 0f,
        0.299f, 0.587f, 0.114f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
)

private val DocumentColorMatrix = ColorMatrix(
    floatArrayOf(
        1.8f * 0.299f, 1.8f * 0.587f, 1.8f * 0.114f, 0f, -60f,
        1.8f * 0.299f, 1.8f * 0.587f, 1.8f * 0.114f, 0f, -60f,
        1.8f * 0.299f, 1.8f * 0.587f, 1.8f * 0.114f, 0f, -60f,
        0f, 0f, 0f, 1f, 0f
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAppUi(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val scannedPages by viewModel.scannedPages.collectAsStateWithLifecycle()
    val isCompiling by viewModel.isCompiling.collectAsStateWithLifecycle()
    val compileSuccess by viewModel.compileSuccess.collectAsStateWithLifecycle()

    var activeViewDoc by remember { mutableStateOf<ScannedDocument?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    var showRenameDialog by remember { mutableStateOf<ScannedDocument?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ScannedDocument?>(null) }

    // Interactive Cropper & Framing states
    var pageToCrop by remember { mutableStateOf<String?>(null) }
    var pageIdToCrop by remember { mutableStateOf<String?>(null) }
    var isNewPageToCrop by remember { mutableStateOf(true) }

    // Dialog trigger states
    var showMergeDialog by remember { mutableStateOf(false) }
    var showProtectDialog by remember { mutableStateOf(false) }

    // Standalone PDF to Word conversion states
    var selectedExternalPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedExternalPdfName by remember { mutableStateOf("") }
    var showExternalPdfWordDialog by remember { mutableStateOf(false) }
    var externalPdfConversionError by remember { mutableStateOf<String?>(null) }
    var externalPdfConvertedFile by remember { mutableStateOf<File?>(null) }

    val externalPdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedExternalPdfUri = uri
            selectedExternalPdfName = getUriDisplayName(context, uri)
            externalPdfConversionError = null
            externalPdfConvertedFile = null
            showExternalPdfWordDialog = true
            
            // Start the conversion!
            val tempFile = copyUriToTempFile(context, uri, selectedExternalPdfName)
            if (tempFile != null) {
                viewModel.convertLocalPdfToWord(context, tempFile, selectedExternalPdfName) { file, err ->
                    if (err != null) {
                        externalPdfConversionError = err
                    } else if (file != null) {
                        externalPdfConvertedFile = file
                    }
                    // Clean up temp imported PDF file
                    try {
                        tempFile.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                externalPdfConversionError = "Không thể đọc và tải tệp tin PDF này."
            }
        }
    }

    val ocrResult by viewModel.ocrResult.collectAsStateWithLifecycle()
    val isOcrRunning by viewModel.isOcrRunning.collectAsStateWithLifecycle()

    // Camera Result Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoFile != null) {
            pageToCrop = currentPhotoFile!!.absolutePath
            isNewPageToCrop = true
        }
    }

    // Gallery Picker Result Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val tempFile = try {
                viewModel.createTempPhotoFile(context)
            } catch (e: Exception) {
                null
            }
            if (tempFile != null) {
                val copied = try {
                    val input = context.contentResolver.openInputStream(uri)
                    if (input != null) {
                        try {
                            val output = FileOutputStream(tempFile)
                            try {
                                input.copyTo(output)
                            } finally {
                                output.close()
                            }
                        } finally {
                            input.close()
                        }
                    }
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                if (copied) {
                    pageToCrop = tempFile.absolutePath
                    isNewPageToCrop = true
                }
            }
        }
    }

    // Camera Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = viewModel.createTempPhotoFile(context)
                currentPhotoFile = file
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.aistudio.scanpdf.whzpqn.fileprovider",
                    file
                )
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture documents.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                              )
                        }
                        Column {
                            Text(
                                text = "ScanPDF",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "v1.2.0 (Build 240)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.testTag("action_camera")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan with Camera",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("action_gallery")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Import from Gallery",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (documents.isEmpty()) {
                // Empty State with generated image
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 450.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Load our generated hero image!
                            Image(
                                painter = painterResource(id = R.drawable.img_scanner_empty_state_1783000305870),
                                contentDescription = "ScanPDF Illustration",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "No Scanned PDFs Yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Convert receipts, books, or notes into beautifully formatted PDFs in seconds. Tap below to begin your first scan.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_state_scan_button")
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Document")
                            }
                        }
                    }
                }
            } else {
                // Geometric Balance List of Scans & Dashboard
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Capture Viewfinder Hero
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ViewfinderCard(
                            onClick = {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        )
                    }

                    // 2. Quick Tools Section
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        QuickToolsGrid(
                            onImportClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onMergeClick = {
                                if (documents.size >= 2) {
                                    showMergeDialog = true
                                } else {
                                    Toast.makeText(context, "Cần ít nhất 2 tài liệu để gộp PDF.", Toast.LENGTH_LONG).show()
                                }
                            },
                            onOcrClick = {
                                if (documents.isNotEmpty()) {
                                    val firstDoc = documents.first()
                                    try {
                                        val file = File(firstDoc.filePath)
                                        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                                        try {
                                            val renderer = PdfRenderer(pfd)
                                            try {
                                                if (renderer.pageCount > 0) {
                                                    val page = renderer.openPage(0)
                                                    try {
                                                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                                                        bitmap.eraseColor(android.graphics.Color.WHITE)
                                                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                                        val tempFile = viewModel.createTempPhotoFile(context)
                                                        val fos = FileOutputStream(tempFile)
                                                        try {
                                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                                                        } finally {
                                                            fos.close()
                                                        }
                                                        viewModel.runOcrOnPage(tempFile.absolutePath)
                                                        bitmap.recycle()
                                                    } finally {
                                                        page.close()
                                                    }
                                                }
                                            } finally {
                                                renderer.close()
                                            }
                                        } finally {
                                            pfd.close()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Lỗi render OCR: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Hãy thực hiện quét tài liệu trước khi chạy AI OCR.", Toast.LENGTH_LONG).show()
                                }
                            },
                            onProtectClick = {
                                if (documents.isNotEmpty()) {
                                    showProtectDialog = true
                                } else {
                                    Toast.makeText(context, "Hãy thực hiện quét tài liệu trước khi tạo dấu bản quyền.", Toast.LENGTH_LONG).show()
                                }
                            },
                            onPdfToWordClick = {
                                try {
                                    externalPdfPickerLauncher.launch("application/pdf")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Không thể mở bộ chọn tệp: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    // 3. Section Title
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT SCANS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Text(
                                text = "Total: ${documents.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 4. Scanned PDF Cards
                    items(documents, key = { it.id }) { doc ->
                        DocumentCard(
                            document = doc,
                            onClick = { activeViewDoc = doc },
                            onRename = { showRenameDialog = doc },
                            onDelete = { showDeleteDialog = doc },
                            onShare = { sharePdf(context, doc.filePath, doc.name) }
                        )
                    }
                }
            }

            // Interactive Scan Session Composer Overlay
            AnimatedVisibility(
                visible = scannedPages.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                ScanComposerView(
                    pages = scannedPages,
                    isCompiling = isCompiling,
                    onSave = { title -> viewModel.compileToPdf(context, title) },
                    onCancel = { viewModel.clearSession() },
                    onRotate = { pageId -> viewModel.rotatePage(pageId) },
                    onFilter = { pageId, filter -> viewModel.changePageFilter(pageId, filter) },
                    onDeletePage = { pageId -> viewModel.removePage(pageId) },
                    onMoveUp = { idx -> viewModel.movePageUp(idx) },
                    onMoveDown = { idx -> viewModel.movePageDown(idx) },
                    onAddMoreCamera = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    onAddMoreGallery = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCropPage = { pageId, filePath ->
                        pageToCrop = filePath
                        pageIdToCrop = pageId
                        isNewPageToCrop = false
                    }
                )
            }

            // Inline PDF Reader overlay
            AnimatedVisibility(
                visible = activeViewDoc != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                activeViewDoc?.let { doc ->
                    PdfViewerDialog(
                        document = doc,
                        onDismiss = { activeViewDoc = null },
                        onShare = { sharePdf(context, doc.filePath, doc.name) },
                        viewModel = viewModel
                    )
                }
            }

            // Rename Dialog
            showRenameDialog?.let { doc ->
                var nameInput by remember { mutableStateOf(doc.name.removeSuffix(".pdf")) }
                AlertDialog(
                    onDismissRequest = { showRenameDialog = null },
                    title = { Text("Đổi tên tệp PDF", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Tên tài liệu") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rename_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nameInput.isNotBlank()) {
                                    viewModel.renameDocument(doc, nameInput.trim())
                                    showRenameDialog = null
                                }
                            },
                            modifier = Modifier.testTag("rename_confirm")
                        ) {
                            Text("Lưu")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = null }) {
                            Text("Hủy")
                        }
                    }
                )
            }

            // Delete Confirmation Dialog
            showDeleteDialog?.let { doc ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Xóa tài liệu", fontWeight = FontWeight.Bold) },
                    text = { Text("Bạn có chắc chắn muốn xóa tài liệu '${doc.name}'? Thầy cô sẽ không thể khôi phục lại tài liệu này.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteDocument(doc)
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_confirm")
                        ) {
                            Text("Xóa")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Hủy")
                        }
                    }
                )
            }

            // --- Dialogs & Overlays for High-Fidelity Scan Tools ---

            // 1. Perspective Cropper Dialog
            pageToCrop?.let { path ->
                PerspectiveCropperDialog(
                    filePath = path,
                    onDismiss = {
                        pageToCrop = null
                        pageIdToCrop = null
                    },
                    onApply = { warpedPath ->
                        if (isNewPageToCrop) {
                            viewModel.addPage(warpedPath)
                        } else {
                            val id = pageIdToCrop
                            if (id != null) {
                                val originalPage = scannedPages.find { it.id == id }
                                if (originalPage != null) {
                                    val originalFile = File(originalPage.filePath)
                                    val warpedFile = File(warpedPath)
                                    if (warpedFile.exists()) {
                                        try {
                                            warpedFile.copyTo(originalFile, overwrite = true)
                                            warpedFile.delete()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    viewModel.changePageFilter(id, originalPage.filter)
                                }
                            }
                        }
                        pageToCrop = null
                        pageIdToCrop = null
                    },
                    viewModel = viewModel
                )
            }

            // 2. Merge PDFs Dialog
            if (showMergeDialog) {
                MergePdfsDialog(
                    documents = documents,
                    onDismiss = { showMergeDialog = false },
                    onMerge = { selectedDocs, name ->
                        viewModel.mergeMultiplePdfs(context, selectedDocs, name)
                        showMergeDialog = false
                    }
                )
            }

            // 3. Protect/Watermark PDF Dialog
            if (showProtectDialog) {
                ProtectPdfDialog(
                    documents = documents,
                    onDismiss = { showProtectDialog = false },
                    onProtect = { doc, watermarkText, outputName ->
                        viewModel.protectPdfWithWatermark(context, doc, watermarkText, outputName)
                        showProtectDialog = false
                    }
                )
            }

            // 4. OCR Result Dialog
            if (ocrResult != null) {
                OcrResultDialog(
                    text = ocrResult ?: "",
                    onDismiss = { viewModel.clearOcr() }
                )
            }

            // 5. Standalone PDF to Word Dialog
            val isPdfToWordRunning by viewModel.isPdfToWordRunning.collectAsStateWithLifecycle()
            val pdfToWordProgress by viewModel.pdfToWordProgress.collectAsStateWithLifecycle()

            if (showExternalPdfWordDialog) {
                StandalonePdfToWordDialog(
                    isPdfToWordRunning = isPdfToWordRunning,
                    pdfToWordProgress = pdfToWordProgress,
                    conversionError = externalPdfConversionError,
                    convertedFile = externalPdfConvertedFile,
                    displayName = selectedExternalPdfName,
                    onDismiss = {
                        showExternalPdfWordDialog = false
                        externalPdfConversionError = null
                        externalPdfConvertedFile = null
                        viewModel.clearPdfToWordResult()
                    }
                )
            }
        }
    }
}

@Composable
fun PdfThumbnail(
    filePath: String,
    modifier: Modifier = Modifier
) {
    var thumbnailBitmap by remember(filePath) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(filePath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            if (renderer.pageCount > 0) {
                                renderer.openPage(0).use { page ->
                                    val scale = 0.2f
                                    val width = (page.width * scale).toInt().coerceAtLeast(100)
                                    val height = (page.height * scale).toInt().coerceAtLeast(150)
                                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    thumbnailBitmap = bitmap
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (thumbnailBitmap != null) {
        Image(
            bitmap = thumbnailBitmap!!.asImageBitmap(),
            contentDescription = "PDF Page Thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "PDF Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun DocumentCard(
    document: ScannedDocument,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("document_card_${document.id}")
    ) {
        Column {
            // Live-Rendered PDF First Page Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White)
            ) {
                PdfThumbnail(
                    filePath = document.filePath,
                    modifier = Modifier.fillMaxSize()
                )
                // Page indicator badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "${document.pageCount} ${if (document.pageCount == 1) "page" else "pages"}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Document Details
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = document.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp).testTag("doc_menu_button_${document.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options Menu",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onShare()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onRename()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(document.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formatFileSize(document.fileSize),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanComposerView(
    pages: List<ScanPage>,
    isCompiling: Boolean,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    onRotate: (String) -> Unit,
    onFilter: (String, ScanFilter) -> Unit,
    onDeletePage: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onAddMoreCamera: () -> Unit,
    onAddMoreGallery: () -> Unit,
    onCropPage: (String, String) -> Unit
) {
    var docTitle by remember { mutableStateOf("") }
    var selectedPageId by remember(pages) { mutableStateOf(pages.firstOrNull()?.id ?: "") }
    val selectedPage = pages.find { it.id == selectedPageId } ?: pages.firstOrNull()

    // Default title based on time
    LaunchedEffect(pages) {
        if (docTitle.isEmpty()) {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            docTitle = "Scan_${sdf.format(Date())}"
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isCompiling) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Compiling PDF...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enhancing scans and flattening pages into a high quality PDF document",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Scan Session", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onCancel) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel Scan")
                            }
                        },
                        actions = {
                            Button(
                                onClick = { onSave(docTitle) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                modifier = Modifier.testTag("save_pdf_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save PDF")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    // Document Title Input
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("PDF Filename") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pdf_title_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Selected Page Preview (Large)
                    selectedPage?.let { page ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.DarkGray)
                        ) {
                            // Live Color Matrix Rendering to showcase filter real-time!
                            val matrixFilter = when (page.filter) {
                                ScanFilter.ORIGINAL -> null
                                ScanFilter.GRAYSCALE -> ColorFilter.colorMatrix(GrayscaleColorMatrix)
                                ScanFilter.DOCUMENT -> ColorFilter.colorMatrix(DocumentColorMatrix)
                            }

                            Image(
                                bitmap = BitmapFactory.decodeFile(page.filePath).asImageBitmap(),
                                contentDescription = "Active Page Preview",
                                colorFilter = matrixFilter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(page.rotation),
                                contentScale = ContentScale.Fit
                            )

                            // Editor Actions overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                IconButton(onClick = { onCropPage(page.id, page.filePath) }) {
                                    Icon(Icons.Default.Crop, contentDescription = "AI Crop", tint = Color.White)
                                }
                                IconButton(onClick = { onRotate(page.id) }) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    val nextFilter = when (page.filter) {
                                        ScanFilter.ORIGINAL -> ScanFilter.GRAYSCALE
                                        ScanFilter.GRAYSCALE -> ScanFilter.DOCUMENT
                                        ScanFilter.DOCUMENT -> ScanFilter.ORIGINAL
                                    }
                                    onFilter(page.id, nextFilter)
                                }) {
                                    Icon(Icons.Default.ColorLens, contentDescription = "Change Filter", tint = Color.White)
                                }
                                IconButton(onClick = { onDeletePage(page.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            
                            // Page badge overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val pageIndex = pages.indexOf(page)
                                Text(
                                    text = "Page ${pageIndex + 1} of ${pages.size} • Filter: ${page.filter.name}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Horizontal Thumbnails Scroller & Reordering
                    Text(
                        text = "Pages Order",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Empty space, wait we can show pages horizontal scroller instead
                        }
                        
                        // Let's implement horizontal carousel scrollable page queue manually or with standard LazyRow
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(pages.size) { index ->
                                val p = pages[index]
                                val isSelected = p.id == selectedPageId
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedPageId = p.id }
                                ) {
                                    Image(
                                        bitmap = BitmapFactory.decodeFile(p.filePath).asImageBitmap(),
                                        contentDescription = "Thumbnail",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .rotate(p.rotation),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Small reorder handles overlay on selection
                                    if (isSelected) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.6f)),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Move Left",
                                                tint = if (index > 0) Color.White else Color.DarkGray,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable(enabled = index > 0) { onMoveUp(index) }
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Move Right",
                                                tint = if (index < pages.size - 1) Color.White else Color.DarkGray,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable(enabled = index < pages.size - 1) { onMoveDown(index) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add Page shortcuts inside Composer
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onAddMoreCamera,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Add page via camera", modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onAddMoreGallery,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Add page via gallery", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfViewerDialog(
    document: ScannedDocument,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    viewModel: DocumentViewModel
) {
    val filePath = document.filePath
    val title = document.name
    val context = LocalContext.current
    var pagesCount by remember { mutableStateOf(0) }
    var loadedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            pagesCount = renderer.pageCount
                            val list = mutableListOf<Bitmap>()
                            for (i in 0 until renderer.pageCount) {
                                renderer.openPage(i).use { page ->
                                    val scale = 1.5f
                                    val width = (page.width * scale).toInt()
                                    val height = (page.height * scale).toInt()
                                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    list.add(bitmap)
                                }
                            }
                            loadedPages = list
                        }
                    }
                } else {
                    errorMsg = "PDF file was not found on storage."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "Could not render PDF: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (pagesCount > 0) {
                                Text(
                                    text = "$pagesCount ${if (pagesCount == 1) "page" else "pages"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        val isOcrRunning by viewModel.isOcrRunning.collectAsStateWithLifecycle()
                        if (loadedPages.isNotEmpty()) {
                            if (isOcrRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(4.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                IconButton(onClick = {
                                    val firstBmp = loadedPages.firstOrNull()
                                    if (firstBmp != null) {
                                        val tempFile = viewModel.createTempPhotoFile(context)
                                        try {
                                            val fos = FileOutputStream(tempFile)
                                            try {
                                                firstBmp.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                                            } finally {
                                                fos.close()
                                            }
                                            viewModel.runOcrOnPage(tempFile.absolutePath)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = "AI OCR Trích xuất chữ",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Share Document")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (loadedPages.isNotEmpty()) {
                    var showProgressDialog by remember { mutableStateOf(false) }
                    var conversionError by remember { mutableStateOf<String?>(null) }
                    var convertedFile by remember { mutableStateOf<File?>(null) }
                    
                    val isPdfToWordRunning by viewModel.isPdfToWordRunning.collectAsStateWithLifecycle()
                    val pdfToWordProgress by viewModel.pdfToWordProgress.collectAsStateWithLifecycle()

                    LaunchedEffect(isPdfToWordRunning) {
                        if (isPdfToWordRunning) {
                            showProgressDialog = true
                        }
                    }

                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.convertPdfToWord(context, document) { file, err ->
                                        if (err != null) {
                                            conversionError = err
                                        } else if (file != null) {
                                            convertedFile = file
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("convert_pdf_to_word_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Chuyển PDF sang Word (AI)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Progress / Success / Error Custom Dialog
                    if (showProgressDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                if (!isPdfToWordRunning) {
                                    showProgressDialog = false
                                    conversionError = null
                                    convertedFile = null
                                    viewModel.clearPdfToWordResult()
                                }
                            },
                            title = null,
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    if (isPdfToWordRunning) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text(
                                            text = "Đang số hóa tài liệu...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = pdfToWordProgress,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoFixHigh,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Mẹo: Trí tuệ nhân tạo đang tự động tách đề thi, bảng biểu và văn bản thành các cột, hàng thông minh để thầy cô chỉnh sửa dễ nhất trên máy tính.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    } else if (conversionError != null) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Rất tiếc, đã xảy ra lỗi",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = conversionError ?: "Lỗi không xác định",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    } else if (convertedFile != null) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Chuyển Đổi Thành Công! 🎉",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Tài liệu Word sẵn sàng để thầy cô chỉnh sửa, in ấn.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = convertedFile?.name ?: "",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "Microsoft Word Document • ${formatFileSize(convertedFile?.length() ?: 0)}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                if (!isPdfToWordRunning) {
                                    if (convertedFile != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    convertedFile?.let { file ->
                                                        shareWordFile(context, file, title)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Chia sẻ")
                                            }
                                            Button(
                                                onClick = {
                                                    convertedFile?.let { file ->
                                                        openWordFile(context, file)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Mở Word")
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                showProgressDialog = false
                                                conversionError = null
                                                convertedFile = null
                                                viewModel.clearPdfToWordResult()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Đóng")
                                        }
                                    }
                                }
                            },
                            dismissButton = {
                                if (!isPdfToWordRunning && convertedFile != null) {
                                    TextButton(
                                        onClick = {
                                            showProgressDialog = false
                                            conversionError = null
                                            convertedFile = null
                                            viewModel.clearPdfToWordResult()
                                        }
                                    ) {
                                        Text("Đóng")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading Document...")
                    }
                } else if (errorMsg != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(errorMsg ?: "", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss) {
                            Text("Go Back")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(loadedPages.size) { idx ->
                            val bmp = loadedPages[idx]
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Page ${idx + 1}",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                            Text(
                                text = "Page ${idx + 1} of $pagesCount",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return java.text.DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun sharePdf(context: Context, filePath: String, title: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.scanpdf.whzpqn.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Scanned PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun openWordFile(context: Context, file: File) {
    try {
        if (!file.exists()) {
            Toast.makeText(context, "File không tồn tại.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.scanpdf.whzpqn.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/msword")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Mở tài liệu Word bằng..."))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Không thể tìm thấy ứng dụng mở tài liệu Word: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun shareWordFile(context: Context, file: File, title: String) {
    try {
        if (!file.exists()) {
            Toast.makeText(context, "File không tồn tại.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.scanpdf.whzpqn.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/msword"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Chia sẻ tài liệu Word"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Lỗi chia sẻ file Word: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ViewfinderCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val cornerColor = MaterialTheme.colorScheme.primary
            val cornerSize = 16.dp
            val strokeWidth = 3.dp

            // Top-Left corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopStart)
                    .drawBehind {
                        drawRect(color = cornerColor, size = size.copy(width = strokeWidth.toPx()))
                        drawRect(color = cornerColor, size = size.copy(height = strokeWidth.toPx()))
                    }
            )

            // Top-Right corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopEnd)
                    .drawBehind {
                        drawRect(
                            color = cornerColor,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width - strokeWidth.toPx(), 0f),
                            size = size.copy(width = strokeWidth.toPx())
                        )
                        drawRect(
                            color = cornerColor,
                            size = size.copy(height = strokeWidth.toPx())
                        )
                    }
            )

            // Bottom-Left corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomStart)
                    .drawBehind {
                        drawRect(
                            color = cornerColor,
                            size = size.copy(width = strokeWidth.toPx())
                        )
                        drawRect(
                            color = cornerColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidth.toPx()),
                            size = size.copy(height = strokeWidth.toPx())
                        )
                    }
            )

            // Bottom-Right corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomEnd)
                    .drawBehind {
                        drawRect(
                            color = cornerColor,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width - strokeWidth.toPx(), 0f),
                            size = size.copy(width = strokeWidth.toPx())
                        )
                        drawRect(
                            color = cornerColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidth.toPx()),
                            size = size.copy(height = strokeWidth.toPx())
                        )
                    }
            )

            // Main display inside the viewfinder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Capture New Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Auto-detect borders & enhance text quality",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickToolsGrid(
    onImportClick: () -> Unit,
    onMergeClick: () -> Unit,
    onOcrClick: () -> Unit,
    onProtectClick: () -> Unit,
    onPdfToWordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "TOOLS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolCard(
                icon = Icons.Default.Description,
                title = "PDF sang Word",
                onClick = onPdfToWordClick,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                icon = Icons.Default.PictureAsPdf,
                title = "Merge Scans",
                onClick = onMergeClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolCard(
                icon = Icons.Default.AutoFixHigh,
                title = "OCR Enhance",
                onClick = onOcrClick,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                icon = Icons.Default.Save,
                title = "Protect PDF",
                onClick = onProtectClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolCard(
                icon = Icons.Default.FolderOpen,
                title = "Import Files",
                onClick = onImportClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ToolCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .height(80.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- High-Fidelity Dialog & Scan Custom Tool Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerspectiveCropperDialog(
    filePath: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    viewModel: DocumentViewModel
) {
    val context = LocalContext.current
    val bitmap = remember(filePath) {
        try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap == null) {
        onDismiss()
        return
    }

    var tl by remember { mutableStateOf(android.graphics.PointF(0.15f, 0.15f)) }
    var tr by remember { mutableStateOf(android.graphics.PointF(0.85f, 0.15f)) }
    var br by remember { mutableStateOf(android.graphics.PointF(0.85f, 0.85f)) }
    var bl by remember { mutableStateOf(android.graphics.PointF(0.15f, 0.85f)) }

    var selectedTemplate by remember { mutableStateOf("CUSTOM") }
    var dragPointIndex by remember { mutableStateOf(-1) }
    var currentDragOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(bitmap) {
        val detected = viewModel.detectPaperCorners(bitmap)
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()
        tl = android.graphics.PointF((detected[0].x / w).coerceIn(0f, 1f), (detected[0].y / h).coerceIn(0f, 1f))
        tr = android.graphics.PointF((detected[1].x / w).coerceIn(0f, 1f), (detected[1].y / h).coerceIn(0f, 1f))
        br = android.graphics.PointF((detected[2].x / w).coerceIn(0f, 1f), (detected[2].y / h).coerceIn(0f, 1f))
        bl = android.graphics.PointF((detected[3].x / w).coerceIn(0f, 1f), (detected[3].y / h).coerceIn(0f, 1f))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Scaffold(
                containerColor = Color.Black,
                topBar = {
                    TopAppBar(
                        title = { Text("Cân Chỉnh Góc & Biên (AI)", color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                        actions = {
                            TextButton(
                                onClick = {
                                    val warpedPath = viewModel.applyPerspectiveWarp(
                                        srcFilePath = filePath,
                                        points = listOf(tl, tr, br, bl),
                                        aspectTemplate = selectedTemplate,
                                        context = context
                                    )
                                    if (warpedPath != null) {
                                        onApply(warpedPath)
                                    } else {
                                        Toast.makeText(context, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Xong", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    )
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .background(Color.DarkGray.copy(alpha = 0.8f))
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ĐỊNH DẠNG TÀI LIỆU",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("CUSTOM" to "Tự do", "A4" to "Khổ A4", "CCCD" to "Thẻ CCCD").forEach { (key, label) ->
                                OutlinedButton(
                                    onClick = {
                                        selectedTemplate = key
                                        if (key == "A4") {
                                            tl = android.graphics.PointF(0.2f, 0.1f)
                                            tr = android.graphics.PointF(0.8f, 0.1f)
                                            br = android.graphics.PointF(0.8f, 0.9f)
                                            bl = android.graphics.PointF(0.2f, 0.9f)
                                        } else if (key == "CCCD") {
                                            tl = android.graphics.PointF(0.15f, 0.3f)
                                            tr = android.graphics.PointF(0.85f, 0.3f)
                                            br = android.graphics.PointF(0.85f, 0.7f)
                                            bl = android.graphics.PointF(0.15f, 0.7f)
                                        } else {
                                            tl = android.graphics.PointF(0.15f, 0.15f)
                                            tr = android.graphics.PointF(0.85f, 0.15f)
                                            br = android.graphics.PointF(0.85f, 0.85f)
                                            bl = android.graphics.PointF(0.15f, 0.85f)
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (selectedTemplate == key) MaterialTheme.colorScheme.primary else Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                val detected = viewModel.detectPaperCorners(bitmap)
                                val w = bitmap.width.toFloat()
                                val h = bitmap.height.toFloat()
                                tl = android.graphics.PointF((detected[0].x / w).coerceIn(0f, 1f), (detected[0].y / h).coerceIn(0f, 1f))
                                tr = android.graphics.PointF((detected[1].x / w).coerceIn(0f, 1f), (detected[1].y / h).coerceIn(0f, 1f))
                                br = android.graphics.PointF((detected[2].x / w).coerceIn(0f, 1f), (detected[2].y / h).coerceIn(0f, 1f))
                                bl = android.graphics.PointF((detected[3].x / w).coerceIn(0f, 1f), (detected[3].y / h).coerceIn(0f, 1f))
                                Toast.makeText(context, "Tự động nhận diện thành công!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tự động bắt khung (AI Scaner)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) { paddingValues ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val boxW = constraints.maxWidth.toFloat()
                    val boxH = constraints.maxHeight.toFloat()
                    val boxRatio = boxW / boxH
                    val imgRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

                    val drawW: Float
                    val drawH: Float
                    if (imgRatio > boxRatio) {
                        drawW = boxW
                        drawH = boxW / imgRatio
                    } else {
                        drawH = boxH
                        drawW = boxH * imgRatio
                    }

                    val offsetX = (boxW - drawW) / 2
                    val offsetY = (boxH - drawH) / 2

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(drawW.dp, drawH.dp),
                        contentScale = ContentScale.Fit
                    )

                    val density = LocalDensity.current
                    val limitPx = with(density) { 50.dp.toPx() }
                    val handleRadiusPx = with(density) { 14.dp.toPx() }
                    val strokeWidthPx = with(density) { 3.dp.toPx() }
                    val ringStrokeWidthPx = with(density) { 1.dp.toPx() }

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .size(boxW.dp, boxH.dp)
                            .pointerInput(bitmap) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val tlOffset = Offset(offsetX + tl.x * drawW, offsetY + tl.y * drawH)
                                        val trOffset = Offset(offsetX + tr.x * drawW, offsetY + tr.y * drawH)
                                        val brOffset = Offset(offsetX + br.x * drawW, offsetY + br.y * drawH)
                                        val blOffset = Offset(offsetX + bl.x * drawW, offsetY + bl.y * drawH)

                                        val dTL = (offset - tlOffset).getDistance()
                                        val dTR = (offset - trOffset).getDistance()
                                        val dBR = (offset - brOffset).getDistance()
                                        val dBL = (offset - blOffset).getDistance()

                                        var minD = Float.MAX_VALUE
                                        var idx = -1

                                        if (dTL < minD && dTL < limitPx) { minD = dTL; idx = 0 }
                                        if (dTR < minD && dTR < limitPx) { minD = dTR; idx = 1 }
                                        if (dBR < minD && dBR < limitPx) { minD = dBR; idx = 2 }
                                        if (dBL < minD && dBL < limitPx) { minD = dBL; idx = 3 }

                                        dragPointIndex = idx
                                        currentDragOffset = offset
                                    },
                                    onDrag = { change, dragAmount ->
                                        if (dragPointIndex != -1) {
                                            change.consume()
                                            currentDragOffset += dragAmount
                                            
                                            val normX = ((currentDragOffset.x - offsetX) / drawW).coerceIn(0f, 1f)
                                            val normY = ((currentDragOffset.y - offsetY) / drawH).coerceIn(0f, 1f)
                                            val point = android.graphics.PointF(normX, normY)
                                            
                                            when (dragPointIndex) {
                                                0 -> tl = point
                                                1 -> tr = point
                                                2 -> br = point
                                                3 -> bl = point
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        dragPointIndex = -1
                                    }
                                )
                            }
                    ) {
                        val pTL = Offset(offsetX + tl.x * drawW, offsetY + tl.y * drawH)
                        val pTR = Offset(offsetX + tr.x * drawW, offsetY + tr.y * drawH)
                        val pBR = Offset(offsetX + br.x * drawW, offsetY + br.y * drawH)
                        val pBL = Offset(offsetX + bl.x * drawW, offsetY + bl.y * drawH)

                        val polyPath = Path().apply {
                            moveTo(pTL.x, pTL.y)
                            lineTo(pTR.x, pTR.y)
                            lineTo(pBR.x, pBR.y)
                            lineTo(pBL.x, pBL.y)
                            close()
                        }
                        
                        drawPath(path = polyPath, color = Color(0xFF00FF87).copy(alpha = 0.25f))
                        drawPath(path = polyPath, color = Color(0xFF00FF87), style = Stroke(width = strokeWidthPx))

                        listOf(pTL, pTR, pBR, pBL).forEachIndexed { idx, p ->
                            drawCircle(
                                color = if (dragPointIndex == idx) Color.White else Color(0xFF00FF87),
                                radius = handleRadiusPx,
                                center = p
                            )
                            drawCircle(
                                color = Color.Black,
                                radius = handleRadiusPx + ringStrokeWidthPx,
                                center = p,
                                style = Stroke(width = ringStrokeWidthPx)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MergePdfsDialog(
    documents: List<ScannedDocument>,
    onDismiss: () -> Unit,
    onMerge: (List<ScannedDocument>, String) -> Unit
) {
    var outputName by remember { mutableStateOf("Gộp_Tài_Liệu") }
    var selectedDocs by remember { mutableStateOf(emptyList<ScannedDocument>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gộp nhiều tài liệu PDF", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Chọn và sắp xếp các tài liệu cần gộp:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = outputName,
                    onValueChange = { outputName = it },
                    label = { Text("Tên tệp tin đầu ra (.pdf)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .height(240.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    items(documents) { doc ->
                        val isChecked = selectedDocs.contains(doc)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedDocs = if (isChecked) {
                                        selectedDocs - doc
                                    } else {
                                        selectedDocs + doc
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selectedDocs = if (isChecked) {
                                        selectedDocs - doc
                                    } else {
                                        selectedDocs + doc
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                Text("${doc.pageCount} trang • ${formatFileSize(doc.fileSize)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            if (isChecked) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            val idx = selectedDocs.indexOf(doc)
                                            if (idx > 0) {
                                                val newList = selectedDocs.toMutableList()
                                                newList.removeAt(idx)
                                                newList.add(idx - 1, doc)
                                                selectedDocs = newList
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            val idx = selectedDocs.indexOf(doc)
                                            if (idx < selectedDocs.size - 1) {
                                                val newList = selectedDocs.toMutableList()
                                                newList.removeAt(idx)
                                                newList.add(idx + 1, doc)
                                                selectedDocs = newList
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDocs.size >= 2 && outputName.isNotBlank()) {
                        onMerge(selectedDocs, outputName.trim())
                    }
                },
                enabled = selectedDocs.size >= 2 && outputName.isNotBlank()
            ) {
                Text("Gộp ngay (${selectedDocs.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ")
            }
        }
    )
}

@Composable
fun ProtectPdfDialog(
    documents: List<ScannedDocument>,
    onDismiss: () -> Unit,
    onProtect: (ScannedDocument, String, String) -> Unit
) {
    var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
    var watermarkText by remember { mutableStateOf("BẢN SAO BẢO MẬT") }
    var outputName by remember { mutableStateOf("Bảo_Mật_Tài_Liệu") }
    var showDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đóng dấu bảo mật PDF (Watermark)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Chọn tài liệu và nhập chữ đóng dấu bản quyền/bảo mật:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Document Selection display
                Text("Tài liệu cần đóng dấu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { showDropdown = !showDropdown }
                        .padding(12.dp)
                ) {
                    Text(selectedDoc?.name ?: "Chưa chọn tài liệu", fontWeight = FontWeight.Bold)
                }
                
                if (showDropdown) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        documents.forEach { doc ->
                            Text(
                                text = doc.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDoc = doc
                                        showDropdown = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Chữ đóng dấu (Ví dụ: CCCD KHÔNG SAO CHÉP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = outputName,
                    onValueChange = { outputName = it },
                    label = { Text("Tên tệp mới sau đóng dấu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoc
                    if (doc != null && watermarkText.isNotBlank() && outputName.isNotBlank()) {
                        onProtect(doc, watermarkText, outputName)
                    }
                },
                enabled = selectedDoc != null && watermarkText.isNotBlank() && outputName.isNotBlank()
            ) {
                Text("Đóng dấu ngay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ")
            }
        }
    )
}

@Composable
fun OcrResultDialog(
    text: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Văn bản trích xuất (AI OCR)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Dưới đây là văn bản được mô hình AI Gemini trích xuất trung thực từ ảnh tài liệu quét:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(text = text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("OCR text", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Đã sao chép văn bản vào khay nhớ tạm!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sao Chép")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun StandalonePdfToWordDialog(
    isPdfToWordRunning: Boolean,
    pdfToWordProgress: String,
    conversionError: String?,
    convertedFile: File?,
    displayName: String,
    onDismiss: () -> Unit,
    context: Context = LocalContext.current
) {
    if (isPdfToWordRunning || conversionError != null || convertedFile != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isPdfToWordRunning) {
                    onDismiss()
                }
            },
            title = null,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    if (isPdfToWordRunning) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Đang chuyển đổi PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pdfToWordProgress,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mẹo: Trí tuệ nhân tạo đang tự động phân tích cấu trúc, đề thi, câu hỏi và bảng biểu thông minh để thầy cô chỉnh sửa dễ nhất trên Microsoft Word.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else if (conversionError != null) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đã xảy ra lỗi chuyển đổi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = conversionError,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (convertedFile != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chuyển Đổi Thành Công! 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tài liệu Word đã sẵn sàng để thầy cô tải về, chỉnh sửa và in ấn.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = convertedFile.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Microsoft Word Document • ${formatFileSize(convertedFile.length())}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isPdfToWordRunning) {
                    if (convertedFile != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    shareWordFile(context, convertedFile, displayName)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chia sẻ", color = MaterialTheme.colorScheme.primary)
                            }
                            Button(
                                onClick = {
                                    openWordFile(context, convertedFile)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mở Word")
                            }
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Đóng")
                        }
                    }
                }
            }
        )
    }
}

fun copyUriToTempFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getUriDisplayName(context: Context, uri: Uri): String {
    var name = "Document.pdf"
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = cursor.getString(index)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) {
                name = path.substring(cut + 1)
            }
        }
    }
    return name
}

