package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.novamarket.Adapter.ViewPager_adpater;
import com.example.novamarket.Fragment.Frag_Sold_Out;
import com.example.novamarket.Fragment.Frag_Reserve;
import com.example.novamarket.Fragment.Frag_Sale;
import com.example.novamarket.databinding.ActivityListSaleBinding;


public class ListSale extends AppCompatActivity {
    private ActivityListSaleBinding bind;
    private Frag_Sale frag_sale; // 판매중
    private Frag_Reserve frag_reserve; // 예약중
    private Frag_Sold_Out frag_sold_out; // 판매완료
    private ViewPager_adpater viewPager_adpater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityListSaleBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        /* 유저 정보 가져오기 */
        createFramgent();
        createViewPager();
    }

    private void createViewPager() {
        frag_sale = Frag_Sale.newInstance();
        frag_reserve = Frag_Reserve.newInstance();
        frag_sold_out = Frag_Sold_Out.newInstance();
    }

    private void createFramgent() {
        viewPager_adpater = new ViewPager_adpater(this);


    }

}