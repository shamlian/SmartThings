/**
 *  Copyright 2017 Steven Shamlian
 *  Based on license-free code by Dave Gutheinz
 *
 *  Creates a switch to control an ESP8266-based IR blaster (https://github.com/mdhiggins/ESP8266-HTTP-IR-Blaster)
 *  Useful if you just want the hardware to show up as a simple switch. Doesn't actually check to see if the
 *  device turned on; I guess I could add this later. Maybe with a current sensor?
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
	definition (name: "Simple Example for ESP8266", namespace: "shamlian", author: "Steven Shamlian") {
		capability "Switch"
		capability "refresh"
		capability "Sensor"
		capability "Actuator"
	}
	tiles(scale: 2) {
//	The below is from a switch DH Dave Gutheinz authored.  Should work as-is.
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc",
				nextState:"waiting"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff",
				nextState:"waiting"
				attributeState "waiting", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#15EE10",
				nextState:"waiting"
				attributeState "commsError", label:'Comms Error', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#e86d13",
				nextState:"waiting"
			}
 			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}		 
		
		main("switch")
		details("switch", "refresh")
	}
//	Preferences (accessed in device details in the smartpone app
//	via the gear icon in upper right corner.
	preferences {
		input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
		input("password", "text", title: "Device Password", required: true, displayDuringSetup: true)
    }
	main "switch"
	details(["switch", "refresh"])
}
def installed() {
	updated()
}
def updated() {
	unschedule()
}

def on() {
    sendPower()
	sendEvent(name: "switch", value: "on")
    log.info "${device.label}: On/Off state is ${device.currentValue("switch")}."
}
def off() {
    sendPower()
    sendEvent(name: "switch", value: "off")
    log.info "${device.label}: On/Off state is ${device.currentValue("switch")}."
}

def sendPower() {
    def powerPath = "/msg?code=E0E040BF:SAMSUNG:32&pulse=3&pass=" + password + "&simple=1"
    def switchPath = "/msg?code=FF50AF:NEC:32&pulse=1&pass=" + password + "&simple=1"

    def headers = [:] 
    headers.put("HOST", deviceIP + ":80")
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    def method = "GET"

    sendHubCommand(
        new physicalgraph.device.HubAction(
            method: method,
        	path: powerPath,
        	body: command,
        	headers: headers
        )
    )

    sendHubCommand(
        new physicalgraph.device.HubAction(
            method: method,
        	path: switchPath,
        	body: command,
        	headers: headers
        )
    )
    
    log.debug "Sent power command."
}

//	----- PARSE RESPONSE DATA BASED ON METHOD -----
def parseResponse(resp) {
//	for initial testing, I log the parse response received from the
//	device.
	log.debug "RAW RESPONSE:  ${resp}"
//	The below is an example for my device.  Yours may not return
//	anything (key press interface) or may return a success of some
//	sort.  The below may result in a clear text response that could
//	be readily read.
	def responseBody = (new XmlSlurper().parseText(resp.body))
    log.debug "BODY:  ${responseBody}"
}