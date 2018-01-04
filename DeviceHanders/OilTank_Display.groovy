/**
 *  Copyright 2017 Steven Shamlian
 *  Based on license-free code by Dave Gutheinz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Simple Example for Oil", namespace: "shamlian", author: "Steven Shamlian") {
		capability "Refresh"
        capability "Temperature Measurement"
		capability "Sensor"
		capability "Actuator"
	}
	tiles(scale: 2) {
		multiAttributeTile(name: "level", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}%',
						backgroundColors: [
								[value: 0, color: "#bc2323"],
								[value: 25, color: "#d04e00"],
								[value: 40, color: "#f1d801"],
								[value: 100, color: "#44b621"]
						]
			}
 			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}		 
		
		main("level")
		details("level", "refresh")
	}
//	Preferences (accessed in device details in the smartpone app
//	via the gear icon in upper right corner.
	preferences {
		input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
		input("devicePort", "text", title: "Device Port", required: true, displayDuringSetup: true)
    }
	main "level"
	details(["level", "refresh"])
}
def installed() {
    log.info "Installed."
	updated()
}
def updated() {
    log.info "Updated."
	unschedule()
    refresh()
    runEvery1Hour(refresh)
}

def refresh() {
    sendCommand("/p/", "parseResponse")
    log.info "${device.label}: State is ${device.currentValue("level")}."
}

//	----- SEND COMMAND  -----
private sendCommand(command, action){
	def cmdStr = new physicalgraph.device.HubAction([
		method: "GET",
		path: command,
		headers: [
			HOST: "${deviceIP}:${devicePort}"
		]],
		null,
		[callback: action]
	)

    log.debug cmdStr
	sendHubCommand(cmdStr)
}
//	----- PARSE RESPONSE DATA BASED ON METHOD -----
def parseResponse(resp) {
	log.debug "RAW RESPONSE:  ${resp}"
    log.debug "BODY:  ${resp.body}"
    
	sendEvent(name: "temperature", value: resp.body)
}