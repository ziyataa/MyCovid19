package com.example.covid19

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.covid19.network.InfoCountry
import com.example.covid19.network.InfoService
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.activity_chart_country.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ChartCountryActivity : AppCompatActivity() {

    private var sharedPrefFile = "kotlinsharedpreferences"
    private lateinit var sharedPreferences: SharedPreferences
    private var dayCases = ArrayList<String>()

    companion object{
        const val EXTRA_COUNTRY = "country"
        const val EXTRA_LATEST_UPDATE = "date"
        const val EXTRA_COUNTRY_CODE = "country_code"
        const val EXTRA_NEW_DEATHS = "new_deaths"
        const val EXTRA_NEW_CONFIRMED = "new_confirmed"
        const val EXTRA_NEW_RECOVERED = "new_recovered"
        const val EXTRA_TOTAL_CONFIRMED = "total_confirmed"
        const val EXTRA_TOTAL_DEATHS = "total_deaths"
        const val EXTRA_TOTAL_RECOVERED = "total_recovered"

        lateinit var dataCountry : String
        lateinit var dataFlag : String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_country)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val country = intent.getStringExtra(EXTRA_COUNTRY)
        val date = intent.getStringExtra(EXTRA_NEW_DEATHS)
        val countryCode = intent.getStringExtra(EXTRA_COUNTRY_CODE)
        val  newDeaths = intent.getStringExtra(EXTRA_NEW_DEATHS)
        val  newConfirmed = intent.getStringExtra(EXTRA_NEW_CONFIRMED)
        val  newRecovered = intent.getStringExtra(EXTRA_NEW_RECOVERED)
        val  totalConfirmed = intent.getStringExtra(EXTRA_TOTAL_CONFIRMED)
        val  totalDeaths = intent.getStringExtra(EXTRA_TOTAL_DEATHS)
        val  totalRecovered = intent.getStringExtra(EXTRA_TOTAL_RECOVERED)

        val formatter : NumberFormat = DecimalFormat("#,###")
        txt_country_chart.text = country
        txt_current.text = date

        txt_total_confirmed_current.text = formatter.format(totalConfirmed?.toDouble())
        txt_new_confirmed_current.text =formatter.format(newConfirmed?.toDouble())
        txt_total_deaths_current.text = formatter.format(totalDeaths?.toDouble())

        txt_new_deaths_current.text = formatter.format(newDeaths?.toDouble())
        txt_recovered_current.text = formatter.format(newRecovered?.toDouble())
        txt_total_recovered_current.text = formatter.format(totalRecovered?.toDouble())

        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString(country, country)
        editor.apply()  // untuk menyimpan data yang telah di taruh
        editor.commit() // untuk menerapkan SharedPreference

        //Menampung data yang telah tersimpan ke dalam varible saveDataCountry
        val saveDataCountry = sharedPreferences.getString(country, country)
        val saveCountryFlag = sharedPreferences.getString(countryCode, countryCode)
        dataCountry = saveDataCountry.toString() // Convert data to String
        dataFlag = saveCountryFlag.toString() + "/flat/64.png"

        if (saveCountryFlag != null){
            Glide.with(this).load("https://www.countryflags.io/$dataFlag")
                .into(img_flag_country)
        } else {
            Toast.makeText(this, "Image not Found.", Toast.LENGTH_SHORT).show()
        }

        getCountry()
    }

    private fun getCountry(){
        val okHttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(dataCountry).enqueue(object : Callback<List<InfoCountry>>{
            override fun onFailure(call: Call<List<InfoCountry>>, t: Throwable) {

            }
            override fun onResponse(
                call: Call<List<InfoCountry>>,response: Response<List<InfoCountry>>) {
                if (response.isSuccessful) {
                    val dataCovid = response.body()
                    val barEntries1: ArrayList<BarEntry> = ArrayList()
                    val barEntries2: ArrayList<BarEntry> = ArrayList()
                    val barEntries3: ArrayList<BarEntry> = ArrayList()
                    val barEntries4: ArrayList<BarEntry> = ArrayList()
                    var i = 0

                    while (i < dataCovid?.size ?: 0) {
                        for (s in dataCovid!!) {
                            val barEntry1 = BarEntry(i.toFloat(), s.Confirmed.toFloat())
                            val barEntry2 = BarEntry(i.toFloat(), s.Deaths.toFloat())
                            val barEntry3 = BarEntry(i.toFloat(), s.Recovered.toFloat())
                            val barEntry4 = BarEntry(i.toFloat(), s.Active.toFloat())

                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'")
                            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                            val date: Date? = inputFormat.parse(s.Date)
                            val formattedDate: String = outputFormat.format(date!!)
                            dayCases.add(formattedDate)

                            barEntries1.add(barEntry1)
                            barEntries2.add(barEntry2)
                            barEntries3.add(barEntry3)
                            barEntries4.add(barEntry4)

                            i++
                        }
                    }

                    val xAxis: XAxis = chart_view.xAxis
                    xAxis.valueFormatter = IndexAxisValueFormatter(dayCases)
                    chart_view.axisLeft.axisMinimum = 0f
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setCenterAxisLabels(true)
                    xAxis.isGranularityEnabled = true

                    val barDataSet1 = BarDataSet(barEntries1, "Confirmed")
                    val barDataSet2 = BarDataSet(barEntries2, "Deaths")
                    val barDataSet3 = BarDataSet(barEntries3, "Recovered")
                    val barDataSet4 = BarDataSet(barEntries4, "Active")

                    barDataSet1.setColors(Color.parseColor("#FF5722"))
                    barDataSet2.setColors(Color.parseColor("#FFEB3B"))
                    barDataSet3.setColors(Color.parseColor("#84FFFF"))
                    barDataSet4.setColors(Color.parseColor("#FF448AFF"))

                    val data = BarData(barDataSet1, barDataSet2, barDataSet3, barDataSet4)
                    chart_view.data = data

                    val barSpace = 0.02f
                    val groupSpace = 0.3f
                    val groupCount = 4f

                    data.barWidth = 0.15f
                    chart_view.invalidate()
                    chart_view.setNoDataTextColor(android.R.color.black)
                    chart_view.setTouchEnabled(true)
                    chart_view.description.isEnabled = false

                    // TODO AXIS MINIMUM
                    chart_view.xAxis.axisMinimum = 0f
                    chart_view.setVisibleXRangeMaximum(0f + chart_view.barData.getGroupWidth(groupSpace, barSpace) * groupCount)
                    chart_view.groupBars(0f, groupSpace, barSpace)
                }
            }
        })
    }
}