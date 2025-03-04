package com.example.apicaller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apicaller.components.ArticleAdapter;
import com.example.apicaller.models.Article;
import com.example.apicaller.dao.AppDatabase;
import com.example.apicaller.models.NewsResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ArticleAdapter articleAdapter;
    private AppDatabase db;
    private EditText searchEditText;
    private Button searchButton;
    private Spinner countrySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        countrySpinner = findViewById(R.id.countrySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.country_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        articleAdapter = new ArticleAdapter();
        recyclerView.setAdapter(articleAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString().trim();
                String country = countrySpinner.getSelectedItem().toString();
                if (!query.isEmpty()) {
                    fetchAndStoreArticlesWithQuery(query);
                } else {
                    fetchAndStoreArticles(country);
                }
            }
        });

        loadArticlesFromDb();
    }

    private void fetchAndStoreArticlesWithQuery(String query) {
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);
        Call<NewsResponse> call = apiService.getEverything(query, Constants.API_KEY);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();
                    new Thread(() -> {
                        db.articleDao().insertArticles(articles);
                        runOnUiThread(() -> articleAdapter.setArticles(articles));
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void fetchAndStoreArticles(String country) {
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);
        Call<NewsResponse> call = apiService.getTopHeadlines(country, Constants.API_KEY);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();
                    new Thread(() -> {
                        db.articleDao().insertArticles(articles);
                        runOnUiThread(() -> articleAdapter.setArticles(articles));
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void loadArticlesFromDb() {
        new Thread(() -> {
            List<Article> articles = db.articleDao().getAllArticles();
            if (articles.isEmpty()) {
                fetchAndStoreArticles("us");
            } else {
                runOnUiThread(() -> articleAdapter.setArticles(articles));
            }
        }).start();
    }
}