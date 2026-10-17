package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DayOfWeekEnum
import com.example.util.NaturalLanguageScheduleParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("UniSchedule", appName)
  }

  @Test
  fun `test natural language parsing english`() {
    val input = "Sunday Physics from 10:00 to 12:00 Group B Room 204"
    val result = NaturalLanguageScheduleParser.parseOffline(input)
    assertEquals(DayOfWeekEnum.SUNDAY.name, result.dayOfWeek)
    assertEquals("10:00", result.startTime)
    assertEquals("12:00", result.endTime)
    assertEquals("Group B", result.groupName)
    assertEquals("Room 204", result.place)
  }

  @Test
  fun `test natural language parsing arabic`() {
    val input = "الأحد رياضيات من 9 ل 11 مجموعة 1 مدرج أ"
    val result = NaturalLanguageScheduleParser.parseOffline(input)
    assertEquals(DayOfWeekEnum.SUNDAY.name, result.dayOfWeek)
    assertEquals("09:00", result.startTime)
    assertEquals("11:00", result.endTime)
    assertEquals("Group 1", result.groupName)
  }

  @Test
  fun `test main activity launch`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    assertNotNull(controller.get())
  }
}
