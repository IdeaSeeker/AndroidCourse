package com.nikitastroganov.androidcourse.ui.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.postDelayed
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.nikitastroganov.androidcourse.R
import com.nikitastroganov.androidcourse.databinding.FragmentOnboardingBinding
import com.nikitastroganov.androidcourse.onboardingTextAdapterDelegate
import com.nikitastroganov.androidcourse.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.min

@AndroidEntryPoint
class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    private val viewBinding by viewBinding(FragmentOnboardingBinding::bind)

    private val viewModel: OnboardingViewModel by viewModels()

    private var player: ExoPlayer? = null
    private var playerVolumeOn = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player = SimpleExoPlayer.Builder(requireContext()).build().apply {
            addMediaItem(MediaItem.fromUri("asset:///onboarding.mp4"))
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
        }

        viewBinding.playerView.player = player
        setPlayerVolume(false)

        viewBinding.viewPager.setTextPages()
        viewBinding.viewPager.attachDots(viewBinding.onboardingTextTabLayout)
        viewBinding.viewPager.offscreenPageLimit = 1

        viewBinding.viewPager.setPageTransformer { page: View, position: Float ->
            val shift = abs(position)
            page.scaleX = 1 - shift / 2
            page.scaleY = 1 - shift / 2
            page.translationX = -400 * position
            page.alpha = min(1.0f, 1 - shift + 0.3f)
        }

        viewBinding.volumeControlButton.setOnClickListener {
            playerVolumeOn = !playerVolumeOn
            setPlayerVolume(playerVolumeOn)
        }
        viewBinding.signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_signInFragment)
        }
        viewBinding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_signUpFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
        viewModel.isScrolled = true
        autoscroll()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
        viewModel.isScrolled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isScrolled = false
    }

    private fun ViewPager2.setTextPages() {
        adapter = ListDelegationAdapter(onboardingTextAdapterDelegate()).apply {
            items = listOf(
                getString(R.string.onboarding_view_pager_text_1),
                getString(R.string.onboarding_view_pager_text_2),
                getString(R.string.onboarding_view_pager_text_3)
            )
        }
    }

    private fun ViewPager2.attachDots(tabLayout: TabLayout) {
        TabLayoutMediator(tabLayout, this) { _, _ -> }.attach()
    }

    private fun setPlayerVolume(newVolume: Boolean) {
        if (newVolume) {
            viewBinding.playerView.player?.volume = 1F
            viewBinding.volumeControlButton.setImageResource(R.drawable.ic_volume_on_white_24dp)
        } else {
            viewBinding.playerView.player?.volume = 0F
            viewBinding.volumeControlButton.setImageResource(R.drawable.ic_volume_off_white_24dp)
        }
    }

    private val autoScrollingDelay = 4000L

    private fun autoscroll() {
        viewBinding.viewPager.setCurrentItem(viewModel.viewPagerPage, true)

        viewBinding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    viewModel.autoScrollIndex += 1
                    val autoScrollIndexSaved = viewModel.autoScrollIndex
                    viewModel.viewPagerPage = position

                    Handler(Looper.getMainLooper()).postDelayed(autoScrollingDelay) {
                        activity?.runOnUiThread {
                            if (viewModel.isScrolled && viewModel.autoScrollIndex == autoScrollIndexSaved) {
                                viewModel.viewPagerPage = (viewModel.viewPagerPage + 1) % 3
                                viewBinding.viewPager.setCurrentItem(viewModel.viewPagerPage, true)
                            }
                        }
                    }
                }
            }
        )
    }
}