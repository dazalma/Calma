package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context verifies CALMA app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CALMA", appName)
  }

  @Test
  fun `disclaimer string contains psychoeducational notice`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val disclaimer = context.getString(R.string.disclaimer_footer)
    assertTrue(disclaimer.contains("psicoeducativo"))
    assertTrue(disclaimer.contains("no sustituye la terapia psicológica profesional"))
  }
}
