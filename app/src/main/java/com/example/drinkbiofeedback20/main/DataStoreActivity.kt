package com.example.drinkbiofeedback20.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drinkbiofeedback20.R
import com.example.drinkbiofeedback20.main.database.DrinkDataBase
import com.example.drinkbiofeedback20.main.database.DrinkViewModel
import com.example.drinkbiofeedback20.main.database.DrinkVolumeViewModelFactory
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*

class DataStoreActivity : FragmentActivity() {

    private lateinit var mDrinkViewModel: DrinkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_list)
        actionBar!!.title = "Database"
       val adapter = ListAdapter()
        val recyclerView = recyclerview
        recyclerView.adapter = adapter
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val application = requireNotNull(this@DataStoreActivity).application

        val dataSource = DrinkDataBase.getInstance(application).drinkDataBaseDao()

        val viewModelFactory = DrinkVolumeViewModelFactory(dataSource, application)
        // UserViewModel
        mDrinkViewModel = ViewModelProvider(this, viewModelFactory).get(DrinkViewModel::class.java)
        mDrinkViewModel.getAllData.observe(this, Observer { volume ->
            adapter.setData(volume)
        })
      /*  mDrinkViewModel.getAllData.observe(life, Observer { volume ->
            adapter.setData(volume)
        })*/
    }
}
