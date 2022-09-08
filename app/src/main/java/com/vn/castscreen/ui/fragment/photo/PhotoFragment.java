package com.vn.castscreen.ui.fragment.photo;

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
import com.vn.castscreen.databinding.FragmentPhotoBinding;


public class PhotoFragment extends BaseFragment<FragmentPhotoBinding> {

    @Override
    protected FragmentPhotoBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentPhotoBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerPhotoAdapter viewPagerPhotoAdapter = new ViewPagerPhotoAdapter(getChildFragmentManager());
        binding.viewPagePhoto.setAdapter(viewPagerPhotoAdapter);
        binding.tabLayoutPhoto.setupWithViewPager(binding.viewPagePhoto);
    }

    @Override
    protected void initEvent() {
        binding.imvBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });
    }

    public class ViewPagerPhotoAdapter extends FragmentPagerAdapter {

        public ViewPagerPhotoAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            if (position == 0) {
                fragment = new AllPhotoFragment();
            } else if (position == 1) {
                fragment = new AlbumPhotoFragment();
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
                return getString(R.string.all_photos);
            } else if (position == 1) {
                return getString(R.string.album);
            }
            return title;
        }
    }

}