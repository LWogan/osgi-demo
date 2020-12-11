package com.example.launcher.sandbox

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.hooks.bundle.FindHook

/**
 * This hook is invoked when a bundle tries to access the list of bundles available in it's bundles context.
 * e.g {@link BundleContext#getBundle()}
 * This hoook will return all bundles in the target bundles sandbox, bundles in any visible sandboxes and
 * bundles without sandboxes.
 */
class IsolatingFindHook(private var sandboxes: HashSet<Sandbox>) :  FindHook {

    override fun find(context: BundleContext, bundles: MutableCollection<Bundle>) {
        var targetSandbox = context.bundle?.owningSandbox(sandboxes)
        var copyCandidates = bundles.toMutableList()

        context.getBundle()
        if (copyCandidates != null) {
            for (candidate in copyCandidates) {
                var candidateSandbox = candidate.owningSandbox(sandboxes)

                //candidate lives in main bundle space so should be possible to find
                if (candidateSandbox == null) {
                    continue
                }
                else if (targetSandbox != null && !targetSandbox.isVisibleNonTransitive(candidateSandbox)) {
                    //candidate is in a sandbox that is not visible so can be removed
                    bundles?.remove(candidate)
                }
            }
        }
    }
}
