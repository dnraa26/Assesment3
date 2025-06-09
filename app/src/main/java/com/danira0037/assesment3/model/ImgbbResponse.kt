package com.danira0037.assesment3.model

data class ImgbbResponse(
    val data: ImgbbData,
    val success: Boolean,
    val status: Int
)

data class ImgbbData(
    val url: String
)
