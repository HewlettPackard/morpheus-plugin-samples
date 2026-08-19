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
 * Example of a bulk {@link ActionProvider}.
 *
 * <p>Demonstrates using {@link ActionRequest#getRefIds()} to act on many targets at once and
 * returning one {@link ActionItemResult} per target so the UI can report partial success.
 * {@code providerCode} is {@code arcus.component.restart}.</p>
 */
@Slf4j
class RestartComponentsActionProvider extends BaseProvider implements ActionProvider {

    RestartComponentsActionProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'arcus-component-restart-action'
    }

    @Override
    String getName() {
        return 'Restart Components'
    }

    @Override
    String getNamespace() {
        return 'arcus.component'
    }

    @Override
    String getKey() {
        return 'restart'
    }

    @Override
    ActionInfo.ActionState getState(ActionRequest request) {
        return targetIds(request) ? ActionInfo.ActionState.ENABLED : ActionInfo.ActionState.DISABLED
    }

    @Override
    ServiceResponse<HashMap<String, Object>> prepare(ActionRequest request) {
        def data = new HashMap<String, Object>()
        data.put('targetCount', targetIds(request).size())
        data.put('confirmationRequired', true)
        return ServiceResponse.success(data)
    }

    @Override
    ServiceResponse<ActionResponse> runAction(ActionRequest request) {
        List<Long> ids = targetIds(request)
        if(!ids) {
            return ServiceResponse.error('No components selected')
        }

        log.info("Restarting ${ids.size()} ${request.refType} component(s)")

        def results = ids.collect { Long id ->
            new ActionItemResult(
                refId: id,
                success: true,
                message: "Restart requested for component ${id}".toString()
            )
        }

        def response = new ActionResponse(
            state: ActionInfo.ActionState.ENABLED,
            results: results,
            data: [restarted: ids.size()]
        )

        return ServiceResponse.success(response)
    }

    /**
     * {@code refIds} carries the full selection for a bulk invocation while {@code refId} holds
     * the single target. Falling back keeps the provider usable from both contexts.
     */
    private List<Long> targetIds(ActionRequest request) {
        if(request?.refIds) {
            return request.refIds
        }
        return request?.refId != null ? [request.refId] : []
    }
}
