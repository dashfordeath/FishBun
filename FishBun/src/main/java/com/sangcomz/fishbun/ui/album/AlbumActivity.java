package com.sangcomz.fishbun.ui.album;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.sangcomz.fishbun.ItemDecoration.DividerItemDecoration;
import com.sangcomz.fishbun.adapter.AlbumListAdapter;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.permission.PermissionCheck;

import java.util.ArrayList;
import java.util.List;

import kr.co.sangcomz.albummodule.R;


public class AlbumActivity extends AppCompatActivity {

    private List<Album> albumlist = new ArrayList<>();
    private RecyclerView recyclerView;
    private AlbumListAdapter adapter;
    private static List<String> thumbList;
    private PermissionCheck permissionCheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        permissionCheck = new PermissionCheck(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck.CheckStoragePermission())
                new DisplayImage().execute();
        } else
            new DisplayImage().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Define.PERMISSION_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new DisplayImage().execute();
                    // permission was granted, yay! do the
                    // calendar task you need to do.
                } else {
                    permissionCheck.showPermissionDialog(recyclerView);
                    finish();
                }
                return;
            }
        }
    }


    public class DisplayImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            final String orderBy = MediaStore.Images.Media.BUCKET_ID;
            final ContentResolver resolver = getContentResolver();
            String[] projection = new String[]{
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID};

            Cursor imagecursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, orderBy);

            long previousid = 0;

            int bucketColumn = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int bucketcolumnid = imagecursor
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            albumlist = new ArrayList<Album>();
            Album totalAlbum = new Album();
            totalAlbum.bucketid = 0;
            totalAlbum.bucketname = getString(R.string.str_all_view);
            totalAlbum.counter = 0;
            albumlist.add(totalAlbum);
            int totalCounter = 0;
            while (imagecursor.moveToNext()) {
                totalCounter++;
                long bucketid = imagecursor.getInt(bucketcolumnid);
                if (previousid != bucketid) {
                    Album album = new Album();
                    album.bucketid = bucketid;
                    album.bucketname = imagecursor.getString(bucketColumn);
                    album.counter++;

                    albumlist.add(album);
                    previousid = bucketid;

                } else {
                    if (albumlist.size() > 0)
                        albumlist.get(albumlist.size() - 1).counter++;
                }
                if (imagecursor.isLast()) {
                    albumlist.get(0).counter = totalCounter;
                }
            }
            imagecursor.close();
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                if (result.equals("ok")) {
                    adapter = new AlbumListAdapter(AlbumActivity.this, albumlist, getIntent().getStringArrayListExtra(Define.INTENT_PATH));
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    new DisplayThumbnail().execute();
                }
            }
        }
    }

    public class DisplayThumbnail extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            thumbList = new ArrayList<String>();
        }

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < albumlist.size(); i++) {
                Album album = albumlist.get(i);

                String path = getAllMediaThumbnailsPath(
                        AlbumActivity.this, album.bucketid);
                thumbList.add(path);
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            adapter.setThumbList(thumbList);
        }
    }


    public static String getAllMediaThumbnailsPath(Context context, long id) {
        String path = "";
        String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        String bucketid = String.valueOf(id);
        String sort = MediaStore.Images.Thumbnails._ID + " DESC";
        String[] selectionArgs = {bucketid};

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c;
        if (!bucketid.equals("0")) {
            c = context.getContentResolver().query(images, null,
                    selection, selectionArgs, sort);
        } else {
            c = context.getContentResolver().query(images, null,
                    null, null, sort);
        }


        if (c.moveToNext()) {
            selection = MediaStore.Images.Media._ID + " = ?";
            String photoID = c.getString(c.getColumnIndex(MediaStore.Images.Media._ID));
            selectionArgs = new String[]{photoID};

            images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
            Cursor cursor = context.getContentResolver().query(images, null,
                    selection, selectionArgs, sort);
            if (cursor != null && cursor.moveToNext()) {
                path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            } else
                path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        } else {
            Log.e("id", "from else");
        }

        c.close();
        return path;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}