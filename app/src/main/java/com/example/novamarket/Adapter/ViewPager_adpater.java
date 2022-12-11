package com.example.novamarket.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPager_adpater extends FragmentStateAdapter {
    private final List<Fragment> items = new ArrayList<>(); // Fragment 담을 arraylist

    public void addItem(Fragment item) {
        int pos = items.size();
        this.items.add(item);
        notifyItemInserted(pos);
    }

    public void refreshItem(int pos){
        notifyItemChanged(pos);
    }

    public void changeItem(Fragment item , int pos) {
        this.items.set(pos,item);
        notifyItemChanged(pos);
    }

    public void clearItem(){
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0,size);
    }

    // 생성자
    public ViewPager_adpater(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    public ViewPager_adpater(@NonNull Fragment fragment) {
        super(fragment);
    }
    public ViewPager_adpater(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
