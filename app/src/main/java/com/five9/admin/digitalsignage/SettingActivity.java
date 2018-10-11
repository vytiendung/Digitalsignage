package com.five9.admin.digitalsignage;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.five9.admin.digitalsignage.Common.Config;
import com.five9.admin.digitalsignage.Common.DataStorage;
import com.five9.admin.digitalsignage.Common.DataTranform;
import com.five9.admin.digitalsignage.Common.ListSchedulesManager;

import static com.five9.admin.digitalsignage.Common.Constant.END_POINT;
import static com.five9.admin.digitalsignage.Common.Constant.PASSWORD;
import static com.five9.admin.digitalsignage.Common.Constant.USERNAME;

public class SettingActivity extends AppCompatActivity {

	private EditText edtServerPath;
	private EditText edtUserName;
	private EditText edtPassword;
	private Button btnSave;

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			if (edtPassword.getText().length() > 0 && edtUserName.getText().length() > 0 && edtPassword.getText().length() > 0){
				if (!edtPassword.getText().toString().equals(Config.getPw())
						|| !edtServerPath.getText().toString().equals(Config.getPw())
						|| !edtUserName.getText().toString().equals(Config.getPw())) {
					btnSave.setEnabled(true);
					btnSave.setBackgroundColor(getResources().getColor(R.color.greenPrimary));
				} else {
					btnSave.setEnabled(false);
					btnSave.setBackgroundColor(getResources().getColor(R.color.btnDisable));
				}
			} else {
				btnSave.setBackgroundColor(getResources().getColor(R.color.btnDisable));
				btnSave.setEnabled(false);
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		edtPassword = findViewById(R.id.edt_password);
		edtServerPath = findViewById(R.id.edt_path_server);
		edtUserName = findViewById(R.id.edt_username);
		btnSave = findViewById(R.id.btn_save);

		edtUserName.setText(Config.getUseName());
		edtServerPath.setText(Config.getServerEndpoint());
		edtPassword.setText(Config.getPw());
		edtUserName.addTextChangedListener(textWatcher);
		edtPassword.addTextChangedListener(textWatcher);
		edtServerPath.addTextChangedListener(textWatcher);
		edtServerPath.setOnFocusChangeListener(getOnFocusChangeListener(edtServerPath));
		edtUserName.setOnFocusChangeListener(getOnFocusChangeListener(edtUserName));
		edtPassword.setOnFocusChangeListener(getOnFocusChangeListener(edtPassword));


		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSave.setEnabled(false);
				DataStorage.getInstance().saveString(END_POINT, edtServerPath.getText().toString());
				DataStorage.getInstance().saveString(USERNAME, edtUserName.getText().toString());
				DataStorage.getInstance().saveString(PASSWORD, edtPassword.getText().toString());
				DataTranform.clearData();
				ListSchedulesManager.getInstance().clearData();
				Intent i = new Intent(SettingActivity.this, MainActivity.class);
				startActivity(i);
				finishAffinity();
			}
		});

	}

	@NonNull
	private View.OnFocusChangeListener getOnFocusChangeListener(final EditText edt) {
		return new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){
					edt.setSelection(edt.getText().length());
					showKeyboard();
				}
			}
		};
	}

	public static void showKeyboard() {
		try {
			InputMethodManager imm = (InputMethodManager) MyApplication.getInstance().getSystemService(Context
					.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		} catch (Exception e) {
		}
	}
}
