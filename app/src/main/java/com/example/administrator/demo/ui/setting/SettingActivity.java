package com.example.administrator.demo.ui.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;
import com.example.administrator.demo.ui.common.CommonTitleActivity;
import com.example.administrator.demo.conf.LoggingConfigure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**d
 * Created by Administrator on 2018/5/25 0025.
 */

public class SettingActivity extends CommonTitleActivity implements View.OnClickListener{

    private final static String TAG = "SettingActivity";

    @BindView(R.id.bound_spinner) Spinner bound_spinner;
    @BindView(R.id.tv_item_1_bound) TextView tv_item_1_bound;
    @BindView(R.id.et_item_1_down) EditText et_item_1_down;
    @BindView(R.id.tv_item_1_split) TextView tv_item_1_split;
    @BindView(R.id.et_item_1_up) EditText et_item_1_up;
    @BindView(R.id.tv_item_1_unit) TextView tv_item_1_unit;
    @BindView(R.id.tv_item_2_bound) TextView tv_item_2_bound;
    @BindView(R.id.et_item_2_down) EditText et_item_2_down;
    @BindView(R.id.tv_item_2_split) TextView tv_item_2_split;
    @BindView(R.id.et_item_2_up) EditText et_item_2_up;
    @BindView(R.id.tv_item_2_unit) TextView tv_item_2_unit;
    @BindView(R.id.tv_item_3_bound) TextView tv_item_3_bound;
    @BindView(R.id.et_item_3_down) EditText et_item_3_down;
    @BindView(R.id.tv_item_3_split) TextView tv_item_3_split;
    @BindView(R.id.et_item_3_up) EditText et_item_3_up;
    @BindView(R.id.tv_item_3_unit) TextView tv_item_3_unit;
    @BindView(R.id.tv_item_4_bound) TextView tv_item_4_bound;
    @BindView(R.id.et_item_4_down) EditText et_item_4_down;
    @BindView(R.id.tv_item_4_split) TextView tv_item_4_split;
    @BindView(R.id.et_item_4_up) EditText et_item_4_up;
    @BindView(R.id.tv_item_4_unit) TextView tv_item_4_unit;
    @BindView(R.id.tv_item_5_bound) TextView tv_item_5_bound;
    @BindView(R.id.et_item_5_down) EditText et_item_5_down;
    @BindView(R.id.tv_item_5_split) TextView tv_item_5_split;
    @BindView(R.id.et_item_5_up) EditText et_item_5_up;
    @BindView(R.id.tv_item_5_unit) TextView tv_item_5_unit;
    @BindView(R.id.tv_item_6_bound) TextView tv_item_6_bound;
    @BindView(R.id.et_item_6_down) EditText et_item_6_down;
    @BindView(R.id.tv_item_6_split) TextView tv_item_6_split;
    @BindView(R.id.et_item_6_up) EditText et_item_6_up;
    @BindView(R.id.tv_item_6_unit) TextView tv_item_6_unit;
    @BindView(R.id.tv_item_7_bound) TextView tv_item_7_bound;
    @BindView(R.id.et_item_7_down) EditText et_item_7_down;
    @BindView(R.id.tv_item_7_split) TextView tv_item_7_split;
    @BindView(R.id.et_item_7_up) EditText et_item_7_up;
    @BindView(R.id.tv_item_7_unit) TextView tv_item_7_unit;
    @BindView(R.id.tv_item_8_bound) TextView tv_item_8_bound;
    @BindView(R.id.et_item_8_down) EditText et_item_8_down;
    @BindView(R.id.tv_item_8_split) TextView tv_item_8_split;
    @BindView(R.id.et_item_8_up) EditText et_item_8_up;
    @BindView(R.id.tv_item_8_unit) TextView tv_item_8_unit;
    @BindView(R.id.btn_post_cmpt_range) Button btn_post_cmpt_range;

    private static final int UPDATE_SPINNER_ITEMS = 0;
    private static final int GET_MOTOR_BOUND = 1;
    private static final int SUCCESS = 2;
    private static final int FAIL = 3;

    private Unbinder unbinder;

    private List<String> spinnerList;
    private String spinnerItem;
    private ArrayAdapter<String> spinnerAdapter;
    private TextView[] bounds;
    private EditText[] downs;
    private TextView[] split;
    private EditText[] ups;
    private TextView[] units;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_SPINNER_ITEMS:
                    spinnerList.clear();
                    Collections.addAll(spinnerList, (String[]) msg.obj);
                    spinnerAdapter.notifyDataSetChanged();
                    spinnerItem = spinnerList.get(0);
                    requestCmptRange();
                    break;
                case GET_MOTOR_BOUND:
                    updateMotorBoundUI((String) msg.obj);
                    break;
                case SUCCESS:
                    Toast.makeText(SettingActivity.this, "修改成功",
                            Toast.LENGTH_SHORT).show();
                    break;
                case FAIL:
                    Toast.makeText(SettingActivity.this, "修改失败",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        unbinder = ButterKnife.bind(this);
        initUIArray();
        initSpinner();
        requestMotorName();
    }

    @Override
    public void setTitle() {
        titleBar.setText(getText(R.string.setting));
    }

    @Override
    public void beforeFinish() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        JSONArray attrArray = new JSONArray();
        JSONArray upArray = new JSONArray();
        JSONArray downArray = new JSONArray();
        for (int i = 0; i < bounds.length; i++) {
            String boundText = bounds[i].getText().toString();
            if (boundText.isEmpty()) {
                continue;
            }
            attrArray.put(boundText.replace(": ", ""));

            upArray.put(ups[i].getText().toString());
            String downText = downs[i].getText().toString();
            if (downText.isEmpty()) {
                downArray.put(null);
            } else {
                downArray.put(downText);
            }
        }

        final JSONObject data = new JSONObject();
        try {
            data.put("mid", 1);
            data.put("motorName", spinnerItem);
            data.put("attrs", attrArray);
            data.put("up", upArray);
            data.put("down", downArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = RequestManager.updateCmptRange;
                String content = data.toString();
                String response = RequestSender.postRequest(url, content);
                ResponseMessage message = null;
                if (response != null) {
                    message = ResponseParser.parseResponse(response);
                }
                Message msg = Message.obtain();
                if (message != null && message.isSuccess()) {
                    msg.what = SUCCESS;
                } else {
                    msg.what = FAIL;
                }
                msg.obj = content;
                handler.sendMessage(msg);
                if (message != null && !message.isSuccess()) {
                    if (LoggingConfigure.LOGGING) {
                        Log.d(TAG, "requestMotorName() { "
                                + "errorCode: " + message.getErrorCode()
                                + "errorString: " + message.getErrorString()
                                + "}");
                    }
                }
            }
        }).start();
    }

    private void initUIArray() {
        bounds = new TextView[] {tv_item_1_bound, tv_item_2_bound, tv_item_3_bound, tv_item_4_bound,
                tv_item_5_bound, tv_item_6_bound, tv_item_7_bound, tv_item_8_bound};
        downs = new EditText[] {et_item_1_down, et_item_2_down, et_item_3_down, et_item_4_down,
                et_item_5_down, et_item_6_down, et_item_7_down, et_item_8_down};
        ups = new EditText[] {et_item_1_up, et_item_2_up, et_item_3_up, et_item_4_up,
                et_item_5_up, et_item_6_up, et_item_7_up, et_item_8_up};
        units = new TextView[]{tv_item_1_unit, tv_item_2_unit, tv_item_3_unit, tv_item_4_unit,
                tv_item_5_unit, tv_item_6_unit, tv_item_7_unit, tv_item_8_unit};
        split = new TextView[]{tv_item_1_split, tv_item_2_split, tv_item_3_split, tv_item_4_split,
                tv_item_5_split, tv_item_6_split, tv_item_7_split, tv_item_8_split};
        btn_post_cmpt_range.setOnClickListener(this);
    }

    private void initSpinner() {
        spinnerList = new ArrayList<>(8);
        spinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        bound_spinner.setAdapter(spinnerAdapter);

        bound_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (spinnerList.size() != 0) {
                    spinnerItem = spinnerList.get(position);
                    requestCmptRange();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void requestMotorName() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = RequestManager.getCmptNames;
                String content = "{\"equipmentName\":" + "\"排土机\"}";
                String response = RequestSender.postRequest(url, content);
                ResponseMessage message = null;
                if (response != null) {
                    message = ResponseParser.parseResponse(response);
                }
                if (message != null && message.isSuccess()) {
                    String[] motors = parseMotorName(message.getData());
                    Message msg = Message.obtain();
                    msg.obj = motors;
                    msg.what = UPDATE_SPINNER_ITEMS;
                    handler.sendMessage(msg);
                } else if (message != null) {
                    if (LoggingConfigure.LOGGING) {
                        Log.d(TAG, "requestMotorName() { "
                                + "errorCode: " + message.getErrorCode()
                                + "errorString: " + message.getErrorString()
                                + "}");
                    }
                }
            }
        }).start();
    }

    private String[] parseMotorName(String dataJson) {
        if (dataJson == null || dataJson.isEmpty())
            return null;

        Log.d(TAG, dataJson);
        try {
            JSONArray motorArray = new JSONArray(dataJson);
            String[] motors = new String[motorArray.length()];
            for (int i = 0; i < motorArray.length(); i++) {
                motors[i] = motorArray.getString(i);
            }
            return motors;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void requestCmptRange() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = RequestManager.getCmptRange;
                String content = "{\"cmptName\":\""  + spinnerItem + "\"}";
                String response = RequestSender.postRequest(url, content);
                ResponseMessage message = null;
                if (response != null) {
                    message = ResponseParser.parseResponse(response);
                }
                if (message != null && message.isSuccess()) {
                    Message msg = Message.obtain();
                    msg.obj = message.getData();
                    msg.what = GET_MOTOR_BOUND;
                    handler.sendMessage(msg);
                } else if (message != null) {
                    if (LoggingConfigure.LOGGING) {
                        Log.d(TAG, "requestCmptRange() { "
                                + "errorCode: " + message.getErrorCode()
                                + "errorString: " + message.getErrorString()
                                + "}");
                    }
                }
            }
        }).start();
    }

    private void updateMotorBoundUI(String dataJson) {
        Log.d(TAG, "dataJson" + dataJson);
        if (dataJson == null || dataJson.isEmpty())
            return;

        try {
            initUI();

            JSONObject data = new JSONObject(dataJson);
            JSONArray attrsArray = data.getJSONArray("attrs");
            JSONArray upArray = data.getJSONArray("up");
            JSONArray downArray = data.getJSONArray("down");
            JSONArray unitArray = data.getJSONArray("unit");

            int shortStart = 0, longStart = 6;
            int length = attrsArray.length();
            for (int i = 0; i < length; i++) {
                String text = attrsArray.getString(i);
                int start = longStart;
                if (text.length() < 10) {
                    start = shortStart;
                }

                String boundText = text + ": ";
                bounds[start].setText(boundText);
                ups[start].setText(upArray.getString(i));
                units[start].setText(unitArray.getString(i));
                if (downArray.getString(i) != null && !downArray.getString(i).equals("null")) {
                    downs[start].setText(downArray.getString(i));
                } else {
                    downs[start].setText("");
                }

                bounds[start].setVisibility(View.VISIBLE);
                downs[start].setVisibility(View.VISIBLE);
                ups[start].setVisibility(View.VISIBLE);
                units[start].setVisibility(View.VISIBLE);
                split[start].setVisibility(View.VISIBLE);

                if (text.length() < 10) {
                    shortStart++;
                } else {
                    longStart++;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        int length = bounds.length;
        for (int i = 0; i < length; i++) {
            bounds[i].setText("");
            downs[i].setText("");
            ups[i].setText("");
            bounds[i].setVisibility(View.GONE);
            downs[i].setVisibility(View.GONE);
            ups[i].setVisibility(View.GONE);
            units[i].setVisibility(View.GONE);
            split[i].setVisibility(View.GONE);
        }
    }

}
