package com.lckiss.weekly.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lckiss.weekly.R;

/**
 * Created by root on 17-7-3.
 * 静态加载的布局,不建议在其中进行和父控件有关的操作
 */

public class CustomKeyboard extends LinearLayout implements View.OnClickListener {

    private EditText money_edit;
    private TextView tips_text;
    private View view;
    private Editable editable;
    private MyNumberTextView[] number;
    private MyNumberTextView plus, minus, dot, backSpace, income;

    private boolean isIncome = false;

    private int[] id = new int[]{R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.zero};

    public CustomKeyboard(Context context) {
        this(context, null);
    }

    public CustomKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_keyboard, this, true);
    }

    private void initKeyBoard() {
        number = new MyNumberTextView[10];
        for (int i = 0; i < 10; i++) {
            number[i] = view.findViewById(id[i]);
            number[i].setOnClickListener(this);
        }

        plus = view.findViewById(R.id.add);
        minus = view.findViewById(R.id.sub);
        dot = view.findViewById(R.id.dot);

        backSpace = view.findViewById(R.id.del);
        income = view.findViewById(R.id.toggle);

        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        dot.setOnClickListener(this);
        backSpace.setOnClickListener(this);
        income.setOnClickListener(this);

        tips_text = view.findViewById(R.id.tips_text);
        initMoneyEdit();
    }

    public  void  initMoneyEdit(){
        money_edit = view.findViewById(R.id.money_edit);
        editable = money_edit.getText();
    }

    public void setParentView(View view) {
        this.view = view;
        initKeyBoard();
    }

    @Override
    public void onClick(View view) {
        initMoneyEdit();
        switch (view.getId()) {
            case R.id.one:
                inputNumber("1");
                break;
            case R.id.two:
                inputNumber("2");
                break;
            case R.id.three:
                inputNumber("3");
                break;
            case R.id.four:
                inputNumber("4");
                break;
            case R.id.five:
                inputNumber("5");
                break;
            case R.id.six:
                inputNumber("6");
                break;
            case R.id.seven:
                inputNumber("7");
                break;
            case R.id.eight:
                inputNumber("8");
                break;
            case R.id.nine:
                inputNumber("9");
                break;
            case R.id.zero:
                inputNumber("0");
                break;
            case R.id.add:
                inputNumber("+");
                break;
            case R.id.sub:
                inputNumber("-");
                break;
            case R.id.dot:
                inputNumber(".");
                break;
            case R.id.del:
                delNumber();
                break;
            case R.id.toggle:
                if (isIncome) {
                    tips_text.setText("支出");
                    tips_text.setTextColor(ContextCompat.getColor(getContext(), R.color.reduce_color));
                    money_edit.setTextColor(ContextCompat.getColor(getContext(), R.color.reduce_color));
                } else {
                    tips_text.setText("收入");
                    tips_text.setTextColor(ContextCompat.getColor(getContext(), R.color.rent));
                    money_edit.setTextColor(ContextCompat.getColor(getContext(), R.color.rent));
                }
                isIncome = !isIncome;
                break;
        }
    }

    private void delNumber() {
//        int start = money_edit.getSelectionStart();
        int start = editable.length();
        if (editable != null && editable.length() > 0) {
            if (start > 0) {
                editable.delete(start - 1, start);
            }
        }
    }

    /**
     * 如果去掉注释代表无法在第一个字前插入数据
     * @param number
     */
    private void inputNumber(String number) {
//        int start = money_edit.getSelectionStart();
        int start = editable.length();
            if (editable != null && editable.length() > 0) {
//            if (start > 0) {
                editable.insert(start, number);
//            }
            } else {
                editable.append(number);
            }
    }
}
