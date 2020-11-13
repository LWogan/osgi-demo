package com.example.launcher.activator

import org.osgi.framework.Bundle
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

class HostActivator : BundleActivator {
    private var bundleContext : BundleContext? = null

    override fun start(context: BundleContext?) {
        bundleContext = context
    }

    override fun stop(context: BundleContext?) {
        bundleContext = null
    }

    fun bundles(): List<Bundle> {
        return bundleContext?.bundles?.toList() ?: emptyList()
    }

    fun context(): BundleContext? {
        return bundleContext
    }
}