package com.java.chenyuxiang;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.java.chenyuxiang.channelUI.ChannelActivity;
import com.java.chenyuxiang.listViewUi.MyFragmentPagerAdapter;
import com.java.tanghao.AppManager;
import com.java.tanghao.Category;
import com.java.tanghao.CategoryManager;
import com.java.tanghao.Description;
import com.java.tanghao.NewsManager;
import com.java.tanghao.YiqingScholarDescription;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyFragmentPagerAdapter mFragmentPagerAdapter;
    private Toolbar mToolbar;
    private String currentCategory;
    ArrayList<Description> newsList = new ArrayList<>();
    ArrayList<YiqingScholarDescription> scholarList = new ArrayList<>();
    ArrayList<YiqingScholarDescription> pastScholarList = new ArrayList<>();
    private NewsManager mNewsManager;
    private CategoryManager mCategoryManager;
    private HashMap<String,Integer> loadPage= new HashMap<>();
    public static final int MIN_CLICK_DELAY_TIME = 900;
    private long lastClickTime = 0;

    public static List<String> chinaList = Arrays.asList("Hong Kong","Xinjiang","Beijing","Sichuan","Gansu","Shanghai","Guangdong","Taiwan","Hebei",
            "Shaanxi","Shanxi","Yunnan","Chongqing","Inner Mongol","Shandong","Zhejiang","Tianjin","Liaoning","Fujian","Jiangsu","Hainan","Macao",
            "Jilin","Hubei","Jiangxi","Heilongjiang","Anhui","Guizhou","Hunan","Henan","Guangxi","Ningxia","Qinghai","Xizang");
    public static List<String> worldList = Arrays.asList("China","United States of America","India","Brazil","Russia","Peru","Colombia","Mexico",
    "South Africa","Spain","Argentina","Chile","Iran","France","United Kingdom","Saudi Arabia","Pakistan","Turkey","Italy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        // 三个参数分别是上下文、应用的appId、是否检查签名（默认为false）
        IWXAPI mWxApi = WXAPIFactory.createWXAPI(this, "wxa713e14b9f46a7ec", true);
        // 注册
        mWxApi.registerApp("wxa713e14b9f46a7ec");
        initData();
        //初始化视图
        initViews();
    }
    private void initData(){
        currentCategory = "全部";
        AppManager.getAppManager(this);
        AppManager.getYiqingScholarManager().getPageScholar("https://innovaapi.aminer.cn/predictor/api/v1/valhalla/highlight/get_ncov_expers_list?v=2");
        mCategoryManager = AppManager.getCategoryManager();
        mNewsManager = AppManager.getNewsManager();
        ArrayList<Category> categoryList = mCategoryManager.getAllCategories();
        if(categoryList==null || categoryList.size()==0){
            mCategoryManager.insertCategory(new Category("全部",true));
            mCategoryManager.insertCategory(new Category("论文",true));
            mCategoryManager.insertCategory(new Category("新闻",true));
            mCategoryManager.insertCategory(new Category("国内",false));
            mCategoryManager.insertCategory(new Category("国外",false));
        }
        loadPage.put("news",2);
        loadPage.put("paper",2);
        loadPage.put("event",2);
        loadPage.put("all",2);

//        Description[] news = mNewsManager.getPageNews(generateUrl("event"));
//        news = mNewsManager.getPageNews(generateUrl("all"));
//        init news
        Description[] news = new Description[0];
        try {
            news = mNewsManager.initNews(new InputStreamReader(getResources().getAssets().open("event.json")));
            news = mNewsManager.initNews(new InputStreamReader(getResources().getAssets().open("news.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Description> list = Arrays.asList(news).subList(0,20);
        newsList = new ArrayList<>(list);
//        newsList = mNewsManager.getAllNews();

        scholarList = AppManager.getYiqingScholarManager().getScholar(false);
        if(scholarList.size()>20){
            scholarList = new ArrayList<>(scholarList.subList(0,20));
        }
        pastScholarList = AppManager.getYiqingScholarManager().getScholar(true);
        if(pastScholarList.size()>20){
            pastScholarList = new ArrayList<>(pastScholarList.subList(0,20));
        }



//                start test cluster
//        if (! Python.isStarted()) {
//            Python.start(new AndroidPlatform(this));
//            Python py = Python.getInstance();
//            ArrayList<Description> d = new ArrayList<>();
//            d = mNewsManager.getTypeNews("event");
//            Description[] dd = (Description[])d.toArray(new Description[d.size()]);
//            StringBuilder sb = new StringBuilder();
//            Gson gson = new Gson();
//            sb.append("3");
//            sb.append("QingSteamSplit");
//            for(int j = 0; j < dd.length; j++){
//                sb.append(gson.toJson(dd[j]));
//                sb.append("QingSteamSplit");
//            }
//            String param = sb.toString();
//            PyObject obj = py.getModule("cluster").callAttr("cluster_func", param);
//            py.getBuiltins().get("help").call();
//            String data = obj.toJava(String.class);
            System.out.println("qing");
//        }
//        end test cluster
    }
    private String generateUrl(String type){
        Integer page = loadPage.get(type);
        String url= "http://covid-dashboard.aminer.cn/api/events/list?type="+type+"&page="+page+"&size="+20;
        page+=1;
        loadPage.put(type, page);
        return url;
    }

    private void initViews() {
        //使用适配器将ViewPager与Fragment绑定在一起
        mViewPager= (ViewPager) findViewById(R.id.viewPager);
        mFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),newsList,
                chinaList,worldList,scholarList, pastScholarList,2,currentCategory);
        mViewPager.setAdapter(mFragmentPagerAdapter);

        //将TabLayout与ViewPager绑定在一起
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        mToolbar = findViewById(R.id.toolbar);
        MenuItem item=  mToolbar.findViewById(R.id.item_search);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_news, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime <= MIN_CLICK_DELAY_TIME)
            return false;
        lastClickTime = currentTime;
        int id = item.getItemId();
        Intent intent=null;
        switch (id){
            case R.id.item_search:
                break;
            case R.id.item_category:
                intent = new Intent(this, ChannelActivity.class);
                mCategoryManager.updateInCategory(new Category(currentCategory,true));
                intent.putExtra("currentCategory",currentCategory);
                startActivityForResult(intent,1);
                break;
            case R.id.item_history:
                break;
        }
        return false;
    }



    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        ArrayList<Description>newsList;
        currentCategory = Objects.requireNonNull(data.getExtras()).getString("result");//得到新Activity 关闭后返回的数据
        assert currentCategory != null;
        switch (currentCategory){
            case "论文":
                newsList = mNewsManager.getTypeNews("paper");
                if(newsList.size()==0){
                    Description[]temp = mNewsManager.getPageNews(generateUrl("paper"));
                    newsList = new ArrayList<Description>(Arrays.asList(temp));
                }
                break;
            case "新闻":
                newsList = mNewsManager.getTypeNews("news");
                if(newsList.size()==0){
                    Description[]temp = mNewsManager.getPageNews(generateUrl("news"));
                    newsList = new ArrayList<Description>(Arrays.asList(temp));
                }
                break;
            default:
                newsList = mNewsManager.getAllNews();
                break;
        }
        if(newsList.size()>20)
            mFragmentPagerAdapter.updateNews(new ArrayList<Description>(newsList.subList(0,20)));
        else
            mFragmentPagerAdapter.updateNews(newsList);
        mFragmentPagerAdapter.updateNewsCategory(currentCategory);
        Toast.makeText(this,currentCategory,Toast.LENGTH_SHORT).show();
    }
}
