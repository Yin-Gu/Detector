package com.example.administrator.demo.ui.common;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public abstract class CommonTitleActivity extends AppCompatActivity {

    public static final String BACK_TITLE_NAME_TAG="BACK_TITLE_NAME";

    protected String titleName;
    protected String backTitleName;

    @BindView(R.id.tv_title)protected TextView titleBar;
    @BindView(R.id.tv_back_name)protected TextView backName;

    @OnClick(R.id.ib_back)void onBackClicked(){
        beforeFinish();
        this.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backTitleName = getIntent().getStringExtra(BACK_TITLE_NAME_TAG);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        setTitle();
        if(backTitleName!=null){
            backName.setText(backTitleName);
        }
    }

    protected void startActivityWithBack(Class<?> cls){
        Intent intent = new Intent(this,cls);
        intent.putExtra(BACK_TITLE_NAME_TAG,getString(R.string.msg_back));
        startActivity(intent);
    }

    protected void startActivityWithTitle(Class<?> cls , String title){
        Intent intent =new Intent(this,cls);
        intent.putExtra(BACK_TITLE_NAME_TAG,title);
        startActivity(intent);
    }

    public void showMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    ProgressDialog loadingDialog = null;
    public void showLoadingDialog(String message){
        if(loadingDialog==null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    public void dismissLoadingDialog(){
        if(loadingDialog!=null&&loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    public void showMessageDialog(String message, DialogInterface.OnClickListener confirmListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.msg_hint);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.msg_confirm,confirmListener);
        builder.create().show();
    }

    public void showMessageDialog(String title, String message, DialogInterface.OnClickListener confirmListener, boolean cancelBtn){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.msg_confirm,confirmListener);
        if(cancelBtn){
            builder.setNegativeButton(R.string.msg_cancel,null);
        }
        builder.create().show();
    }

    public void showConfirmMessage(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.msg_hint);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.msg_confirm,null);
        builder.create().show();
    }

    public abstract void setTitle();

    public abstract void beforeFinish();

}
