/**
 *  Pushslack
 *
 *  Copyright 2017 Kevin Siml / Pushsafer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  Edited to push messages to Slack
 */ 
definition(
    name: "Pushslack",
    namespace: "smartapp.pushsafer",
    author: "Kevin Siml",
    description: "Send a Pushsafer.com notification when a device event occurs.",
    category: "Safety & Security",
    iconUrl: "https://www.pushsafer.com/icon60.png",
    iconX2Url: "https://www.pushsafer.com/icon120.png",
    iconX3Url: "https://www.pushsafer.com/icon180.png")


preferences
{
    section("Devices...") {
        input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
        input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
        input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
        input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
        input "accelerationSensors", "capability.accelerationSensor", title: "Which Acceleration Sensors?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
    }
    section("Application...") {
        input "push", "enum", title: "SmartThings App Notification?", required: true, multiple: false,
        metadata :[
           values: [ 'No', 'Yes' ]
        ]
     }
    section("Pushsafer...") {
        input "privatekey", "text", title: "Private or Alias Key", required: true
    }
}

def installed()
{
    log.debug "'Pushslack' installed with settings: ${settings}"
    initialize()
}

def updated()
{
    log.debug "'Pushslack' updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize()
{
    /**
     * You can customize each of these to only receive one type of notification
     * by subscribing only to the individual event for each type. Additional
     * logic would be required in the Preferences section and the device handler.
     */

    if (switches) {
        // switch.on or switch.off
        subscribe(switches, "switch", handler)
    }
    if (motionSensors) {
        // motion.active or motion.inactive
        subscribe(motionSensors, "motion", handler)
    }
    if (contactSensors) {
        // contact.open or contact.closed
        subscribe(contactSensors, "contact", handler)
    }
    if (presenceSensors) {
        // presence.present or 'presence.not present'  (Why the space? It is dumb.)
        subscribe(presenceSensors, "presence", handler)
    }
    if (accelerationSensors) {
        // acceleration.active or acceleration.inactive
        subscribe(accelerationSensors, "acceleration", handler)
    }
    if (locks) {
        // lock.locked or lock.unlocked
        subscribe(locks, "lock", handler)
    }
}

def handler(evt) {
    log.debug "$evt.displayName is $evt.value"

    if (push == "Yes")
    {
        sendPush("${evt.displayName} is ${evt.value} [Sent from 'Pushsafer']");
    }

    // Define the initial postBody keys and values for all messages
    def postBody = [
        token: "$privatekey",
        text: "${evt.displayName} is ${evt.value}",
        channel: "Smartthings",
        parse: "full",
        link_names: "1",
        unfurl_links: "true",
        unfurl_media: "false",
        as_user: "false",
        icon_url: "http://lorempixel.com/48/48",
        icon_emoji:":chart_with_upwards_trend:"
    ]

	
    // Prepare the package to be sent
    def params = [
        uri: "https://slack.com/api/chat.postMessage",
        body: postBody
    ]

    log.debug postBody
    log.debug "Sending Pushsafer: Private/Alias key '${privatekey}'"
	
    httpPost(params){
        response ->
            if(response.status != 200)
            {
                sendPush("ERROR: 'Pushsafer' received HTTP error ${response.status}. Check your key!")
                log.error "Received HTTP error ${response.status}. Check your key!"
            }
            else
            {
                log.debug "HTTP response received [$response.status]"
            }
    }

}
