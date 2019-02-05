package com.lckiss.weekly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.lckiss.weekly.adapter.PluginListAdapter;
import com.lckiss.weekly.db.Type;
import com.lckiss.weekly.util.InitDB;
import com.lckiss.weekly.widget.DragListView;

import org.zackratos.ultimatebar.UltimateBar;

import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.util.DataUtil.findAllType;
import static com.lckiss.weekly.util.DataUtil.maxType;
import static com.lckiss.weekly.util.DataUtil.updateAllType;

public class CategoryEditActivity extends AppCompatActivity {

    private List<Type> listType = new ArrayList<Type>();
    private PluginListAdapter mAdapter;
    private DragListView category_list;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    listType = ((List<Type>) msg.obj);
                    break;
            }
        }
    };

    private int[] types = {R.mipmap.food, R.mipmap.drink, R.mipmap.cloth, R.mipmap.kanbing,
            R.mipmap.room, R.mipmap.shop, R.mipmap.study, R.mipmap.transport, R.mipmap.yule,
            R.mipmap.yundong, R.mipmap.banknote,
            R.mipmap.wallet, R.mipmap.movie, R.mipmap.yashua, R.mipmap.hobby, R.mipmap.save,
            R.mipmap.message, R.mipmap.business, R.mipmap.rent
            , R.mipmap.gift, R.mipmap.mucle, R.mipmap.gas, R.mipmap.phone, R.mipmap.card};

    private View dialogView;

    private Spinner spinner;

    private MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter();

    private Type selectedType;
    private Type itemType;
    private int listPosition;

    private EditText dialogComment;
    private int newType;
    private static final String TAG = "info:";

    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_type);

        initView();
        initDialogView();
        initData();
        initDragView();
    }


    private void initData() {
        if (listType.isEmpty()) {
            listType = findAllType();
        } else {
            listType.clear();
            listType.addAll(findAllType());
        }
        if (maxType()<24){
            newType=24;
        }else {
            newType = maxType() + 1;
        }
        intent=new Intent();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //状态栏的特殊处理
        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.line_color));

        category_list = (DragListView) findViewById(R.id.category_list);

        category_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPosition=i;
                itemType = listType.get(listPosition);
                buildDialog();
            }
        });
    }

    private void initDialogView() {
               /*在此处实例化解决java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
    */
        dialogView = View.inflate(this, R.layout.type_edit_item, null);//填充dialog编辑type布局
        //dialog的编辑框
        dialogComment = dialogView.findViewById(R.id.type_edit_txt);
        spinner = dialogView.findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedType = new Type();
                selectedType.setColor(InitDB.getColors(i));
                selectedType.setImage_id(InitDB.getTypes(i));
                selectedType.setType(String.valueOf(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initDragView() {

        category_list.setDragItemListener(new DragListView.SimpleAnimationDragItemListener() {

            private Rect mFrame = new Rect();
            private boolean mIsSelected;

            @Override
            public boolean canDrag(View dragView, int x, int y) {
                // 获取可拖拽的图标
                View dragger = dragView.findViewById(R.id.drag_list_item_image);
                if (dragger == null || dragger.getVisibility() != View.VISIBLE) {
                    return false;
                }
                float tx = x - dragView.getX();
                float ty = y - dragView.getY();
                dragger.getHitRect(mFrame);
                return mFrame.contains((int) tx, (int) ty);
            }


            @Override
            public void beforeDrawingCache(View dragView) {
                mIsSelected = dragView.isSelected();
                View drag = dragView.findViewById(R.id.drag_list_item_image);
                dragView.setSelected(true);
                if (drag != null) {
                    drag.setSelected(true);
                }
            }

            @Override
            public Bitmap afterDrawingCache(View dragView, Bitmap bitmap) {
                dragView.setSelected(mIsSelected);
                View drag = dragView.findViewById(R.id.drag_list_item_image);
                if (drag != null) {
                    drag.setSelected(false);
                }
                return bitmap;
            }

            @Override
            public boolean canExchange(int srcPosition, int position) {
                return mAdapter.exchange(srcPosition, position);
            }
        });

        mAdapter = new PluginListAdapter(this, handler, listType);
        category_list.setAdapter(mAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_add_type:
                itemType = null;
                buildDialog();
                break;
            case R.id.ic_add_accept:
                updateAllType(listType);
                setResult(4,intent);
                finish();
                break;
            case android.R.id.home:
                setResult(4,intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void buildDialog() {
        //重新初始化一次dialogView
        initDialogView();
        final AlertDialog listDialog;
        if (itemType != null) {
            dialogComment.setText(itemType.getDescribe());
            String status = itemType.getStatus();
            if (status!=null) {
                spinner.setSelection(Integer.valueOf(status));
            } else {
                spinner.setSelection(Integer.valueOf(itemType.getType()));
            }
            listDialog = new AlertDialog.Builder(this)
                    .setTitle("编辑")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String comment = dialogComment.getText().toString();
                            if (comment.length() < 1 || comment.length() > 27) {
                                showError();
                                return;
                            }
                            itemType.setImage_id(selectedType.getImage_id());
                            itemType.setColor( selectedType.getColor());
                            itemType.setDescribe(comment);
                            itemType.setStatus(selectedType.getType());

                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            listType.remove(listPosition);
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNeutralButton("取消", null)
                    .setView(dialogView)//在这里把写好的这个listview的布局加载dialog中
                    .create();


        } else {
            listDialog = new AlertDialog.Builder(this)
                    .setTitle("添加")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String comment = dialogComment.getText().toString();
                            if (comment.length() < 1 || comment.length() > 27) {
                                showError();
                                return;
                            }
                            //添加
                            Type tmpType = new Type();
                            tmpType.setColor(selectedType.getColor());
                            tmpType.setType(String.valueOf(newType));
                            tmpType.setDescribe(comment);
                            tmpType.setImage_id(selectedType.getImage_id());
                            tmpType.setStatus(selectedType.getType());
                            //view数据的更新操作
                            listType.add(0, tmpType);
                            //防止同时添加多个时类别重复
                            newType++;
//                            updateAllType(listType);
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setView(dialogView)//在这里把写好的这个listview的布局加载dialog中
                    .create();
        }
        listDialog.show();
    }

    private void showError() {
        AlertDialog.Builder erroDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("输入为空或者类别太长")
                .setPositiveButton("确定", null);
        erroDialog.show();
    }

    private class MySpinnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return types.length;
        }

        @Override
        public Object getItem(int i) {
            return types[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = getLayoutInflater().inflate(R.layout.spinner_item, null);
                viewHolder.spinnerImg = view.findViewById(R.id.spinner_img);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.spinnerImg.setImageResource(types[i]);
            return view;
        }
    }

    private class ViewHolder {
        ImageView spinnerImg;
    }
}
