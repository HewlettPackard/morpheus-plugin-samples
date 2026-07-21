package com.morpheusdata.system.example

import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Icon
import com.morpheusdata.system.example.datasets.StorageTypeDatasetProvider
import com.morpheusdata.system.example.workflow.ArcusSystemConfigurationWorkflowProvider
import com.morpheusdata.system.example.wizards.*
import com.morpheusdata.system.example.tabs.SystemExampleCustomReactContentTabProvider
import com.morpheusdata.system.example.tabs.SystemExampleCustomReactContentWithFocusUITabProvider

import groovy.util.logging.Slf4j
/**
 * Example System Provider Plugin demonstrating configuration workflow workflow
 * Based on the mip-arcus-10 system configuration screenshot
 */

@Slf4j
class SystemExamplePlugin extends Plugin {

    @Override
    String getCode() {
        return 'morpheus-system-example-plugin'
    }

    @Override
    void initialize() {
         this.setName("System Example Plugin")
         log.info("Initializing System Example Plugin")
         try {
            // PHASE 1: Register core providers first
            log.debug("Registering ArcusInputTypeLibraryProvider")
            this.registerProvider(new ArcusInputTypeLibraryProvider(this, morpheus))
            log.debug("Registering StorageTypeDatasetProvider")
            this.registerProvider(new StorageTypeDatasetProvider(this, morpheus))
           
            // PHASE 2: Register wizard providers (before workflow that depends on them)
            log.debug("Registering SystemConfigWizardProvider")
            this.registerProvider(new SystemConfigWizardProvider(this, morpheus))
            log.debug("Registering SwitchConfigWizardProvider")
            this.registerProvider(new SwitchConfigWizardProvider(this, morpheus))
            log.debug("Registering HostConfigWizardProvider")
            this.registerProvider(new HostConfigWizardProvider(this, morpheus))
            log.debug("Registering StorageConfigWizardProvider")
            this.registerProvider(new StorageConfigWizardProvider(this, morpheus))
            log.debug("Registering DataNetworkConfigWizardProvider")
            this.registerProvider(new DataNetworkConfigWizardProvider(this, morpheus))
            log.debug("Registering ClusterConfigWizardProvider")
            this.registerProvider(new ClusterConfigWizardProvider(this, morpheus))
            log.debug("Registering PrechecksWizardProvider")
            this.registerProvider(new PrechecksWizardProvider(this, morpheus))
            log.debug("Registering SingleStepWizardProvider")
            this.registerProvider(new SingleStepWizardProvider(this, morpheus))

            log.debug("Registering FormValidationWizardProvider")
            this.registerProvider(new FormValidationWizardProvider(this, morpheus))

            // PHASE 3: Register configuration workflow (depends on wizards)
            log.debug("Registering ArcusSystemConfigurationWorkflowProvider")
            this.registerProvider(new ArcusSystemConfigurationWorkflowProvider(this, morpheus))
          
            // PHASE 4: Register system provider (depends on workflow)
            log.debug("Registering ArcusSystemProvider")
            this.registerProvider(new ArcusSystemProvider(this, morpheus))

            // PHASE 5: Register tab providers (UI components)
            log.debug("Registering SystemExampleCustomReactContentTabProvider")
            this.registerProvider(new SystemExampleCustomReactContentTabProvider(this, morpheus))
            log.debug("Registering SystemExampleCustomReactContentWithFocusUITabProvider")
            this.registerProvider(new SystemExampleCustomReactContentWithFocusUITabProvider(this, morpheus))
            log.info("System Example Plugin initialized successfully")
        } catch (Exception e) {
            log.error("Error initializing System Example Plugin", e)
        }    
     }

    @Override
    void onDestroy() {
        log.info("Destroying System Example Plugin")
        // Plugin cleanup if needed
    }
}
