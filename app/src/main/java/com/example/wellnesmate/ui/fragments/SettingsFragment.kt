package com.example.wellnesmate.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wellnesmate.MainActivity
import com.example.wellnesmate.R
import com.example.wellnesmate.data.repository.SharedPreferencesManager
import com.example.wellnesmate.ui.auth.LoginActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var cardLogout: MaterialCardView
    private lateinit var switchNotifications: SwitchMaterial
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // (activity as? MainActivity)?.updateToolbarTitle(getString(R.string.settings_title))
        initializeViews(view)
        setupClickListeners()
        loadSettings()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        cardLogout = view.findViewById(R.id.card_logout)
        switchNotifications = view.findViewById(R.id.switch_notifications)
    }
    
    private fun setupClickListeners() {
        
        cardLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        
        
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationSettings(isChecked)
        }
    }
    
    private fun loadSettings() {
        val hydrationSettings = prefsManager.getHydrationSettings()
        switchNotifications.isChecked = hydrationSettings.reminderEnabled
    }
    
    private fun updateNotificationSettings(enabled: Boolean) {
        val currentSettings = prefsManager.getHydrationSettings()
        val newSettings = currentSettings.copy(reminderEnabled = enabled)
        prefsManager.saveHydrationSettings(newSettings)
        
        if (enabled) {
            android.widget.Toast.makeText(
                requireContext(),
                "Notifications enabled",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            androidx.work.WorkManager.getInstance(requireContext())
                .cancelUniqueWork("hydration_reminder")
            android.widget.Toast.makeText(
                requireContext(),
                "Notifications disabled",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.about))
            .setMessage(getString(R.string.about_description))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun logout() {
        prefsManager.setUserLoggedIn(false)
        
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
    
}