package com.example.habify.ui.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habify.MainActivity
import com.example.habify.R
import com.example.habify.data.models.MoodEntry
import com.example.habify.data.models.MoodType
import com.example.habify.data.repository.SharedPreferencesManager
import com.example.habify.ui.adapters.MoodSelectorAdapter
import com.example.habify.ui.adapters.MoodHistoryAdapter
 
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import android.util.TypedValue
import kotlin.math.roundToInt
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Fragment for mood journaling with emoji selector
 */
class MoodFragment : Fragment() {
    
    private lateinit var layoutMoodCalendar: View
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var moodSelectorAdapter: MoodSelectorAdapter
 
    
    private var selectedMood: MoodType? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }
    
    private fun showAddMoodDialog() {
        val dialogContent = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_mood_inline, null)

        // Bind dialog recycler to same adapter configuration (5 columns, grouped order)
        val dialogRecycler = dialogContent.findViewById<RecyclerView>(R.id.recycler_mood_selector_dialog)
        dialogRecycler.layoutManager = GridLayoutManager(context, 4)
        val dialogAdapter = MoodSelectorAdapter { mood ->
            selectedMood = mood
        }
        dialogRecycler.adapter = dialogAdapter
        dialogAdapter.updateMoods(MoodType.getAllMoods())
        dialogRecycler.setHasFixedSize(true)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogContent)
            .setPositiveButton(getString(R.string.save)) { d, _ ->
                if (selectedMood != null) {
                    saveMoodEntry()
                }
                d.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupClickListeners()
        loadMoodHistory()

        // Keep only calendar view visible
        layoutMoodCalendar.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        loadMoodHistory()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        layoutMoodCalendar = view.findViewById(R.id.layout_mood_calendar)
    }
    
    
    private fun setupMoodHistory() {
        // History list removed; nothing to initialize.
    }
    
    // Removed tabs: calendar is the only view
    
    private fun setupClickListeners() {
        // Add Mood floating button opens dialog with emoji grid
        view?.findViewById<View>(R.id.fab_add_mood)?.setOnClickListener {
            showAddMoodDialog()
        }
    }
    
    
    private fun saveMoodEntry() {
        val mood = selectedMood ?: return
        
        val moodEntry = MoodEntry(
            mood = mood,
            emoji = mood.emoji,
            notes = "", // Removed notes field
            timestamp = Date()
        )
        
        prefsManager.saveMoodEntry(moodEntry)
        
        // Reset selection
        selectedMood = null
        
        // Refresh history
        loadMoodHistory()
        
        // Show success message
        android.widget.Toast.makeText(
            requireContext(),
            "Mood saved successfully!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun loadMoodHistory() {
        val moodEntries = prefsManager.getMoodEntries()


        // Always update calendar view only
        generateCalendarView()
    }
    
    private fun deleteMoodEntry(entry: MoodEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                prefsManager.deleteMoodEntry(entry.id)
                loadMoodHistory()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun shareMoodEntry(entry: MoodEntry) {
        val shareText = "My mood: ${entry.mood.label} ${entry.emoji}"
        val formattedText = getString(R.string.share_mood_summary, shareText)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formattedText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun shareTodaysMood() {
        val todayEntries = prefsManager.getTodayMoodEntries()
        if (todayEntries.isEmpty()) return
        
        val moodSummary = if (todayEntries.size == 1) {
            "${todayEntries.first().mood.label} ${todayEntries.first().emoji}"
        } else {
            val moods = todayEntries.joinToString(", ") { "${it.mood.label} ${it.emoji}" }
            "Multiple moods today: $moods"
        }
        
        val shareText = getString(R.string.share_mood_summary, moodSummary)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    // List/Calendar toggle removed – calendar is the sole view
    
    private fun generateCalendarView() {
        // Clear existing calendar content
        if (layoutMoodCalendar is ViewGroup) {
            (layoutMoodCalendar as ViewGroup).removeAllViews()
        }
        
        // Get mood entries grouped by date
        val moodEntries = prefsManager.getMoodEntries()
        if (moodEntries.isEmpty()) {
            // Show empty state for calendar view
            val emptyView = TextView(requireContext()).apply {
                text = getString(R.string.no_mood_entries_calendar)
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                setPadding(32, 64, 32, 64)
            }
            if (layoutMoodCalendar is ViewGroup) {
                (layoutMoodCalendar as ViewGroup).addView(emptyView)
            }
            return
        }
        
        // Group entries by date
        val entriesByDate = moodEntries.groupBy { it.date }
        
        // Sort dates in descending order (newest first)
        val sortedDates = entriesByDate.keys.sortedDescending()
        
        // Create calendar layout
        val calendarLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Title removed at user's request; keep explanation below
        val explanation = TextView(requireContext()).apply {
            text = getString(R.string.mood_calendar_explanation)
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, 0, 0, 16)
            gravity = android.view.Gravity.CENTER
        }
        calendarLayout.addView(explanation)
        
        // Display entries grouped by date
        sortedDates.forEachIndexed { index, date ->
            val entries = entriesByDate[date] ?: emptyList()
            
            // Create a card for each date
            val dateCard = com.google.android.material.card.MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                setCardBackgroundColor(requireContext().getColor(R.color.card_background))
                radius = 12f
                cardElevation = 4f
            }
            
            val dateCardContent = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }
            
            // Date header with better styling
            val dateHeader = TextView(requireContext()).apply {
                text = formatDateHeader(date)
                textSize = 18f
                setTextColor(requireContext().getColor(R.color.primary))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, 16)
            }
            dateCardContent.addView(dateHeader)
            
            // Mood entries for this date
            entries.forEachIndexed { entryIndex, entry ->
                val entryView = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                }
                
                // Emoji with larger size
                val emojiView = TextView(requireContext()).apply {
                    text = entry.emoji
                    textSize = 24f
                    setPadding(0, 0, 16, 0)
                    minWidth = 48
                    gravity = android.view.Gravity.CENTER
                }
                entryView.addView(emojiView)
                
                // Mood label and time
                val infoLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                }
                
                val moodLabel = TextView(requireContext()).apply {
                    text = entry.mood.label
                    textSize = 16f
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    setTypeface(null, Typeface.BOLD)
                }
                infoLayout.addView(moodLabel)
                
                val timeText = TextView(requireContext()).apply {
                    text = formatTime(entry.timestamp)
                    textSize = 14f
                    setTextColor(requireContext().getColor(R.color.text_secondary))
                    setPadding(0, 4, 0, 0)
                }
                infoLayout.addView(timeText)
                
                entryView.addView(infoLayout)

                // Spacer to push action buttons to the end
                val spacer = View(requireContext())
                spacer.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                entryView.addView(spacer)

                // Resolve borderless ripple background
                val outValue = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                val rippleResId = outValue.resourceId

                val size = (32 * resources.displayMetrics.density).roundToInt()
                val pad = (4 * resources.displayMetrics.density).roundToInt()
                val marginSmall = (4 * resources.displayMetrics.density).roundToInt()

                // Share button (match history item style)
                val shareButton = android.widget.ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.ic_share)
                    contentDescription = getString(R.string.share)
                    setBackgroundResource(rippleResId)
                    setPadding(pad, pad, pad, pad)
                    setColorFilter(requireContext().getColor(R.color.text_secondary))
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(0, 0, marginSmall, 0)
                    }
                    setOnClickListener { shareMoodEntry(entry) }
                }
                entryView.addView(shareButton)

                // Delete button (match history item style)
                val deleteButton = android.widget.ImageButton(requireContext()).apply {
                    setImageResource(R.drawable.ic_delete)
                    contentDescription = getString(R.string.delete)
                    setBackgroundResource(rippleResId)
                    setPadding(pad, pad, pad, pad)
                    setColorFilter(requireContext().getColor(R.color.error_red))
                    layoutParams = LinearLayout.LayoutParams(size, size)
                    setOnClickListener { deleteMoodEntry(entry) }
                }
                entryView.addView(deleteButton)
                dateCardContent.addView(entryView)
                
                // Add separator between entries (except for the last one)
                if (entryIndex < entries.size - 1) {
                    val divider = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                        setBackgroundColor(requireContext().getColor(R.color.divider_color))
                    }
                    dateCardContent.addView(divider)
                }
            }
            
            dateCard.addView(dateCardContent)
            calendarLayout.addView(dateCard)
        }
        
        // Add to calendar layout
        if (layoutMoodCalendar is ViewGroup) {
            (layoutMoodCalendar as ViewGroup).addView(calendarLayout)
        }
    }
    
    private fun formatDateHeader(dateString: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: return dateString
            val today = Date()
            val todayString = dateFormat.format(today)
            
            val displayFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val formattedDate = displayFormat.format(date)
            
            // Add "Today" indicator if this is today's date
            if (dateString == todayString) {
                "$formattedDate (Today)"
            } else {
                formattedDate
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(date)
    }
}