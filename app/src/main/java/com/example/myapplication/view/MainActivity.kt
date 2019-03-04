package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.data.places.search.Place
import com.example.myapplication.model.storage.FavoritePlacesManager
import com.example.myapplication.view.adapter.SearchViewAdapter
import com.example.myapplication.viewmodel.SearchViewModel
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var favoritePlacesManager: FavoritePlacesManager

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var searchViewModel: SearchViewModel
    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        searchViewModel =
            ViewModelProviders
                .of(this, viewModelFactory)
                .get(SearchViewModel::class.java)
        initFAB()
        searchViewModel.init()
        searchViewModel.placesLiveData.observe(this, Observer {
            Timber.d("List is loaded. Size = ${it.size}")
            if (it.isNotEmpty()) {
                noData.visibility = View.GONE
                placesList.visibility = View.VISIBLE
                placesList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                val searchViewAdapter = SearchViewAdapter(it, favoritePlacesManager)
                searchViewAdapter.onClickListener =
                    View.OnClickListener { v ->
                        val itemPosition = placesList.getChildLayoutPosition(v)
                        val intent = Intent(applicationContext, DetailActivity::class.java)
                        intent.putExtra(DetailActivity.PLACE_ID, it[itemPosition].id)
                        intent.putExtra(DetailActivity.DISTANCE_FROM_CENTER, it[itemPosition].distanceFromCenter)

                        startActivity(intent)
                    }
                placesList.adapter = searchViewAdapter
                floatingActionButton.visibility = View.VISIBLE
            } else {
                noData.visibility = View.VISIBLE
                floatingActionButton.visibility = View.GONE
            }
        })
        searchViewModel.loadingLiveData.observe(this, Observer {
            if (it) {
                loadingIndicator.visibility = View.VISIBLE
                placesList.visibility = View.GONE
                floatingActionButton.visibility = View.GONE
            } else {
                loadingIndicator.visibility = View.GONE
            }
        })
        searchViewModel.errorLiveData.observe(this, Observer {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            floatingActionButton.visibility = View.GONE
        })
    }

    private fun initFAB() {
        floatingActionButton.setOnClickListener {
            val intent = Intent(this@MainActivity, MapActivity::class.java)
            val values = ArrayList<Place>()
            val currentPlaces = (placesList.adapter as SearchViewAdapter).places
            values.addAll(currentPlaces)
            intent.putParcelableArrayListExtra(MapActivity.OBJECTS_KEY, values)
            startActivity(intent)
        }
    }

    override fun onResume() {
        if (placesList.adapter != null) {
            placesList.adapter!!.notifyDataSetChanged()
        }
        if (searchView != null) {
            searchView!!.clearFocus()
        }
        super.onResume()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem!!.actionView as SearchView
        searchView!!.maxWidth = Integer.MAX_VALUE
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()) {
                    Timber.d("Try to search $query")
                    searchViewModel.search(query)
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                Timber.d("onQueryTextChange $s")
                if (s.isNotEmpty()) {
                    firstPrompt.visibility = View.GONE
                } else {
                    firstPrompt.visibility = View.VISIBLE
                }
                if (s.length >= 2) {
                    searchViewModel.search(s)
                }
                return false
            }
        })
        return true
    }
}
