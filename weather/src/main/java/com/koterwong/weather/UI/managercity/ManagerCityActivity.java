package com.koterwong.weather.ui.managercity;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koterwong.weather.MyApp;
import com.koterwong.weather.R;
import com.koterwong.weather.bean.WeatherBean;
import com.koterwong.weather.common.ActivityStatueBarCompat;
import com.koterwong.weather.ui.managercity.presenter.ManagerCityPresenter;
import com.koterwong.weather.ui.managercity.presenter.ManagerCityPresenterImpl;
import com.koterwong.weather.ui.managercity.view.ManagerCityView;
import com.koterwong.weather.utils.RxBus;
import com.koterwong.weather.widget.BorderDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class ManagerCityActivity extends SwipeBackActivity implements ManagerCityView {

    private RecyclerView mRecyclerView;
    private ManagerCityAdapter mAdapter;

    private List<String> mCityDatas;
    private ManagerCityPresenter mPresenter;
    private ArrayList<String> mResultList = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new ManagerCityPresenterImpl(this);
        this.initView();
        this.initRecyclerView();
    }

    private void initView() {
        setContentView(R.layout.activity_manager_city);
        ActivityStatueBarCompat.compat(this);
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        mSwipeBackLayout.setScrollThresHold(0.5F);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        FloatingActionButton mFloatingAb = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingAb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.slide_delete, Snackbar.LENGTH_LONG).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_manager_city);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new BorderDividerItemDecoration(
                getResources().getDimensionPixelOffset(R.dimen.item_ver),
                getResources().getDimensionPixelOffset(R.dimen.item_hor)
        ));
        mCityDatas = mPresenter.querySavedCityList();
        if (mCityDatas != null && mCityDatas.size() > 0) {
            mAdapter = new ManagerCityAdapter();
            SlideInBottomAnimationAdapter adapter = new SlideInBottomAnimationAdapter(mAdapter);
            adapter.setFirstOnly(false);
            mRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        new ItemTouchHelper(new MySimpleCallBack())
                .attachToRecyclerView(mRecyclerView);
    }

    class MySimpleCallBack extends ItemTouchHelper.SimpleCallback {

        /**
         * 1、dragDirs - 表示拖拽的方向，有六个类型的值：LEFT、RIGHT、START、END、UP、DOWN
         * 2、swipeDirs - 表示滑动的方向，有六个类型的值：LEFT、RIGHT、START、END、UP、DOWN
         * 3. 传入零表示不执行操作
         */
        public MySimpleCallBack() {
            super(0, ItemTouchHelper.LEFT);
        }

        /**
         * @param recyclerView recyclerView
         * @param viewHolder   拖动的ViewHolder
         * @param target       目标位置的ViewHolder
         * @return boolean
         */
        @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        /**
         * @param viewHolder 滑动的ViewHolder
         * @param direction  滑动的方向
         */
        @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (mCityDatas.size() == 1) {
                Snackbar.make(mRecyclerView, R.string.mast_save_one, Snackbar.LENGTH_LONG).show();
                mAdapter.notifyItemRemoved(position);
                return;
            }
            String removeCity = mCityDatas.remove(position);
            mPresenter.deleteCity(removeCity);
            mResultList.add(removeCity);
            mAdapter.notifyItemRemoved(position);
        }

        @Override public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                //滑动时改变Item的透明度
                final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
            }
        }
    }

    class ManagerCityAdapter extends RecyclerView.Adapter<ManagerCityHolder> {

        @Override public ManagerCityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ManagerCityHolder(LayoutInflater.from(MyApp.getApp()).
                    inflate(R.layout.item_maneger_city, parent, false));
        }

        @Override public int getItemCount() {
            return mCityDatas == null ? 0 : mCityDatas.size();
        }

        @Override public void onBindViewHolder(ManagerCityHolder holder, int position) {
            holder.mTextView.setText(mCityDatas.get(position));
            WeatherBean.NowBean nowBean = mPresenter.querySimpleWeather(mCityDatas.get(position));
            if (nowBean != null) {
                holder.mTmpTV.setText(nowBean.tmp + "°");
                holder.mDesTv.setText(nowBean.cond.txt);
            }
        }
    }

    static class ManagerCityHolder extends RecyclerView.ViewHolder {

        private TextView mTextView, mTmpTV, mDesTv;

        public ManagerCityHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_city);
            mTmpTV = (TextView) itemView.findViewById(R.id.tv_tmp);
            mDesTv = (TextView) itemView.findViewById(R.id.tv_des);
        }
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (mResultList.size() > 0) {
            RxBus.getInstance()
                    .send(mResultList);
        }
        super.onDestroy();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
