package com.yunwltn98.youtubeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.yunwltn98.youtubeapp.adapter.VideoAdapter;
import com.yunwltn98.youtubeapp.config.Config;
import com.yunwltn98.youtubeapp.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editSearch;
    ImageView imgSearch;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    VideoAdapter adapter;
    ArrayList<Video> videoList = new ArrayList<>();
    String keyword;
    String pageToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editSearch = findViewById(R.id.editSearch);
        imgSearch = findViewById(R.id.imgSearch);
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 맨 마지막데이터가 화면에 보이면 네트워크 통해서 데이터를 추가로 받아온다
                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                // 리사이클러뷰 데이터 갯수
                int totalCount = recyclerView.getAdapter().getItemCount();

                // 스크롤을 데이터 맨 아래까지 한 상태
                if (lastPosition +1 == totalCount) {
                    // 네트워크 통해서 데이터 추가로 받아와서 화면에 표시하기
                    addNetworkData();
                }
            }
        });

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = editSearch.getText().toString().trim();

                if (keyword.isEmpty()) {
                    return;
                }
                // 네트워크 처리하는 함수 호출
                getNetworkData();
            }
        });
    }

    private void getNetworkData() {
        String URL = Config.BASE_URL + Config.PATH+"?key="+Config.API_KEY+"&part=snippet&q="+keyword+"&maxResults=20&type=video";

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                videoList.clear();
                pageToken = null;

                try {
                    progressBar.setVisibility(View.GONE);
                    pageToken = response.getString("nextPageToken");

                    JSONArray responseData = response.getJSONArray("items");
                    for (int i = 0; i < responseData.length(); i++) {
                        JSONObject data = responseData.getJSONObject(i);

                        JSONObject idData = data.getJSONObject("id");
                        JSONObject snippetData = data.getJSONObject("snippet");
                        JSONObject thumbnailsData = snippetData.getJSONObject("thumbnails");
                        JSONObject mediumData = thumbnailsData.getJSONObject("medium");
                        JSONObject highData = thumbnailsData.getJSONObject("high");

                        Video video = new Video(idData.getString("videoId"), snippetData.getString("title"), snippetData.getString("description"),
                                mediumData.getString("url"), highData.getString("url"));

                        videoList.add(video);
                    }

                } catch (JSONException e) {
                    Log.i("YOUTUBE", e.toString());
                    return;
                }

                if (adapter == null) {
                    adapter = new VideoAdapter(MainActivity.this, videoList);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "서버에러발생", Toast.LENGTH_SHORT).show();
            }
        }
        );
        progressBar.setVisibility(View.VISIBLE);
        queue.add(request);
    }

    private void addNetworkData() {
        String URL = Config.BASE_URL + Config.PATH+"?key="+Config.API_KEY+"&part=snippet&q="+keyword+"&maxResults=20&type=video&pageToken="+pageToken;

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    progressBar.setVisibility(View.GONE);
                    pageToken = response.getString("nextPageToken");

                    JSONArray responseData = response.getJSONArray("items");
                    for (int i = 0; i < responseData.length(); i++) {
                        JSONObject data = responseData.getJSONObject(i);

                        JSONObject idData = data.getJSONObject("id");
                        JSONObject snippetData = data.getJSONObject("snippet");
                        JSONObject thumbnailsData = snippetData.getJSONObject("thumbnails");
                        JSONObject mediumData = thumbnailsData.getJSONObject("medium");
                        JSONObject highData = thumbnailsData.getJSONObject("high");

                        Video video = new Video(idData.getString("videoId"), snippetData.getString("title"), snippetData.getString("description"),
                                mediumData.getString("url"), highData.getString("url"));

                        videoList.add(video);
                    }

                } catch (JSONException e) {
                    return;
                }
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "서버에러발생", Toast.LENGTH_SHORT).show();
            }
        }
        );
        progressBar.setVisibility(View.VISIBLE);
        queue.add(request);
    }

    // 웹브라우저 실행시키는 메소드드
   public void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}