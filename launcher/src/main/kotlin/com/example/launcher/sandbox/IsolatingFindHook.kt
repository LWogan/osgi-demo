package com.example.launcher.sandbox

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.hooks.bundle.FindHook

/**
 * This hook is invoked when a bundle tries to access the list of bundles available in it's bundles context.
 * e.g {@link BundleContext#getBundle()}
 */
class IsolatingFindHook(sandboxes: HashSet<Sandbox>) : BaseBundleHook(sandboxes), FindHook {

    override fun find(context: BundleContext?, bundles: MutableCollection<Bundle>?) {
        var currentTargetSandbox = context?.bundle?.owningSandbox(sandboxes)
        filterBundleCandidatesBySandbox(currentTargetSandbox, bundles)
    }
}
