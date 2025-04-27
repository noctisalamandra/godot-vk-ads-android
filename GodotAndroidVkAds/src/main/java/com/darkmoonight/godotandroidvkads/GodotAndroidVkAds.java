package com.darkmoonight.godotandroidvkads;

import android.app.Activity;
import android.graphics.Color;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.my.target.ads.InterstitialAd;
import com.my.target.ads.MyTargetView;
import com.my.target.ads.Reward;
import com.my.target.ads.RewardedAd;
import com.my.target.common.MyTargetManager;
import com.my.target.common.models.IAdLoadingError;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

public class GodotAndroidVkAds extends GodotPlugin {
    private final Activity activity;
    @Nullable
    private MyTargetView bannerAdView = null;
    @Nullable
    private RewardedAd rewardedAd = null;
    @Nullable
    private InterstitialAd interstitialAd = null;
    private RelativeLayout layout = null;
    private RelativeLayout.LayoutParams adParams = null;

    public GodotAndroidVkAds(Godot godot) {
        super(godot);
        this.activity = getActivity();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotAndroidVkAds";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new ArraySet<>();
        addBannerSignals(signals);
        addRewardedSignals(signals);
        addInterstitialSignals(signals);
        return signals;
    }

    private void addBannerSignals(Set<SignalInfo> signals) {
        signals.add(new SignalInfo("_on_banner_loaded"));
        signals.add(new SignalInfo("_on_banner_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_banner_clicked"));
        signals.add(new SignalInfo("_on_banner_show"));
    }

    private void addRewardedSignals(Set<SignalInfo> signals) {
        signals.add(new SignalInfo("_on_rewarded_video_ad_loaded"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_display"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_dismissed"));
        signals.add(new SignalInfo("_on_rewarded_video_ad_clicked"));
        signals.add(new SignalInfo("_on_rewarded", String.class));
        signals.add(new SignalInfo("_on_rewarded_video_ad_failed_to_show", String.class));
    }

    private void addInterstitialSignals(Set<SignalInfo> signals) {
        signals.add(new SignalInfo("_on_interstitial_loaded"));
        signals.add(new SignalInfo("_on_interstitial_failed_to_load", Integer.class));
        signals.add(new SignalInfo("_on_interstitial_ad_display"));
        signals.add(new SignalInfo("_on_interstitial_ad_video_completed"));
        signals.add(new SignalInfo("_on_interstitial_ad_dismissed"));
        signals.add(new SignalInfo("_on_interstitial_clicked"));
        signals.add(new SignalInfo("_on_interstitial_ad_failed_to_show", String.class));
    }

    /* Init */
    @UsedByGodot
    public void init() {
        MyTargetManager.initSdk(activity);
    }

    /* Banner */
    @Override
    public View onMainCreate(Activity activity) {
        layout = new RelativeLayout(activity);
        return layout;
    }

    @NonNull
    private MyTargetView initBanner(final int id, final boolean isOnTop) {
        if (layout == null) layout = new RelativeLayout(activity);
        adParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isOnTop) adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        else adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        bannerAdView = new MyTargetView(activity);
        bannerAdView.setSlotId(id);
        bannerAdView.setBackgroundColor(Color.TRANSPARENT);
        bannerAdView.setListener(createBannerAdListener());

        layout.addView(bannerAdView, adParams);
        bannerAdView.load();
        return bannerAdView;
    }

    private MyTargetView.MyTargetViewListener createBannerAdListener() {
        return new MyTargetView.MyTargetViewListener() {
            @Override
            public void onLoad(@NonNull MyTargetView myTargetView) {
                Log.w("godot", "VkAds: onBannerAdLoaded");
                emitSignal("_on_banner_loaded");
            }

            @Override
            public void onNoAd(@NonNull IAdLoadingError error, @NonNull MyTargetView myTargetView) {
                Log.w("godot", "VkAds: onBannerAdFailedToLoad. Error: " + error.getCode());
                emitSignal("_on_banner_failed_to_load", error.getCode());
            }

            @Override
            public void onShow(@NonNull MyTargetView myTargetView) {
                Log.w("godot", "VkAds: onBannerAdShown");
                emitSignal("_on_banner_show");
            }

            @Override
            public void onClick(@NonNull MyTargetView myTargetView) {
                Log.w("godot", "VkAds: onBannerAdClicked");
                emitSignal("_on_banner_clicked");
            }
        };
    }

    @UsedByGodot
    public void loadBanner(final int id, final boolean isOnTop) {
        activity.runOnUiThread(() -> {
            if (bannerAdView == null) {
                bannerAdView = initBanner(id, isOnTop);
            } else {
                bannerAdView.load();
                Log.w("godot", "VkAds: Banner already created: " + id);
            }
        });
    }

    @UsedByGodot
    public void showBanner() {
        activity.runOnUiThread(() -> {
            if (bannerAdView != null) {
                bannerAdView.setVisibility(View.VISIBLE);
                Log.d("godot", "VkAds: Show Banner");
            } else {
                Log.w("godot", "VkAds: Banner not found");
            }
        });
    }

    @UsedByGodot
    public void removeBanner() {
        activity.runOnUiThread(() -> {
            if (layout == null || adParams == null) {
                return;
            }

            if (bannerAdView != null) {
                layout.removeView(bannerAdView);
                Log.d("godot", "VkAds: Remove Banner");
            } else {
                Log.w("godot", "VkAds: Banner not found");
            }
        });
    }

    @UsedByGodot
    public void hideBanner() {
        activity.runOnUiThread(() -> {
            if (bannerAdView != null) {
                bannerAdView.setVisibility(View.GONE);
                Log.d("godot", "VkAds: Hide Banner");
            } else {
                Log.w("godot", "VkAds: Banner not found");
            }
        });
    }

    /* Rewarded */
    private RewardedAd initRewardedVideo(final int id) {
        rewardedAd = new RewardedAd(id, activity);
        rewardedAd.setListener(createRewardedAdListener());
        rewardedAd.load();
        return rewardedAd;
    }

    private RewardedAd.RewardedAdListener createRewardedAdListener() {
        return new RewardedAd.RewardedAdListener() {
            @Override
            public void onLoad(@NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: onRewardedVideoAdLoaded");
                emitSignal("_on_rewarded_video_ad_loaded");
            }

            @Override
            public void onNoAd(@NonNull IAdLoadingError adRequestError, @NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: onRewardedVideoAdFailedToLoad. Error: " + adRequestError.getCode());
                emitSignal("_on_rewarded_video_ad_failed_to_load", adRequestError.getCode());
            }

            @Override
            public void onClick(@NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: onRewardedVideoAdClicked");
                emitSignal("_on_rewarded_video_ad_clicked");
            }

            @Override
            public void onFailedToShow(@NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: onRewardedVideoAdFailedToShow");
                emitSignal("_on_rewarded_video_ad_failed_to_show", "Rewarded ad failed to show");
            }

            @Override
            public void onDismiss(@NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: onRewardedVideoAdDismissed");
                emitSignal("_on_rewarded_video_ad_dismissed");
            }

            @Override
            public void onReward(@NonNull Reward reward, @NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: " + String.format("onRewarded! type: %s", reward.type));
                emitSignal("_on_rewarded", reward.type);
            }

            @Override
            public void onDisplay(@NonNull RewardedAd rewardedAd) {
                Log.w("godot", "VkAds: onRewardedVideoAdDisplay");
                emitSignal("_on_rewarded_video_ad_display");
            }
        };
    }

    @UsedByGodot
    public void loadRewardedVideo(final int id) {
        activity.runOnUiThread(() -> {
            try {
                rewardedAd = initRewardedVideo(id);
            } catch (Exception e) {
                Log.e("godot", e.toString());
            }
        });
    }

    @UsedByGodot
    public void showRewardedVideo() {
        activity.runOnUiThread(() -> {
            if (rewardedAd != null) {
                rewardedAd.show(activity);
            }
        });
    }

    /* Interstitial */
    private InterstitialAd initInterstitial(final int id) {
        interstitialAd = new InterstitialAd(id, activity);
        interstitialAd.setListener(createInterstitialAdListener());
        interstitialAd.load();
        return interstitialAd;
    }

    private InterstitialAd.InterstitialAdListener createInterstitialAdListener() {
        return new InterstitialAd.InterstitialAdListener() {
            @Override
            public void onLoad(@NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdLoaded");
                emitSignal("_on_interstitial_loaded");
            }

            @Override
            public void onNoAd(@NonNull IAdLoadingError adRequestError, @NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdFailedToLoad. Error: " + adRequestError.getCode());
                emitSignal("_on_interstitial_failed_to_load", adRequestError.getCode());
            }

            @Override
            public void onClick(@NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdClicked");
                emitSignal("_on_interstitial_clicked");
            }

            @Override
            public void onFailedToShow(@NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdFailedToShow");
                emitSignal("_on_interstitial_ad_failed_to_show", "Interstitial ad failed to show");
            }

            @Override
            public void onDismiss(@NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdDismissed");
                emitSignal("_on_interstitial_ad_dismissed");
            }

            @Override
            public void onVideoCompleted(@NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdVideoCompleted");
                emitSignal("_on_interstitial_ad_video_completed");
            }

            @Override
            public void onDisplay(@NonNull InterstitialAd interstitialAd) {
                Log.w("godot", "VkAds: onInterstitialAdDisplay");
                emitSignal("_on_interstitial_ad_display");
            }
        };
    }

    @UsedByGodot
    public void loadInterstitial(final int id) {
        activity.runOnUiThread(() -> {
            try {
                interstitialAd = initInterstitial(id);
            } catch (Exception e) {
                Log.e("godot", e.toString());
            }
        });
    }

    @UsedByGodot
    public void showInterstitial() {
        activity.runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.show(activity);
            }
        });
    }
}
