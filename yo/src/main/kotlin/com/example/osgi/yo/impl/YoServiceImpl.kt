package com.example.osgi.yo.impl;

import com.example.osgi.yo.service.YoService
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference
import org.osgi.service.log.Logger
import org.osgi.service.log.LoggerFactory

@Component(immediate = true)
class YoServiceImpl @Activate constructor(
        @Reference(service = LoggerFactory::class)
        private val logger: Logger
) : YoService {

    override fun doSomething() {
        logger.info("I am the YoService... doing something?")
    }
}
