package com.morpheusdata.system.example.actions

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.action.ActionInfo
import com.morpheusdata.core.action.ActionItemResult
import com.morpheusdata.core.action.ActionRequest
import com.morpheusdata.core.action.ActionResponse
import com.morpheusdata.core.providers.ActionProvider
import com.morpheusdata.model.system.System
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.system.example.BaseProvider
import groovy.util.logging.Slf4j

/**
 * Example {@link ActionProvider} whose availability depends on the state of the target.
 *
 * <p>Demonstrates loading the target model from {@code refType}/{@code refId} and returning
 * a state of {@code ENABLED}, {@code DISABLED} or {@code NA} so the UI can grey out or hide
 * the action. {@code providerCode} is {@code arcus.system.maintenanceMode}.</p>
 */
@Slf4j
class SystemMaintenanceModeActionProvider extends BaseProvider implements ActionProvider {

    static final String REF_TYPE_SYSTEM = 'system'

    SystemMaintenanceModeActionProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'arcus-system-maintenance-mode-action'
    }

    @Override
    String getName() {
        return 'Toggle Maintenance Mode'
    }

    @Override
    String getNamespace() {
        return 'arcus.system'
    }

    @Override
    String getKey() {
        return 'maintenanceMode'
    }

    /**
     * Only offer maintenance mode against a fully configured system. Anything that is not an
     * Arcus system is reported as {@code NA} so the UI can omit the action entirely, while a
     * system that has not finished initializing is {@code DISABLED} (visible but not clickable).
     */
    @Override
    ActionInfo.ActionState getState(ActionRequest request) {
        System system = loadSystem(request)
        if(!system) {
            return ActionInfo.ActionState.NA
        }
        if(system.status == 'uninitialized' || system.enabled == false) {
            return ActionInfo.ActionState.DISABLED
        }
        return ActionInfo.ActionState.ENABLED
    }

    /**
     * Seeds the confirmation dialog with the current mode so the UI can label the button
     * correctly ('Enter' vs 'Exit' maintenance mode).
     */
    @Override
    ServiceResponse<HashMap<String, Object>> prepare(ActionRequest request) {
        System system = loadSystem(request)
        def data = new HashMap<String, Object>()
        data.put('currentStatus', system?.status)
        data.put('inMaintenance', system?.status == 'maintenance')
        return ServiceResponse.success(data)
    }

    @Override
    ServiceResponse<ActionResponse> runAction(ActionRequest request) {
        System system = loadSystem(request)
        if(!system) {
            return ServiceResponse.error("System ${request.refId} not found")
        }

        boolean enteringMaintenance = system.status != 'maintenance'
        system.status = enteringMaintenance ? 'maintenance' : 'ok'
        system.statusMessage = enteringMaintenance ? 'System placed in maintenance mode' : null

        // In a real provider persist the change, e.g.:
        // morpheus.async.system.save(system).blockingGet()

        def response = new ActionResponse(
            // recompute the state so the UI can refresh the action without a page reload
            state: ActionInfo.ActionState.ENABLED,
            results: [
                new ActionItemResult(
                    refId: request.refId,
                    success: true,
                    message: enteringMaintenance ? 'System entered maintenance mode' : 'System exited maintenance mode'
                )
            ],
            data: [status: system.status]
        )

        return ServiceResponse.success(response)
    }

    private System loadSystem(ActionRequest request) {
        if(request?.refId == null || (request.refType && request.refType != REF_TYPE_SYSTEM)) {
            return null
        }
        return morpheus.async.system.get(request.refId).blockingGet()
    }
}
