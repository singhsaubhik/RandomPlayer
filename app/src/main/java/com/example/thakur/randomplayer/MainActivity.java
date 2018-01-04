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
import android.support.design.widget.TabLayout;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thakur.randomplayer.Adapters.ViewPagerAdapter;
import com.example.thakur.randomplayer.Fragments.*;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.items.Song;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {



   // ContextMenuDialogFragment mMenuDialogFragment = new ContextMenuDialogFragment();
    android.support.v4.app.FragmentManager fragmentManager;
    ArrayList<Song> list = new ArrayList<>();
    MusicService mService;
    ImageView nav_album_art;
    ViewPagerAdapter adapter;
    ViewPager viewPager;

    private boolean isSearchOpened=false;
    private InputMethodManager imm;
    private EditText editSearch;
    private  String searchQuery="";
    private  MenuItem mSearchAction;

    private SlidingUpPanelLayout slidingUpPanelLayout;


    public static boolean running = true;
    public boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowAnimations();
        connectToService();

        slidingUpPanelLayout =(SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        list = ListSongs.getSongList(getApplicationContext());

        nav_album_art = (ImageView) findViewById(R.id.nav_album_art);

        mService = MyApp.getMyService();

        setContentView(R.layout.activity_main);
        running = true;

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.add(new SongFragment(),"SongList");
        adapter.add(new AlbumListFragment(),"AlbumList");
        adapter.add(new ArtistList(),"ArtistList");
        adapter.add(new Playlist(),"Play List");


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout viewpagertab =(TabLayout) findViewById(R.id.viewpagertab);
        viewPager.setAdapter(adapter);

        viewpagertab.setupWithViewPager(viewPager);





        fragmentManager = getSupportFragmentManager();

        //initMenuFragment();






        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




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

        registerReceiver(mReciever,filter);

        //Toast.makeText(this, ""+mService.getTitle(), Toast.LENGTH_SHORT).show();

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        else if(viewPager.getCurrentItem() != adapter.getItemId(0)){
            viewPager.setCurrentItem(0,true);
        }

        else if(isSearchOpened){
            
            getSupportActionBar().setDisplayShowCustomEnabled(false);
            clearSearch();
            isSearchOpened = false;
        }
        else if(slidingUpPanelLayout!=null &&
                (slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED
                || slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.ANCHORED)){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }


        else {

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
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){

            case R.id.action_search:
                handleSearch();
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

            startActivity(new Intent(this,PlayListList.class));


        } else if (id == R.id.nav_folder) {

           /* Handler mHandler1 = new Handler();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    setAlbumList();
                }
            };

            mHandler1.postDelayed(runnable,500);
            */


        } else if (id == R.id.nav_nowplaying) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent playerIntent = new Intent(MainActivity.this,PlayerActivity.class);
                    playerIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(playerIntent);
                    //startActivity(new Intent(context, com.example.thakur.randomplayer.PlayList.class));
                    (MainActivity.this).overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

                }
            },200);


                    //setNowPlaying();



        } else if (id == R.id.nav_setting) {

            //UtilityFun.shareApplication(getApplicationContext(),"","");
            //startActivity(new Intent(this,Test.class));
            /*Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/*");
            shareIntent.putExtra(Intent.EXTRA_PACKAGE_NAME,"get this");
            startActivity(Intent.createChooser(shareIntent,"Choose"));
            */

            //startActivity(new Intent(this,SettingsActivity.class));


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

            Toast.makeText(MainActivity.this, "songPos = "+intent.getIntExtra("position",0), Toast.LENGTH_SHORT).show();
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
        try{
            if(isBound){
                unbindService(mConnection1);
                isBound = false;
            }
        }catch (Exception e){

        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void connectToService(){
        if(!MusicService.isServiceRunning) {
            Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
            bindService(serviceIntent, mConnection1, BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

    private void filterList(){

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
        if(Build.VERSION.SDK_INT>=21) {
            Slide slideTransition = new Slide();
            slideTransition.setSlideEdge(Gravity.LEFT);
            slideTransition.setDuration(1000);
            getWindow().setReenterTransition(slideTransition);
            getWindow().setExitTransition(slideTransition);
        }
    }




    private void searchAdapters(String searchQuery){
        if(adapter.getItem(viewPager.getCurrentItem()) instanceof SongFragment){
            ((SongFragment) adapter.getItem(viewPager.getCurrentItem()))
                    .filter(String.valueOf(searchQuery));
        }else if(adapter.getItem(viewPager.getCurrentItem()) instanceof AlbumListFragment){
            ((AlbumListFragment) adapter.getItem(viewPager.getCurrentItem()))
                    .filter(String.valueOf(searchQuery));
        }
    }

    protected void handleSearch(){
        if(isSearchOpened){ //test if the search is open
            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowCustomEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }

            //hides the keyboard
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_search_white_48dp));
            clearSearch();
            searchQuery="";
            //findViewById(R.id.mini_player).setVisibility(View.VISIBLE);

            isSearchOpened = false;
        } else { //open the search entry
            //findViewById(R.id.mini_player).setVisibility(View.GONE);

            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowCustomEnabled(true); //enable it to display a custom view
                getSupportActionBar().setCustomView(R.layout.search_bar_layout);//add the custom view
                getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the title
            }
            editSearch = getSupportActionBar().getCustomView().findViewById(R.id.edtSearch); //the text editor
            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery=String.valueOf(s).toLowerCase();
                    searchAdapters(searchQuery);
                    //Toast.makeText(MainActivity.this, ""+searchQuery, Toast.LENGTH_SHORT).show();
                }
            });
            editSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            editSearch.requestFocus();

            //open the keyboard focused in the edtSearch
            imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);

            //mSearchAction.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_close_white_24dp));
            //add the close icon
            //mSearchAction.setIcon(getResources().getDrawable(R.drawable.cancel));
            isSearchOpened = true;
        }
    }

    private void clearSearch() {
        if(viewPager.getCurrentItem()==0) {
            ((SongFragment) adapter.getItem(viewPager.getCurrentItem()))
                    .filter("");
        }
        else if(viewPager.getCurrentItem()==1){
            ((AlbumListFragment) adapter.getItem(viewPager.getCurrentItem()))
                    .filter("");
        }
    }

}
