package net.freshplatform

import kotlinx.serialization.Serializable

@Serializable
data class DownloadUrl(
    val url: String,
    val sha256: String
)
