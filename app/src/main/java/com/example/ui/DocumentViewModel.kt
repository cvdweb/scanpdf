package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ScannedDocument
import com.example.data.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

enum class ScanFilter {
    ORIGINAL,
    GRAYSCALE,
    DOCUMENT
}

data class ScanPage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filePath: String,
    val rotation: Float = 0f,
    val filter: ScanFilter = ScanFilter.ORIGINAL
)

class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {

    val allDocuments: StateFlow<List<ScannedDocument>> = repository.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _scannedPages = MutableStateFlow<List<ScanPage>>(emptyList())
    val scannedPages: StateFlow<List<ScanPage>> = _scannedPages.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _compileSuccess = MutableStateFlow<Boolean?>(null)
    val compileSuccess: StateFlow<Boolean?> = _compileSuccess.asStateFlow()

    fun addPage(filePath: String) {
        val current = _scannedPages.value.toMutableList()
        current.add(ScanPage(filePath = filePath))
        _scannedPages.value = current
    }

    fun removePage(pageId: String) {
        val page = _scannedPages.value.find { it.id == pageId }
        val current = _scannedPages.value.filter { it.id != pageId }
        _scannedPages.value = current
        page?.let {
            val file = File(it.filePath)
            if (file.exists() && file.parentFile?.name == "scan_cache") {
                file.delete()
            }
        }
    }

    fun rotatePage(pageId: String) {
        val current = _scannedPages.value.map { page ->
            if (page.id == pageId) {
                val nextRotation = (page.rotation + 90f) % 360f
                page.copy(rotation = nextRotation)
            } else {
                page
            }
        }
        _scannedPages.value = current
    }

    fun changePageFilter(pageId: String, filter: ScanFilter) {
        val current = _scannedPages.value.map { page ->
            if (page.id == pageId) {
                page.copy(filter = filter)
            } else {
                page
            }
        }
        _scannedPages.value = current
    }

    fun movePageUp(index: Int) {
        if (index <= 0 || index >= _scannedPages.value.size) return
        val current = _scannedPages.value.toMutableList()
        val temp = current[index]
        current[index] = current[index - 1]
        current[index - 1] = temp
        _scannedPages.value = current
    }

    fun movePageDown(index: Int) {
        if (index < 0 || index >= _scannedPages.value.size - 1) return
        val current = _scannedPages.value.toMutableList()
        val temp = current[index]
        current[index] = current[index + 1]
        current[index + 1] = temp
        _scannedPages.value = current
    }

    fun clearSession() {
        _scannedPages.value.forEach { page ->
            val file = File(page.filePath)
            if (file.exists() && file.parentFile?.name == "scan_cache") {
                file.delete()
            }
        }
        _scannedPages.value = emptyList()
        _compileSuccess.value = null
    }

    fun createTempPhotoFile(context: Context): File {
        val cacheDir = File(context.cacheDir, "scan_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File.createTempFile("scan_page_", ".jpg", cacheDir)
    }

    fun addPageFromUri(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val file = createTempPhotoFile(context)
            val copied = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            if (copied) {
                addPage(file.absolutePath)
            }
        }
    }

    fun compileToPdf(context: Context, title: String) {
        if (_scannedPages.value.isEmpty()) return
        _isCompiling.value = true
        _compileSuccess.value = null

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val pdfDocument = PdfDocument()
                    val pages = _scannedPages.value

                    for ((index, scanPage) in pages.withIndex()) {
                        var originalBitmap = BitmapFactory.decodeFile(scanPage.filePath)
                        if (originalBitmap == null) continue

                        if (scanPage.rotation != 0f) {
                            val matrix = Matrix().apply { postRotate(scanPage.rotation) }
                            val rotated = Bitmap.createBitmap(
                                originalBitmap, 0, 0,
                                originalBitmap.width, originalBitmap.height, matrix, true
                            )
                            originalBitmap.recycle()
                            originalBitmap = rotated
                        }

                        val filteredBitmap = when (scanPage.filter) {
                            ScanFilter.ORIGINAL -> originalBitmap
                            ScanFilter.GRAYSCALE -> applyGrayscaleFilter(originalBitmap)
                            ScanFilter.DOCUMENT -> applyDocumentFilter(originalBitmap)
                        }

                        val pageInfo = PdfDocument.PageInfo.Builder(
                            filteredBitmap.width,
                            filteredBitmap.height,
                            index + 1
                        ).create()
                        val page = pdfDocument.startPage(pageInfo)
                        page.canvas.drawBitmap(filteredBitmap, 0f, 0f, null)
                        pdfDocument.finishPage(page)

                        if (filteredBitmap != originalBitmap) {
                            filteredBitmap.recycle()
                        }
                        originalBitmap.recycle()
                    }

                    val docDir = File(context.filesDir, "scanned_pdfs")
                    if (!docDir.exists()) {
                        docDir.mkdirs()
                    }

                    val sanitizedTitle = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf"
                    val pdfFile = File(docDir, sanitizedTitle)
                    
                    FileOutputStream(pdfFile).use { fos ->
                        pdfDocument.writeTo(fos)
                    }
                    pdfDocument.close()

                    val newDoc = ScannedDocument(
                        name = sanitizedTitle,
                        filePath = pdfFile.absolutePath,
                        pageCount = pages.size,
                        fileSize = pdfFile.length(),
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertDocument(newDoc)

                    try {
                        context.cacheDir.resolve("scan_cache").deleteRecursively()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    _scannedPages.value = emptyList()

                    _compileSuccess.value = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    _compileSuccess.value = false
                } finally {
                    _isCompiling.value = false
                }
            }
        }
    }

    fun deleteDocument(document: ScannedDocument) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val file = File(document.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                    repository.deleteDocumentById(document.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun renameDocument(document: ScannedDocument, newName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val sanitized = if (newName.endsWith(".pdf", ignoreCase = true)) newName else "$newName.pdf"
                    val oldFile = File(document.filePath)
                    val newFile = File(oldFile.parentFile, sanitized)
                    if (oldFile.exists() && oldFile.renameTo(newFile)) {
                        val updated = document.copy(name = sanitized, filePath = newFile.absolutePath)
                        repository.updateDocument(updated)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun applyGrayscaleFilter(src: Bitmap): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        val cm = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    private fun applyDocumentFilter(src: Bitmap): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()
        
        val contrastAndBrightness = ColorMatrix(floatArrayOf(
            1.8f, 0f, 0f, 0f, -60f,
            0f, 1.8f, 0f, 0f, -60f,
            0f, 0f, 1.8f, 0f, -60f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val grayscale = ColorMatrix().apply { setSaturation(0f) }
        val combined = ColorMatrix().apply {
            setConcat(contrastAndBrightness, grayscale)
        }
        
        paint.colorFilter = ColorMatrixColorFilter(combined)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    // --- High-Fidelity Scanner Corner Detection & Warping ---

    fun detectPaperCorners(bitmap: Bitmap): List<android.graphics.PointF> {
        val w = bitmap.width
        val h = bitmap.height
        // Fallback points (inset by 10% from margins)
        val fallback = listOf(
            android.graphics.PointF(w * 0.15f, h * 0.15f), // TL
            android.graphics.PointF(w * 0.85f, h * 0.15f), // TR
            android.graphics.PointF(w * 0.85f, h * 0.85f), // BR
            android.graphics.PointF(w * 0.15f, h * 0.85f)  // BL
        )
        
        val scanScale = 4
        val scanW = (w / scanScale).coerceAtLeast(10)
        val scanH = (h / scanScale).coerceAtLeast(10)
        
        try {
            val scaled = Bitmap.createScaledBitmap(bitmap, scanW, scanH, false)
            val pixels = IntArray(scanW * scanH)
            scaled.getPixels(pixels, 0, scanW, 0, 0, scanW, scanH)
            scaled.recycle()
            
            // Sweep from corners diagonally inward to find first major brightness change
            fun findEdgeAlongLine(startX: Int, startY: Int, endX: Int, endY: Int): android.graphics.PointF {
                val steps = 60
                var prevLuma = -1.0
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val x = (startX + (endX - startX) * t).toInt().coerceIn(0, scanW - 1)
                    val y = (startY + (endY - startY) * t).toInt().coerceIn(0, scanH - 1)
                    val p = pixels[y * scanW + x]
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val luma = 0.299 * r + 0.587 * g + 0.114 * b
                    
                    if (prevLuma >= 0) {
                        val diff = Math.abs(luma - prevLuma)
                        if (diff > 18.0 && luma > 100) { // white paper edge transition
                            return android.graphics.PointF(x * scanScale.toFloat(), y * scanScale.toFloat())
                        }
                    }
                    prevLuma = luma
                }
                return android.graphics.PointF(startX * scanScale.toFloat(), startY * scanScale.toFloat())
            }
            
            val tl = findEdgeAlongLine(0, 0, scanW / 2, scanH / 2)
            val tr = findEdgeAlongLine(scanW - 1, 0, scanW / 2, scanH / 2)
            val br = findEdgeAlongLine(scanW - 1, scanH - 1, scanW / 2, scanH / 2)
            val bl = findEdgeAlongLine(0, scanH - 1, scanW / 2, scanH / 2)
            
            return listOf(tl, tr, br, bl)
        } catch (e: Exception) {
            e.printStackTrace()
            return fallback
        }
    }

    fun applyPerspectiveWarp(
        srcFilePath: String,
        points: List<android.graphics.PointF>,
        aspectTemplate: String, // "A4", "CCCD", "CUSTOM"
        context: Context
    ): String? {
        val srcBitmap = BitmapFactory.decodeFile(srcFilePath) ?: return null
        val w = srcBitmap.width.toFloat()
        val h = srcBitmap.height.toFloat()
        
        // Target dimensions
        var targetW = 1000
        var targetH = 1414 // Default A4 vertical ratio
        
        when (aspectTemplate) {
            "CCCD" -> {
                targetW = 1012
                targetH = 638 // Standard bank / identity card aspect ratio
            }
            "A4" -> {
                targetW = 1000
                targetH = 1414
            }
            else -> {
                // CUSTOM aspect ratio: compute from average segment sizes
                val topWidth = Math.hypot((points[1].x - points[0].x).toDouble() * w, (points[1].y - points[0].y).toDouble() * h)
                val bottomWidth = Math.hypot((points[2].x - points[3].x).toDouble() * w, (points[2].y - points[3].y).toDouble() * h)
                val leftHeight = Math.hypot((points[3].x - points[0].x).toDouble() * w, (points[3].y - points[0].y).toDouble() * h)
                val rightHeight = Math.hypot((points[2].x - points[1].x).toDouble() * w, (points[2].y - points[1].y).toDouble() * h)
                
                targetW = Math.max(topWidth, bottomWidth).toInt().coerceIn(400, 2500)
                targetH = Math.max(leftHeight, rightHeight).toInt().coerceIn(400, 2500)
            }
        }
        
        return try {
            val destBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(destBitmap)
            val matrix = Matrix()
            
            val srcArr = floatArrayOf(
                points[0].x * w, points[0].y * h, // TL
                points[1].x * w, points[1].y * h, // TR
                points[2].x * w, points[2].y * h, // BR
                points[3].x * w, points[3].y * h  // BL
            )
            
            val destArr = floatArrayOf(
                0f, 0f,
                targetW.toFloat(), 0f,
                targetW.toFloat(), targetH.toFloat(),
                0f, targetH.toFloat()
            )
            
            matrix.setPolyToPoly(srcArr, 0, destArr, 0, 4)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(srcBitmap, matrix, paint)
            srcBitmap.recycle()
            
            val outputFile = createTempPhotoFile(context)
            FileOutputStream(outputFile).use { fos ->
                destBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
            destBitmap.recycle()
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            srcBitmap.recycle()
            null
        }
    }

    // --- Gemini AI OCR Text Extraction ---

    private val _isOcrRunning = MutableStateFlow(false)
    val isOcrRunning: StateFlow<Boolean> = _isOcrRunning.asStateFlow()

    private val _ocrResult = MutableStateFlow<String?>(null)
    val ocrResult: StateFlow<String?> = _ocrResult.asStateFlow()

    fun runOcrOnPage(filePath: String) {
        _isOcrRunning.value = true
        _ocrResult.value = null
        
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    return@withContext "Lỗi: Khóa API Gemini chưa được thiết lập trong cài đặt ứng dụng."
                }
                
                try {
                    val bitmap = BitmapFactory.decodeFile(filePath) ?: return@withContext "Lỗi: Không thể tải ảnh để quét OCR."
                    
                    val outputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val base64Image = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
                    bitmap.recycle()
                    
                    val requestJson = org.json.JSONObject().apply {
                        val contentsArray = org.json.JSONArray().apply {
                            val contentObj = org.json.JSONObject().apply {
                                val partsArray = org.json.JSONArray().apply {
                                    val textPart = org.json.JSONObject().put("text", "Trích xuất toàn bộ chữ viết từ ảnh tài liệu này một cách đầy đủ và trung thực nhất. Trả về dưới định dạng Markdown đẹp, giữ nguyên dòng, đoạn văn và tiêu đề rõ ràng bằng tiếng Việt.")
                                    val imagePart = org.json.JSONObject().put("inlineData", org.json.JSONObject().apply {
                                        put("mimeType", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                    put(textPart)
                                    put(imagePart)
                                }
                                put("parts", partsArray)
                            }
                            put(contentObj)
                        }
                        put("contents", contentsArray)
                    }
                    
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                        
                    val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
                    
                    var ocrTextResult = ""
                    var lastErrorMsg = ""
                    val modelsToTry = listOf("gemini-3.5-flash", "gemini-2.5-flash", "gemini-1.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview")
                    
                    for (modelName in modelsToTry) {
                        try {
                            val request = okhttp3.Request.Builder()
                                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                                .post(requestBody)
                                .build()
                                
                            val text = client.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) {
                                    val errorBody = response.body?.string() ?: ""
                                    val detailedMsg = try {
                                        org.json.JSONObject(errorBody).getJSONObject("error").getString("message")
                                    } catch (e: Exception) {
                                        errorBody
                                    }
                                    throw Exception("Mã lỗi ${response.code}: $detailedMsg")
                                }
                                val responseString = response.body?.string() ?: throw Exception("Không nhận được phản hồi từ AI")
                                val responseJson = org.json.JSONObject(responseString)
                                val candidates = responseJson.optJSONArray("candidates")
                                if (candidates != null && candidates.length() > 0) {
                                    val firstCandidate = candidates.getJSONObject(0)
                                    val contentObj = firstCandidate.optJSONObject("content")
                                    val partsArray = contentObj?.optJSONArray("parts")
                                    if (partsArray != null && partsArray.length() > 0) {
                                        partsArray.getJSONObject(0).optString("text") ?: ""
                                    } else {
                                        ""
                                    }
                                } else {
                                    ""
                                }
                            }
                            if (text.isNotEmpty()) {
                                ocrTextResult = text
                                lastErrorMsg = ""
                                break
                            }
                        } catch (e: Exception) {
                            lastErrorMsg = e.message ?: "Lỗi chưa rõ"
                        }
                    }
                    
                    if (ocrTextResult.isEmpty() && lastErrorMsg.isNotEmpty()) {
                        "Lỗi kết nối AI: $lastErrorMsg"
                    } else if (ocrTextResult.isEmpty()) {
                        "AI không phát hiện thấy bất kỳ văn bản nào trong tài liệu này."
                    } else {
                        ocrTextResult
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Lỗi hệ thống OCR: ${e.message}"
                }
            }
            _ocrResult.value = result
            _isOcrRunning.value = false
        }
    }

    fun clearOcr() {
        _ocrResult.value = null
    }

    // --- PDF Merging Capability ---

    fun mergeMultiplePdfs(context: Context, docList: List<ScannedDocument>, mergedName: String) {
        if (docList.size < 2) return
        _isCompiling.value = true
        _compileSuccess.value = null
        
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val pdfDocument = PdfDocument()
                    var globalPageIndex = 1
                    
                    for (doc in docList) {
                        val file = File(doc.filePath)
                        if (!file.exists()) continue
                        
                        val pfd = android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                        try {
                            val renderer = PdfRenderer(pfd)
                            try {
                                for (i in 0 until renderer.pageCount) {
                                    val page = renderer.openPage(i)
                                    try {
                                        val width = page.width
                                        val height = page.height
                                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                        bitmap.eraseColor(android.graphics.Color.WHITE)
                                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                        
                                        val pageInfo = PdfDocument.PageInfo.Builder(width, height, globalPageIndex++).create()
                                        val newPage = pdfDocument.startPage(pageInfo)
                                        newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                                        pdfDocument.finishPage(newPage)
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
                    }
                    
                    val docDir = File(context.filesDir, "scanned_pdfs")
                    if (!docDir.exists()) docDir.mkdirs()
                    
                    val sanitizedTitle = if (mergedName.endsWith(".pdf", ignoreCase = true)) mergedName else "$mergedName.pdf"
                    val outputFile = File(docDir, sanitizedTitle)
                    
                    FileOutputStream(outputFile).use { fos ->
                        pdfDocument.writeTo(fos)
                    }
                    pdfDocument.close()
                    
                    val newDoc = ScannedDocument(
                        name = sanitizedTitle,
                        filePath = outputFile.absolutePath,
                        pageCount = globalPageIndex - 1,
                        fileSize = outputFile.length(),
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertDocument(newDoc)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            _compileSuccess.value = success
            _isCompiling.value = false
        }
    }

    // --- PDF Watermarking / Protect Capability ---

    fun protectPdfWithWatermark(context: Context, doc: ScannedDocument, watermarkText: String, outputName: String) {
        _isCompiling.value = true
        _compileSuccess.value = null
        
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val srcFile = File(doc.filePath)
                    if (!srcFile.exists()) return@withContext false
                    
                    val pdfDocument = PdfDocument()
                    var pageIndex = 1
                    
                    val pfd = android.os.ParcelFileDescriptor.open(srcFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                    try {
                        val renderer = PdfRenderer(pfd)
                        try {
                            for (i in 0 until renderer.pageCount) {
                                val page = renderer.openPage(i)
                                try {
                                    val width = page.width
                                    val height = page.height
                                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    bitmap.eraseColor(android.graphics.Color.WHITE)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    
                                    val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageIndex++).create()
                                    val newPage = pdfDocument.startPage(pageInfo)
                                    val canvas = newPage.canvas
                                    
                                    // Draw original page bitmap
                                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                                    
                                    // Overlay custom security watermark text
                                    val paint = Paint().apply {
                                        color = android.graphics.Color.RED
                                        alpha = 32 // transparent watermark
                                        textSize = (width / 14).toFloat()
                                        isAntiAlias = true
                                        textAlign = Paint.Align.CENTER
                                        style = Paint.Style.FILL
                                        strokeWidth = 2f
                                    }
                                    
                                    canvas.save()
                                    canvas.rotate(-40f, (width / 2).toFloat(), (height / 2).toFloat())
                                    
                                    // Draw multiple repeating lines of watermark for security
                                    canvas.drawText(watermarkText, (width / 2).toFloat(), (height / 2).toFloat() - 100, paint)
                                    canvas.drawText(watermarkText, (width / 2).toFloat(), (height / 2).toFloat() + 100, paint)
                                    
                                    canvas.restore()
                                    pdfDocument.finishPage(newPage)
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
                    
                    val docDir = File(context.filesDir, "scanned_pdfs")
                    if (!docDir.exists()) docDir.mkdirs()
                    
                    val sanitizedTitle = if (outputName.endsWith(".pdf", ignoreCase = true)) outputName else "$outputName.pdf"
                    val outputFile = File(docDir, sanitizedTitle)
                    
                    FileOutputStream(outputFile).use { fos ->
                        pdfDocument.writeTo(fos)
                    }
                    pdfDocument.close()
                    
                    val newDoc = ScannedDocument(
                        name = sanitizedTitle,
                        filePath = outputFile.absolutePath,
                        pageCount = pageIndex - 1,
                        fileSize = outputFile.length(),
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertDocument(newDoc)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            _compileSuccess.value = success
            _isCompiling.value = false
        }
    }

    // --- Advanced PDF to Word Capability (Education/Teacher Oriented) ---

    private val _isPdfToWordRunning = MutableStateFlow(false)
    val isPdfToWordRunning: StateFlow<Boolean> = _isPdfToWordRunning.asStateFlow()

    private val _pdfToWordProgress = MutableStateFlow("")
    val pdfToWordProgress: StateFlow<String> = _pdfToWordProgress.asStateFlow()

    private val _pdfToWordResultFile = MutableStateFlow<File?>(null)
    val pdfToWordResultFile: StateFlow<File?> = _pdfToWordResultFile.asStateFlow()

    fun clearPdfToWordResult() {
        _pdfToWordResultFile.value = null
        _pdfToWordProgress.value = ""
    }

    fun convertPdfToWord(context: Context, doc: ScannedDocument, onFinished: (File?, String?) -> Unit) {
        convertLocalPdfToWord(context, File(doc.filePath), doc.name, onFinished)
    }

    fun convertLocalPdfToWord(context: Context, pdfFile: File, displayName: String, onFinished: (File?, String?) -> Unit) {
        val TAG = "PdfToWordConversion"
        _isPdfToWordRunning.value = true
        _pdfToWordResultFile.value = null
        _pdfToWordProgress.value = "Khởi tạo tiến trình chuyển đổi tài liệu..."
        
        android.util.Log.i(TAG, "Bắt đầu chuyển đổi PDF sang Word. File: $displayName, Đường dẫn: ${pdfFile.absolutePath}, Kích thước: ${pdfFile.length()} bytes")
        
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _isPdfToWordRunning.value = false
                    android.util.Log.e(TAG, "Lỗi: Khóa API Gemini chưa được thiết lập!")
                    onFinished(null, "Lỗi: Khóa API Gemini chưa được thiết lập trong phần cài đặt của ứng dụng.")
                    return@launch
                }

                val srcFile = pdfFile
                if (!srcFile.exists() || srcFile.length() == 0L) {
                    _isPdfToWordRunning.value = false
                    android.util.Log.e(TAG, "Lỗi: File PDF gốc không tồn tại hoặc rỗng! Đường dẫn: ${srcFile.absolutePath}")
                    onFinished(null, "Lỗi: File PDF gốc không hợp lệ hoặc rỗng.")
                    return@launch
                }

                android.util.Log.d(TAG, "Mở ParcelFileDescriptor cho tệp PDF gốc...")
                val pfd = try {
                    android.os.ParcelFileDescriptor.open(srcFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                } catch (e: Exception) {
                    _isPdfToWordRunning.value = false
                    android.util.Log.e(TAG, "Không thể mở ParcelFileDescriptor của tệp PDF", e)
                    onFinished(null, "Không thể đọc tệp PDF: ${e.localizedMessage}")
                    return@launch
                }

                var totalPages = 0
                val pageHtmls = mutableListOf<String>()

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                var hasError = false
                var errorString: String? = null

                withContext(Dispatchers.IO) {
                    try {
                        android.util.Log.d(TAG, "Đang khởi tạo PdfRenderer để phân tích cấu trúc trang...")
                        val renderer = try {
                            PdfRenderer(pfd)
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Lỗi nghiêm trọng khi khởi tạo PdfRenderer. File có thể bị hỏng hoặc có mật khẩu bảo vệ.", e)
                            throw Exception("Tệp PDF có thể bị hỏng, bị mã hóa/bảo vệ bằng mật khẩu hoặc có định dạng không được hỗ trợ. (Chi tiết: ${e.localizedMessage})")
                        }

                        totalPages = renderer.pageCount
                        android.util.Log.i(TAG, "PdfRenderer khởi tạo thành công. Tổng số trang: $totalPages")
                        var activeModel = "gemini-3.5-flash"
                        val modelsToTry = listOf("gemini-3.5-flash", "gemini-2.5-flash", "gemini-1.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview")
                        
                        try {
                            for (i in 0 until totalPages) {
                                _pdfToWordProgress.value = "Đang xử lý phân tích AI nâng cao trang ${i + 1}/$totalPages..."
                                android.util.Log.d(TAG, "Đang mở và kết xuất trang ${i + 1}/$totalPages...")
                                
                                val page = try {
                                    renderer.openPage(i)
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Lỗi không thể mở trang ${i + 1}", e)
                                    throw Exception("Không thể mở trang ${i + 1} của tài liệu PDF. Trang có thể bị hỏng.")
                                }
                                
                                val base64Image = try {
                                    val maxDim = 1200
                                    val scale = (maxDim.toFloat() / maxOf(page.width, page.height)).coerceAtMost(1.0f)
                                    val width = (page.width * scale).toInt().coerceAtLeast(1)
                                    val height = (page.height * scale).toInt().coerceAtLeast(1)
                                    android.util.Log.d(TAG, "Trang ${i + 1} kích thước gốc: ${page.width}x${page.height}, kích thước kết xuất: ${width}x${height} (tỷ lệ: $scale)")
                                    
                                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    bitmap.eraseColor(android.graphics.Color.WHITE)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    
                                    val outputStream = java.io.ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                                    val b64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
                                    bitmap.recycle()
                                    android.util.Log.d(TAG, "Kết xuất trang ${i + 1} thành công. Kích thước chuỗi Base64: ${b64.length} ký tự.")
                                    b64
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Lỗi khi kết xuất trang ${i + 1} thành ảnh", e)
                                    throw Exception("Lỗi khi kết xuất trang ${i + 1} của tài liệu PDF thành hình ảnh: ${e.localizedMessage}")
                                } finally {
                                    page.close()
                                }

                                // High fidelity prompt for teachers (education layout, math formula representation, questions, worksheets)
                                val prompt = "Bạn là một trợ lý AI chuyên nghiệp cho ngành giáo dục. Nhiệm vụ của bạn là số hóa trang tài liệu học tập này (đề thi, bài kiểm tra, giáo án, bài tập, bảng biểu...) và chuyển thành mã HTML chất lượng cao để giáo viên tải về làm file Microsoft Word chỉnh sửa được.\n" +
                                        "Hãy trích xuất chính xác 100% nội dung chữ tiếng Việt, công thức, bảng biểu, danh sách lựa chọn trắc nghiệm A, B, C, D.\n" +
                                        "YÊU CẦU ĐỊNH DẠNG ĐẦU RA:\n" +
                                        "1. Chỉ trả về mã HTML sạch sẽ nằm bên trong thẻ body (ví dụ: các thẻ h1, h2, p, ul, ol, li, table, tr, td, th, b, i, strong, em). KHÔNG bọc toàn bộ trong thẻ <html>, <head>, <body> hay ```html. Chỉ xuất ra nội dung trực tiếp.\n" +
                                        "2. Giữ nguyên định dạng cấu trúc gốc một cách chính xác nhất:\n" +
                                        "   - Các tiêu đề lớn viết chữ đậm, căn lề giữa hoặc phóng to thích hợp (sử dụng h1, h2, h3).\n" +
                                        "   - Các đoạn văn bản nằm trong thẻ <p>.\n" +
                                        "   - Các câu hỏi trắc nghiệm hoặc bài tập phải được trình bày rõ ràng, sạch đẹp.\n" +
                                        "   - Bảng biểu được định dạng bằng thẻ <table> với viền rõ ràng (border=\"1\" style=\"border-collapse: collapse; width: 100%;\"), có các tiêu đề bảng <th> và dữ liệu <td> tương ứng.\n" +
                                        "   - Các danh sách câu hỏi dạng gạch đầu dòng hoặc số thứ tự phải được chuyển thành <ul>/<li> hoặc <ol>/<li>.\n" +
                                        "3. ĐẶC BIỆT XỬ LÝ CÁC BỐ CỤC PHỨC TẠP:\n" +
                                        "   - Bố cục nhiều cột (Multi-column): Nếu trang tài liệu chia thành 2 hoặc nhiều cột song song (như đề kiểm tra thường chia đôi trang), hãy đọc một cách tự nhiên theo thứ tự logic đúng của tài liệu (ví dụ đọc hết cột bên trái rồi sang cột bên phải, hoặc sắp xếp lại thành một luồng đọc từ trên xuống dưới mượt mà) để tránh văn bản từ các cột bị trộn lẫn xen kẽ.\n" +
                                        "   - Công thức Toán/Lý/Hóa: Sử dụng các biểu diễn ký hiệu trực quan rõ ràng nhất có thể (ví dụ: dùng x² thay vì x^2 nếu được, dùng căn bậc hai bằng kí hiệu √, hoặc các chỉ số trên/dưới rõ ràng <sub>/<sup>) để khi giáo viên mở bằng Word sẽ hiển thị chính xác nhất.\n" +
                                        "   - Phần tiêu đề đầu trang (Header/Thông tin lớp học): Nếu có khung thông tin như Họ tên, Lớp, Trường, Số báo danh... ở đầu đề thi, hãy dùng bảng không viền hoặc định dạng thích hợp để giữ nguyên vị trí cân đối của các mục này.\n" +
                                        "4. Tuyệt đối không được tự ý tóm tắt, cắt xén, dịch sai hay bỏ sót chữ. Nhận diện chính xác 100% tiếng Việt từ hình ảnh."

                                val requestJson = org.json.JSONObject().apply {
                                    val contentsArray = org.json.JSONArray().apply {
                                        val contentObj = org.json.JSONObject().apply {
                                            val partsArray = org.json.JSONArray().apply {
                                                val textPart = org.json.JSONObject().put("text", prompt)
                                                val imagePart = org.json.JSONObject().put("inlineData", org.json.JSONObject().apply {
                                                    put("mimeType", "image/jpeg")
                                                    put("data", base64Image)
                                                })
                                                put(textPart)
                                                put(imagePart)
                                            }
                                            put("parts", partsArray)
                                        }
                                        put(contentObj)
                                    }
                                    put("contents", contentsArray)
                                }

                                val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
                                
                                var pageHtml = ""
                                var lastErrorMsg = ""
                                val currentModels = if (activeModel in modelsToTry) {
                                    listOf(activeModel) + (modelsToTry - activeModel)
                                } else {
                                    modelsToTry
                                }
                                
                                for (modelName in currentModels) {
                                    try {
                                        android.util.Log.d(TAG, "Gửi yêu cầu phân tích trang ${i + 1} tới model AI: $modelName...")
                                        val request = okhttp3.Request.Builder()
                                            .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                                            .post(requestBody)
                                            .build()

                                        val startTime = System.currentTimeMillis()
                                        pageHtml = client.newCall(request).execute().use { response ->
                                            val latency = System.currentTimeMillis() - startTime
                                            if (!response.isSuccessful) {
                                                val errorBody = response.body?.string() ?: ""
                                                val detailedMsg = try {
                                                    org.json.JSONObject(errorBody).getJSONObject("error").getString("message")
                                                } catch (e: Exception) {
                                                    errorBody
                                                }
                                                android.util.Log.w(TAG, "Yêu cầu trang ${i + 1} với model $modelName thất bại sau ${latency}ms (Mã lỗi: ${response.code}). Chi tiết: $detailedMsg")
                                                throw Exception("Mã lỗi ${response.code}: $detailedMsg")
                                            }
                                            val responseString = response.body?.string() ?: throw Exception("Không nhận được phản hồi từ AI")
                                            val responseJson = org.json.JSONObject(responseString)
                                            val candidates = responseJson.optJSONArray("candidates")
                                            if (candidates != null && candidates.length() > 0) {
                                                val firstCandidate = candidates.getJSONObject(0)
                                                val contentObj = firstCandidate.optJSONObject("content")
                                                val partsArray = contentObj?.optJSONArray("parts")
                                                if (partsArray != null && partsArray.length() > 0) {
                                                    var rawText = partsArray.getJSONObject(0).optString("text") ?: ""
                                                    rawText = rawText.replace("```html", "")
                                                    rawText = rawText.replace("```", "")
                                                    rawText.trim()
                                                } else {
                                                    ""
                                                }
                                            } else {
                                                ""
                                            }
                                        }
                                        android.util.Log.i(TAG, "Nhận kết quả trang ${i + 1} từ model $modelName thành công trong ${System.currentTimeMillis() - startTime}ms. Kích thước HTML nhận được: ${pageHtml.length} ký tự.")
                                        activeModel = modelName
                                        lastErrorMsg = ""
                                        break
                                    } catch (e: Exception) {
                                        lastErrorMsg = e.message ?: "Lỗi chưa rõ"
                                        android.util.Log.w(TAG, "Lỗi khi chạy model $modelName ở trang ${i + 1}: $lastErrorMsg. Đang thử model tiếp theo...")
                                    }
                                }
                                
                                if (pageHtml.isEmpty() && lastErrorMsg.isNotEmpty()) {
                                    android.util.Log.e(TAG, "Tất cả các model AI đều thất bại ở trang ${i + 1}!")
                                    throw Exception("Trang ${i + 1} thất bại: $lastErrorMsg")
                                }
                                
                                pageHtmls.add(pageHtml)
                            }
                        } finally {
                            renderer.close()
                        }
                    } catch (e: Exception) {
                        hasError = true
                        errorString = e.localizedMessage
                        android.util.Log.e(TAG, "Lỗi xảy ra trong vòng lặp chuyển đổi PDF sang Word", e)
                    } finally {
                        try {
                            pfd.close()
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Lỗi khi đóng ParcelFileDescriptor", e)
                        }
                    }
                }

                if (hasError) {
                    _isPdfToWordRunning.value = false
                    _pdfToWordProgress.value = ""
                    android.util.Log.e(TAG, "Quá trình chuyển đổi PDF sang Word thất bại! Lỗi: $errorString")
                    onFinished(null, "Lỗi chuyển đổi: $errorString")
                    return@launch
                }

                _pdfToWordProgress.value = "Đang tối ưu hóa định dạng Microsoft Word..."
                android.util.Log.i(TAG, "Tất cả $totalPages trang đã được phân tích thành công. Đang lắp ghép tài liệu Word cuối cùng...")

                val finalHtml = StringBuilder().apply {
                    append("<!DOCTYPE html>\n")
                    append("<html>\n")
                    append("<head>\n")
                    append("<meta charset=\"utf-8\">\n")
                    append("<title>${displayName.removeSuffix(".pdf")}</title>\n")
                    append("<style>\n")
                    append("  body {\n")
                    append("    font-family: 'Times New Roman', Times, serif, 'Calibri', Arial;\n")
                    append("    line-height: 1.6;\n")
                    append("    margin: 1.0in 1.0in 1.0in 1.0in;\n")
                    append("    color: #000000;\n")
                    append("  }\n")
                    append("  h1, h2, h3, h4 {\n")
                    append("    color: #1A365D;\n")
                    append("    font-family: 'Calibri', Arial, sans-serif;\n")
                    append("    margin-top: 16pt;\n")
                    append("    margin-bottom: 6pt;\n")
                    append("    font-weight: bold;\n")
                    append("  }\n")
                    append("  h1 {\n")
                    append("    font-size: 18pt;\n")
                    append("    text-align: center;\n")
                    append("    margin-bottom: 12pt;\n")
                    append("  }\n")
                    append("  h2 {\n")
                    append("    font-size: 14pt;\n")
                    append("  }\n")
                    append("  p {\n")
                    append("    margin-top: 0;\n")
                    append("    margin-bottom: 8pt;\n")
                    append("    font-size: 12pt;\n")
                    append("    text-align: justify;\n")
                    append("  }\n")
                    append("  table {\n")
                    append("    border-collapse: collapse;\n")
                    append("    width: 100%;\n")
                    append("    margin-top: 10pt;\n")
                    append("    margin-bottom: 10pt;\n")
                    append("    font-size: 11pt;\n")
                    append("  }\n")
                    append("  table, th, td {\n")
                    append("    border: 1px solid #000000;\n")
                    append("  }\n")
                    append("  th {\n")
                    append("    background-color: #F2F2F2;\n")
                    append("    padding: 6px;\n")
                    append("    font-weight: bold;\n")
                    append("    text-align: center;\n")
                    append("  }\n")
                    append("  td {\n")
                    append("    padding: 6px;\n")
                    append("  }\n")
                    append("  ul, ol {\n")
                    append("    margin-top: 0;\n")
                    append("    margin-bottom: 8pt;\n")
                    append("    padding-left: 20pt;\n")
                    append("  }\n")
                    append("  li {\n")
                    append("    font-size: 12pt;\n")
                    append("    margin-bottom: 3pt;\n")
                    append("  }\n")
                    append("  .header-table {\n")
                    append("    border: none !important;\n")
                    append("    margin-bottom: 18pt;\n")
                    append("  }\n")
                    append("  .header-table td {\n")
                    append("    border: none !important;\n")
                    append("    padding: 2px;\n")
                    append("    font-size: 11pt;\n")
                    append("  }\n")
                    append("  .banner-box {\n")
                    append("    border: 1px dashed #7F8C8D;\n")
                    append("    background-color: #F9FAFC;\n")
                    append("    padding: 10px;\n")
                    append("    margin-bottom: 15pt;\n")
                    append("    font-size: 10pt;\n")
                    append("    color: #555555;\n")
                    append("    text-align: center;\n")
                    append("  }\n")
                    append("</style>\n")
                    append("</head>\n")
                    append("<body>\n")
                    
                    append("  <div class=\"banner-box\">\n")
                    append("    <strong>TÀI LIỆU SỐ HÓA BỞI SCANPDF (PHIÊN BẢN CHO GIÁO VIÊN)</strong><br>\n")
                    append("    Giáo viên có thể sửa đổi nội dung, căn chỉnh cột, thêm công thức, câu hỏi tùy ý trong MS Word hoặc Google Docs.\n")
                    append("  </div>\n")

                    for (idx in pageHtmls.indices) {
                        if (idx > 0) {
                            append("  <div style=\"page-break-before: always;\"></div>\n")
                        }
                        append("  <!-- Trang ${idx + 1} -->\n")
                        append(pageHtmls[idx])
                        append("\n")
                    }

                    append("</body>\n")
                    append("</html>\n")
                }.toString()

                val wordFile = withContext(Dispatchers.IO) {
                    val docDir = File(context.filesDir, "exported_word")
                    if (!docDir.exists()) docDir.mkdirs()

                    val baseName = displayName.removeSuffix(".pdf")
                    val file = File(docDir, "${baseName}_Word.doc")
                    FileOutputStream(file).use { out ->
                        out.write(finalHtml.toByteArray(Charsets.UTF_8))
                    }
                    file
                }

                _pdfToWordResultFile.value = wordFile
                _isPdfToWordRunning.value = false
                _pdfToWordProgress.value = ""
                android.util.Log.i(TAG, "Đã hoàn thành tạo tệp Word thành công! Đường dẫn tệp: ${wordFile.absolutePath}, Kích thước: ${wordFile.length()} bytes")
                onFinished(wordFile, null)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Lỗi nghiêm trọng không mong muốn xảy ra trong quá trình xuất tệp Word", e)
                _isPdfToWordRunning.value = false
                _pdfToWordProgress.value = ""
                onFinished(null, "Không thể xuất file Word: ${e.localizedMessage}")
            }
        }
    }
}
