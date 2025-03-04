package com.example.apicaller.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.apicaller.models.Article;

import java.util.List;

@Dao
public interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertArticles(List<Article> articles);

    @Query("SELECT * FROM article")
    List<Article> getAllArticles();
}
