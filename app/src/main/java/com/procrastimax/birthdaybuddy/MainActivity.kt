package com.procrastimax.birthdaybuddy

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.procrastimax.birthdaybuddy.fragments.EventListFragment
import com.procrastimax.birthdaybuddy.handler.EventHandler
import com.procrastimax.birthdaybuddy.models.EventBirthday
import com.procrastimax.birthdaybuddy.models.EventDay
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DateFormat
import java.util.*

/**
 *
 * TODO:
 *  - bug when localization is changed after first start of app -> add possibility to change all encodings at app start when error occurs -> fix this by only use one format for saving
 *  - dont show last seperation character in list view
 *  - dont draw item decoration on month divider
 *  - add checking for existing forename/surname pair when adding a new birthday/event
 *  - BUG: app closes when switched to potrait mode and changing fragments
 *  - add long click for birthday event item
 */
class MainActivity : AppCompatActivity() {

    private val settings_shared_pref_file_name = "com.procrasticmax.birthdaybuddy.settings_shared_pref"
    private val settings_shared_pref_isFirstStart = "isFirstStart"
    private val shared_pref_settings by lazy {
        this.getSharedPreferences(
            this.settings_shared_pref_file_name,
            Context.MODE_PRIVATE
        )
    }

    private lateinit var application_container: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        changeToolbarState(Companion.ToolbarState.Default)

        //get application context
        EventDataIO.registerIO(this.applicationContext)

        //read all data from shared prefs
        EventHandler.addMap(EventDataIO.readAll())

        //set month divier map from eventHandler
        EventHandler.validateMonthDivierMap()

        if (isFirstStart()) {
            val month_begin_date = Calendar.getInstance()
            month_begin_date.set(Calendar.YEAR, 1)
            month_begin_date.set(Calendar.DAY_OF_MONTH, 1)

            EventHandler.addEvent(
                EventBirthday(
                    EventDay.parseStringToDate("06.02.00", DateFormat.SHORT),
                    "Procrastimax",
                    "Dev",
                    false
                ), applicationContext,
                true
            )
        }

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(R.id.fragment_placeholder, EventListFragment.newInstance())
        ft.commit()
    }

    /**
     * getMonthFromIndex returns a month name specified in the string resources by an index
     * starts at 0 with january
     * @param index: Int
     * @return String
     */
    private fun getMonthFromIndex(index: Int): String {
        return resources.getStringArray(R.array.month_names)[index]
    }

    private fun isFirstStart(): Boolean {
        return if (shared_pref_settings.getBoolean(settings_shared_pref_isFirstStart, true)) {
            //change isFirstStatus in shared prefs to false
            this.shared_pref_settings.edit().putBoolean(settings_shared_pref_isFirstStart, false).apply()
            true
        } else {
            false
        }
    }

    fun changeToolbarState(state: ToolbarState) {
        when (state) {
            Companion.ToolbarState.Default -> {
                toolbar.removeAllViews()
                supportActionBar!!.setDisplayShowTitleEnabled(true)
            }

            Companion.ToolbarState.AddBirthday -> {
                if (toolbar.childCount > 0) {
                    toolbar.removeAllViews()
                }
                toolbar.addView(
                    layoutInflater.inflate(
                        R.layout.toolbar_add_birthday,
                        findViewById(android.R.id.content),
                        false
                    )
                )
                setSupportActionBar(toolbar)
                supportActionBar!!.setDisplayShowTitleEnabled(false)
            }
            Companion.ToolbarState.EditBirtday -> {
                if (toolbar.childCount > 0) {
                    toolbar.removeAllViews()
                }
                toolbar.addView(
                    layoutInflater.inflate(
                        R.layout.toolbar_edit_birthday,
                        findViewById(android.R.id.content),
                        false
                    )
                )
                setSupportActionBar(toolbar)
                supportActionBar!!.setDisplayShowTitleEnabled(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            // R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun convertPxToDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }

        enum class ToolbarState {
            Default,
            AddBirthday,
            EditBirtday
        }
    }
}
