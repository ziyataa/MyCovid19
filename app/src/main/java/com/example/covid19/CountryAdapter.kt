package com.example.covid19

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.covid19.network.Countries
import kotlinx.android.synthetic.main.list_country.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class CountryAdapter(val country: ArrayList<Countries>, val clickListener: (Countries) -> Unit):
    RecyclerView.Adapter<CountryAdapter.CountryViewHolder>(), Filterable {

    var countryFilterList = ArrayList<Countries>()
    init {
        countryFilterList = country
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_country, parent, false))
    }

    override fun getItemCount(): Int = countryFilterList.size

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bindItem(countryFilterList[position], clickListener)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                countryFilterList = if (charSearch.isEmpty()) {
                    country
                } else {
                    val resultList = ArrayList<Countries>()
                    for (row in country){
                        if (row.Country.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(
                                Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }

                val filterResult = FilterResults()
                filterResult.values = countryFilterList
                return filterResult
            }


            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                countryFilterList = results?.values as ArrayList<Countries>
                notifyDataSetChanged()
            }

        }
    }

    class CountryViewHolder (view : View): RecyclerView.ViewHolder(view){
        val tvCountry = itemView.findViewById<TextView>(R.id.txt_country_name)
        val tvTotalCase = itemView.findViewById<TextView>(R.id.txt_total_case)
        val tvTotalDeath = itemView.findViewById<TextView>(R.id.txt_total_deaths)
        val tvTotalRecorved = itemView.findViewById<TextView>(R.id.txt_total_recovered)
        val imgFlag = itemView.findViewById<ImageView>(R.id.img_flag_circle)

        fun bindItem(countries: Countries, clickListener: (Countries) -> Unit) {
            val formatter : NumberFormat = DecimalFormat("#,###")
            tvCountry.txt_country_name.text = countries.Country
            tvTotalCase.txt_total_case.text = formatter.format(countries.TotalConfirmed.toDouble())
            tvTotalRecorved.txt_total_recovered.text = formatter.format(countries.TotalRecovered.toDouble())
            tvTotalDeath.txt_total_deaths.text = formatter.format(countries.TotalDeaths.toDouble())
            Glide.with(itemView.context)
                .load("https://www.countryflags.io/" + countries.CountryCode + "/flat/16.png")
                .into(imgFlag)

            itemView.setOnClickListener {
                clickListener(countries)
            }
        }
    }


}