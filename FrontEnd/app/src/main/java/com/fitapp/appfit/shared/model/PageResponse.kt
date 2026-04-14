package com.fitapp.appfit.shared.model

import com.google.gson.annotations.SerializedName

data class PageResponse<T>(
    @SerializedName("content")
    val content: List<T>,

    @SerializedName("number")
    val pageNumber: Int,

    @SerializedName("size")
    val pageSize: Int,

    @SerializedName("totalelements")
    val totalElements: Long,

    @SerializedName("totalpages")
    val totalPages: Int,

    @SerializedName("first")
    val first: Boolean,

    @SerializedName("last")
    val last: Boolean,

    @SerializedName("numberofelements")
    val numberOfElements: Int,

    @SerializedName("sort")
    val sort: SortInfo? = null,

    @SerializedName("pageable")
    val pageable: PageableInfo? = null
) {
    data class SortInfo(
        @SerializedName("empty")
        val empty: Boolean,

        @SerializedName("sorted")
        val sorted: Boolean,

        @SerializedName("unsorted")
        val unsorted: Boolean
    )

    data class PageableInfo(
        @SerializedName("pagenumber")
        val pageNumber: Int,

        @SerializedName("pagesize")
        val pageSize: Int,

        @SerializedName("offset")
        val offset: Int,

        @SerializedName("paged")
        val paged: Boolean,

        @SerializedName("unpaged")
        val unpaged: Boolean,

        @SerializedName("sort")
        val sort: SortInfo? = null
    )
}