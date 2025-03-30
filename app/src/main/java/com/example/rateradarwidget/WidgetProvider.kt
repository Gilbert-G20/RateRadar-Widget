package com.example.rateradarwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Fetch data from API
        CoroutineScope(Dispatchers.IO).launch {
            val data = fetchCurrencyData()
            if (data != null) {
                views.setTextViewText(R.id.dollar_value, "USD: ${data["dolar"]}")
                views.setTextViewText(R.id.euro_value, "EUR: ${data["euro"]}")
            }

            // Update all widgets
            val componentName = ComponentName(context, WidgetProvider::class.java)
            appWidgetManager.updateAppWidget(componentName, views)
        }
    }

    private fun fetchCurrencyData(): Map<String, String>? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://rateradar.onrender.com/get-value?url=https://www.bcv.org.ve")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                mapOf(
                    "dolar" to json.getString("dolar"),
                    "euro" to json.getString("euro")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
