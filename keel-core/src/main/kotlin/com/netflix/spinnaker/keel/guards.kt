/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.keel

import com.google.common.annotations.VisibleForTesting
import com.netflix.spinnaker.keel.event.BeforeIntentDeleteEvent
import com.netflix.spinnaker.keel.event.BeforeIntentUpsertEvent
import com.netflix.spinnaker.keel.event.IntentAwareEvent
import com.netflix.spinnaker.keel.exceptions.GuardConditionFailed
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

// TODO rz - account guard
abstract class WhitelistingIntentGuard(
  private val properties: WhitelistingIntentGuardProperties
) : ApplicationListener<IntentAwareEvent> {

  private val log = LoggerFactory.getLogger(javaClass)

  private val whitelist: List<String> = properties.whitelist.map { it.trim().toLowerCase() }

  init {
    if (properties.enabled) {
      log.info("Guard enabled: ${javaClass.simpleName}")
    } else {
      log.warn("Guard disabled: ${javaClass.simpleName}")
    }
  }

  abstract protected val supportedEvents: List<Class<out IntentAwareEvent>>

  abstract protected fun extractValue(event: IntentAwareEvent): String?

  override fun onApplicationEvent(obj: IntentAwareEvent) {
    if (properties.enabled && matchesEventTypes(obj)) {
      val value = extractValue(obj)?.trim()?.toLowerCase()
      if (value != null && !whitelist.contains(value)) {
        throw GuardConditionFailed(
          javaClass.simpleName,
          "Whitelist does not contain '$value' in $obj"
        )
      }
    }
  }

  @VisibleForTesting
  internal fun matchesEventTypes(event: IntentAwareEvent) =
    supportedEvents.isEmpty() || supportedEvents.any { it.isInstance(event) }
}

open class WhitelistingIntentGuardProperties {
  var enabled: Boolean = true
  var whitelist: List<String> = listOf()
}

@Component
class ApplicationIntentGuard(
  @Value("\${intentGuards.application}") properties: WhitelistingIntentGuardProperties
) : WhitelistingIntentGuard(properties) {

  override val supportedEvents = listOf(BeforeIntentUpsertEvent::class.java, BeforeIntentDeleteEvent::class.java)

  override fun extractValue(event: IntentAwareEvent) =
    (event.intent.spec as? ApplicationAwareIntentSpec)?.application
}

@Component
class KindIntentGuard(
  @Value("\${intentGuards.kind}") properties: WhitelistingIntentGuardProperties
) : WhitelistingIntentGuard(properties) {

  override val supportedEvents: List<Class<out IntentAwareEvent>>
    get() = listOf()

  override fun extractValue(event: IntentAwareEvent) = event.intent.kind
}