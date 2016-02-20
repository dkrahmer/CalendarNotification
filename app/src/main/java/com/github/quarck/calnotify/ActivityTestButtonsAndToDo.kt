//
//   Calendar Notifications Plus  
//   Copyright (C) 2016 Sergey Parshin (s.parshin.sc@gmail.com)
//
//   This program is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
// 
//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with this program; if not, write to the Free Software Foundation,
//   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
//

package com.github.quarck.calnotify

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import java.util.*

class ActivityTestButtonsAndToDo : Activity()
{

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_test_buttons_and_to_do)

		(findViewById(R.id.log) as TextView).text = DebugTransactionLog(this).getMessages(" - ", "\n\n");
		(findViewById(R.id.debug_logging_toggle) as ToggleButton).isChecked = Settings(this).debugTransactionLogEnabled;
	}


	fun OnDebugLoggingToggle(v: View)
	{
		if (v is ToggleButton)
		{
			Settings(this).debugTransactionLogEnabled = v.isChecked;
			if (!v.isChecked)
				DebugTransactionLog(this).dropAll();
		}
	}


	fun OnButtonTestActivityClick(v: View)
	{
		startActivity(Intent(applicationContext, ActivitySnooze::class.java));
	}

	fun randomTitle(currentTime: Long): String
	{
		var dict = arrayOf("hello", "world", "item", "remove", "code", "is", "bug",
			"memory", "leak", "detected", "avoid", "refactoring" ,
			"China", "keeps", "pretending", "to do", "this", "too", "because",
			"of", "misplaced", "nationalism", "ignoring", "the", "fact",
			"that", "pretty", "much", "all", "OS", "development",
			"takes", "place", "in the", "USA",
			"You want", "to move", "to Linux", "Russia?",
			"Then maybe", "you should", "actually",
			"fix Cyrillic display",
			"in a", "major", "distro", "DE (like Debian and GNOME)", "and adopt that")

		var sb = StringBuilder();
		var r = Random(currentTime);

		var len = r.nextInt(30);

		var prev = -1;
		for (i in 0..len)
		{
			var new = 0;
			do { new = r.nextInt(dict.size) } while (new == prev);
			sb.append(dict[new]);
			sb.append(" ")
			prev = new;
		}

		return sb.toString();
	}

	private var cnt = 0;

	fun OnButtonTestClick(v: View)
	{
		var db = EventsStorage(this)

		var first = (v.id == R.id.buttonTest);

		var currentTime = System.currentTimeMillis();

		db.addEvent(
			if (first) 101010101L else currentTime,
			System.currentTimeMillis(),
			if (first) "Test Notification" else randomTitle(currentTime) + " " + ((currentTime/100) % 10000).toString(),
			"This is a test notification",
			currentTime + 3600L*1000L,
			currentTime + 2*3600L*1000L,
			if((cnt % 2)==0) "" else "Connolly st., Dublin, IFSC",
			System.currentTimeMillis(),
			false,
			0xffFFC107.toInt()
		)

		cnt++;

		EventNotificationManager().postEventNotifications(applicationContext, false);
	}
}