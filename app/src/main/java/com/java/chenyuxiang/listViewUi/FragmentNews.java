package com.java.chenyuxiang.listViewUi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.java.chenyuxiang.R;
import com.java.chenyuxiang.Utils.NetWorkUtils;
import com.java.chenyuxiang.detailUI.NewsDetailActivity;
import com.java.chenyuxiang.view.SwipeRefreshView;
import com.java.tanghao.AppManager;
import com.java.tanghao.Description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


public class FragmentNews extends ListFragment {
    ArrayList<Description> newsList;
    NewsListAdapter adapter;//new出适配器的实例
    private SwipeRefreshView mSwipeRefreshView;
    private Integer currentPage;
    private String currentCategory;
    public static final int MIN_CLICK_DELAY_TIME = 900;
    private long lastClickTime = 0;
    public FragmentNews(ArrayList<Description> list,Integer currentPage,String category){
        newsList = list;
        this.currentPage = currentPage;
        updateCategory(category);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        adapter = new NewsListAdapter(newsList);//new出适配器的实例
        setListAdapter(adapter);//和List绑定
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        mSwipeRefreshView = view.findViewById(R.id.view_news_swipe);

        // 设置下拉刷新监听器
        mSwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getContext(), "更新新闻", Toast.LENGTH_SHORT).show();
                mSwipeRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myUpdateOperation();
                        adapter.notifyDataSetChanged();
                        // 更新完后调用该方法结束刷新
                        mSwipeRefreshView.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        // 加载监听器
        mSwipeRefreshView.setOnLoadListener(new SwipeRefreshView.OnLoadListener() {
            @Override
            public void onLoad() {
                mSwipeRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myLoadOperation();
                        adapter.notifyDataSetChanged();
                        // 加载完后调用该方法
                        mSwipeRefreshView.setLoading(false);
                    }
                }, 1500);
            }
        });
        return view;
    }
    private void myUpdateOperation(){
        boolean network = NetWorkUtils.isNetworkAvailable();
        currentPage = 1;
        Description[] temp;
        ArrayList<Description> list;
        switch (currentCategory){
            case "news": case"paper": case "all":
                if(network){
                    temp = AppManager.getNewsManager().getPageNews(generateUrl(currentCategory,currentPage));
                    list = new ArrayList<>(Arrays.asList(temp));
                }else{
                    Toast.makeText(getActivity(),"无网络连接,显示本地新闻",Toast.LENGTH_LONG).show();
                    switch (currentCategory){
                        case "news": case"paper":
                            list = AppManager.getNewsManager().getTypeNews(currentCategory);
                            break;
                        case "all":
                            list = AppManager.getNewsManager().getAllNews();
                            break;
                        default:
                            list = AppManager.getNewsManager().getClusterNews(currentCategory);
                            break;
                    }
                    if(list.size()>20){
                       list = new ArrayList<>(list.subList(0,20));
                    }
                }
                break;
            default:
                list = AppManager.getNewsManager().getClusterNews(currentCategory);
                break;
        }
        updateNews(list);
    }

    private void myLoadOperation(){
        Description[] temp;
        ArrayList<Description> list;
        boolean network = NetWorkUtils.isNetworkAvailable();
        switch (currentCategory){
            case "news": case"paper": case "all":
                if(network){
                    temp = AppManager.getNewsManager().getPageNews(generateUrl(currentCategory,currentPage+1));
                    newsList.addAll(Arrays.asList(temp));
                    currentPage+=1;
                } else {
                    Toast.makeText(getActivity(),"无网络连接,显示本地新闻",Toast.LENGTH_LONG).show();
                    switch (currentCategory){
                        case "news": case"paper":
                            list = AppManager.getNewsManager().getTypeNews(currentCategory);
                            break;
                        case "all":
                            list = AppManager.getNewsManager().getAllNews();
                            break;
                        default:
                            list = AppManager.getNewsManager().getClusterNews(currentCategory);
                            break;
                    }
                    if(list.size()<currentPage*20){
                        Toast.makeText(getContext(),"没有更多了",Toast.LENGTH_SHORT).show();
                    }else{
                        currentPage+=1;
                        if(list.size()<currentPage*20){
                            newsList.addAll(list.subList((currentPage-1)*20,list.size()));
                        }else{
                            newsList.addAll(list.subList((currentPage-1)*20,currentPage*20));
                        }
                    }

                }
                break;
            default:
                list = AppManager.getNewsManager().getClusterNews(currentCategory);
                if(list.size()<currentPage*20){
                    Toast.makeText(getContext(),"没有更多了",Toast.LENGTH_SHORT).show();
                }else{
                    currentPage+=1;
                    if(list.size()<currentPage*20){
                        newsList.addAll(list.subList((currentPage-1)*20,list.size()));
                    }else{
                        newsList.addAll(list.subList((currentPage-1)*20,currentPage*20));
                    }
                }
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private String generateUrl(String type,Integer page){
        return "http://covid-dashboard.aminer.cn/api/events/list?type="+type+"&page="+page+"&size="+20;
    }

    public void updateNews(ArrayList<Description>list){
        newsList.clear();
        newsList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    public void updateCategory(String category){
        switch (category){
            case "全部":
                currentCategory="all";
                break;
            case "新闻":
                currentCategory="news";
                break;
            case "论文":
                currentCategory="paper";
                break;
            default:
                currentCategory=category;
                break;
        }
    }


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime <= MIN_CLICK_DELAY_TIME) {
            return;
        }
        lastClickTime = currentTime;
        Description detail = newsList.get(position);

        Intent intent = new Intent(this.getActivity(), NewsDetailActivity.class);
        intent.putExtra("id",detail.getId());
        startActivity(intent);

        detail.setIsRead(true);
        newsList.set(position,detail);
        adapter.notifyDataSetChanged();
    }

    class NewsListAdapter extends ArrayAdapter<Description> {
        private ArrayList<Description> mList;
        public NewsListAdapter(ArrayList<Description> list) {
            super(getActivity(), android.R.layout.simple_list_item_1, list);
            mList=list;
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (null == convertView) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_news, null);
            }
            Description c = mList.get(position);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.news_list_item_titleTextView);
            TextView dateTextView = (TextView) convertView.findViewById(R.id.news_list_item_dateTextView);
            TextView sourceTextView = (TextView) convertView.findViewById(R.id.news_list_item_sourceTextView);
            assert c != null;

            titleTextView.setText(c.getTitle());
            dateTextView.setText(c.getDate());
            sourceTextView.setText(c.getSource());

            if(c.getIsRead()!=null && c.getIsRead()){
                String temp = "[已读]"+c.getTitle();
                titleTextView.setText(temp);
                titleTextView.setTextColor(Color.parseColor("#C0C0C0"));
                dateTextView.setTextColor(Color.parseColor("#C0C0C0"));
                sourceTextView.setTextColor(Color.parseColor("#C0C0C0"));
            } else{
                titleTextView.setTextColor(Color.parseColor("#000000"));
                dateTextView.setTextColor(Color.parseColor("#000000"));
                sourceTextView.setTextColor(Color.parseColor("#000000"));
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

    }
}


