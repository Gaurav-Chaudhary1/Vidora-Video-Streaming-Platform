package com.vidora.app.data.repository.file

import com.vidora.app.data.local.datastore.UserPreferences
import com.vidora.app.data.remote.file.FileApi
import javax.inject.Inject

class FileRepository @Inject constructor(
    private val api: FileApi,
    private val prefs: UserPreferences
) {

    suspend fun fetchSignedUrl(rawUrl: String): Result<String> {
        val token = prefs.getToken().orEmpty()
        return kotlin.runCatching {
            val resp = api.getSignedUrl("Bearer $token", rawUrl)
            if (!resp.isSuccessful) throw Exception(resp.message())
            resp.body()!!.signedUrl
        }
    }
}