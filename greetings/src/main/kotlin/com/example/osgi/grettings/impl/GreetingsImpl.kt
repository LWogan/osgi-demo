package com.example.osgi.grettings.impl

import com.example.osgi.grettings.api.Greetings
import org.osgi.service.component.annotations.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
//class GreetingsImpl @Activate constructor(@Reference(service = LoggerFactory::class) logger: Logger) : Greetings {
class GreetingsImpl : Greetings {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(GreetingsImpl::class.java)
    }

    override fun greet(name: String): String {
        return "Hello $name!"
    }
}