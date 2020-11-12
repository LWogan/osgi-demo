package com.example.osgi.grettings.activator

import com.example.osgi.yo.service.YoService
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference

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
        logger.info("GreetingsActivator calling out to yo service...")
        yoService.doSomething()
    }

}