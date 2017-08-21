package tk.internet.praktikum.foursquare.search;


import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tk.internet.praktikum.foursquare.R;


public class FastSearchFragment extends Fragment {

    private List<Category> _categories;
    private List<String> categoriesAsList;
    private Map<String, List> categoriesMap;
    private RecyclerView rv_fast_search;


    private void initCategories() {
        categoriesAsList = new ArrayList<>();
        categoriesAsList.add(getCategory(R.string.category_1));
        categoriesAsList.add(getCategory(R.string.category_2));
        categoriesAsList.add(getCategory(R.string.category_3));
        categoriesAsList.add(getCategory(R.string.category_4));
        categoriesAsList.add(getCategory(R.string.category_5));

        categoriesMap= new HashMap<String, List>();
        categoriesMap.put(categoriesAsList.get(0), getCategoryItems(R.array.food_drink));
        categoriesMap.put(categoriesAsList.get(1), getCategoryItems(R.array.holiday_relaxation));
        categoriesMap.put(categoriesAsList.get(2), getCategoryItems(R.array.service));
        categoriesMap.put(categoriesAsList.get(3), getCategoryItems(R.array.shops));
        categoriesMap.put(categoriesAsList.get(4), getCategoryItems(R.array.infrastructure));

    }

    private String getCategory(int id) {
        return getContext().getResources().getString(id);
    }

    private List getCategoryItems(int id) {
        TypedArray categoryResources = getContext().getResources().obtainTypedArray(id);

        Map<String, Drawable> itemsMap = new HashMap<String, Drawable>();
        List<String> itemsName=new ArrayList<>();
        for (int i = 0; i < categoryResources.length(); i++) {
            int resId = categoryResources.getResourceId(i, -1);
            if (resId < 0) {
                continue;
            }
            TypedArray category_items= getContext().getResources().obtainTypedArray(resId);
           /* Drawable icon = category_items.getDrawable(category_items.getResourceId(0,-1));
            int itemNameResId=category_items.getResourceId(1,-1);*/
            String[] itemsArray=getContext().getResources().getStringArray(resId);
            String itemName = itemsArray[1];
            System.out.println("resource: "+itemsArray[0]);
            Drawable icon = getContext().getDrawable(getContext().getResources().getIdentifier(itemsArray[0], "drawable", getContext().getPackageName()));
            itemsMap.put(itemName, icon);
            itemsName.add(itemName);
        }
        List categoryItems=new ArrayList<>();
        categoryItems.add(itemsName);
        categoryItems.add(itemsMap);
        return categoryItems;
    }

    private void createContent() {
        _categories = new ArrayList<Category>();
        for (String categoryName: categoriesAsList) {
            Map<String, Drawable> elements = (Map<String, Drawable>) categoriesMap.get(categoryName).get(1);
            List<String> elementsAsList = (List<String>) categoriesMap.get(categoryName).get(0);
            Log.d(FastSearchFragment.class.getSimpleName(), "Elements: " + elements.toString() + "     " + " as List: " + elementsAsList);
            _categories.add(new Category(categoryName, elements, elementsAsList));
            Log.d(FastSearchFragment.class.getSimpleName(), "Categories");
            Log.d(FastSearchFragment.class.getSimpleName(), _categories.toString());
        }
    }

    CategoryAdapter adapter;
    TextView hotelsquare;
    View view;

    public FastSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fast_search, container, false);
        initCategories();
        createContent();
        Log.d(FastSearchFragment.class.getSimpleName(), "Categories");
        Log.d(FastSearchFragment.class.getSimpleName(), _categories.toString());

        rv_fast_search = (RecyclerView) view.findViewById(R.id.rv_fast_search);

        rv_fast_search.setHasFixedSize(true);
        adapter = new CategoryAdapter(_categories);
        rv_fast_search.setAdapter(adapter);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv_fast_search.setLayoutManager(llm);


        //searchView=(SearchView)view.findViewById(R.id.fast_search);
        setHasOptionsMenu(true);
        /**for (int i=1;i<10;i++){
         String buttonId=PREFIX_SUGGESTION+i;
         Button button = (Button) view.findViewById(getResources().getIdentifier(buttonId,"id",getActivity().getPackageName().toString()));
         button.setOnClickListener(v->deepSearch(button.getText().toString()));
         }*/

        Typeface type = Typeface.createFromAsset(getContext().getAssets(), "fonts/Pacifico.ttf");
        hotelsquare = (TextView) view.findViewById(R.id.hotelsquare);
        hotelsquare.setTypeface(type);

        return view;
    }

    private void deepSearch(String keyWord) {


        // also gets the suggested value from 9 categories
        Fragment fragment = new DeepSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString("keyword", keyWord);
        fragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        inflater.inflate(R.menu.search_view, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getResources().getString(R.string.searching_question));

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        // adapter.setFilter(mCountryModel);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        deepSearch("");
                        return true; // Return true to expand action view
                    }
                });
    }

    private static class Category {

        private String category_name;
        private Map<String, Drawable> category_elements;
        private List<String> category_elements_as_list;

        public Category(String category_name, Map<String, Drawable> category_elements, List<String> category_elements_as_list) {
            this.category_name = category_name;
            this.category_elements = category_elements;
            this.category_elements_as_list = category_elements_as_list;
        }

        public String getCategoryName() {
            return category_name;
        }

        public void setCategoryName(String category_name) {
            this.category_name = category_name;
        }

        public Map<String, Drawable> getCategoryElements() {
            return category_elements;
        }

        public void setCategoryElements(Map<String, Drawable> category_elements) {
            this.category_elements = category_elements;
        }

        public List<String> getCategoryElementsAsList() {
            return category_elements_as_list;
        }

        public void setCategoryElementsAsList(List<String> category_elements_as_list) {
            this.category_elements_as_list = category_elements_as_list;
        }
    }


    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {
        private List<Category> data;

        class CategoryHolder extends RecyclerView.ViewHolder {
            TextView tv_category, tv1, tv2, tv3;
            ImageView iv1, iv2, iv3;
            LinearLayout ll1, ll2, ll3;

            CategoryHolder(View view) {
                super(view);
                this.tv_category = (TextView) view.findViewById(R.id.tv_category);
                this.tv1 = (TextView) view.findViewById(R.id.tv_fast_search1);
                this.tv2 = (TextView) view.findViewById(R.id.tv_fast_search2);
                this.tv3 = (TextView) view.findViewById(R.id.tv_fast_search3);
                this.iv1 = (ImageView) view.findViewById(R.id.iv_fast_search1);
                this.iv2 = (ImageView) view.findViewById(R.id.iv_fast_search2);
                this.iv3 = (ImageView) view.findViewById(R.id.iv_fast_search3);
                this.ll1 = (LinearLayout) view.findViewById(R.id.ll_1);
                this.ll2 = (LinearLayout) view.findViewById(R.id.ll_2);
                this.ll3 = (LinearLayout) view.findViewById(R.id.ll_3);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public CategoryAdapter(List<Category> categorydata) {
            data = categorydata;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public CategoryAdapter.CategoryHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fast_search_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            CategoryHolder vh = new CategoryHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, final int position) {
            Log.d(FastSearchFragment.class.getSimpleName(), "Data size: " + data.size());
            Log.d(FastSearchFragment.class.getSimpleName(), "Data as array size: " + data.get(position).getCategoryElementsAsList().toString());
            holder.tv_category.setText(data.get(position).category_name);
            holder.tv1.setText(data.get(position).getCategoryElementsAsList().get(0));
            holder.tv2.setText(data.get(position).getCategoryElementsAsList().get(1));
            holder.tv3.setText(data.get(position).getCategoryElementsAsList().get(2));
            holder.iv1.setImageDrawable(data.get(position).getCategoryElements().get(data.get(position).getCategoryElementsAsList().get(0)));
            holder.iv2.setImageDrawable(data.get(position).getCategoryElements().get(data.get(position).getCategoryElementsAsList().get(1)));
            holder.iv3.setImageDrawable(data.get(position).getCategoryElements().get(data.get(position).getCategoryElementsAsList().get(2)));
            holder.ll1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deepSearch(holder.tv1.getText().toString());
                }
            });
            holder.ll2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deepSearch(holder.tv2.getText().toString());
                }
            });
            holder.ll3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deepSearch(holder.tv3.getText().toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

}
