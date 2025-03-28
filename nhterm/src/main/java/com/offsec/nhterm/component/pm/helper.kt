package com.offsec.nhterm.component.pm

import android.util.Log
import com.offsec.nhterm.App
import com.offsec.nhterm.R
import com.offsec.nhterm.component.ComponentManager
import com.offsec.nhterm.component.config.NeoPreference
import com.offsec.nhterm.component.config.NeoTermPath
import com.offsec.nhterm.framework.NeoTermDatabase
import com.offsec.nhterm.utils.NLog
import com.topjohnwu.superuser.Shell
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

object SourceHelper {
  fun syncSource() {
    val sourceManager = ComponentManager.getComponent<PackageComponent>().sourceManager
    syncSource(sourceManager)
  }

  fun syncSource(sourceManager: SourceManager) {
    val content = buildString {
      this.append("# Generated by NetHunter TerminalPreference\n")
      sourceManager.getEnabledSources()
        .joinTo(this, "\n") { "deb [trusted=yes] ${it.url} ${it.repo}\n" }
    }
    kotlin.runCatching {
      Files.write(Paths.get(NeoTermPath.SOURCE_FILE), content.toByteArray())
    }
  }

  fun detectSourceFiles(): List<File> {
    // Workaround to get things running
    // TODO: ( APT ) Make it prettier?
    copySourceFromChroot()

    val sourceManager = ComponentManager.getComponent<PackageComponent>().sourceManager
    val sourceFiles = ArrayList<File>()
    try {
      val prefixes = sourceManager.getEnabledSources()
        .map { detectSourceFilePrefix(it) }
        .filter { it.isNotEmpty() }

      File(NeoTermPath.PACKAGE_LIST_DIR)
        .listFiles()
              ?.filterTo(sourceFiles) { file ->
                prefixes.count { file.name.startsWith(it) } > 0
              }
    } catch (e: Exception) {
      sourceFiles.clear()
      NLog.e("PM", "Failed to detect source files: ${e.localizedMessage}")
    }

    return sourceFiles
  }

  private fun detectSourceFilePrefix(source: Source): String {
    try {
      val url = URL(source.url)
      val builder = StringBuilder(url.host)
      if (url.port != -1) {
        builder.append(":${url.port}")
      }

      val path = url.path
      if (path != null && path.isNotEmpty()) {
        builder.append("_")
        val fixedPath = path.replace("/", "_").substring(1) // skip the last '/'
        builder.append(fixedPath)
      }
      builder.append("_dists_${source.repo.replace(" ".toRegex(), "_")}_binary-")
      Log.e("ERROR:", builder.toString())
      return builder.toString()
    } catch (e: Exception) {
      NLog.e("PM", "Failed to detect source file prefix: ${e.localizedMessage}")
      return ""
    }
  }

  private fun copySourceFromChroot() {
    val APP_MNT = NeoTermPath.USR_PATH
    val MNT = "/data/local/nhsystem/kalifs"
    val sources = "$MNT/etc/apt/sources.list"
    val lists = "$MNT/var/lib/apt/lists"

    // Make sure that nhterm has locally required apt dir's
    Shell.cmd("mkdir -p $APP_MNT/etc/apt").exec()
    Shell.cmd("mkdir -p $APP_MNT/var/lib/apt/lists").exec()

    // Also we cant be sure that folders are empty from last use so lets remove stuff
    Shell.cmd("rm -f $APP_MNT/etc/apt/*").exec()
    Shell.cmd("rm -f $APP_MNT/var/lib/apt/lists/*").exec()

    // Now lets copy chroot apt sources.list and lists data to app
    // This allows us to read and show list of packages for user in manager
    Shell.cmd("cp -f $sources $APP_MNT/etc/apt/sources.list").exec()
    Shell.cmd("cp -f $lists/* $APP_MNT/var/lib/apt/lists/").exec()

    // Now play with permissions so things are read/writable
    Shell.cmd("chmod -R 775 $APP_MNT/etc/apt").exec()
    Shell.cmd("chmod -R 775 $APP_MNT/var/lib/apt/lists").exec()
  }

  fun updateChrootSource() {
    // TODO: ( APT ) Add option for user to edit and update sources.list in Package Manager option
    return
  }
}

class SourceManager internal constructor() {
  private val database = NeoTermDatabase.instance("sources")

  init {
    if (database.findAll<Source>(
            Source::class.java).isEmpty()) {
      App.get().resources.getStringArray(R.array.pref_package_source_values)
        .forEach {
          database.saveBean(
              Source(
                  it,
                  "kali-rolling main",
                  true
              )
          )
        }
    }
  }

  fun addSource(sourceUrl: String, repo: String, enabled: Boolean) {
    database.saveBean(
        Source(
            sourceUrl,
            repo,
            enabled
        )
    )
  }

  fun removeSource(sourceUrl: String) {
    database.deleteBeanByWhere(Source::class.java, "url == '$sourceUrl'")
  }

  fun updateAll(sources: List<Source>) {
    database.dropAllTable()
    database.saveBeans(sources)
  }

  fun getAllSources(): List<Source> {
    return database.findAll(Source::class.java)
  }

  fun getEnabledSources(): List<Source> {
    return getAllSources().filter { it.enabled }
  }

  fun getMainPackageSource(): String {
    return getEnabledSources()
      .map { it.repo }
      .singleOrNull { it.trim() == "kali-rolling main" }
      ?: NeoTermPath.DEFAULT_MAIN_PACKAGE_SOURCE
  }

  fun applyChanges() {
    database.vacuum()
  }
}

