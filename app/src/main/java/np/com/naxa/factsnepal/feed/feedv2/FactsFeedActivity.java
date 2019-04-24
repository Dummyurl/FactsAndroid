package np.com.naxa.factsnepal.feed.feedv2;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import androidx.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.button.MaterialButton;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.recyclerview.widget.SnapHelper;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.littlemango.stacklayoutmanager.StackLayoutManager;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import np.com.naxa.factsnepal.R;
import np.com.naxa.factsnepal.common.BaseActivity;
import np.com.naxa.factsnepal.common.Constant;
import np.com.naxa.factsnepal.common.ItemOffsetDecoration;
import np.com.naxa.factsnepal.feed.Fact;
import np.com.naxa.factsnepal.feed.FactsLocalSource;
import np.com.naxa.factsnepal.feed.bookmarkedfacts.BookmarkedFactsActivity;
import np.com.naxa.factsnepal.feed.detail.FactDetailActivity;
import np.com.naxa.factsnepal.feed.dialog.BottomDialogFragment;
import np.com.naxa.factsnepal.feed.list.FactsFeedAdapter;
import np.com.naxa.factsnepal.feed.list.FactsRemoteSource;
import np.com.naxa.factsnepal.feed.list.resource.FactsRepo;
import np.com.naxa.factsnepal.notification.CountDrawable;
import np.com.naxa.factsnepal.notification.NotificationActivity;
import np.com.naxa.factsnepal.notification.NotificationCount;
import np.com.naxa.factsnepal.preferences.PreferencesActivity;
import np.com.naxa.factsnepal.publicpoll.PublicPollActivity;
import np.com.naxa.factsnepal.surveys.SurveyCompanyListActivity;
import np.com.naxa.factsnepal.utils.ActivityUtil;

public class FactsFeedActivity extends BaseActivity implements FactsFeedAdapter.OnFeedCardItemClickListener, View.OnClickListener, BottomDialogFragment.OnCategoriesSelectedListener {

    private View rootLayout;
    private FactsFeedAdapter adapter;
    private RecyclerView recyclerViewFeed;
    private DisposableObserver<List<Fact>> factsDisposable;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;
    private MaterialButton btnHome, btnPublicPoll, btnSurvey, btnBookmarked, btnAccount;
    private NotificationCount notificationCount;


    String colors[] = new String[]{"#571821", "#5C3219", "#103B31"};
    private Menu menu;
    private LiveData<List<Fact>> factsLiveData;
    private Handler uiFadeHanlder = new Handler();
    private Runnable uiFadeRunnable = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.layout_progress).setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facts_feed);
        notificationCount = new NotificationCount(this);

        bindUI();
        setupRecyclerView();
        factsLiveData = FactsRepo.getINSTANCE().getAllFacts(true);
        factsLiveData.observe(this, facts -> {
            fadeProgress();
            adapter.updateList(facts);
        });

        setUpToolbar();
        loadFacts();

    }

    private void fadeProgress() {
        runOnUiThread(() -> {
            findViewById(R.id.layout_progress).setVisibility(View.VISIBLE);
            uiFadeHanlder.postDelayed(uiFadeRunnable, 1000);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        factsDisposable.dispose();
    }

    private void loadFacts() {
        factsDisposable = FactsRemoteSource.getINSTANCE()
                .getAllFacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Fact>>() {
                    @Override
                    public void onNext(List<Fact> facts) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void bindUI() {
        recyclerViewFeed = findViewById(R.id.rv_facts);
        rootLayout = findViewById(R.id.root_activity_facts_feed);
        toolbar = findViewById(R.id.app_bar);

        btnAccount = findViewById(R.id.backdrop_account);
        btnBookmarked = findViewById(R.id.backdrop_bookmark);
        btnHome = findViewById(R.id.backdrop_home);
        btnPublicPoll = findViewById(R.id.backdrop_public_poll);
        btnSurvey = findViewById(R.id.backdrop_public_survey);

        btnAccount.setOnClickListener(this);
        btnBookmarked.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnPublicPoll.setOnClickListener(this);
        btnSurvey.setOnClickListener(this);
    }

    private void setupRecyclerView() {
        adapter = new FactsFeedAdapter(new ArrayList<>(), this);
        boolean useSnapLayout = false;

        if (useSnapLayout) {
            layoutManager = new LinearLayoutManager(this);
            recyclerViewFeed.setLayoutManager(layoutManager);
            /*
             * stackoverflow.com/questions/38247602/android-how-can-i-get-current-positon-on-recyclerview-that-user-scrolled-to-item
             */
            recyclerViewFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@androidx.annotation.NonNull @NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        int position = getCurrentItem();
                        getColorPallete(position);
                    }
                }
            });

            SnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerViewFeed);
        } else {
            StackLayoutManager manager = new StackLayoutManager(StackLayoutManager.ScrollOrientation.TOP_TO_BOTTOM, 2);
            recyclerViewFeed.setLayoutManager(manager);

            ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.margin_large);
            recyclerViewFeed.addItemDecoration(itemDecoration);
        }

        RecyclerView.ItemAnimator animator = recyclerViewFeed.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerViewFeed.setAdapter(adapter);


    }

    private void getColorPallete(int position) {
        Fact fact = adapter.getItems().get(position);
        if (fact != null) {
            String url = fact.getImagePath();

        }
    }

    /*
     *https://stackoverflow.com/questions/2614545/animate-change-of-view-background-color-on-android
     */
    private void onPageChanged(int position) {
        int lastPosition = position - 1;
        int colorTo = Color.parseColor(colors[lastPosition == -1 ? 0 : lastPosition]);
        int colorFrom = Color.parseColor(colors[position]);
        int duration = 1000;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

        colorAnimation.setDuration(1000);
        colorAnimation.addUpdateListener(animator -> rootLayout.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private int getCurrentItem() {
        return layoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public void onCardTap(Fact fact, ImageView imageView) {
        Intent intent = new Intent(this, FactDetailActivity.class);
        intent.putExtra(Constant.EXTRA_IMAGE, fact);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, imageView, getString(R.string.transtion_fact_list_details));
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onBookmarkButtonTap(Fact fact) {
        FactsLocalSource.getINSTANCE().toggleBookMark(fact)
                .subscribeOn(Schedulers.io())
                .subscribe();

        toast(fact.isBookmarked() ? getString(R.string.msg_not_bookmarked) : getString(R.string.msg_bookmared));
    }

    @Override
    public void onShareButtonTap(Fact fact) {
        ActivityUtil.openShareIntent(this, fact.getTitle());
    }


    private void setUpToolbar() {
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                this,
                findViewById(R.id.rv_facts),
                new AccelerateDecelerateInterpolator(),
                getResources().getDrawable(R.drawable.ic_expand_more_black_24dp), // Menu open icon
                getResources().getDrawable(R.drawable.ic_expand_less_black_24dp))); // Menu close icon
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            long count = notificationCount.getNotificationCount();
            setNotificationCount(this, count + "", menu);
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_notification:
                ActivityUtil.openActivity(NotificationActivity.class, this);
                break;

            case R.id.action_profile:
                try {
                    notificationCount.saveNotification(notificationCount.getJsonArray());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onPrepareOptionsMenu(menu);
                break;

            case R.id.action_categories:

                BottomDialogFragment bottomSheetDialog = BottomDialogFragment.getInstance(this);
                bottomSheetDialog.show(getSupportFragmentManager(), "Chips Dialog");

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setNotificationCount(Context context, String count, Menu defaultMenu) {
        MenuItem menuItem = defaultMenu.findItem(R.id.menu_notification);
        LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        CountDrawable badge;

        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_group_count);
        if (reuse instanceof CountDrawable) {
            badge = (CountDrawable) reuse;
        } else {
            badge = new CountDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_group_count, badge);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backdrop_home:
                break;
            case R.id.backdrop_account:
                ActivityUtil.openActivity(PreferencesActivity.class, this);
                break;
            case R.id.backdrop_public_poll:
                ActivityUtil.openActivity(PublicPollActivity.class, this);
                break;
            case R.id.backdrop_bookmark:
                ActivityUtil.openActivity(BookmarkedFactsActivity.class, this);
                break;
            case R.id.backdrop_public_survey:
                ActivityUtil.openActivity(SurveyCompanyListActivity.class, this);
                break;
        }
    }

    @Override
    public void onCategoriesSelected(List<Integer> categories) {
        fadeProgress();

        factsLiveData = FactsRepo.getINSTANCE().getByCategoryIds(categories, true);
        factsLiveData.observe(FactsFeedActivity.this, facts -> {
            adapter.updateList(facts);
        });

    }
}
