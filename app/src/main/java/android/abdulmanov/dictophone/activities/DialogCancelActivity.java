package android.abdulmanov.dictophone.activities;

import android.abdulmanov.dictophone.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DialogCancelActivity extends AppCompatActivity {

    private static final String ARG_TITLE = "Title";
    private static final String ARG_NAME_POSITIVE_BUTTON = "NAME_POSITIVE_BUTTON";
    private TextView mTitleTextView;
    private Button mPositiveButton;
    private Button mCancelButton;

    public static Intent newIntent(Context packageContext, String title,
            String namePositiveButton) {
        Intent intent = new Intent(packageContext, DialogCancelActivity.class);
        intent.putExtra(ARG_TITLE, title);
        intent.putExtra(ARG_NAME_POSITIVE_BUTTON, namePositiveButton);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_cancel);

        mTitleTextView = (TextView) findViewById(R.id.activity_dialog_cancel_title);
        mTitleTextView.setText(getIntent().getStringExtra(ARG_TITLE));

        mPositiveButton = (Button) findViewById(R.id.activity_dialog_cancel_cancel_ok);
        mPositiveButton.setText(getIntent().getStringExtra(ARG_NAME_POSITIVE_BUTTON));
        mPositiveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResult(Activity.RESULT_OK);
            }
        });

        mCancelButton = (Button) findViewById(R.id.activity_dialog_cancel_cancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResult(Activity.RESULT_CANCELED);
            }
        });
    }

    private void sendResult(int resultCode) {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }
}
