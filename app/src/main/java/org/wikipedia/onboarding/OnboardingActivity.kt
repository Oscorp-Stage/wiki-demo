package org.wikipedia.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import org.wikipedia.MainActivity
import org.wikipedia.databinding.ActivityOnboardingBinding
import org.wikipedia.settings.Prefs

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()
    private val adapter = OnboardingPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupFlowCollectors()
    }

    private fun setupViews() {
        binding.viewPager.apply {
            adapter = this@OnboardingActivity.adapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.updatePage(position)
                }
            })
        }

        binding.skipButton.setOnClickListener {
            viewModel.skipOnboarding()
        }

        binding.continueButton.setOnClickListener {
            if (viewModel.onboardingState.value.isLastPage) {
                viewModel.completeOnboarding()
            } else {
                binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            }
        }

        binding.pageIndicator.count = OnboardingPage.values().size
    }

    private fun setupFlowCollectors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.onboardingState.collect { state ->
                        binding.pageIndicator.selection = state.currentPage
                        binding.skipButton.isVisible = !state.isLastPage
                        binding.continueButton.setText(
                            if (state.isLastPage) org.wikipedia.R.string.onboarding_get_started
                            else org.wikipedia.R.string.onboarding_continue
                        )

                        if (state.isComplete || state.isSkipped) {
                            startMainActivity()
                        }
                    }
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, OnboardingActivity::class.java)
        }
    }
} 