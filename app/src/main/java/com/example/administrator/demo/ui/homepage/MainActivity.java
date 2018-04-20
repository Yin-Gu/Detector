package com.example.administrator.demo.ui.homepage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.hompage.HomepageItem;
import com.example.administrator.demo.ui.common.ItemClickListener;
import com.example.administrator.demo.ui.comprehensive_report.DataAnalysisActivity;
import com.example.administrator.demo.ui.comprehensive_report.EventStatisticsActivity;
import com.example.administrator.demo.ui.condition_monitoring.AbnormalAlarmActivity;
import com.example.administrator.demo.ui.condition_monitoring.DataMonitoringActivity;
import com.example.administrator.demo.ui.equipment_maintenance.EquipmentInspectionActivity;
import com.example.administrator.demo.ui.equipment_maintenance.EquipmentMaintenanceActivity;
import com.example.administrator.demo.ui.equipment_maintenance.HiddenManagementActivity;
import com.example.administrator.demo.ui.homepage.adpater.HomepageAdapter;
import com.example.administrator.demo.ui.condition_monitoring.DeviceMapActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.condition_monitoring) RecyclerView conditionMonitoring;
    @BindView(R.id.equipment_maintenance) RecyclerView equipmentMaintenance;
    @BindView(R.id.comprehensive_report) RecyclerView comprehensiveReport;

    private List<HomepageItem> conditionMonitoringList = new ArrayList<>();
    private List<HomepageItem> equipmentMaintenanceList = new ArrayList<>();
    private List<HomepageItem> comprehensiveReportList = new ArrayList<>();

    private Unbinder unbinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        createConditionMonitoring();
        createEquipmentMaintenance();
        createComprehensiveReport();

        initConditionMonitoring();
        initEquipmentMaintenance();
        initComprehensiveReport();
    }

    public void createConditionMonitoring(){
        StaggeredGridLayoutManager layoutConditionMonitoring = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        conditionMonitoring.setLayoutManager(layoutConditionMonitoring);
        HomepageAdapter conditionMonitoringAdapter = new HomepageAdapter(conditionMonitoringList);
        conditionMonitoring.setAdapter(conditionMonitoringAdapter);

        conditionMonitoringAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent();
                switch (position){
                    case 0: intent = new Intent(MainActivity.this, DataMonitoringActivity.class);
                    break;
                    case 1: intent = new Intent(MainActivity.this, AbnormalAlarmActivity.class);
                    break;
                    case 2: intent = new Intent(MainActivity.this, DeviceMapActivity.class);
                    break;
                    default: break;
                }
                startActivity(intent);
            }
        });
    }

    public void createEquipmentMaintenance(){
        StaggeredGridLayoutManager layoutEquipmentMaintenance = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        equipmentMaintenance.setLayoutManager(layoutEquipmentMaintenance);
        HomepageAdapter equipmentMaintenanceAdapter = new HomepageAdapter(equipmentMaintenanceList);
        equipmentMaintenance.setAdapter(equipmentMaintenanceAdapter);

        equipmentMaintenanceAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent();
                switch (position){
                    case 0: intent = new Intent(MainActivity.this, EquipmentMaintenanceActivity.class);
                        break;
                    case 1: intent = new Intent(MainActivity.this, EquipmentInspectionActivity.class);
                        break;
                    case 2: intent = new Intent(MainActivity.this, HiddenManagementActivity.class);
                        break;
                    default: break;
                }
                startActivity(intent);
            }
        });
    }

    public void createComprehensiveReport(){
        StaggeredGridLayoutManager layoutComprehensiveReport = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        comprehensiveReport.setLayoutManager(layoutComprehensiveReport);
        HomepageAdapter comprehensiveReportAdapter = new HomepageAdapter(comprehensiveReportList);
        comprehensiveReport.setAdapter(comprehensiveReportAdapter);

        comprehensiveReportAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent();
                switch (position){
                    case 0: intent = new Intent(MainActivity.this, EventStatisticsActivity.class);
                        break;
                    case 1: intent = new Intent(MainActivity.this, DataAnalysisActivity.class);
                        break;
                    default: break;
                }
                startActivity(intent);
            }
        });

    }


    public void initConditionMonitoring(){
        HomepageItem first = new HomepageItem(getString(R.string.data_monitoring),R.drawable.data_monitoring);
        HomepageItem second = new HomepageItem(getString(R.string.abnormal_alarm),R.drawable.abnormal_alarm);
        HomepageItem third = new HomepageItem(getString(R.string.map_display), R.drawable.device_map);
        conditionMonitoringList.add(first);
        conditionMonitoringList.add(second);
        conditionMonitoringList.add(third);
    }
    public void initEquipmentMaintenance(){
        HomepageItem first = new HomepageItem(getString(R.string.equipment_maintenance),R.drawable.equipment_maintenance);
        HomepageItem second = new HomepageItem(getString(R.string.equipment_inspection),R.drawable.equipment_inspection);
        HomepageItem third = new HomepageItem(getString(R.string.hidden_management),R.drawable.hidden_management);
        equipmentMaintenanceList.add(first);
        equipmentMaintenanceList.add(second);
        equipmentMaintenanceList.add(third);
    }
    public void initComprehensiveReport(){
        HomepageItem first = new HomepageItem(getString(R.string.event_statistics),R.drawable.event_statistics);
        HomepageItem second = new HomepageItem(getString(R.string.data_analysis),R.drawable.data_analysis);
        comprehensiveReportList.add(first);
        comprehensiveReportList.add(second);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
