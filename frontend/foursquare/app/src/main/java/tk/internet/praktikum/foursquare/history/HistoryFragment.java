package tk.internet.praktikum.foursquare.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.storage.LocalDataBaseManager;
import tk.internet.praktikum.foursquare.storage.LocalStorage;


public class HistoryFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private HistoryRecyclerViewAdapter historyRecyclerViewAdapter;
    public HistoryFragment() {
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
        view = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.history_result);
        this.setRetainInstance(true);
        return view;

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager( new LinearLayoutManager(getActivity()));
        List<HistoryEntry> historyEntries= LocalDataBaseManager.getLocalDatabaseManager(getContext()).getDaoSession().getHistoryEntryDao()
                .queryBuilder().orderDesc(HistoryEntryDao.Properties.Date).list();
        LocalStorage ls = LocalStorage.getLocalStorageInstance(getContext());
        SharedPreferences sp = LocalStorage.getSharedPreferences(getContext());
        if (!ls.isLoggedIn()) {
            Toast.makeText(getContext(), "You have to login to see history", Toast.LENGTH_SHORT).show();
        }
        else {
            updateRecyclerView(historyEntries);
        }
    }

    protected void updateRecyclerView(List<HistoryEntry> historyEntries) {
            historyRecyclerViewAdapter=new HistoryRecyclerViewAdapter(historyEntries);
            historyRecyclerViewAdapter.setContext(getContext());
            recyclerView.setAdapter(historyRecyclerViewAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
    }
}
