package com.vidora.app.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/** for text/plain parts */
fun String.toRequestBody(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())

/** for arbitrary media types (e.g. JSON) */
fun String.toRequestBody(mediaType: MediaType?): RequestBody =
    toRequestBody(mediaType)
