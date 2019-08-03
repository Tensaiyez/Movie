package com.example.tensaiye.popularmovie.Activity;

import android.app.ActivityOptions;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

//import com.android.support:design:28.0.0;
import com.example.tensaiye.popularmovie.API.RetrofitService;
import com.example.tensaiye.popularmovie.API.ServiceInterface;
import com.example.tensaiye.popularmovie.Adapters.CastAdapter;
import com.example.tensaiye.popularmovie.Adapters.FavoriteAdapter;
import com.example.tensaiye.popularmovie.Adapters.ReviewAdapter;
import com.example.tensaiye.popularmovie.Adapters.TrailerAdapter;
import com.example.tensaiye.popularmovie.Models.BasicCredit;
import com.example.tensaiye.popularmovie.Models.BasicReview;
import com.example.tensaiye.popularmovie.Models.BasicTrailer;
import com.example.tensaiye.popularmovie.Constants;
import com.example.tensaiye.popularmovie.Database.FavoriteEntry;
import com.example.tensaiye.popularmovie.Database.MovieDatabase;
import com.example.tensaiye.popularmovie.Models.Credit;
import com.example.tensaiye.popularmovie.Models.Movie;
import com.example.tensaiye.popularmovie.MovieExecutors;
import com.example.tensaiye.popularmovie.R;
import com.example.tensaiye.popularmovie.Models.Review;
import com.example.tensaiye.popularmovie.Models.Trailer;
import com.squareup.picasso.Picasso;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Movie movies;
    TextView mTitle;
    TextView mUserRating;
    TextView mReleaseDate;
    TextView mDescription;
    TextView mVote;
    TextView mReviewContent;
    TextView mNoReview;
    FloatingActionButton mButton;
    String vote;
    String title;
    String releaseDate;
    String userRating;
    String overView;
    String poster;
    String id;
    String backdrop;
    private List<Review> reviewList = new ArrayList<>();
    private List<Trailer> TrailerList = new ArrayList<>();
    private List<FavoriteEntry> FavoriteList = new ArrayList<>();
    private List<Movie> MovieList = new ArrayList<>();
    private List<Credit> CastList=new ArrayList<>();

    private String Tag;
    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;
    private FavoriteAdapter mFavoriteAdapter;
    private CastAdapter mCastAdapter;
    private MovieDatabase mDb;
    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    private boolean isFavorite = false;
    private boolean isfav = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final RecyclerView recyclerView2 = (RecyclerView) findViewById(R.id.Favorite_rv);
        recyclerView2.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
        collapsingToolbarLayout = (net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.AppBar);
        mFavoriteAdapter = new FavoriteAdapter(FavoriteList, this,MovieList);
        recyclerView2.setAdapter(mFavoriteAdapter);



        Toolbar mtoolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




//          The following code allows the collapsingtoolbar title to be set only after it is collapsed. Reference:https://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed/32724422

//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            boolean check = false;
//            int Range = -1;
//
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//                if (Range == -1) {
//                    Range = appBarLayout.getTotalScrollRange();
//                }
//                if (Range + i == 0) {
//                    collapsingToolbarLayout.setTitle("     " + title);
//                    check = true;
//                } else if (check) {
//                    collapsingToolbarLayout.setTitle(" ");
//                    check = false;
//                }
//            }
//        });


        mNoReview = (TextView) findViewById(R.id.NoReview_tv);
        mButton = (FloatingActionButton) findViewById(R.id.DetailsaveButton);


        mButton.setOnClickListener(this);
        if (savedInstanceState == null || !savedInstanceState.containsKey(Constants.BUNDLE_KEY)) {
            Intent intent = getIntent();
            if (intent.hasExtra("original_name")) {
                title = intent.getStringExtra("original_name");
                releaseDate = intent.getStringExtra("release_date");
                userRating = intent.getStringExtra("user_rating");
                overView = intent.getStringExtra("overview");
                poster = intent.getStringExtra("backdrop_path");
                id = intent.getStringExtra("id");
                backdrop = intent.getStringExtra("poster_image");
                vote=intent.getStringExtra("vote_count");


                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                FetchReviews(id);
                FetchTrailer(id);
                FetchCast(id);


                populateUI(movies);

            }
            mDb = MovieDatabase.getInstance(getApplicationContext());

            collapsingToolbarLayout.setTitle(title);
            final Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/delilafreebold.ttf");
            collapsingToolbarLayout.setCollapsedTitleTypeface(tf);
            collapsingToolbarLayout.setExpandedTitleTypeface(tf);
            initializeFavoriteButton(id);


        }

    }


    public void SaveFavorite() {
//        String title= movies.getOriginalName();
//        String id=movies.getId();
//        String poster=movies.getPosterImage();
        final FavoriteEntry favoriteEntry = new FavoriteEntry(title, id, backdrop,userRating);
        MovieExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.favoriteDao().insertFavorite(favoriteEntry);


            }
        });

    }
    private void FetchCast(String movie_id){
        RetrofitService retrofitService = new RetrofitService();
        ServiceInterface serviceInterface = retrofitService.getRetrofit().create(ServiceInterface.class);
        Call<BasicCredit> call=serviceInterface.getCredit(movie_id,Constants.API_KEY);
        call.enqueue(new Callback<BasicCredit>() {
            @Override
            public void onResponse(Call<BasicCredit> call, Response<BasicCredit> response) {
                final RecyclerView recyclerView=(RecyclerView)findViewById(R.id.Cast_rv);
                recyclerView.setLayoutManager(new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                CastList=response.body().getCast();
                mCastAdapter=new CastAdapter(CastList,getApplicationContext());
                recyclerView.setAdapter(mCastAdapter);
            }

            @Override
            public void onFailure(Call<BasicCredit> call, Throwable throwable) {

            }
        });
    }

    private void FetchReviews(String movie_id) {
        RetrofitService retrofitService = new RetrofitService();
        ServiceInterface serviceInterface = retrofitService.getRetrofit().create(ServiceInterface.class);
        Call<BasicReview> call = serviceInterface.getReviews(movie_id, Constants.API_KEY);
        call.enqueue(new Callback<BasicReview>() {
            @Override
            public void onResponse(Call<BasicReview> call, Response<BasicReview> response) {
                final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.Review_rv);
                recyclerView.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
                reviewList = response.body().getResults();
                if (reviewList.isEmpty()) {
                    mNoReview.setVisibility(View.VISIBLE);
                }
                mReviewAdapter = new ReviewAdapter(reviewList, getApplicationContext());
                recyclerView.setAdapter(mReviewAdapter);

            }

            @Override
            public void onFailure(Call<BasicReview> call, Throwable t) {

            }
        });
    }

    private void FetchTrailer(String movie_id) {
        RetrofitService retrofitService = new RetrofitService();
        ServiceInterface serviceInterface = retrofitService.getRetrofit().create(ServiceInterface.class);
        Call<BasicTrailer> call = serviceInterface.getTrailer(movie_id, Constants.API_KEY);
        call.enqueue(new Callback<BasicTrailer>() {
            @Override
            public void onResponse(Call<BasicTrailer> call, Response<BasicTrailer> response) {
                final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.Trailer_rv);
                recyclerView.setLayoutManager(new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                TrailerList = response.body().getResults();
                mTrailerAdapter = new TrailerAdapter(TrailerList, getApplicationContext(), poster);
                recyclerView.setAdapter(mTrailerAdapter);

            }

            @Override
            public void onFailure(Call<BasicTrailer> call, Throwable t) {

            }
        });
    }

    private void populateUI(Movie movie) {

        ImageView Imageshown = findViewById(R.id.Poster_tv);
        ImageView DetailPortrait = findViewById(R.id.detail_portrait);

        mTitle = (TextView) findViewById(R.id.Original_tv);
        mDescription = (TextView) findViewById(R.id.Overview_tv);
        mUserRating = (TextView) findViewById(R.id.Vote_tv);
        mReleaseDate = (TextView) findViewById(R.id.Release_tv);
        mReviewContent = (TextView) findViewById(R.id.Reviews_tv);
        mVote=(TextView) findViewById(R.id.vote_count_tv);

        mTitle.setText(title);
        mDescription.setText(overView);
        mUserRating.setText(userRating);
        mReleaseDate.setText(releaseDate);
        mVote.setText(vote);

        Picasso.with(this).load(poster).into(Imageshown);
        Picasso.with(this).load(backdrop).into(DetailPortrait);
    }

    private void Delete() {
        final FavoriteEntry favoriteEntry = new FavoriteEntry(title, id, backdrop,userRating);
        mDb.favoriteDao().deleteFavorite(id);
    }

    private void initializeFavoriteButton(final String movieId) {
        final LiveData<List<String>> idList = mDb.favoriteDao().loadFavoriteById(movieId);
        idList.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> strings) {
                idList.removeObserver(this);
                if (strings.isEmpty()) {
                    isFavorite = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButton.setColorFilter(Color.WHITE);
                        }
                    });
                } else {
                    isFavorite = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButton.setColorFilter(Color.RED);
                        }
                    });
                }
            }
        });
    }


    private void favoriteButtonHandler(String movieId) {
        if (isFavorite) {
            Log.d(Tag, "deleting favorite");
            Delete();

            isFavorite = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mButton.setColorFilter(Color.WHITE);
                    Snackbar.make(findViewById(R.id.coordinate), "Movie deleted from Favorites", Snackbar.LENGTH_LONG).show();
                }
            });


        } else {
            SaveFavorite();

            isFavorite = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mButton.setColorFilter(Color.RED);
                    Snackbar.make(findViewById(R.id.coordinate), "Movie added to Favorites", Snackbar.LENGTH_LONG).show();
                }
            });

        }

    }


    @Override
    public void onClick(View v) {
//        if (v.getId()==R.id.DetailsaveButton) {
//            MovieExecutors.getInstance().diskIO().execute(new Runnable() {
//                @Override
//                public void run() {
//                    favoriteButtonHandler(id);
//
//                    if(isfav){
//                        mButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
//                    }else{
//                        mButton.setImageResource(R.drawable.ic_favorite_black_24dp);
//                    }
//
//                    isfav = !isfav; // reverse
//                }
//            });
//        }
        switch (v.getId()) {
            case R.id.DetailsaveButton:
                MovieExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        favoriteButtonHandler(id);
                    }
                });
                break;

        }

    }
}