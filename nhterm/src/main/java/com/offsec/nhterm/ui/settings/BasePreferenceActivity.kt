/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offsec.nhterm.ui.settings

import android.content.res.Configuration
import android.content.res.Resources.Theme
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat.ThemeCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.offsec.nhterm.R

/**
 * A [android.preference.PreferenceActivity] which implements and proxies the necessary calls
 * to be used with AppCompat.
 *
 *
 * This technique can be used with an [android.app.Activity] class, not just
 * [android.preference.PreferenceActivity].
 */
abstract class BasePreferenceActivity : PreferenceActivity() {
  private var mDelegate: AppCompatDelegate? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    delegate.installViewFactory()
    delegate.onCreate(savedInstanceState)
    delegate.setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    delegate.onPostCreate(savedInstanceState)
  }

  val supportActionBar: ActionBar?
    get() = delegate.supportActionBar

  override fun getMenuInflater(): MenuInflater {
    return delegate.menuInflater
  }

  override fun setContentView(@LayoutRes layoutResID: Int) {
    delegate.setContentView(layoutResID)
  }

  override fun setContentView(view: View) {
    delegate.setContentView(view)
  }

  override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
    delegate.setContentView(view, params)
  }

  override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
    delegate.addContentView(view, params)
  }

  override fun onPostResume() {
    super.onPostResume()
    delegate.onPostResume()
  }

  override fun onTitleChanged(title: CharSequence, color: Int) {
    super.onTitleChanged(title, color)
    delegate.setTitle(title)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    delegate.onConfigurationChanged(newConfig)
  }

  override fun onStop() {
    super.onStop()
    delegate.onStop()
  }

  override fun onDestroy() {
    super.onDestroy()
    delegate.onDestroy()
  }

  override fun invalidateOptionsMenu() {
    delegate.invalidateOptionsMenu()
  }

  private val delegate: AppCompatDelegate
    get() {
      if (mDelegate == null) {
        mDelegate = AppCompatDelegate.create(this, null)
      }
      return mDelegate!!
    }
}
