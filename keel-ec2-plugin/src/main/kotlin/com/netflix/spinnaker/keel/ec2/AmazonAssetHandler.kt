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
package com.netflix.spinnaker.keel.ec2

import com.netflix.spinnaker.keel.api.Asset
import com.netflix.spinnaker.keel.api.AssetName
import com.netflix.spinnaker.keel.api.ec2.SecurityGroup

interface AmazonAssetHandler<S: Any> {
  /**
   * Retrieve the current state for the provided asset based on the [spec].
   */
  fun current(spec: S, request: Asset<S>): S?

  /**
   * Converge on the provided asset.
   *
   * @param assetName is provided for use in correlation IDs.
   */
  fun converge(assetName: AssetName, spec: S)

  /**
   * Delete an asset.

   * @param assetName is provided for use in correlation IDs.
   */
  fun delete(assetName: AssetName, spec: SecurityGroup)
}
