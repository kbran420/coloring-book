/*/*
 * Copyright (c) 2013 Byron Sanchez (hackbytes.com)
 * www.chompix.com
 *
 * This file is part of "Coloring Book for Android."
 *
 * "Coloring Book for Android" is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, version 2 of the
 * license.
 *
 * "Coloring Book for Android" is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Coloring Book for Android."  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package net.globide.coloring_book_08;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Displays the default menu screen.
 */

public class MainActivity extends FragmentActivity implements
        OnMainPanelTouchListener, OnPageChangeListener, OnClickListener {

    // TODO: Create a static class LayoutProperties which will contain global
    // layout values for dependencies. Then simplify the xml layouts that have
    // these sorts of dependencies.

    // TODO: Make the position of each element dynamic. Right now it's absolute
    // in dp, and it looks fine in all layouts, but true fluidness should be the
    // goal for the next update.

    // Define whether or not this activity continues music from the
    // MusicManager.
    private boolean mContinueMusic = true;

    // SharedPreferences persistent data storage properties
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    // SharedPreferences storage properties
    private static String sFilename = "coloring_book_settings";

    // Define our small db API object for database interaction.
    private NodeDatabase mDbNodeHelper = null;
    // Define a category object that will store the category data from the
    // database.
    private Category[] mCategoryData;

    // Define fragments.
    private MainPagerAdapter mPagerAdapter;

    // Declare a variable to store the current item id.
    private int mCurrentItemId = 0;
    // Number of coloring books/categories available.
    private int mCategoryLength = 0;

    // Define views.
    private ViewPager mPager;
    private ImageButton mIbMainSettings;
    private ImageButton mIbMainHelp;
    private ImageButton mIbPagerLeft;
    private ImageButton mIbPagerRight;
    private RelativeLayout mRlMainRightTop;
    private RelativeLayout mRlMainLeftTop;

    // Tablet vs. phone boolean. Defaults to phone.
    public static boolean sIsTablet = false;
    public static boolean sIsSmall = false;
    public static boolean sIsNormal = false;
    public static boolean sIsLarge = false;
    public static boolean sIsExtraLarge = false;

    /**
     * private RelativeLayout mRlMainRight; private RelativeLayout mRlMainLeft;
     * Implements onCreate().
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        MainActivity.sIsTablet = getResources().getBoolean(R.bool.isTablet);
        MainActivity.sIsSmall = getResources().getBoolean(R.bool.isSmall);
        MainActivity.sIsNormal = getResources().getBoolean(R.bool.isNormal);
        MainActivity.sIsLarge = getResources().getBoolean(R.bool.isLarge);
        MainActivity.sIsExtraLarge = getResources().getBoolean(R.bool.isExtraLarge);

        mSharedPreferences = getSharedPreferences(sFilename, 0);
        mEditor = mSharedPreferences.edit();

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        switch (audio.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                if (!MusicManager.sIsManualSound) {
                    mEditor.putBoolean("tbSettingsMusicIsChecked", false);
                    mEditor.commit();
                }
                break;
        }

        if (!MusicManager.sIsManualSound) {
            MusicManager.setPreferences(mSharedPreferences);
            MusicManager.updateVolume();
            MusicManager.updateStatusFromPrefs(this);

            MusicManager.sIsManualSound = true;
        }

        boolean tbSettingsMusicIsChecked = mSharedPreferences.getBoolean(
                "tbSettingsMusicIsChecked", false);

        if (tbSettingsMusicIsChecked) {
            MusicManager.start(this, MusicManager.MUSIC_A);
        }

        mIbPagerLeft = (ImageButton) findViewById(R.id.ibPagerLeft);
        mIbPagerRight = (ImageButton) findViewById(R.id.ibPagerRight);
        mIbMainHelp = (ImageButton) findViewById(R.id.ibMainHelp);
        mIbMainSettings = (ImageButton) findViewById(R.id.ibMainSettings);
        mRlMainLeftTop = (RelativeLayout) findViewById(R.id.rlMainLeftTop);
        mRlMainRightTop = (RelativeLayout) findViewById(R.id.rlMainRightTop);

        /*
         * This screen needs to be dynamically positioned to fit each screen
         * size fluidly.
         */

        // Get the screen metrics.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;

        // Determine the floor section size
        Drawable image = this.getResources().getDrawable(R.drawable.floor);
        // Store the height locally
        int floorHeight = image.getIntrinsicHeight();

        // Determine the title section size

        // Get the top spacing (THIS IS ALSO DEFINED EXACTLY AS IT IS HERE IN
        // EACH XML FILE.
        int topSpacing = 0;

        if (MainActivity.sIsTablet) {
            if (MainActivity.sIsSmall) {
                topSpacing = 18;
            }
            else if (MainActivity.sIsNormal) {
                topSpacing = 24;
            }
            else if (MainActivity.sIsLarge) {
                topSpacing = 27;
            }
            else if (MainActivity.sIsExtraLarge) {
                topSpacing = 30;
            }
        } else {
            topSpacing = 12;
        }

        Drawable imageTitle = this.getResources().getDrawable(R.drawable.main_title);
        // Store the height locally
        int titleHeight = imageTitle.getIntrinsicHeight() + topSpacing;

        // Resize the layout views to be centered in their proper positions
        // based on the sizes calculated.
        ViewGroup.LayoutParams paramsLeftTop = mRlMainLeftTop.getLayoutParams();
        ViewGroup.LayoutParams paramsRightTop = mRlMainRightTop.getLayoutParams();
        int resultHeight = (screenHeight - floorHeight) - titleHeight;
        paramsLeftTop.height = resultHeight;
        paramsRightTop.height = resultHeight;
        mRlMainLeftTop.setLayoutParams(paramsLeftTop);
        mRlMainRightTop.setLayoutParams(paramsRightTop);

        // TODO: See if there are better methods of retrieving the floor height
        // value from the xml layout.

        // Set listeners to objects that can receive user input.
        mIbPagerRight.setOnClickListener(this);
        mIbPagerLeft.setOnClickListener(this);
        mIbMainHelp.setOnClickListener(this);
        mIbMainSettings.setOnClickListener(this);

        // Database check!

        // Create our database access object.
        mDbNodeHelper = new NodeDatabase(this);

        // Call the create method right just in case the user has never run the
        // app before. If a database does not exist, the prepopulated one will
        // be copied from the assets folder. Else, a connection is established.
        mDbNodeHelper.createDatabase();

        // Query the database for all purchased categories.

        // Set a conditional buffer. Internally, the orderby is set to _id ASC
        // (NodeDatabase.java).
        mDbNodeHelper.setConditions("isAvailable", "1");
        // Execute the query.
        mCategoryData = mDbNodeHelper.getCategoryListData();
        // Store the number of categories available.
        mCategoryLength = mCategoryData.length;
        // Flush the buffer.
        mDbNodeHelper.flushQuery();

        // This activity no longer needs the connection, so close it.
        mDbNodeHelper.close();

        // Initialize the pager
        this.initializePaging();
    }

    /**
     * Implements onResume().
     */
    @Override
    protected void onResume() {
        super.onResume();
        mContinueMusic = false;

        MusicManager.start(this, MusicManager.MUSIC_A);

        // Database check!

        // Create our database access object.
        mDbNodeHelper = new NodeDatabase(this);

        // Call the create method right just in case the user has never run the
        // app before. If a database does not exist, the prepopulated one will
        // be copied from the assets folder. Else, a connection is established.
        mDbNodeHelper.createDatabase();

        // Query the database for all purchased categories.

        // Set a conditional buffer. Internally, the orderby is set to _id ASC
        // (NodeDatabase.java).
        mDbNodeHelper.setConditions("isAvailable", "1");
        // Execute the query.
        mCategoryData = mDbNodeHelper.getCategoryListData();
        // Store the number of categories available.
        int newLength = mCategoryData.length;
        // Flush the buffer.
        mDbNodeHelper.flushQuery();

        // This activity no longer needs the connection, so close it.
        mDbNodeHelper.close();

        // Only rebuild the pager if there has been a change in length of
        // available categories. This means that the user has purchased a new
        // coloring book!
        if (newLength > mCategoryLength) {
            mCategoryLength = newLength;
            // Rebuild the pager.
            this.initializePaging();
        }
    }

    /**
     * Initialize the fragments to be paged.
     */
    private void initializePaging() {

        // TODO: Use the image width attribute from within each fragment to
        // determine the width of the ViewPager.

        // If more than one category is available, enable the pagerRight button
        if (mCategoryLength > 1) {
            mIbPagerRight.setImageResource(R.drawable.button_next);
            mIbPagerRight.setEnabled(true);
        }

        // Create a new drawable object to retrieve the cover size.
        Drawable image = this.getResources().getDrawable(R.drawable.cover_1);

        // Store the width locally
        int coverSize = image.getIntrinsicWidth();

        List<Fragment> fragments = new Vector<Fragment>();

        // For each available coloring book (category)...
        for (Category category : mCategoryData) {

            // Bundle for category data to pass to each instantiated fragment
            // (one set per available category). We only pass the category name.
            // Currently, the Touch Listener passes the cid so ColorActivity
            // knows which image set to load.
            Bundle extraArgs = new Bundle();
            extraArgs.putString("category", category.category);
            extraArgs.putInt("id", category.id);

            // Add a fragment to the fragment list.
            fragments.add(Fragment.instantiate(this,
                    MainPanelFragment.class.getName(), extraArgs));
        }

        // Add the list of fragments to the pager adapter.
        this.mPagerAdapter = new MainPagerAdapter(
                super.getSupportFragmentManager(), fragments);

        // Attach the VP to it's corresponding resource id.
        mPager = (ViewPager) super.findViewById(R.id.vpMain);

        // Set the viewpager size dynamically.
        ViewGroup.LayoutParams params = mPager.getLayoutParams();
        params.width = coverSize;
        mPager.setLayoutParams(params);

        // Insert our adapter into this VP.
        mPager.setAdapter(this.mPagerAdapter);

        // Set pager animation speed
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            DecelerateInterpolator sInterpolator = new DecelerateInterpolator(1.0f);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mPager.getContext(),
                    sInterpolator);
            mScroller.set(mPager, scroller);
        } catch (NoSuchFieldException noSuchFieldException) {
            // If the field does not exist, ignore the error and create the
            // default ViewPager. This exception does not need to be handled.
        } catch (IllegalAccessException illegalAccessException) {
            // This exception means our custom scroller could not be attached to
            // the view pager. If this happens, we can ignore it and the default
            // animation will remain unchanged.
        }

        // Set the swipe listener on the pager.
        mPager.setOnPageChangeListener(this);
    }

    /**
     * Initializes the button displays based on the currentItemId.
     */
    private void updateButtons() {

        int count;

        /*
         * Left direciton.
         */

        // Store the total and subtract one to normalize to zero-index.
        count = mPager.getChildCount() - 1;
        if (mCurrentItemId <= 0) {
            mCurrentItemId = 0;

            // Set the button to be disabled.
            mIbPagerLeft.setImageResource(R.drawable.button_previous_disabled);
            mIbPagerLeft.setEnabled(false);

            // mPager.beginFakeDrag();
            // mPager.fakeDragBy(-25);
            // mPager.endFakeDrag();
        }
        if (mCurrentItemId == count - 1) {
            // Enable the right pager if the current position is 1 screen away
            // from
            // the final position.
            mIbPagerRight.setImageResource(R.drawable.button_next);
            mIbPagerRight.setEnabled(true);
        }

        /*
         * Right Direction.
         */

        // Store the total and subtract one to normalize to zero-index.
        count = mPager.getChildCount() - 1;
        if (mCurrentItemId >= count) {
            
            mCurrentItemId = mPager.getChildCount() - 1;

            // Set the button to be disabled.
            mIbPagerRight.setImageResource(R.drawable.button_next_disabled);
            mIbPagerRight.setEnabled(false);

            // mPager.beginFakeDrag();
            // mPager.fakeDragBy(25);
            // mPager.endFakeDrag();
        }
        if (mCurrentItemId == 0 + 1) {
            // Enable the left pager if the current position is 1 screen away
            // from
            // the min (0) position.
            mIbPagerLeft.setImageResource(R.drawable.button_previous);
            mIbPagerLeft.setEnabled(true);
        }
    }

    /**
     * Implements onClick().
     */
    @Override
    public void onClick(View v) {
        Intent i;
        String intentClass = "";
        Class<?> selectedClass = null;
        if (mCategoryLength == 1) {
            switch (v.getId()) {
                case R.id.ibPagerLeft:
                    Toast.makeText( this,"More coloring book packs are available in the Settings -> Shop screen.",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.ibPagerRight:
                    Toast.makeText(this,"More coloring book packs are available in the Settings -> Shop screen.",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else {
            switch (v.getId()) {
                case R.id.ibPagerLeft:
                    mCurrentItemId--;
                    if (mCurrentItemId <= 0) {
                        mCurrentItemId = 0;
                    }
                    
                    mPager.setCurrentItem(mCurrentItemId);

                    break;
                case R.id.ibPagerRight:
                    mCurrentItemId++;
                    if (mCurrentItemId >= mPager.getChildCount() - 1) {
                        mCurrentItemId = mPager.getChildCount() - 1;
                    }
                    mPager.setCurrentItem(mCurrentItemId);
                    break;
            }
        }

        switch (v.getId()) {
            case R.id.ibMainSettings:
                intentClass = "SettingsActivity";
                break;

            case R.id.ibMainHelp:
                intentClass = "HelpActivity";
                break;
        }

        if (intentClass != "") {
            try {
                selectedClass = Class.forName("net.globide.coloring_book_08." + intentClass);
            } catch (ClassNotFoundException e) {
                intentClass = "SettingsActivity";
                try {
                    selectedClass = Class.forName("net.globide.coloring_book_08." + intentClass);
                } catch (ClassNotFoundException e2) {
                    throw new RuntimeException("Application installation is corrupt. Please reinstall the application.");
                }
            } finally {
                if (selectedClass != null) {
                    i = new Intent(this, selectedClass);
                    startActivity(i);
                }
            }
        }
    }

    /**
     * Implements onBackPressed().
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MusicManager.sIsManualSound = false;
        MusicManager.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mContinueMusic) {
            MusicManager.pause();
        }
    }

    @Override
    public void onMainPanelTouchListener(int dbId) {
        Intent i;
        Class<?> selectedClass = null;
        String intentClass = "ColorActivity";
        if (intentClass != "") {
            try {
                selectedClass = Class.forName("net.globide.coloring_book_08." + intentClass);
            } catch (ClassNotFoundException e) {
                intentClass = "ColorActivity";
                try {
                    selectedClass = Class.forName("net.globide.coloring_book_08." + intentClass);
                } catch (ClassNotFoundException e2) {
                    throw new RuntimeException("Application installation is corrupt. Please reinstall the application.");
                }
            } finally {
                if (selectedClass != null) {
                    i = new Intent(this, selectedClass);
                    i.putExtra("id", dbId);
                    startActivity(i);
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {
        mCurrentItemId = position;
        updateButtons();
    }
}
