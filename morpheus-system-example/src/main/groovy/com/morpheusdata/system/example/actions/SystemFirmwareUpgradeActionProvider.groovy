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
import com.morpheusdata.system.example.wizards.FirmwareUpgradeWizardProvider
import groovy.util.logging.Slf4j

/**
 * Example of a wizard backed {@link ActionProvider}.
 *
 * <p>The matching {@code ActionType} points at {@code FirmwareUpgradeWizardProvider}, so the UI
 * collects input through that wizard before invoking {@link #runAction}. Values gathered by the
 * wizard arrive on {@link ActionRequest#getData()}, while {@link #prepare} supplies the defaults
 * used to pre-populate the wizard. {@code providerCode} is {@code arcus.system.firmwareUpgrade}.</p>
 */
@Slf4j
class SystemFirmwareUpgradeActionProvider extends BaseProvider implements ActionProvider {

    SystemFirmwareUpgradeActionProvider(Plugin plugin, MorpheusContext morpheus) {
        super(plugin, morpheus)
    }

    @Override
    String getCode() {
        return 'arcus-system-firmware-upgrade-action'
    }

    @Override
    String getName() {
        return 'Upgrade Firmware'
    }

    @Override
    String getNamespace() {
        return 'arcus.system'
    }

    @Override
    String getKey() {
        return 'firmwareUpgrade'
    }

    @Override
    ActionInfo.ActionState getState(ActionRequest request) {
        System system = loadSystem(request)
        if(!system) {
            return ActionInfo.ActionState.NA
        }
        // firmware cannot be upgraded while the system is still being configured
        return system.status == 'uninitialized' ? ActionInfo.ActionState.DISABLED : ActionInfo.ActionState.ENABLED
    }

    /**
     * Called before the wizard is shown. The returned map is used to pre-populate the
     * wizard fields defined by {@link FirmwareUpgradeWizardProvider}.
     */
    @Override
    ServiceResponse<HashMap<String, Object>> prepare(ActionRequest request) {
        System system = loadSystem(request)

        def data = new HashMap<String, Object>()
        data.put('targetVersion', '2.1.0')
        data.put('rollingUpgrade', true)
        data.put('maintenanceWindowMinutes', 60)
        data.put('componentCount', system?.components?.size() ?: 0)

        return ServiceResponse.success(data)
    }

    @Override
    ServiceResponse<ActionResponse> runAction(ActionRequest request) {
        def targetVersion = request.data?.targetVersion
        if(!targetVersion) {
            return ServiceResponse.error('A target firmware version is required')
        }

        log.info("Upgrading firmware on ${request.refType}:${request.refId} to ${targetVersion}")

        def response = new ActionResponse(
            state: ActionInfo.ActionState.DISABLED, // disabled while the upgrade is in flight
            results: [
                new ActionItemResult(
                    refId: request.refId,
                    success: true,
                    message: "Firmware upgrade to ${targetVersion} queued".toString()
                )
            ],
            data: [
                targetVersion: targetVersion,
                rollingUpgrade: request.data?.rollingUpgrade,
                maintenanceWindowMinutes: request.data?.maintenanceWindowMinutes
            ]
        )

        return ServiceResponse.success(response)
    }

    private System loadSystem(ActionRequest request) {
        if(request?.refId == null) {
            return null
        }
        return morpheus.async.system.get(request.refId).blockingGet()
    }
}
