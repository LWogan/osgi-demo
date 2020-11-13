package com.example.launcher.sandbox

import org.osgi.framework.Bundle
import org.osgi.framework.hooks.bundle.CollisionHook

/**
* Filter collisions caused by installing a bundle with the same symbolic name.
* The target (current bundle installing) bundles sandbox will be null as it won't be added to it's sandbox
* until after it is installed. Collision candidates may be have a sandbox or may have a null sandbox (e.g system bundles or bundles without sandboxes).
* Note it is not possible to determine at this stage if we are installing a bundle to the same sandbox twice.
*/
class IsolatingCollisionBundleHook(sandboxes: HashSet<Sandbox>) : BaseBundleHook(sandboxes), CollisionHook {

    override fun filterCollisions(operationType: Int, target: Bundle?, collisionCandidates: MutableCollection<Bundle>?) {
        var currentTargetSandbox = target?.owningSandbox(sandboxes)
        filterBundleCandidatesBySandbox(currentTargetSandbox, collisionCandidates)
    }
}
