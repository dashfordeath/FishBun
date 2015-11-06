package com.sangcomz.fishbun.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sangcomz.fishbun.bean.Album;
import com.sangcomz.fishbun.define.Define;
import com.sangcomz.fishbun.ui.picker.PickerActivity;

import java.util.ArrayList;
import java.util.List;

import kr.co.sangcomz.albummodule.R;

public class AlbumListAdapter
        extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder> {

    private Context context;
    private List<Album> albumlist;
    private List<String> thumbList = new ArrayList<String>();
    private String thumPath;
    private ArrayList<String> path;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAlbum;
        private TextView txtAlbum;
        private TextView txtAlbumCount;
        private RelativeLayout areaAlbum;


        public ViewHolder(View view) {
            super(view);
            imgAlbum = (ImageView) view.findViewById(R.id.img_album);
            imgAlbum.setLayoutParams(new RelativeLayout.LayoutParams(Define.ALBUM_THUMNAIL_SIZE, Define.ALBUM_THUMNAIL_SIZE));

            txtAlbum = (TextView) view.findViewById(R.id.txt_album);
            txtAlbumCount = (TextView) view.findViewById(R.id.txt_album_count);
            areaAlbum = (RelativeLayout) view.findViewById(R.id.area_album);
        }
    }

    public AlbumListAdapter(Context context, List<Album> albumlist, ArrayList<String> path) {
        this.context = context;
        this.albumlist = albumlist;
        this.path = path;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new ViewHolder(view);
    }

    public void setThumbList(List<String> thumbList) {
        this.thumbList = thumbList;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (thumbList != null && thumbList.size() > position)
            thumPath = thumbList.get(position);


        if (thumbList != null) {
            if (thumbList.size() > position) {
                Glide
                        .with(context)
                        .load(thumPath)
                        .asBitmap()
                        .override(Define.ALBUM_THUMNAIL_SIZE, Define.ALBUM_THUMNAIL_SIZE)
                        .placeholder(R.mipmap.loading_img)
                        .into(holder.imgAlbum);
            } else {
                Glide.with(context).load(R.mipmap.loading_img).into(holder.imgAlbum);
            }
        }
        holder.areaAlbum.setTag(albumlist.get(position));
        Album a = (Album) holder.areaAlbum.getTag();
        holder.txtAlbum.setText(albumlist.get(position).bucketname);
        holder.txtAlbumCount.setText(String.valueOf(a.counter));


        holder.areaAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Album a = (Album) v.getTag();
                Intent i = new Intent(context, PickerActivity.class);
                i.putExtra("album", a);
                i.putExtra("album_title", albumlist.get(position).bucketname);
                i.putStringArrayListExtra(Define.INTENT_PATH, path);
                ((Activity) context).startActivityForResult(i, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumlist.size();
    }


}

