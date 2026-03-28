package com.example.ophim.model

data class FilterResponse(
    val status: String,
    val data: FilterListData
)

data class FilterListData(
    val items: List<FilterItem>
)

data class FilterItem(
    val _id: String,
    val name: String,
    val slug: String
)
