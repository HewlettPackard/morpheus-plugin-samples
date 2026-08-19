package com.morpheusdata.system.example.actions

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.action.ActionInfo
import com.morpheusdata.core.action.ActionItemResult
import com.morpheusdata.core.action.ActionRequest
import com.morpheusdata.core.action.ActionResponse
import com.morpheusdata.core.providers.ActionProvider
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.system.example.BaseProvider
import groovy.util.logging.Slf4j

/**
 * Simplest possible {@link ActionProvider} example.
 *
 * <p>The action is always available, takes no user input (no wizard) and operates on a
 * single target. Its {@code providerCode} - the value an {@code ActionType} must reference
 * to bind to this provider - is {@code arcus.system.healthCheck} (namespace + '.' + key).</p>
 */
@Slf4j
class SystemHealthCheckActionProvider extends BaseProvider implements ActionProvider {

    SystemHealthCheckActionProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'arcus-system-health-check-action'
    }

    @Override
    String getName() {
        return 'Run Health Check'
    }

    @Override
    String getNamespace() {
        return 'arcus.system'
    }

    @Override
    String getKey() {
        return 'healthCheck'
    }

    /**
     * A health check can always be run, so the action is unconditionally enabled.
     */
    @Override
    ActionInfo.ActionState getState(ActionRequest request) {
        return ActionInfo.ActionState.ENABLED
    }

    /**
     * No user input is collected for this action, so there is nothing to prepare.
     */
    @Override
    ServiceResponse<HashMap<String, Object>> prepare(ActionRequest request) {
        return ServiceResponse.success(new HashMap<String, Object>())
    }

    @Override
    ServiceResponse<ActionResponse> runAction(ActionRequest request) {
        log.info("Running health check for ${request.refType}:${request.refId}")

        def response = new ActionResponse(
            state: ActionInfo.ActionState.ENABLED,
            results: [
                new ActionItemResult(
                    refId: request.refId,
                    success: true,
                    message: 'Health check completed, all components reporting healthy',
                    data: [switches: 'ok', hosts: 'ok', storage: 'ok', networks: 'ok']
                )
            ],
            data: [checkedAt: new Date().toString()]
        )

        return ServiceResponse.success(response)
    }
}
