package org.wikipedia.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wikipedia.settings.Prefs

class OnboardingViewModel : ViewModel() {
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState = _onboardingState.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    fun updatePage(position: Int) {
        _currentPage.value = position
        _onboardingState.value = _onboardingState.value.copy(
            currentPage = position,
            isLastPage = position == OnboardingPage.values().size - 1
        )
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            Prefs.isOnboardingEnabled = false
            _onboardingState.value = _onboardingState.value.copy(isComplete = true)
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            Prefs.isOnboardingEnabled = false
            _onboardingState.value = _onboardingState.value.copy(isSkipped = true)
        }
    }
}

data class OnboardingState(
    val currentPage: Int = 0,
    val isLastPage: Boolean = false,
    val isComplete: Boolean = false,
    val isSkipped: Boolean = false
)

enum class OnboardingPage(val titleResId: Int, val descriptionResId: Int, val imageResId: Int) {
    WELCOME(
        org.wikipedia.R.string.app_name,
        org.wikipedia.R.string.description_view_article_tabs,
        org.wikipedia.R.drawable.ic_wikipedia
    ),
    EXPLORE(
        org.wikipedia.R.string.nav_item_explore,
        org.wikipedia.R.string.feed_configure_onboarding_text,
        org.wikipedia.R.drawable.ic_globe
    ),
    READING_LISTS(
        org.wikipedia.R.string.nav_item_reading_lists,
        org.wikipedia.R.string.reading_lists_sync_read_lists_info,
        org.wikipedia.R.drawable.ic_bookmark_white_24dp
    )
} 