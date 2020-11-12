package com.example.osgi.logger;

import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.log.LogEntry
import org.osgi.service.log.LogListener
import org.osgi.service.log.LogReaderService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component(immediate = true)
class LogReader {

    @Activate
    fun doIt(context: BundleContext) {
        val ref = context.getServiceReference(LogReaderService::class.java.name)
        if (ref != null) {
            val reader: LogReaderService = context.getService(ref) as LogReaderService
            reader.addLogListener(LogWriter())
        }
    }
}

class LogWriter : LogListener {

    override fun logged(entry: LogEntry) {
        println("Custom-Logger:: " + entry.time.toString() + " - " + entry.logLevel.name + ": " +  entry.message)
    }
}