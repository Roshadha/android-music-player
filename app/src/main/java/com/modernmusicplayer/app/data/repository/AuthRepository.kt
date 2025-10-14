package com.modernmusicplayer.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.modernmusicplayer.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest

class AuthRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    suspend fun signUp(email: String, password: String, displayName: String): Flow<Result<User>> = flow {
        try {
            // Validate inputs
            if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
                emit(Result.failure(Exception("All fields are required")))
                return@flow
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emit(Result.failure(Exception("Invalid email address")))
                return@flow
            }
            
            if (password.length < 6) {
                emit(Result.failure(Exception("Password must be at least 6 characters")))
                return@flow
            }
            
            // Check if user already exists
            if (prefs.contains("email")) {
                emit(Result.failure(Exception("User already exists. Please sign in.")))
                return@flow
            }
            
            // Hash password
            val passwordHash = hashPassword(password)
            
            // Save user data
            prefs.edit().apply {
                putString("email", email)
                putString("password_hash", passwordHash)
                putString("display_name", displayName)
                putString("user_id", email) // Use email as ID for simplicity
                putBoolean("is_logged_in", true)
                apply()
            }
            
            // Create user object
            val user = User(
                uid = email,
                email = email,
                displayName = displayName
            )
            
            emit(Result.success(user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun signIn(email: String, password: String): Flow<Result<User>> = flow {
        try {
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                emit(Result.failure(Exception("Email and password are required")))
                return@flow
            }
            
            // Get stored credentials
            val storedEmail = prefs.getString("email", null)
            val storedPasswordHash = prefs.getString("password_hash", null)
            val storedDisplayName = prefs.getString("display_name", "User")
            
            if (storedEmail == null || storedPasswordHash == null) {
                emit(Result.failure(Exception("No account found. Please sign up.")))
                return@flow
            }
            
            // Verify credentials
            if (email != storedEmail) {
                emit(Result.failure(Exception("Invalid email or password")))
                return@flow
            }
            
            val passwordHash = hashPassword(password)
            if (passwordHash != storedPasswordHash) {
                emit(Result.failure(Exception("Invalid email or password")))
                return@flow
            }
            
            // Mark as logged in
            prefs.edit().putBoolean("is_logged_in", true).apply()
            
            // Create user object
            val user = User(
                uid = email,
                email = email,
                displayName = storedDisplayName ?: "User"
            )
            
            emit(Result.success(user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    fun signOut() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }
    
    fun getCurrentUserId(): String? {
        return if (isUserLoggedIn()) {
            prefs.getString("user_id", null)
        } else {
            null
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }
    
    suspend fun getUserProfile(): Flow<User?> = flow {
        try {
            if (!isUserLoggedIn()) {
                emit(null)
                return@flow
            }
            
            val email = prefs.getString("email", null)
            val displayName = prefs.getString("display_name", "User")
            
            if (email != null) {
                val user = User(
                    uid = email,
                    email = email,
                    displayName = displayName ?: "User"
                )
                emit(user)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    suspend fun updateUserProfile(displayName: String?, photoUrl: String?): Flow<Result<User>> = flow {
        try {
            if (!isUserLoggedIn()) {
                emit(Result.failure(Exception("User not logged in")))
                return@flow
            }
            
            // Update display name if provided
            displayName?.let {
                prefs.edit().putString("display_name", it).apply()
            }
            
            // Get current user data
            val email = prefs.getString("email", null)
            val currentDisplayName = displayName ?: prefs.getString("display_name", "User")
            
            if (email != null) {
                val user = User(
                    uid = email,
                    email = email,
                    displayName = currentDisplayName ?: "User"
                )
                emit(Result.success(user))
            } else {
                emit(Result.failure(Exception("User data not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
