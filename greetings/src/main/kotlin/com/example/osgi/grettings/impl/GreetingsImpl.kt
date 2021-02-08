package com.example.osgi.grettings.impl

import com.example.osgi.grettings.api.Greetings
import com.example.osgi.yo.service.YoService
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference

@Component
class GreetingsImpl @Activate constructor(
        @Reference(service = YoService::class)
        private val yo: YoService) : Greetings {

    override fun greet(name: String): String {
        //yo.doSomething()
        return "Hello $name!"
    }
}