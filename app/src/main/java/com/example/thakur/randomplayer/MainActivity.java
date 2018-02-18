package com.example.thakur.randomplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.example.thakur.randomplayer.Adapters.ViewPagerAdapter;
import com.example.thakur.randomplayer.BaseActivity.BaseThemedActivity;
import com.example.thakur.randomplayer.BaseActivity.SearchActivity;
import com.example.thakur.randomplayer.BaseActivity.SettingsActivity;
import com.example.thakur.randomplayer.Fragments.*;
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.Constants;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


import java.io.File;
import java.util.ArrayList;


public class MainActivity extends BaseThemedActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener,SlidingUpPanelLayout.PanelSlideListener {


    private static MainActivity sMainActivity;
    private ArrayList<Song> list = new ArrayList<>();
    private MusicService mService;
    private ImageView albumart;
    private TextView songtitle,songartist;
    private ViewPagerAdapter adapter;
    private ViewPager viewPager;



    private SlidingUpPanelLayout slidingUpPanelLayout;


    public static boolean running = true;
    public boolean isBound;

    //Drawer layout
    private AccountHeader headerResult = null;
    private Drawer result = null;

    int primaryColor;
    boolean isDarkTheme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowAnimations();
        connectToService();


        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false);

        mService = MyApp.getMyService();

        setContentView(R.layout.activity_main);

        primaryColor = Config.primaryColor(this,getATEKey());

        sMainActivity = this;
        running = true;

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.add(new SongFragment(), "SongList");
        adapter.add(new AlbumListFragment(), "AlbumList");
        adapter.add(new ArtistList(), "ArtistList");

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        SmartTabLayout viewpagertab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewpagertab.setBackgroundColor(primaryColor);

        viewPager.setAdapter(adapter);
        viewpagertab.setViewPager(viewPager);

        slidingUpPanelLayout =  findViewById(R.id.sliding_layout);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));





        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //setSongFragment();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.RECIEVE_SONG);

        registerReceiver(mReciever, filter);

        //Toast.makeText(this, ""+mService.getTitle(), Toast.LENGTH_SHORT).show();


        list = SongLoader.getSongList(MainActivity.this);
        initNavHeader();

        updateHeader();

        registerReceiver(updateHeaderReceiver,new IntentFilter(PlayerFragment.trackChange));

    }

    public static MainActivity getInstance() {
        return sMainActivity;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else if (viewPager.getCurrentItem() != adapter.getItemId(0)) {
            viewPager.setCurrentItem(0, true);
        } else if (slidingUpPanelLayout != null &&
                (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                        || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {

            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //searchView.setOnQueryTextListener(this);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_search:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                    }
                },100);
                //handleSearch();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //setNavigationArt(0);
        int id = item.getItemId();

        if (id == R.id.nav_playlist) {

            startActivity(new Intent(this, PlayListList.class));

        } else if (id == R.id.nav_folder) {

        } else if (id == R.id.nav_nowplaying) {
            gotoNowPlaying();


        } else if (id == R.id.nav_setting) {

            gotoSettings();

        } else if (id == R.id.nav_about) {
            /*Uri uri = Uri.parse(songList.get(0).getPath());
            ArrayList<Uri> u = new ArrayList<>();
            u.add(0,uri);
            UtilityFun.Share(getApplicationContext(),u,"Choose");
            */

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





    /*private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
        mMenuDialogFragment.setItemLongClickListener(this);
    }*/


    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(MainActivity.this, "songPos = " + intent.getIntExtra("position", 0), Toast.LENGTH_SHORT).show();
        }
    };


    ServiceConnection mConnection1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.LocalService lc = (MusicService.LocalService) iBinder;
            mService = lc.getMusicService();
            MyApp.setMyService(lc.getMusicService());
            isBound = true;
            //Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            isBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciever);
        running = false;
        //unbindService(mConnection1);

    }

    @Override
    protected void onStop() {
        try {
            if (isBound) {
                unbindService(mConnection1);
                isBound = false;
            }
        } catch (Exception e) {

        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void connectToService() {
        if (!MusicService.isServiceRunning) {
            Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
            bindService(serviceIntent, mConnection1, BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

    private void filterList() {

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {


        return false;
    }

    private void setupWindowAnimations() {
        // Re-enter transition is executed when returning to this activity
        if (Build.VERSION.SDK_INT >= 21) {
            Slide slideTransition = new Slide();
            slideTransition.setSlideEdge(Gravity.LEFT);
            slideTransition.setDuration(1000);
            getWindow().setReenterTransition(slideTransition);
            getWindow().setExitTransition(slideTransition);
        }
    }





    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case EXPANDED:
                onPanelExpanded(panel);
                break;
            case ANCHORED:
                collapsePanel(); // this fixes a bug where the panel would get stuck for some reason
                break;
        }
    }

    private void onPanelCollapsed(View panel) {
    }

    private void onPanelExpanded(View panel) {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    private void collapsePanel() {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public SlidingUpPanelLayout.PanelState getPanelState() {
        return slidingUpPanelLayout == null ? null : slidingUpPanelLayout.getPanelState();
    }



    public void expandPanel() {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    private void gotoSettings(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                (MainActivity.this).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        },200);

    }

    private void gotoNowPlaying(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent playerIntent = new Intent(MainActivity.this, PlayerActivity.class);
                playerIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(playerIntent);
                //startActivity(new Intent(context, com.example.thakur.randomplayer.PlayList.class));
                (MainActivity.this).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            }
        }, 200);
    }

    private void initNavHeader(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.inflateHeaderView(R.layout.nav_header_main);

        albumart =  header.findViewById(R.id.album_art);
        songtitle =  header.findViewById(R.id.song_title);
        songartist =  header.findViewById(R.id.song_artist);

    }



    public void setDetailsToHeader() {
        int pos = MyApp.getMyService().getSongPos();
        String name = list.get(pos).getName();
        String artist = list.get(pos).getArtist();

        String path = RandomUtils.getAlbumArt(MainActivity.this,list.get(pos).getAlbumId());

        if (name != null && artist != null) {
            songtitle.setText(name);
            songartist.setText(artist);
        }

        if(RandomUtils.isPathValid(path)){
            Glide.with(MainActivity.this).load(new File(path))
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(albumart);
        }else{
            Glide.with(MainActivity.this).load(R.drawable.defualt_art2)
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(albumart);
        }


    }

    private void updateHeader(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setDetailsToHeader();
            }
        },1200);
    }

    BroadcastReceiver updateHeaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setDetailsToHeader();
        }
    };


}
