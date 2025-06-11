package com.danira0037.assesment3.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canhub.cropper.CropImage.CancelledResult.bitmap
import com.danira0037.assesment3.model.Buku
import com.danira0037.assesment3.network.ApiStatus
import com.danira0037.assesment3.network.BukuApi
import com.danira0037.assesment3.network.ImgbbApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.ByteArrayOutputStream

class MainViewModel: ViewModel() {

    var data = mutableStateOf(emptyList<Buku>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = MutableStateFlow<String?>(null)
        private set

    var isUploading = mutableStateOf(false)

    init {
        retrieveData()
    }

    fun retrieveData() {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = BukuApi.service.getBuku()
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun uploadAndPostPakaian(
        bitmap: Bitmap,
        namaBuku: String,
        author: String,
        email: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isUploading.value = true
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val requestBody = stream.toByteArray()
                    .toRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.imgbb.com/1/")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()

                val service = retrofit.create(ImgbbApiService::class.java)
                val response = service.uploadImage("1c45d85d0ad0778978465455c2b5eebc", part)
                val imageUrl = response.data.url

                val newData = Buku(
                    namaBuku = namaBuku,
                    author = author,
                    gambar = imageUrl,
                    mine = true,
                    owner = email
                )
                BukuApi.service.addBuku(newData)

                retrieveData()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                isUploading.value = false
                Log.e("UPLOAD_POST", "Error: ${e.message}")
                errorMessage.value = e.message
            } finally {
                isUploading.value = false
            }
        }
    }

    fun updateBuku(
        updated: Buku,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isUploading.value = true
                BukuApi.service.updateBuku(updated.id, updated)
                retrieveData()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                isUploading.value = false
                Log.e("EDIT_BUKU", "Error update: ${e.message}")
                errorMessage.value = "Gagal update: ${e.message}"
            } finally {
              isUploading.value = false
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun deleteBuku(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                BukuApi.service.deleteBuku(id)
                retrieveData()
            } catch (e: Exception) {
                Log.e("DELETE_POST", "Error: ${e.message}")
                errorMessage.value = e.message
            }
        }
    }

    fun uploadImageToImgBB(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val requestBody = stream.toByteArray()
                    .toRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.imgbb.com/1/")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()

                val service = retrofit.create(ImgbbApiService::class.java)
                val response = service.uploadImage("1c45d85d0ad0778978465455c2b5eebc", part)
                onSuccess(response.data.url)
            } catch (e: Exception) {
                errorMessage.value = "Gagal upload gambar: ${e.message}"
            }
        }
    }
}