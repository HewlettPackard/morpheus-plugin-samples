package com.morpheusdata.system.example

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.data.DataQuery
import com.morpheusdata.core.providers.ClusterProvider
import com.morpheusdata.core.providers.SystemProvider
import com.morpheusdata.model.Icon
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.ComputeServerGroup
import com.morpheusdata.model.Network
import com.morpheusdata.model.NetworkSwitch
import com.morpheusdata.model.StorageServer
import com.morpheusdata.model.ActionType
import com.morpheusdata.model.UpdateDefinition
import com.morpheusdata.model.UpdateOperation
import com.morpheusdata.model.Wizard
import com.morpheusdata.model.system.*
import com.morpheusdata.model.system.System
import com.morpheusdata.model.system.SystemComponent
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.system.example.workflow.ArcusSystemConfigurationWorkflowProvider
import groovy.util.logging.Slf4j

/**
 * System Provider for Arcus systems
 */
@Slf4j
class ArcusSystemProvider implements SystemProvider, ClusterProvider.ClusterUpdateFacet {

    static final String CLUSTER_COMPONENT_CODE = 'arcus-cluster'
    static final String CLUSTER_REF_TYPE = 'ComputeServerGroup'

    Plugin plugin
    MorpheusContext morpheusContext

    ArcusSystemProvider(Plugin plugin, MorpheusContext morpheusContext) {
        this.plugin = plugin
        this.morpheusContext = morpheusContext
    }

    @Override
    MorpheusContext getMorpheus() {
        return morpheusContext
    }

    @Override
    String getCode() {
        return 'arcus-system-provider'
    }

    @Override
    String getName() {
        return 'Arcus System Provider'
    }

    @Override
    String getDescription() {
        return 'Provider for managing Arcus infrastructure systems with configuration workflow'
    }

    @Override
    Icon getIcon() {
        return new Icon(path: "system-icon.svg", darkPath: "system-icon-dark.svg")
    }

    @Override
    Collection<SystemComponentType> getSystemComponentTypes() {
        return [
            createComponentType('arcus-switch', 'Arcus Switch', 'Network switch component', NetworkSwitch.class),
            createComponentType('arcus-host', 'Arcus Host', 'Host/server component', ComputeServer.class),
            createComponentType('arcus-storage', 'Arcus Storage', 'Storage array component', StorageServer.class),
            createComponentType('arcus-network', 'Arcus Data Network', 'Data network component', Network.class),
            createComponentType('arcus-cluster', 'Arcus Cluster', 'Cluster management component', ComputeServerGroup.class)
        ]
    }

    @Override
    Collection<SystemType> getSystemTypes() {
        SystemType systemType = new SystemType(
            code: 'arcus-system',
            name: 'Arcus Infrastructure System',
            description: 'Complete Arcus infrastructure management system'
        )
        return [systemType]
    }

    @Override
    Collection<SystemTypeLayout> getSystemTypeLayouts() {
        // Get the system type
        SystemType systemType = new SystemType(
            code: 'arcus-system',
            name: 'Arcus Infrastructure System',
            description: 'Complete Arcus infrastructure management system'
        )
        
        // Get or create the ConfigurationWorkflow reference
        // In a real implementation, retrieve this from MorpheusContext
        // For example: def workflow = morpheusContext.getConfigurationWorkflow().find(new DataQuery().withFilter('code', 'arcus-system-configuration-workflow')).blockingGet()
        def workflow = getConfigurationWorkflowByCode('arcus-system-configuration-workflow')
        
        SystemTypeLayout layout = new SystemTypeLayout(
            code: 'arcus-standard-layout',	
            name: 'Arcus Standard Layout',
            description: 'Standard layout for Arcus infrastructure systems with configuration workflow',
            version: '1.0',
            systemType: systemType,
            creatable: true,   // supports the "Add System" -> Create flow
            importable: false,  // also supports importing an existing Arcus system
            configurationWorkflow: workflow  // Direct object reference
        )
        
        // Add component types to the layout
        layout.components = [
            createComponentType('arcus-switch', 'Arcus Switch', 'Network switch component', NetworkSwitch.class),
            createComponentType('arcus-host', 'Arcus Host', 'Host/server component', ComputeServer.class),
            createComponentType('arcus-storage', 'Arcus Storage', 'Storage array component', StorageServer.class),
            createComponentType('arcus-network', 'Arcus Data Network', 'Data network component', Network.class),
            createComponentType('arcus-cluster', 'Arcus Cluster', 'Cluster management component', ComputeServerGroup.class)
        ]

        // Actions surfaced on systems using this layout. Each ActionType is bound to a runtime
        // ActionProvider through providerCode, which is the provider's namespace + '.' + key.
        layout.actionTypes = getSampleActionTypes()

        // A second layout that only supports importing pre-existing Arcus systems — it is
        // deliberately not creatable, so it never appears in the "Add System" -> Create flow,
        // only in the Import flow.
        SystemTypeLayout importOnlyLayout = new SystemTypeLayout(
            code: 'arcus-import-only-layout',
            name: 'Arcus Import-Only Layout',
            description: 'Layout for importing pre-existing Arcus infrastructure systems only; cannot be used to create new systems',
            version: '1.0',
            systemType: systemType,
            creatable: false,
            importable: true,
            configurationWorkflow: workflow
        )
        importOnlyLayout.components = [
            createComponentType('arcus-switch', 'Arcus Switch', 'Network switch component', NetworkSwitch.class),
            createComponentType('arcus-host', 'Arcus Host', 'Host/server component', ComputeServer.class),
            createComponentType('arcus-storage', 'Arcus Storage', 'Storage array component', StorageServer.class),
            createComponentType('arcus-network', 'Arcus Data Network', 'Data network component', Network.class),
            createComponentType('arcus-cluster', 'Arcus Cluster', 'Cluster management component', ComputeServerGroup.class)
        ]

        // A third layout that only supports neither importing nor creating from UI Arcus systems — it is
        // deliberately not creatable, so it never appears in the "Add System" -> Create flow,
        // only in the Import flow.
        SystemTypeLayout neitherLayout = new SystemTypeLayout(
            code: 'arcus-neither-layout',
            name: 'Arcus Neither Layout',
            description: 'Layout for Arcus infrastructure systems that cannot be created or imported from the UI',
            version: '1.0',
            systemType: systemType,
            creatable: false,
            importable: false,
            configurationWorkflow: workflow
        )
        neitherLayout.components = [
            createComponentType('arcus-switch', 'Arcus Switch', 'Network switch component', NetworkSwitch.class),
            createComponentType('arcus-host', 'Arcus Host', 'Host/server component', ComputeServer.class),
            createComponentType('arcus-storage', 'Arcus Storage', 'Storage array component', StorageServer.class),
            createComponentType('arcus-network', 'Arcus Data Network', 'Data network component', Network.class),
            createComponentType('arcus-cluster', 'Arcus Cluster', 'Cluster management component', ComputeServerGroup.class)
        ]


        return [layout, importOnlyLayout, neitherLayout]
    }

    /**
     * Sample ActionTypes demonstrating the different flavors of ActionProvider registered by
     * this plugin: a simple action, a state aware action, a wizard backed action and a bulk action.
     */
    private List<ActionType> getSampleActionTypes() {
        ActionType healthCheck = new ActionType(
            code: 'arcus-system-health-check',
            providerCode: 'arcus.system.healthCheck',
            name: 'Run Health Check',
            messageCode: 'arcus.action.healthCheck',
            description: 'Runs a read only health check across every component in the system',
            category: 'diagnostics',
            sortOrder: 10
        )

        ActionType maintenanceMode = new ActionType(
            code: 'arcus-system-maintenance-mode',
            providerCode: 'arcus.system.maintenanceMode',
            name: 'Toggle Maintenance Mode',
            messageCode: 'arcus.action.maintenanceMode',
            description: 'Places the system into or out of maintenance mode',
            category: 'lifecycle',
            sortOrder: 20
        )

        // Wizard backed action - the wizard is resolved from its provider the same way the
        // configuration workflow is, so the layout ships a fully populated Wizard object.
        ActionType firmwareUpgrade = new ActionType(
            code: 'arcus-system-firmware-upgrade',
            providerCode: 'arcus.system.firmwareUpgrade',
            name: 'Upgrade Firmware',
            messageCode: 'arcus.action.firmwareUpgrade',
            description: 'Collects upgrade options through a wizard and queues a firmware upgrade',
            category: 'lifecycle',
            sortOrder: 30,
            wizard: getWizardByCode('arcus-firmware-upgrade-wizard')
        )

        ActionType restartComponents = new ActionType(
            code: 'arcus-component-restart',
            providerCode: 'arcus.component.restart',
            name: 'Restart Components',
            messageCode: 'arcus.action.restartComponents',
            description: 'Restarts one or more selected components',
            category: 'lifecycle',
            sortOrder: 40
        )

        return [healthCheck, maintenanceMode, firmwareUpgrade, restartComponents]
    }

    @Override
    ServiceResponse prepareInitializeSystem(System system, SystemRequest systemRequest) {
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse initializeSystem(System system, SystemRequest systemRequest) {
        system.status = 'uninitialized'
        return ServiceResponse.success([
            system: system,
            status: 'uninitialized'
        ])
    }

    @Override
    ServiceResponse refreshSystem(System system) {
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse importSystem(System system, SystemRequest systemRequest) {
        log.info("importSystem — system.id={} configOptions={}", system?.id, systemRequest?.configOptions)
        // Post-initialization work would go here (e.g. apply base config, register with external API)
        ServiceResponse.success()
    }

    @Override
    ServiceResponse addSystemComponent(System system, SystemRequest systemRequest, SystemComponentType componentType) {
        if (componentType.code != CLUSTER_COMPONENT_CODE) {
            return ServiceResponse.success()
        }

        return linkClusterComponent(system, systemRequest, true)
    }

    @Override
    ServiceResponse updateSystemComponent(System system, SystemRequest systemRequest, SystemComponentType componentType) {
        if (componentType.code != CLUSTER_COMPONENT_CODE) {
            return ServiceResponse.success()
        }

        return linkClusterComponent(system, systemRequest, false)
    }

    private ServiceResponse linkClusterComponent(System system, SystemRequest systemRequest, boolean unlinkedOnly) {
        Map componentConfig = (systemRequest.getConfigOption('config') as Map) ?: [:]
        def requestedClusterId = componentConfig.clusterId ?:
            systemRequest.getConfigOption(CLUSTER_COMPONENT_CODE) ?:
            systemRequest.getConfigOption('arcusClusterId') ?:
            systemRequest.getConfigOption('clusterId')

        try {
            def matchingComponents = system.components?.findAll {
                it.type?.code == CLUSTER_COMPONENT_CODE && (!unlinkedOnly || !it.externalId)
            } ?: []
            SystemComponent component = requestedClusterId ? matchingComponents.find {
                it.configMap?.clusterId?.toString() == requestedClusterId.toString()
            } : null
            component = component ?: (matchingComponents.size() == 1 ? matchingComponents.first() : null)
            def clusterId = requestedClusterId ?: component?.configMap?.clusterId
            if (!clusterId) {
                return ServiceResponse.success()
            }

            ComputeServerGroup cluster = morpheusContext.services.cluster.get(clusterId.toString().toLong())
            if (!cluster?.type?.id) {
                return ServiceResponse.error("Arcus cluster '${clusterId}' was not found or has no cluster type")
            }

            if (!component) {
                return ServiceResponse.error("Unable to uniquely resolve the Arcus cluster component on system '${system.name}'")
            }

            component.system = system
            component.refType = CLUSTER_REF_TYPE
            component.refId = cluster.id.toString()
            component.externalId = cluster.id.toString()
            morpheusContext.async.system.component.save(component).blockingGet()
            syncClusterUpdateDefinition(cluster)
            return ServiceResponse.success()
        } catch (Exception ex) {
            log.error("Failed to link Arcus cluster component for system ${system?.id}", ex)
            return ServiceResponse.error("Failed to link Arcus cluster component: ${ex.message}")
        }
    }

    @Override
    ServiceResponse validateUpdate(ComputeServerGroup cluster, UpdateDefinition updateDefinition) {
        log.info("Validated Arcus cluster update {} for cluster {}", updateDefinition?.code, cluster?.id)
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse executeUpdate(ComputeServerGroup cluster, UpdateDefinition updateDefinition) {
        log.info("Executed Arcus cluster update {} for cluster {}", updateDefinition?.code, cluster?.id)
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse postUpdate(ComputeServerGroup cluster, UpdateDefinition updateDefinition) {
        log.info("Completed Arcus cluster update {} for cluster {}", updateDefinition?.code, cluster?.id)
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse rollbackUpdate(ComputeServerGroup cluster, UpdateDefinition updateDefinition) {
        log.info("Rolled back Arcus cluster update {} for cluster {}", updateDefinition?.code, cluster?.id)
        return ServiceResponse.success()
    }

    @Override
    ServiceResponse refreshUpdate(ComputeServerGroup cluster, UpdateOperation updateOperation) {
        return ServiceResponse.success()
    }

    private void syncClusterUpdateDefinition(ComputeServerGroup cluster) {
        String code = "arcus.cluster.update.${cluster.type.code ?: cluster.type.id}"
        UpdateDefinition updateDefinition = morpheusContext.services.updateDefinition.find(
            new DataQuery().withFilter('code', code)
        )
        if (!updateDefinition) {
            updateDefinition = new UpdateDefinition(code: code)
        }

        updateDefinition.name = 'Arcus Cluster Example Update'
        updateDefinition.version = '1.0.1'
        updateDefinition.description = 'Example plugin-provided update for validating system-scoped cluster updates.'
        updateDefinition.refType = 'ComputeServerGroupType'
        updateDefinition.refId = cluster.type.id
        updateDefinition.isPlugin = true
        updateDefinition.enabled = true
        updateDefinition.severity = 'normal'
        updateDefinition.type = 'feature'
        updateDefinition.zeroDowntime = true
        updateDefinition.requiresReboot = false
        updateDefinition.supportsRollback = true

        if (updateDefinition.id) {
            morpheusContext.services.updateDefinition.save(updateDefinition)
        } else {
            morpheusContext.services.updateDefinition.create(updateDefinition)
        }
    }



    private SystemComponentType createComponentType(String code, String name, String description, Class modelType) {
        SystemComponentType componentType = new SystemComponentType(
            code: code,
            name: name,
            description: description,
            modelType: modelType
        )
        return componentType
    }

    /**
     * Helper method to retrieve a ConfigurationWorkflow from the provider
     * This retrieves the provider by code and calls getConfigurationWorkflow()
     */
    private def getConfigurationWorkflowByCode(String workflowCode) {
        // Get the ConfigurationWorkflowProvider from the plugin
        def workflowProvider = plugin.getProviderByCode(workflowCode)
        
        if (workflowProvider instanceof com.morpheusdata.core.providers.ConfigurationWorkflowProvider) {
            // Call getConfigurationWorkflow() to get a fresh ConfigurationWorkflow object
            return workflowProvider.getConfigurationWorkflow()
        }
        
        // Fallback - return null if provider not found
        return null
    }

    /**
     * Helper method to retrieve a Wizard from its WizardProvider by code, used to attach a
     * wizard to an ActionType.
     */
    private Wizard getWizardByCode(String wizardCode) {
        def wizardProvider = plugin.getProviderByCode(wizardCode)

        if (wizardProvider instanceof com.morpheusdata.core.providers.WizardProvider) {
            return wizardProvider.getWizard()
        }

        return null
    }
}
