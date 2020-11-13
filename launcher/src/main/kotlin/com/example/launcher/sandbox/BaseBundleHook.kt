package com.example.launcher.sandbox

import org.osgi.framework.Bundle

open class BaseBundleHook(var sandboxes: HashSet<Sandbox>) {

    /**
     * Filter collision candidates out that are not in the same sandbox.
     * @param targetSandbox sandbox of the current bundle. may be null.
     * @param collisionCandidates matching bundles
     */
    fun filterBundleCandidatesBySandbox(targetSandbox: Sandbox?, collisionCandidates: MutableCollection<Bundle>?) {
        var copyCandidates = collisionCandidates?.toMutableList()

        if (copyCandidates != null) {
            for (candidate in copyCandidates) {
                var candidateSandbox = candidate.owningSandbox(sandboxes)
                if (targetSandbox == null && candidateSandbox != null) {
                    collisionCandidates?.remove(candidate)
                } else if (candidateSandbox != null && targetSandbox != null && !targetSandbox.isVisibleNonTransitive(candidateSandbox)) {
                    collisionCandidates?.remove(candidate)
                }
            }
        }
    }
}
