package com.example.launcher.sandbox

import org.osgi.framework.hooks.resolver.ResolverHook
import org.osgi.framework.hooks.resolver.ResolverHookFactory
import org.osgi.framework.wiring.BundleCapability
import org.osgi.framework.wiring.BundleRequirement
import org.osgi.framework.wiring.BundleRevision

class IsolatingResolverHook(private var currentSandbox: Sandbox?, private var sandboxes: HashSet<Sandbox>) : ResolverHook {

    /**
     * Filter bundles that don't match the current installed (but not yet started) bundle's sandbox.
     * @param candidates bundles that have not yet been started and can be resolved.
     */
    override fun filterResolvable(candidates: MutableCollection<BundleRevision>?) {
        filterRevisionCandidatesBySandbox(candidates)
    }

    override fun filterSingletonCollisions(singleton: BundleCapability?, collisionCandidates: MutableCollection<BundleCapability>?) {
        filterCapabilityCandidatesBySandbox(collisionCandidates)
    }

    /**
     * Filter the candidates for this bundles required capabilities and imported packages by removing candidates which
     * are already in a sandbox.
     *
     * @param requirement the package required by this bundle
     * @param candidates bundles that export the current bundle's imports and other requirements.
     */
    override fun filterMatches(requirement: BundleRequirement?, candidates: MutableCollection<BundleCapability>?) {
        filterCapabilityCandidatesBySandbox(candidates)
    }

    override fun end() {
    }

    private fun filterRevisionCandidatesBySandbox(candidates: MutableCollection<BundleRevision>? ) {
        var copyCandidates = candidates?.toMutableList()

        if (copyCandidates != null) {
            for (candidate in copyCandidates) {
                var candidateSandbox = candidate.bundle.owningSandbox(sandboxes)

                if (currentSandbox == null && candidateSandbox != null) {
                    candidates?.remove(candidate)
                } else if (candidateSandbox != null && currentSandbox != null && !currentSandbox!!.isVisibleNonTransitive(candidateSandbox)) {
                    candidates?.remove(candidate)
                }
            }
        }
    }

    private fun filterCapabilityCandidatesBySandbox(candidates: MutableCollection<BundleCapability>? ) {
        var copyCandidates = candidates?.toMutableList()

        if (copyCandidates != null) {
            for (candidate in copyCandidates) {
                var candidateSandbox = candidate.revision.bundle.owningSandbox(sandboxes)

                if (currentSandbox == null && candidateSandbox != null) {
                    candidates?.remove(candidate)
                } else if (candidateSandbox != null && currentSandbox != null && !currentSandbox!!.isVisibleNonTransitive(candidateSandbox)) {
                    candidates?.remove(candidate)
                }
            }
        }
    }
}

class IsolatingResolverHookFactory(private var sandboxes: HashSet<Sandbox>) : ResolverHookFactory {

    /**
     * Get and set the triggering bundles sandbox to the resolver hook so it can be comoared
     * to candidate bundles sandboxes
     * @param triggers the bundle triggering the resolver
     */
    override fun begin(triggers: MutableCollection<BundleRevision>?): ResolverHook {
        val sandbox = triggers!!.single().bundle.owningSandbox(sandboxes)
        return IsolatingResolverHook(sandbox, sandboxes)
    }
}
