package com.tys.gymapp.presentation.screens.checkin

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.tys.gymapp.data.remote.dto.QrPayload
import com.tys.gymapp.data.repository.CheckinRepository
import com.tys.gymapp.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckinViewModel @Inject constructor(
    private val checkinRepository: CheckinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckinUiState>(CheckinUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap = _qrBitmap.asStateFlow()

    init {
        generateQr()
    }

    fun generateQr() {
        viewModelScope.launch {
            _uiState.value = CheckinUiState.Loading

            when (val result = checkinRepository.generateQr()) {
                is Resource.Success -> {
                    result.data?.let { qrPayload ->
                        val qrContent = createQrContent(qrPayload)
                        _qrBitmap.value = generateQrBitmap(qrContent)
                        _uiState.value = CheckinUiState.Success(qrPayload)

                        // Auto refresh after TTL
                        val ttl = (qrPayload.exp - System.currentTimeMillis() / 1000).toLong()
                        if (ttl > 0) {
                            delay(ttl * 1000)
                            generateQr()
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = CheckinUiState.Error(result.message ?: "Lỗi tạo QR")
                }
                else -> {}
            }
        }
    }

    private fun createQrContent(qr: QrPayload): String {
        return "${qr.userId}|${qr.nonce}|${qr.exp}|${qr.sig}"
    }

    private fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        return bitmap
    }
}

sealed class CheckinUiState {
    object Loading : CheckinUiState()
    data class Success(val qrPayload: QrPayload) : CheckinUiState()
    data class Error(val message: String) : CheckinUiState()
}