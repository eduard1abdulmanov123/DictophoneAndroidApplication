package android.abdulmanov.dictophone.activities;

import android.abdulmanov.dictophone.R;
import android.abdulmanov.dictophone.utilities.FileManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DialogEditTextActivity extends AppCompatActivity {

    private static final String ARG_TITLE = "Title";
    private static final String ARG_DEFAULT_TEXT = "DefaultText";
    private static final String ARG_DIRECTORY = "Directory";
    private static final String ARG_FORMAT = "Format";
    private static final String EXTRA_TEXT = "Text";
    private TextView mTitleTextView;
    private EditText mEditText;
    private Button mOKButton;
    private Button mCancelButton;

    public static Intent newIntent(Context packageContext, String title, String directory,
            String defaultText, String format) {
        Intent intent = new Intent(packageContext, DialogEditTextActivity.class);
        intent.putExtra(ARG_TITLE, title);
        intent.putExtra(ARG_DEFAULT_TEXT, defaultText);
        intent.putExtra(ARG_DIRECTORY, directory);
        intent.putExtra(ARG_FORMAT, format);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_edit_text);

        mTitleTextView = (TextView) findViewById(R.id.activity_dialog_edit_text_title);
        mTitleTextView.setText(getIntent().getStringExtra(ARG_TITLE));

        mEditText = (EditText) findViewById(R.id.activity_dialog_edit_text_edit);
        mEditText.setText(getIntent().getStringExtra(ARG_DEFAULT_TEXT));
        mEditText.setSelection(mEditText.getText().length());

        mOKButton = (Button) findViewById(R.id.activity_dialog_edit_text_ok);
        mOKButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String format = getIntent().getStringExtra(ARG_FORMAT);
                String newName = mEditText.getText().toString();
                String defaultName = getIntent().getStringExtra(ARG_DEFAULT_TEXT);
                String directory = getIntent().getStringExtra(ARG_DIRECTORY);
                if (newName.equals(defaultName)
                        || !FileManager
                        .fileExistsInExternalPublicStorage(directory, newName + format)) {
                    sendResult(Activity.RESULT_OK, newName);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.file_exists,
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        mCancelButton = (Button) findViewById(R.id.activity_dialog_edit_text_cancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResult(Activity.RESULT_CANCELED, null);
            }
        });
    }

    private void sendResult(int resultCode, String stringEditText) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TEXT, stringEditText);
        setResult(resultCode, intent);
        finish();
    }

    public static String getResult(Intent intent) {
        return intent.getStringExtra(EXTRA_TEXT);
    }
}
