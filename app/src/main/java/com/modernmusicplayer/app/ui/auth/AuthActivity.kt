package com.modernmusicplayer.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.modernmusicplayer.app.R
import com.modernmusicplayer.app.data.repository.AuthRepository
import com.modernmusicplayer.app.databinding.ActivityAuthBinding
import com.modernmusicplayer.app.ui.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var authRepository: AuthRepository
    private var isSignUpMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        authRepository = AuthRepository(this)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.authButton.setOnClickListener {
            if (isSignUpMode) {
                signUp()
            } else {
                signIn()
            }
        }
        
        binding.toggleButton.setOnClickListener {
            toggleMode()
        }
    }
    
    private fun signIn() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        
        if (!validateInputs(email, password)) return
        
        showLoading(true)
        
        lifecycleScope.launch {
            authRepository.signIn(email, password).collect { result ->
                showLoading(false)
                
                result.onSuccess {
                    navigateToMain()
                }.onFailure { error ->
                    showError(error.message ?: "Sign in failed")
                }
            }
        }
    }
    
    private fun signUp() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val displayName = binding.displayNameInput.text.toString().trim()
        
        if (!validateInputs(email, password, displayName)) return
        
        showLoading(true)
        
        lifecycleScope.launch {
            authRepository.signUp(email, password, displayName).collect { result ->
                showLoading(false)
                
                result.onSuccess {
                    navigateToMain()
                }.onFailure { error ->
                    showError(error.message ?: "Sign up failed")
                }
            }
        }
    }
    
    private fun validateInputs(email: String, password: String, displayName: String? = null): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }
        
        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }
        
        if (isSignUpMode) {
            if (displayName.isNullOrEmpty()) {
                binding.displayNameLayout.error = "Display name is required"
                isValid = false
            } else {
                binding.displayNameLayout.error = null
            }
        }
        
        return isValid
    }
    
    private fun toggleMode() {
        isSignUpMode = !isSignUpMode
        
        if (isSignUpMode) {
            binding.authTitle.text = getString(R.string.sign_up)
            binding.authButton.text = getString(R.string.create_account)
            binding.displayNameLayout.visibility = View.VISIBLE
            binding.toggleText.text = getString(R.string.have_account)
            binding.toggleButton.text = getString(R.string.sign_in)
        } else {
            binding.authTitle.text = getString(R.string.sign_in)
            binding.authButton.text = getString(R.string.sign_in)
            binding.displayNameLayout.visibility = View.GONE
            binding.toggleText.text = getString(R.string.no_account)
            binding.toggleButton.text = getString(R.string.sign_up)
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.authButton.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
