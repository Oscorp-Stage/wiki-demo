package org.wikipedia.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wikipedia.databinding.ItemOnboardingPageBinding

class OnboardingPagerAdapter : RecyclerView.Adapter<OnboardingPagerAdapter.ViewHolder>() {
    private val pages = OnboardingPage.values()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount() = pages.size

    class ViewHolder(private val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(page: OnboardingPage) {
            binding.apply {
                onboardingImage.setImageResource(page.imageResId)
                onboardingTitle.setText(page.titleResId)
                onboardingText.setText(page.descriptionResId)
            }
        }
    }
} 