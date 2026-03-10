package com.morpheusdata.system.example

import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Icon
import com.morpheusdata.system.example.datasets.StorageTypeDatasetProvider
import com.morpheusdata.system.example.workflow.ArcusSystemConfigurationWorkflowProvider
import com.morpheusdata.system.example.wizards.*
import com.morpheusdata.system.example.tabs.*
/**
 * Example System Provider Plugin demonstrating configuration workflow workflow
 * Based on the mip-arcus-10 system configuration screenshot
 */
class SystemExamplePlugin extends Plugin {

    @Override
    String getCode() {
        return 'morpheus-system-example-plugin'
    }

    @Override
    void initialize() {
         this.setName("System Example Plugin")
         try {
            // PHASE 1: Register core providers first
            this.registerProvider(new ArcusInputTypeLibraryProvider(this, morpheus))
            this.registerProvider(new StorageTypeDatasetProvider(this, morpheus))
           
            // PHASE 2: Register wizard providers (before workflow that depends on them)
            this.registerProvider(new SystemConfigWizardProvider(this, morpheus))
            this.registerProvider(new SwitchConfigWizardProvider(this, morpheus))
            this.registerProvider(new HostConfigWizardProvider(this, morpheus))
            this.registerProvider(new StorageConfigWizardProvider(this, morpheus))
            this.registerProvider(new DataNetworkConfigWizardProvider(this, morpheus))
            this.registerProvider(new ClusterConfigWizardProvider(this, morpheus))
            this.registerProvider(new PrechecksWizardProvider(this, morpheus))


            // PHASE 3: Register configuration workflow (depends on wizards)
            this.registerProvider(new ArcusSystemConfigurationWorkflowProvider(this, morpheus))
          
            
            // PHASE 4: Register system provider (depends on workflow)
             this.registerProvider(new ArcusSystemProvider(this, morpheus))

             
            // PHASE 5: Register tab providers (UI components)
            this.registerProvider(new SystemExampleCustomSwitchesTabProvider(this, morpheus))
            this.registerProvider(new SystemExampleCustomSummaryTabProvider(this, morpheus))
            } catch (Exception e) {
                 e.printStackTrace()
                 throw e
            }    
     }

    @Override
    void onDestroy() {
        // Plugin cleanup if needed
    }
}
