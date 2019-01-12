package com.zacharee1.systemuituner.activites

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceFragmentCompat
import com.zacharee1.systemuituner.R
import com.zacharee1.systemuituner.fragments.AnimFragment
import com.zacharee1.systemuituner.misc.OptionSelected
import com.zacharee1.systemuituner.util.checkSamsung
import com.zacharee1.systemuituner.util.forEachPreference
import com.zacharee1.systemuituner.util.getAnimTransaction
import com.zacharee1.systemuituner.util.prefs

class OptionsActivity : BaseAnimActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_item_list)

        backButton.scaleX = 0f
        backButton.scaleY = 0f

        setBackClickable(!prefs.hideWelcomeScreen)

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        supportFragmentManager
                ?.getAnimTransaction()
                ?.replace(R.id.content_main, MainPrefs())
                ?.addToBackStack("main")
                ?.commit()
    }

    override fun onResume() {
        super.onResume()

        setBackClickable(supportFragmentManager.backStackEntryCount > 1 || !prefs.hideWelcomeScreen)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            handleBackPressed()
            return true
        }

        return OptionSelected.doAction(item.itemId, this)
    }

    override fun onBackPressed() {
        handleBackPressed()
    }

    private fun handleBackPressed() {
        when {
            supportFragmentManager != null -> when {
                supportFragmentManager.backStackEntryCount > 1 -> {
                    supportFragmentManager.popBackStackImmediate()

                    val stillAboveOne = supportFragmentManager.backStackEntryCount > 1

                    setBackClickable(stillAboveOne || !prefs.hideWelcomeScreen)
                }
                else -> finish()
            }
            else -> finish()
        }
    }

    class MainPrefs : AnimFragment() {
        override val prefsRes = R.xml.prefs_main

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            removeTouchWizIfNeeded()
            removeLockScreenIfNeeded()
            setListeners()
        }

        override fun onResume() {
            super.onResume()
            updateCustomEnabledState()
        }

        override fun onSetTitle() = resources.getString(R.string.app_name)

        private fun updateCustomEnabledState() {
            val customPref = findPreference("custom")
            val enabled = context!!.prefs.allowCustomSettingsInput

            customPref.isEnabled = enabled
            customPref.summary = if (enabled) null else resources.getString(R.string.enable_in_settings)
        }

        private fun removeTouchWizIfNeeded() {
            if (activity?.checkSamsung() == false) preferenceScreen.removePreference(findPreference("touchwiz") ?: return)
        }

        private fun removeLockScreenIfNeeded() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) preferenceScreen.removePreference(findPreference("lockscreen") ?: return)
        }

        private fun setListeners() {
            preferenceScreen.forEachPreference {
                it.setOnPreferenceClickListener {
                    (activity as BaseAnimActivity).setBackClickable(true)

                    val fragment = Class.forName(it.fragment
                            ?: return@setOnPreferenceClickListener false).newInstance() as PreferenceFragmentCompat
                    fragmentManager
                            ?.getAnimTransaction()
                            ?.replace(R.id.content_main, fragment, it.key)
                            ?.addToBackStack(it.key)
                            ?.commit()
                    true
                }
            }
        }
    }
}