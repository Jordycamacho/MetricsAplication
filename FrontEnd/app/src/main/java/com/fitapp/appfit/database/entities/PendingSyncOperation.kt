package com.fitapp.appfit.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_operations")
data class PendingSyncOperation(
    @PrimaryKey(autoGenerate = true)
    val operationId: Long = 0,
    val entityType: String,        // "ROUTINE", "ROUTINE_EXERCISE", "SET_TEMPLATE"
    val entityId: Long,
    val operation: String,         // "CREATE", "UPDATE", "DELETE"
    val payload: String,           // JSON del body del request
    val endpoint: String,          // e.g. "/api/routines/123"
    val httpMethod: String,        // "POST", "PUT", "DELETE"
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null
)