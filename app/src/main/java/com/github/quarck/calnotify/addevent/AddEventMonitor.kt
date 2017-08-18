//
//   Calendar Notifications Plus
//   Copyright (C) 2017 Sergey Parshin (s.parshin.sc@gmail.com)
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

package com.github.quarck.calnotify.addevent

import android.content.Context
import android.content.Intent
import com.github.quarck.calnotify.Consts
import com.github.quarck.calnotify.addevent.storage.NewEventRecord
import com.github.quarck.calnotify.addevent.storage.NewEventsStorage
import com.github.quarck.calnotify.calendar.CalendarProvider
import com.github.quarck.calnotify.logs.DevLog
import com.github.quarck.calnotify.permissions.PermissionsManager

class AddEventMonitor: AddEventMonitorInterface {

    override fun onRescanFromService(context: Context, intent: Intent) {

        DevLog.debug(LOG_TAG, "onRescanFromService")

        if (!PermissionsManager.hasAllPermissionsNoCache(context)) {
            DevLog.error(context, LOG_TAG, "onRescanFromService - no calendar permission to proceed")
            return
        }

        val provider = CalendarProvider

        val currentTime = System.currentTimeMillis()
        val cleanupEventsTo = currentTime - Consts.NEW_EVENT_MONITOR_KEEP_DAYS * Consts.DAY_IN_MILLISECONDS

        NewEventsStorage(context).use {
            db ->

            val eventsToDelete = mutableListOf<NewEventRecord>()
            val eventsToReCreate = mutableListOf<NewEventRecord>()
            val eventsToUpdate = mutableListOf<NewEventRecord>()

            for (event in db.events) {

                if (event.startTime < cleanupEventsTo && event.endTime < cleanupEventsTo) {
                    eventsToDelete.add(event)
                    DevLog.info(context, LOG_TAG, "Scheduling event creation request for ${event.eventId} for removal from DB")
                    continue
                }

                if (event.eventId == -1L) {
                    eventsToReCreate.add(event)
                    DevLog.info(context, LOG_TAG, "Scheduling event creation request ${event.eventId} for re-creation: id is -1L")
                    continue
                }

                val calendarEvent = provider.getEvent(context, event.eventId)
                if (calendarEvent == null) {
                    eventsToReCreate.add(event)
                    DevLog.info(context, LOG_TAG, "Scheduling event creation request ${event.eventId} for re-creation: cant find provider event")
                    continue
                }

                val isDirty = provider.getEventIsDirty(context, event.eventId)
                DevLog.info(context, LOG_TAG, "Event ${event.eventId}, isDirty=$isDirty")
                if (isDirty != null && isDirty == false) {

                    // FIXME: not that simple, check for a few times, so only remove if 3 conseq scans
                    // would show it is not dirty

                    //eventsToDelete.add(event)
                    DevLog.info(context, LOG_TAG, "Scheduling event creation request ${event.eventId} for removal: it is fully synced now")
                }
            }

            if (eventsToReCreate.isNotEmpty()) {
                for (event in eventsToReCreate) {
                    event.eventId = provider.createEvent(context, event)
                }
                db.updateEvents(eventsToReCreate)
            }

            if (eventsToDelete.isNotEmpty())
                db.deleteEvents(eventsToDelete)

            if (eventsToUpdate.isNotEmpty())
                db.updateEvents(eventsToUpdate)
        }
    }

    companion object {
        private const val LOG_TAG = "AddEventMonitor"
    }
}