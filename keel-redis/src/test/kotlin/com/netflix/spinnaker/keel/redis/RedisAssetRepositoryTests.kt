package com.netflix.spinnaker.keel.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.netflix.spinnaker.keel.persistence.AssetRepositoryTests
import com.netflix.spinnaker.kork.jedis.EmbeddedRedis
import com.netflix.spinnaker.kork.jedis.JedisClientDelegate
import org.junit.jupiter.api.AfterAll
import redis.clients.jedis.Jedis
import java.time.Clock

internal object RedisAssetRepositoryTests : AssetRepositoryTests<RedisAssetRepository>() {

  override fun factory(clock: Clock) = RedisAssetRepository(
    redisClient,
    ObjectMapper().registerKotlinModule(),
    clock
  )

  private val embeddedRedis = EmbeddedRedis.embed()
  private val redisClient = JedisClientDelegate(embeddedRedis.pool)

  override fun flush() {
    redisClient.withCommandsClient { redis -> (redis as Jedis).flushAll() }
  }

  @JvmStatic
  @AfterAll
  fun shutdown() {
    embeddedRedis.destroy()
  }
}
