package com.vn.castscreen.ui.fragment.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.Navigation;

import com.vn.castscreen.R;
import com.vn.castscreen.base.BaseFragment;
import com.vn.castscreen.databinding.FragmentVideoBinding;

public class VideoFragment extends BaseFragment<FragmentVideoBinding> {

    @Override
    protected FragmentVideoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentVideoBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initEvent() {
        binding.imvBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerVideoAdapter viewPagerVideoAdapter = new ViewPagerVideoAdapter(getChildFragmentManager());
        binding.viewPageVideo.setAdapter(viewPagerVideoAdapter);
        binding.tabLayoutVideo.setupWithViewPager(binding.viewPageVideo);
    }

    public class ViewPagerVideoAdapter extends FragmentPagerAdapter {

        public ViewPagerVideoAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            if (position == 0) {
                fragment = new AllVideoFragment();
            } else if (position == 1) {
                fragment = new AlbumVideoFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;

            if (position == 0) {
                return getString(R.string.all_videos);
            } else if (position == 1) {
                return getString(R.string.album);
            }
            return title;
        }
    }
}