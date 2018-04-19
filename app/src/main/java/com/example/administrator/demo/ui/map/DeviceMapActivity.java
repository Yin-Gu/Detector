package com.example.administrator.demo.ui.map;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.example.administrator.demo.R;
import com.example.administrator.demo.ui.condition_monitoring.DataMonitoringActivity;

import java.util.ArrayList;
public class DeviceMapActivity extends AppCompatActivity {
    public static float UNDONE = BitmapDescriptorFactory.HUE_RED;
    public static float DONE = BitmapDescriptorFactory.HUE_GREEN;

    private MapView mapView;
    AMap aMap;
    private UiSettings mUiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_map);
        //获取地图控件引用
        mapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);

        //初始化地图控制器对象aMap
        if(aMap == null){
            aMap = mapView.getMap(); //将mapView交给地图控制器管理
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
            mUiSettings = aMap.getUiSettings();
        }

        /**
         * 设置地图是否可以手势滑动
         */
        mUiSettings.setScrollGesturesEnabled(true);

        /**
         * 设置地图是否可以手势缩放大小
         */
        mUiSettings.setZoomGesturesEnabled(true);
        /**
         * 设置地图是否可以倾斜
         */
        mUiSettings.setTiltGesturesEnabled(true);
        /**
         * 设置地图是否可以旋转
         */
        mUiSettings.setRotateGesturesEnabled(true);
        mUiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示

//        绘制点
        ArrayList<MarkerOptions> markers = new ArrayList<>();

        LatLng latLng1 = new LatLng(38.1478417,111.5486361);
        MarkerOptions markerOption1 = new MarkerOptions();
        markerOption1.position(latLng1);
        markerOption1.title("排土机").snippet("状态：运行中");
        markerOption1.draggable(false);//设置Marker可拖动
        markerOption1.setFlat(true);//设置marker平贴地图效果
        markerOption1.icon(BitmapDescriptorFactory.defaultMarker(DONE));

        LatLng latLng2 = new LatLng(38.1479722,111.54355);
        MarkerOptions markerOption2 = new MarkerOptions();
        markerOption2.position(latLng2);
        markerOption2.title("浮选机").snippet("状态：停止运行");
        markerOption2.draggable(false);//设置Marker可拖动
        markerOption2.setFlat(true);//设置marker平贴地图效果
        markerOption2.icon(BitmapDescriptorFactory.defaultMarker(UNDONE));

        LatLng latLng3 = new LatLng(38.1606611,111.58825);
        MarkerOptions markerOption3 = new MarkerOptions();
        markerOption3.position(latLng3);
        markerOption3.title("破碎站").snippet("状态：停止运行");
        markerOption3.draggable(false);//设置Marker可拖动
        markerOption3.setFlat(true);//设置marker平贴地图效果
        markerOption3.icon(BitmapDescriptorFactory.defaultMarker(UNDONE));

        markers.add(markerOption1);
        markers.add(markerOption2);
        markers.add(markerOption3);

        // 设置当前地图显示为当前位置
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 25));

        aMap.addMarkers(markers,true);
        // 定义 Marker 点击事件监听
        AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
            // marker 对象被点击时回调的接口
            // 返回 true 则表示接口已响应事件，否则返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                } else {
                    marker.hideInfoWindow();
                }
                return true;
            }
        };

        // 绑定 Marker 被点击事件
        aMap.setOnMarkerClickListener(markerClickListener);
        //定义InfoWindow 点击事件
        AMap.OnInfoWindowClickListener listener = new AMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getTitle().equals("排土机")){
                   Intent intent = new Intent(DeviceMapActivity.this, DataMonitoringActivity.class);
                   startActivity(intent);
                }
            }
        };
        //绑定信息窗点击事件
        aMap.setOnInfoWindowClickListener(listener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}
