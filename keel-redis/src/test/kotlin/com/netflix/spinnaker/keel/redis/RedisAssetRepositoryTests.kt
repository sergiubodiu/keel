package com.netflix.spinnaker.keel.redis

import com.netflix.spinnaker.keel.persistence.AssetRepositoryTests
import com.netflix.spinnaker.kork.jedis.EmbeddedRedis
import com.netflix.spinnaker.kork.jedis.JedisClientDelegate
import org.junit.jupiter.api.AfterAll
import redis.clients.jedis.Jedis

internal object RedisAssetRepositoryTests : AssetRepositoryTests<RedisAssetRepository>() {
  override fun factory() = RedisAssetRepository(redisClient)

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
