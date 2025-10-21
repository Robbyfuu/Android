package com.example.miappmodular.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID


@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Date,
    val lastLogin : Date? = null,
    val profileImagePath: String? = null
)
