package com.example.hmsdemo.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hmsdemo.R;

import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MapStyleOptions;
import com.huawei.hms.maps.util.LogM;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class styleMapDemo extends AppCompatActivity implements  OnMapReadyCallback{

    private static final String TAG = "StyleMapDemoActivity";

    private SupportMapFragment mSupportMapFragment;

    private static final LatLng SYDNEY = new LatLng(-33.8688, 151.2093);

    private HuaweiMap hmap;

    private int mSelectedStyleId = R.string.style_label_retro;

    private static final String SELECTED_STYLE = "selected_style";

    private int mStyleIds[] = {R.string.style_label_retro, R.string.style_label_night, R.string.style_label_grayscale,
            R.string.style_label_no_pois_no_transit, R.string.style_label_default,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_map_demo);
        if (savedInstanceState != null) {
            mSelectedStyleId = savedInstanceState.getInt(SELECTED_STYLE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_map_demo);
        }

        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.styledMap);
        mSupportMapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.styled_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_style_choose) {
            showStylesDialog();
        }
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return true;

    }

    private void showStylesDialog() {
        // mStyleIds stores each style's resource ID, and we extract the names here, rather
        // than using an XML array resource which AlertDialog.Builder.setItems() can also
        // accept. We do this since using an array resource would mean we would not have
        // constant values we can switch/case on, when choosing which style to apply.
        List<String> styleNames = new ArrayList<>();
        for (int style : mStyleIds) {
            styleNames.add(getString(style));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.style_choose));
        builder.setItems(styleNames.toArray(new CharSequence[styleNames.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedStyleId = mStyleIds[which];
                        String msg = getString(R.string.style_set_to, getString(mSelectedStyleId));
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        setSelectedStyle();
                    }
                });
        builder.show();
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        LogM.i(TAG, "onMapReady: ");
        hmap = huaweiMap;
        hmap.setMyLocationEnabled(true);
        hmap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 0));
    }

    /**
     * Creates a {@link MapStyleOptions} object via loadRawResourceStyle() (or via the
     * constructor with a JSON String), then sets it on the {@link HuaweiMap} instance,
     * via the setMapStyle() method.
     */
    private void setSelectedStyle() {
        MapStyleOptions style;
        switch (mSelectedStyleId) {
            case R.string.style_label_night:
                // Sets the retro style via raw resource JSON.
                String assetName = "asset://styles/night.json";
                style = MapStyleOptions.loadAssetResouceStyle(assetName);
                break;
            case R.string.style_label_retro:
                // Sets the night style via raw resource JSON.
                style = MapStyleOptions.loadAssetResouceStyle("asset://styles/wvz5326jx.json");
                break;
            case R.string.style_label_grayscale:
                // Sets the grayscale style via raw resource JSON.
                style = MapStyleOptions.loadAssetResouceStyle("asset://styles/dark-matter.json");
                break;
            case R.string.style_label_no_pois_no_transit:
                // Sets the no POIs or transit style via JSON string.
                style = new MapStyleOptions("[" + "  {" + "    \"featureType\":\"poi.business\","
                        + "    \"elementType\":\"all\"," + "    \"stylers\":[" + "      {"
                        + "        \"visibility\":\"off\"" + "      }" + "    ]" + "  }," + "  {"
                        + "    \"featureType\":\"transit\"," + "    \"elementType\":\"all\"," + "    \"stylers\":["
                        + "      {" + "        \"visibility\":\"off\"" + "      }" + "    ]" + "  }" + "]");
                break;
            case R.string.style_label_default:
                // Removes previously set style, by setting it to null.
                style = null;
                break;
            default:
                return;
        }
        if(null != hmap){
            hmap.setMapStyle(style);
        }

    }

}
