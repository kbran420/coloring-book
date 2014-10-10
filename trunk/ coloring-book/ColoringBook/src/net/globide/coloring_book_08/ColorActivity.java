/*
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

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

public class ColorActivity extends Activity implements OnClickListener {
    private boolean mContinueMusic = true;
    private NodeDatabase mDbNodeHelper = null;
    private long mCid;
    private Node[] mNodeData;
    private boolean isSavedState = false;
    private ColorGfxData savedData = null;
    private int sCurrentImageId = 0;
    private int mMinImageId;
    private int mMaxImageId;
    public boolean isDirectionRight = true;
    private FrameLayout mFlColorBody;
    private LinearLayout mLlColorPaletteLeft;
    private LinearLayout mLlColorPaletteLeft2;
    private LinearLayout mLlColorPaletteRight;
    private LinearLayout mLlColorPaletteRight2;
    private ImageButton mIbLeft;
    private ImageButton mIbRight;
    private ToggleButton mTbFillMode;
    private ToggleButton mTbEraseMode;
    public ProgressBar pbFloodFill;
    public ColorGFX colorGFX;
    public HashMap<String, ColorPalette> hmPalette = new HashMap<String, ColorPalette>();
    public static boolean sIsTablet = false;
    public static boolean sIsSmall = false;
    public static boolean sIsNormal = false;
    public static boolean sIsLarge = false;
    public static boolean sIsExtraLarge = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_color);
        ColorActivity.sIsTablet = getResources().getBoolean(R.bool.isTablet);
        ColorActivity.sIsSmall = getResources().getBoolean(R.bool.isSmall);
        ColorActivity.sIsNormal = getResources().getBoolean(R.bool.isNormal);
        ColorActivity.sIsLarge = getResources().getBoolean(R.bool.isLarge);
        ColorActivity.sIsExtraLarge = getResources().getBoolean(R.bool.isExtraLarge);
        final Object colorGfxData = getLastNonConfigurationInstance();
        if (colorGfxData != null) {
            isSavedState = true;
            savedData = ((ColorGfxData) colorGfxData);
            sCurrentImageId = savedData.currentImageId;
        }
        else {
            isSavedState = false;
        }

        mDbNodeHelper = new NodeDatabase(this);
        mDbNodeHelper.createDatabase();
        Bundle extras = getIntent().getExtras();
        mCid = (long) extras.getInt("id");
        mNodeData = mDbNodeHelper.getNodeListData(mCid);
        mMinImageId = 0;
        mMaxImageId = mNodeData.length - 1;
        mDbNodeHelper.close();
        mIbLeft = (ImageButton) findViewById(R.id.ibLeft);
        mLlColorPaletteLeft = (LinearLayout) findViewById(R.id.llColorPaletteLeft);
        mLlColorPaletteLeft2 = (LinearLayout) findViewById(R.id.llColorPaletteLeft2);
        mTbFillMode = (ToggleButton) findViewById(R.id.tbFillMode);
        mIbRight = (ImageButton) findViewById(R.id.ibRight);
        mTbEraseMode = (ToggleButton) findViewById(R.id.tbEraseMode);
        mLlColorPaletteRight = (LinearLayout) findViewById(R.id.llColorPaletteRight);
        mLlColorPaletteRight2 = (LinearLayout) findViewById(R.id.llColorPaletteRight2);
        mFlColorBody = (FrameLayout) findViewById(R.id.flColorBody);

        pbFloodFill = new ProgressBar(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;

        if (ColorActivity.sIsTablet) {
            if (ColorActivity.sIsSmall) {
                params.setMargins(16, 16, 16, 16);
            }
            else if (ColorActivity.sIsNormal) {
                params.setMargins(20, 20, 20, 20);
            }
            else if (ColorActivity.sIsLarge) {
                params.setMargins(24, 24, 24, 24);
            }
            else if (ColorActivity.sIsExtraLarge) {
                params.setMargins(28, 28, 28, 28);
            }
        } else {
            params.setMargins(10, 10, 10, 10);
        }

        pbFloodFill.setLayoutParams(params);
        pbFloodFill.setIndeterminate(true);
        pbFloodFill.setVisibility(View.GONE);

        mFlColorBody.requestFocus();
        loadColorCanvas();
        loadColorPalettes();
        loadPaletteButtons();
        loadBrushes();
        mIbLeft.setOnClickListener(this);
        mIbRight.setOnClickListener(this);
        mTbFillMode.setOnClickListener(this);
        mTbEraseMode.setOnClickListener(this);
        if (colorGfxData != null) {
            if (savedData != null) {
                colorGFX.selectedColor = savedData.selectedColor;
                colorGFX.isFillModeEnabled = savedData.isFillModeEnabled;
                colorGFX.isEraseModeEnabled = savedData.isEraseModeEnabled;
                colorGFX.paint = savedData.paint;
                if (savedData.isEraseModeEnabled) {
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button_disabled);
                }
                else {
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button);
                }
            }
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        ColorGfxData colorGfxData = new ColorGfxData();
        colorGfxData.selectedColor = colorGFX.selectedColor;
        colorGfxData.isFillModeEnabled = colorGFX.isFillModeEnabled;
        colorGfxData.isEraseModeEnabled = colorGFX.isEraseModeEnabled;
        colorGfxData.bitmap = colorGFX.bitmap;
        colorGfxData.paint = colorGFX.paint;
        colorGfxData.currentImageId = sCurrentImageId;
        return colorGfxData;
    }

    private MaskFilter mBlur;
    public void loadBrushes() {
        colorGFX.paint = new Paint();
        colorGFX.paint.setAntiAlias(true);
        colorGFX.paint.setDither(true);
        colorGFX.paint.setColor(colorGFX.selectedColor);
        colorGFX.paint.setStyle(Paint.Style.STROKE);
        colorGFX.paint.setStrokeJoin(Paint.Join.ROUND);
        colorGFX.paint.setStrokeCap(Paint.Cap.ROUND);
        colorGFX.paint.setStrokeWidth(12);
        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        colorGFX.paint.setMaskFilter(mBlur);
    }
    private void loadColorPalettes() {
        String tag = "Palette1";
        HashMap<String, Integer> colors = new HashMap<String, Integer>();
        colors.put("1_lightRed", Color.rgb(255, 106, 106));
        colors.put("2_red", Color.rgb(220, 20, 60));
        colors.put("3_orange", Color.rgb(255, 140, 0));
        colors.put("4_yellow", Color.rgb(255, 255, 0));
        colors.put("5_gold", Color.rgb(255, 185, 15));
        ColorPalette Palette1;

        if (isSavedState) {
            Palette1 = new ColorPalette(this, colors, isSavedState,
                    savedData.selectedColor);
        } else {
            Palette1 = new ColorPalette(this, colors);
        }

        hmPalette.put(tag, Palette1);
        tag = "Palette2";

        HashMap<String, Integer> colors2 = new HashMap<String, Integer>();
        colors2.put("1_green", Color.rgb(0, 205, 0));
        colors2.put("2_darkGreen", Color.rgb(0, 128, 0));
        colors2.put("3_lightBlue", Color.rgb(99, 184, 255));
        colors2.put("4_blue", Color.rgb(0, 0, 255));
        colors2.put("5_darkBlue", Color.rgb(39, 64, 139));

        // Create a new palette based on this information.
        ColorPalette Palette2;

        if (isSavedState) {
            Palette2 = new ColorPalette(this, colors2, isSavedState,
                    savedData.selectedColor);
        } else {
            Palette2 = new ColorPalette(this, colors2);
        }

        hmPalette.put(tag, Palette2);
        tag = "Palette3";

        HashMap<String, Integer> colors3 = new HashMap<String, Integer>();
        colors3.put("1_indigo", Color.rgb(75, 0, 130));
        colors3.put("2_violet", Color.rgb(148, 0, 211));
        colors3.put("3_pink", Color.rgb(255, 105, 180));
        colors3.put("4_peach", Color.rgb(255, 215, 164));
        colors3.put("5_lightBrown", Color.rgb(205, 133, 63));

        ColorPalette Palette3;

        if (isSavedState) {
            Palette3 = new ColorPalette(this, colors3, isSavedState,
                    savedData.selectedColor);
        } else {
            Palette3 = new ColorPalette(this, colors3);
        }

        hmPalette.put(tag, Palette3);
        tag = "Palette4";

        HashMap<String, Integer> colors4 = new HashMap<String, Integer>();
        colors4.put("1_black", Color.rgb(0, 0, 0));
        colors4.put("2_grey", Color.rgb(128, 128, 128));
        colors4.put("3_white", Color.rgb(255, 255, 255));
        colors4.put("4_lightgrey", Color.rgb(183, 183, 183));
        colors4.put("5_brown", Color.rgb(139, 69, 19));

        ColorPalette Palette4;

        if (isSavedState) {
            Palette4 = new ColorPalette(this, colors4, isSavedState, savedData.selectedColor);
        } else {
            Palette4 = new ColorPalette(this, colors4);
        }

        hmPalette.put(tag, Palette4);
    }
    private void loadPaletteButtons() {
        for (String key : hmPalette.keySet()) {
            hmPalette.get(key).calculateButtonSize();
            hmPalette.get(key).createButtons();
        }

        hmPalette.get("Palette1").addToView(mLlColorPaletteLeft);
        hmPalette.get("Palette2").addToView(mLlColorPaletteLeft2);
        hmPalette.get("Palette3").addToView(mLlColorPaletteRight);
        hmPalette.get("Palette4").addToView(mLlColorPaletteRight2);
    }
    private void loadColorCanvas() {
        loadImage();
        colorGFX.setTag(0);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(colorGFX.imageWidth, colorGFX.imageHeight);
        params.gravity = Gravity.CENTER;
        colorGFX.setLayoutParams(params);
        mFlColorBody.addView(colorGFX);
        mFlColorBody.addView(pbFloodFill);
    }
    private void loadImage() {

        if (sCurrentImageId < mMinImageId) {
            sCurrentImageId = mMaxImageId;
        }
        if (sCurrentImageId > mMaxImageId) {
            sCurrentImageId = mMinImageId;
        }
        Node node = mNodeData[sCurrentImageId];
        String mResourceName = node.body;
        String mResourcePaint = mResourceName + "_map";
        int resId = getResources().getIdentifier(mResourceName, "drawable",  getPackageName());
        Bitmap picture = decodeImage(resId);
        if (colorGFX == null) {
            if (isSavedState && savedData != null) {
                colorGFX = new ColorGFX(this, picture.getWidth(), picture.getHeight(), isSavedState, savedData.bitmap);
            }
            else {
                colorGFX = new ColorGFX(this, picture.getWidth(), picture.getHeight());
            }
        }
        else {
            if (colorGFX.pathCanvas != null) {
                colorGFX.clear();
            }
        }
        colorGFX.isNextImage = true;
        colorGFX.pictureBitmapBuffer = picture;
        colorGFX.paintBitmapName = mResourcePaint;
    }

    private Bitmap decodeImage(int resId) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float screenWidth = dm.widthPixels;
        float screenHeight = dm.heightPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);

        int inSampleSize = 1;
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        boolean scaleFailed = false;
        Bitmap scaledBitmap = null;
        float resizeRatioHeight = 1;
        if (imageWidth > screenWidth || imageHeight > screenHeight) {
            resizeRatioHeight = (float) imageHeight / (float) screenHeight;
            inSampleSize = (int) resizeRatioHeight;

            if (inSampleSize <= 1) {
                scaleFailed = true;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap picture = BitmapFactory.decodeResource(getResources(), resId, options);
        if (scaleFailed) {
            int newWidth = (int) (picture.getWidth() / resizeRatioHeight);
            int newHeight = (int) (picture.getHeight() / resizeRatioHeight);
            scaledBitmap = Bitmap.createScaledBitmap(picture, newWidth, newHeight, true);
            picture.recycle();
        }
        else {
            scaledBitmap = picture;
        }

        return scaledBitmap;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mContinueMusic = true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        colorGFX.pause();
        if (!mContinueMusic) {
            MusicManager.pause();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        colorGFX.resume();
        mContinueMusic = false;
        MusicManager.start(this, MusicManager.MUSIC_A);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibLeft:
                sCurrentImageId--;
                isDirectionRight = false;
                if (sCurrentImageId < mMinImageId) {
                    sCurrentImageId = mMaxImageId;
                }
                if (colorGFX.mThread != null && colorGFX.mThread.isAlive()) {
                    colorGFX.isThreadBroken = true;
                }
                isSavedState = false;
                loadImage();
                break;
            case R.id.ibRight:
                sCurrentImageId++;
                isDirectionRight = true;
                if (sCurrentImageId > mMaxImageId) {
                    sCurrentImageId = mMinImageId;
                }
                if (colorGFX.mThread != null && colorGFX.mThread.isAlive()) {
                    colorGFX.isThreadBroken = true;
                }
                isSavedState = false;
                loadImage();

                break;

            case R.id.tbFillMode:
                if (mTbEraseMode.isChecked()) {
                    mTbFillMode.setChecked(!mTbFillMode.isChecked());
                    colorGFX.isFillModeEnabled = mTbFillMode.isChecked();
                    colorGFX.paint.setXfermode(null);
                    colorGFX.paint.setMaskFilter(mBlur);
                    colorGFX.isEraseModeEnabled = false;
                    mTbEraseMode.setChecked(false);
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button);
                }
                else {
                    colorGFX.isFillModeEnabled = mTbFillMode.isChecked();
                }

                break;

            case R.id.tbEraseMode:

                boolean isEraseModeEnabled = mTbEraseMode.isChecked();

                if (isEraseModeEnabled) {
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button_disabled);
                    colorGFX.paint.setXfermode(new PorterDuffXfermode( PorterDuff.Mode.CLEAR));
                    colorGFX.paint.setMaskFilter(null);
                    colorGFX.isEraseModeEnabled = true;
                }
                else {
                    mTbFillMode.setBackgroundResource(R.drawable.bucket_button);
                    colorGFX.paint.setXfermode(null);
                    colorGFX.paint.setMaskFilter(mBlur);
                    colorGFX.isEraseModeEnabled = false;
                }

                break;
        }
    }
}
