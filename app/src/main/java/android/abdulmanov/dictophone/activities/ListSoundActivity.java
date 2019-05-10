package android.abdulmanov.dictophone.activities;

import android.abdulmanov.dictophone.R;
import android.abdulmanov.dictophone.model.Sound;
import android.abdulmanov.dictophone.model.Sound.CallbackSetupSpecifications;
import android.abdulmanov.dictophone.utilities.FileManager;
import android.abdulmanov.dictophone.views.ItemTouchHelper.ItemTouchHelperAdapter;
import android.abdulmanov.dictophone.views.ItemTouchHelper.SimpleItemTouchHelperCallback;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListSoundActivity extends AppCompatActivity {

    private static final String ARG_DIRECTORY_SOUND = "ARG_DIRECTORY_SOUND";
    private static final String ARG_FORMAT = "Format";
    private static final int REQUEST_RENAME = 1;
    private RecyclerView mRecyclerView;
    private SoundAdapter mAdapter;
    private SoundHolder mCurrentSoundHolder;

    public static Intent newIntent(Context packageContext, String directorySound, String format) {
        Intent intent = new Intent(packageContext, ListSoundActivity.class);
        intent.putExtra(ARG_DIRECTORY_SOUND, directorySound);
        intent.putExtra(ARG_FORMAT, format);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sound);
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_list_sound_recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_RENAME) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentSoundHolder.mSound
                        .rename(DialogEditTextActivity.getResult(data) + getIntent()
                                .getStringExtra(ARG_FORMAT));
                mAdapter.notifyItemChanged(mCurrentSoundHolder.getAdapterPosition());
            }
        }
    }

    private void updateUI() {
        List<Sound> sounds = getSounds();
        if (mAdapter == null) {
            mAdapter = new SoundAdapter(sounds);
            mRecyclerView.setAdapter(mAdapter);
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);
        } else {
            mAdapter.setSounds(sounds);
            mAdapter.notifyDataSetChanged();
        }
    }

    private List<Sound> getSounds() {
        List<Sound> sounds = new ArrayList<>();
        File[] files = FileManager
                .getFilesWithExternalPublicStorage(getIntent().getStringExtra(ARG_DIRECTORY_SOUND));
        for (File file : files) {
            sounds.add(new Sound(file));
        }
        return sounds;
    }

    private class SoundHolder extends RecyclerView.ViewHolder implements OnClickListener,
            OnLongClickListener {

        private Sound mSound;
        private TextView mName;
        private TextView mDate;
        private TextView mTime;

        public SoundHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mName = (TextView) itemView.findViewById(R.id.list_item_sound_name_text_view);
            mDate = (TextView) itemView.findViewById(R.id.list_item_sound_date_text_view);
            mTime = (TextView) itemView.findViewById(R.id.list_item_sound_time_text_view);
        }

        public void bindSound(Sound sound) {
            mSound = sound;
            mSound.sutupSpecifications(new CallbackSetupSpecifications() {
                @Override
                public void finish() {
                    mName.post(new Runnable() {
                        @Override
                        public void run() {
                            mName.setText(mSound.getName());
                            mDate.setText(mSound.getDate());
                            mTime.setText(mSound.getTime());
                        }
                    });
                }
            });
        }


        @Override
        public void onClick(View view) {
            Intent intent = DialogPlayerActivity
                    .newIntent(getApplicationContext(), mSound.getFile());
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            mCurrentSoundHolder = this;
            Intent intent = DialogEditTextActivity
                    .newIntent(getApplicationContext(), getString(R.string.rename_file),
                            getIntent().getStringExtra(ARG_DIRECTORY_SOUND),
                            mSound.getName(),
                            getIntent().getStringExtra(ARG_FORMAT));
            startActivityForResult(intent, REQUEST_RENAME);
            return true;
        }

    }

    private class SoundAdapter extends RecyclerView.Adapter<SoundHolder> implements
            ItemTouchHelperAdapter {

        private List<Sound> mSounds;

        public SoundAdapter(List<Sound> sounds) {
            mSounds = sounds;
        }

        public void setSounds(List<Sound> sounds) {
            mSounds = sounds;
        }

        @NonNull
        @Override
        public SoundHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(ListSoundActivity.this);
            View view = inflater.inflate(R.layout.list_item_sound, viewGroup, false);
            return new SoundHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SoundHolder soundHolder, int i) {
            Sound sound = mSounds.get(i);
            soundHolder.bindSound(sound);
        }

        @Override
        public int getItemCount() {
            return mSounds.size();
        }

        @Override
        public void onItemDismiss(int position) {
            mSounds.get(position).delete();
            mSounds.remove(position);
            notifyItemRemoved(position);
        }
    }
}
