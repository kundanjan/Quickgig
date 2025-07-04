package com.example.quickgigapp.models

data class FreelancerProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val skills: List<String>? = null,
    val createdAt: Long = 0L
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", null, 0L)
}