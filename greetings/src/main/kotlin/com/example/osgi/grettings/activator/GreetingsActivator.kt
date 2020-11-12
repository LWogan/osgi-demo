package com.example.osgi.grettings.activator

import com.example.osgi.yo.YoService
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class GreetingsActivator @Activate constructor(
        @Reference(service = YoService::class)
        private val yoService: YoService,
        @Reference(service = org.osgi.service.log.LoggerFactory::class)
        private val logger: org.osgi.service.log.Logger
) {

    @Activate
    fun doIt() {
        logger.info("Activated GreetingsActivator {}", this::class.java)
        yoService.doSomething()
    }

}