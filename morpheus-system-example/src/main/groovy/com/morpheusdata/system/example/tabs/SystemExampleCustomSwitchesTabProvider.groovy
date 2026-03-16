package com.morpheusdata.system.example

import com.morpheusdata.core.providers.AbstractSystemTabProvider
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.model.Account
import com.morpheusdata.model.User
import com.morpheusdata.views.HTMLResponse
import com.morpheusdata.views.ViewModel
import com.morpheusdata.model.TaskConfig
import com.morpheusdata.model.ContentSecurityPolicy

import com.morpheusdata.model.system.System

class SystemExampleCustomSwitchesTabProvider extends AbstractSystemTabProvider {

	Plugin plugin
	MorpheusContext morpheus

	String code = "arcus-system-switches-tab"
	String name = "Switches Tab"

	@Override
	String getCode() { code }

	@Override
	String getName() { name }

	@Override
	Integer getOrder() { 1 }

	SystemExampleCustomSwitchesTabProvider(Plugin plugin, MorpheusContext morpheus) {
		this.plugin = plugin
		this.morpheus = morpheus
	}

	@Override
	Boolean show(System system, User user, Account account) {
		return true
	}

	@Override
	HTMLResponse renderTemplate(System system) {
		ViewModel<System> model = new ViewModel<>()
		model.object = system
		return getRenderer().renderTemplate("hbs/tabs/switchesTab", model)
	}

	/**
	 * Allows various sources used in the template to be loaded
	 * @return
	 */
	@Override
	ContentSecurityPolicy getContentSecurityPolicy() {
		def csp = new ContentSecurityPolicy()
		csp.scriptSrc = '*.jsdelivr.net'
		csp.frameSrc = '*.digitalocean.com'
		csp.imgSrc = '*.wikimedia.org'
		csp.styleSrc = 'https: *.bootstrapcdn.com'
		csp
	}
}