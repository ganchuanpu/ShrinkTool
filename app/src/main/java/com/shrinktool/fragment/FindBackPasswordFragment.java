package com.shrinktool.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shrinktool.R;
import com.shrinktool.app.BaseFragment;
import com.shrinktool.base.net.RestCallback;
import com.shrinktool.base.net.RestRequest;
import com.shrinktool.base.net.RestResponse;
import com.shrinktool.data.CaptchaCommand;
import com.shrinktool.data.FindPasswordCommand;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 用户注册
 * Created by Alashi on 2016/7/25.
 */
public class FindBackPasswordFragment extends BaseFragment {
    private static final String TAG = "RegisterFragment";

    private static final int ID_CAPTCHA = 1;
    private static final int ID_FIND_PASSWORD = 2;

    @Bind(R.id.cellphoneNumber) EditText cellphoneNumber;
    @Bind(R.id.cellphoneVerificationCode) EditText cellphoneVerificationCode;
    @Bind(R.id.cellphonePassword) EditText cellphonePassword;
    @Bind(R.id.cellphoneNumberClear) View cellphoneNumberClear;
    @Bind(R.id.cellphonePasswordClear) View cellphonePasswordClear;
    @Bind(R.id.cellphoneVerificationCodeButton) Button cellphoneVerificationCodeButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflateView(inflater, container, "找回密码", R.layout.find_back_password);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCellphoneLayout();
    }

    private void initCellphoneLayout(){
        cellphoneNumberClear.setVisibility(View.INVISIBLE);
        cellphoneNumber.setOnFocusChangeListener((view1, b) -> {
            if (cellphoneNumberClear != null) {
                cellphoneNumberClear.setVisibility(b? View.VISIBLE : View.INVISIBLE);
            }
        });

        cellphonePasswordClear.setVisibility(View.INVISIBLE);
        cellphonePassword.setOnFocusChangeListener((view1, b) -> {
            if (cellphonePasswordClear != null) {
                cellphonePasswordClear.setVisibility(b? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @OnClick({R.id.cellphoneNumberClear, R.id.cellphonePasswordClear,
            R.id.cellphoneVerificationCodeButton, R.id.cellphoneOk})
    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.cellphoneNumberClear:
                cellphoneNumber.setText(null);
                cellphoneVerificationCode.setText(null);
                cellphonePassword.setText(null);
                cellphoneNumber.requestFocus();
                break;

            case R.id.cellphonePasswordClear:
                cellphonePassword.setText(null);
                cellphonePassword.requestFocus();
                break;

            case R.id.cellphoneVerificationCodeButton:
                if (view.isEnabled()) {
                    requestVerificationCode();
                }
                break;

            case R.id.cellphoneOk:
                //用手机号注册
                registerByCellphone();
                break;
        }
    }

    private void registerByCellphone() {
        if (!isValidCellphoneNumber()) {
            return;
        }
        if (!checkCellphoneCode()) {
            return;
        }
        if (!checkCellphonePassword()) {
            return;
        }

        FindPasswordCommand command = new FindPasswordCommand();
        command.setMobile(cellphoneNumber.getText().toString());
        command.setCode(cellphoneVerificationCode.getText().toString());
        command.setPassword(cellphonePassword.getText().toString());
        executeCommand(command, callback, ID_FIND_PASSWORD);
    }

    private void requestVerificationCode() {
        if (!isValidCellphoneNumber()) {
            return;
        }

        cellphoneVerificationCodeButton.setEnabled(false);
        startCountDown();

        CaptchaCommand command = new CaptchaCommand();
        command.setMobile(cellphoneNumber.getText().toString());
        command.setType("FindPassword");
        executeCommand(command, callback, ID_CAPTCHA);
    }

    private void startCountDown() {
        long startTime = System.currentTimeMillis();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                long second = 60 - (System.currentTimeMillis() - startTime) / 1000;
                if (second <= 0) {
                    cellphoneVerificationCodeButton.setText("获取验证码");
                    cellphoneVerificationCodeButton.setEnabled(true);
                } else {
                    cellphoneVerificationCodeButton.setText(second + "s");
                    handler.postDelayed(this, 300);
                }
            }
        };

        handler.post(runnable);
    }

    private boolean isValidCellphoneNumber() {
        String number = cellphoneNumber.getText().toString();
        if (!number.matches("^1\\d{10}$")) {
            showToast("请输入正确手机号");
            return false;
        }
        return true;
    }

    private RestCallback callback = new RestCallback() {
        @Override
        public boolean onRestComplete(RestRequest request, RestResponse response) {
            if (request.getId() == ID_CAPTCHA) {
                showToast("已发送短信验证码");
            } else if (request.getId() == ID_FIND_PASSWORD) {
                showToast("修改密码成功");
                getActivity().finish();
            }
            return true;
        }

        @Override
        public boolean onRestError(RestRequest request, int errCode, String errDesc) {
            if (request.getId() == ID_CAPTCHA) {
                showToast("短信验证码发送失败");
            }
            return false;
        }

        @Override
        public void onRestStateChanged(RestRequest request, @RestRequest.RestState int state) {

        }
    };

    private boolean checkCellphoneCode() {
        String code = cellphoneVerificationCode.getText().toString();
        if (code.isEmpty()) {
            showToast("请输入验证码", Toast.LENGTH_SHORT);
            return false;
        }

        if (!code.matches("^\\d+$")) {
            showToast("请输入正确验证码", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    private boolean checkCellphonePassword() {
        String newP = cellphonePassword.getText().toString();
        if (newP.isEmpty()) {
            showToast("请输入密码", Toast.LENGTH_SHORT);
            return false;
        }

        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$";
        if (!newP.matches(regex)) {
            showToast("密码长度为6-15字符，不能为纯数字或纯字母", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }
}
