package com.example.osgi.sandbox

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import java.io.InputStream

class SandboxFactory(val sandboxes: HashSet<Sandbox>) {

    fun createSandBox(name: String, bundleContext: BundleContext): Sandbox {
        val sandbox = Sandbox.create(name, bundleContext)
        sandboxes.add(sandbox)
        return sandbox
    }
}

class Sandbox private constructor(val name: String, private val bundleContext: BundleContext) {
    private val SANDBOX_NAME_DELIM = "/"
    private val visibleSandboxes :  HashSet<Sandbox> = HashSet()
    val installedBundles :  HashSet<Long> = HashSet()

    companion object {
        fun create(name: String, bundleContext: BundleContext) : Sandbox {
            return Sandbox(name, bundleContext)
        }
    }

    fun installBundle(location: String, stream: InputStream?) : Bundle {
        //TODO it's not possible to determine if you are adding a bundle to a sandbox twice inside the hooks but may be possible to validate here
        val bundle = bundleContext.installBundle(name + SANDBOX_NAME_DELIM + location, stream)
        installedBundles.add(bundle.bundleId)
        return bundle
    }

    fun addVisibility(other: Sandbox) {
        visibleSandboxes.add(other)
    }

    fun removeVisibility(other: Sandbox) {
        visibleSandboxes.remove(other)
    }

    fun isVisibleNonTransitive(sandbox: Sandbox?) : Boolean {
        return visibleSandboxes.contains(sandbox)
    }

    fun isVisibleNonTransitive(bundle: Bundle) : Boolean {
        for (sandbox in visibleSandboxes) {
            if (sandbox.installedBundles.contains(bundle.bundleId)) {
                return true
            }
        }

        return false
    }
}

fun Bundle.owningSandbox(sandBoxes: HashSet<Sandbox>) : Sandbox? {
    return sandBoxes.find { it.installedBundles.contains(bundleId) }
}