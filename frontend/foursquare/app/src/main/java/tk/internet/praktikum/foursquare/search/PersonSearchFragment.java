package tk.internet.praktikum.foursquare.search;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wanderingcan.persistentsearch.PersistentSearchView;
import com.wanderingcan.persistentsearch.SearchMenu;
import com.wanderingcan.persistentsearch.SearchMenuItem;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.CommentAdapter;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.UserSearchQuery;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;


public class PersonSearchFragment extends Fragment implements PersistentSearchView.OnSearchListener, PersistentSearchView.OnMenuItemClickListener {

    public static final String URL = "https://dev.ip.stimi.ovh";
    private static final String TAG = PersonSearchFragment.class.getName();
    private PersistentSearchView searchView;
    private RecyclerView personRecycler;
    private String lastTerm;
    private PersonSearchResultAdapter personSearchResultAdapter;

    public PersonSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_person_search, container, false);
        searchView = (PersistentSearchView) view.findViewById(R.id.search_bar);
        personRecycler = (RecyclerView) view.findViewById(R.id.person_recycler);

        searchView.setOnSearchListener(this);
        searchView.setOnMenuItemClickListener(this);


        personSearchResultAdapter = new PersonSearchResultAdapter(getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());

        personRecycler.setNestedScrollingEnabled(false);
        personRecycler.setLayoutManager(mLayoutManager);
        personRecycler.setItemAnimator(new DefaultItemAnimator());
        personRecycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        personRecycler.setAdapter(personSearchResultAdapter);

        return view;
    }

    @Override
    public void onSearchOpened() {

    }

    @Override
    public void onSearchClosed() {

    }

    @Override
    public void onSearchCleared() {

    }

    @Override
    public void onSearchTermChanged(CharSequence term) {
        SearchMenu menu = searchView.getSearchMenu();
        if (lastTerm == null || !term.toString().startsWith(lastTerm) && !lastTerm.startsWith(term.toString()))
            menu.clearItems();
        lastTerm = term.toString();
        if (term.length() < 3)
            return;

        try {
            SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getActivity().getApplicationContext());
            String token = sharedPreferences.getString(Constants.TOKEN, "");
            UserService userService = ServiceFactory.createRetrofitService(UserService.class, URL, token);
            UserSearchQuery userSearchQuery = new UserSearchQuery(term.toString());
            userService.search(userSearchQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        int i = 0;
                        menu.clearItems();
                        for (User user : result) {
                            menu.addSearchMenuItem(i++, user.getDisplayName());
                            if(i > 5)
                                break;
                        }
                    }, throwable -> Log.d(TAG, throwable.toString(), throwable));
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        }
    }

    @Override
    public void onSearch(CharSequence text) {
        ProgressDialog pd = ProgressDialog.show(getContext(),"Searching", "Spy is looking for persons", true);
        searchView.closeSearch();
        try {
            SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getActivity().getApplicationContext());
            String token = sharedPreferences.getString(Constants.TOKEN, "");
            UserService userService = ServiceFactory.createRetrofitService(UserService.class, URL, token);
            UserSearchQuery userSearchQuery = new UserSearchQuery(text.toString());
            userService.search(userSearchQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(results -> {
                        pd.dismiss();
                        personSearchResultAdapter.setResults(results);
                    }, throwable -> {
                        pd.dismiss();
                        Log.d(TAG, throwable.toString(), throwable);
                    });
        } catch (Exception e) {
            pd.dismiss();
            Log.d(TAG, e.toString(), e);
        }
    }

    @Override
    public void onMenuItemClick(SearchMenuItem item) {
        searchView.populateSearchText(item.getTitle());
        onSearch(item.getTitle());
    }
}
