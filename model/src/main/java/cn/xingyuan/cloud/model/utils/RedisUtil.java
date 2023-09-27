package cn.xingyuan.cloud.model.utils;

import org.springframework.data.redis.connection.RedisZSetCommands.Limit;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RedisUtils
 * @Author: laizonghao
 * @Description:
 * @Date: 2020/9/20 15:40
 */
@Service
public class RedisUtil implements Serializable {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    public RedisUtil() {
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean expireToString(String key, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }

            return true;
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }

    public long getExpireToString(String key) {
        return this.redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKeyToString(String key) {
        try {
            return this.redisTemplate.hasKey(key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public boolean setToString(String key, Object value) {
        try {
            this.redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public boolean setToString(String key, Object value, long time) {
        try {
            if (time > 0L) {
                this.redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                this.redisTemplate.opsForValue().set(key, value);
            }

            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public Object getToString(String key) {
        return key == null ? null : this.redisTemplate.opsForValue().get(key);
    }

    public Integer appendToString(String key, String value) {
        return this.redisTemplate.opsForValue().append(key, value);
    }

    public Object getSubToString(String key, long start, long end) {
        return this.redisTemplate.opsForValue().get("stringValue", start, end);
    }

    public Object getAndSetToString(String key, Object value) {
        return this.redisTemplate.opsForValue().getAndSet(key, value);
    }

    public boolean setBitToString(String key, long offset, boolean value) {
        return this.redisTemplate.opsForValue().setBit(key, offset, value);
    }

    public boolean getBitToString(String key, long offset) {
        return this.redisTemplate.opsForValue().getBit(key, offset);
    }

    public Long sizeToString(String key) {
        return this.redisTemplate.opsForValue().size(key);
    }

    public Long incrementToString(String key, long delta) {
        return this.redisTemplate.opsForValue().increment(key, delta);
    }

    public double incrementToString(String key, double delta) {
        return this.redisTemplate.opsForValue().increment(key, delta);
    }

    public boolean setIfAbsentToString(String key, Object value) {
        return this.redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public boolean deleteToString(String key) {
        return this.redisTemplate.delete(key);
    }

    public Set<String> keys(String pattern) {
        return this.redisTemplate.keys(pattern);
    }

    public boolean multiSetToString(Map<String, Object> map) {
        try {
            this.redisTemplate.opsForValue().multiSet(map);
            return true;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public List<Object> multiSetToString(Collection<String> keys) {
        return this.redisTemplate.opsForValue().multiGet(keys);
    }

    public boolean multiSetIfAbsentToString(Map<String, Object> map) {
        try {
            this.redisTemplate.opsForValue().multiSetIfAbsent(map);
            return true;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public Long leftPushToList(String key, Object value) {
        return this.redisTemplate.opsForList().leftPush(key, value);
    }

    public Object indexToList(String key, Long index) {
        return this.redisTemplate.opsForList().index(key, index);
    }

    public List<Object> rangeToList(String key, Long start, long end) {
        return this.redisTemplate.opsForList().range(key, start, end);
    }

    public Long leftPushToList(String key, Object pivot, Object value) {
        return this.redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    public Long leftPushAllToList(String key, Object... values) {
        return this.redisTemplate.opsForList().leftPushAll(key, values);
    }

    public Long leftPushAllToList(String key, Collection<Object> values) {
        return this.redisTemplate.opsForList().leftPushAll(key, values);
    }

    public Long leftPushIfPresentToList(String key, Object value) {
        return this.redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    public Long rightPushToList(String key, Object value) {
        return this.redisTemplate.opsForList().rightPush(key, value);
    }

    public Long rightPushToList(String key, Object pivot, Object value) {
        return this.redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    public Long rightPushAllToList(String key, Object... values) {
        return this.redisTemplate.opsForList().rightPushAll(key, values);
    }

    public Long rightPushAllToList(String key, Collection<Object> values) {
        return this.redisTemplate.opsForList().rightPushAll(key, values);
    }

    public Long rightPushIfPresentToList(String key, Object value) {
        return this.redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    public Long sizeToList(String key) {
        return this.redisTemplate.opsForList().size(key);
    }

    public Object leftPopToList(String key) {
        return this.redisTemplate.opsForList().leftPop(key);
    }

    public Object leftPopToList(String key, long timeout) {
        return timeout > 0L ? this.redisTemplate.opsForList().leftPop(key, timeout, TimeUnit.SECONDS) : this.redisTemplate.opsForList().leftPop(key);
    }

    public Object rightPopToList(String key) {
        return this.redisTemplate.opsForList().rightPop(key);
    }

    public Object rightPopToList(String key, long timeout) {
        return timeout > 0L ? this.redisTemplate.opsForList().rightPop(key, timeout, TimeUnit.SECONDS) : this.redisTemplate.opsForList().rightPop(key);
    }

    public Object rightPopAndLeftPushToList(String sourceKey, String destinationKey) {
        return this.redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
    }

    public Object rightPopAndLeftPushToList(String sourceKey, String destinationKey, long timeout) {
        return timeout > 0L ? this.redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, timeout, TimeUnit.SECONDS) : this.redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
    }

    public boolean setToList(String key, long index, Object value) {
        try {
            this.redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public Long removeToList(String key, long count, Object value) {
        return this.redisTemplate.opsForList().remove(key, count, value);
    }

    public boolean trimToList(String key, long start, long end) {
        try {
            this.redisTemplate.opsForList().trim(key, start, start);
            return true;
        } catch (Exception var7) {
            var7.printStackTrace();
            return false;
        }
    }

    public boolean putToHash(String key, String hashKey, Object value) {
        try {
            this.redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }

    public List<Object> valuesToHash(String key) {
        return this.redisTemplate.opsForHash().values(key);
    }

    public Map<Object, Object> entriesToHash(String key) {
        return this.redisTemplate.opsForHash().entries(key);
    }

    public Object getToHash(String key, String hashKey) {
        return this.redisTemplate.opsForHash().get(key, hashKey);
    }

    public boolean hasKeyToHash(String key, String hashKey) {
        return this.redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    public Set<Object> keysToHash(String key) {
        return this.redisTemplate.opsForHash().keys(key);
    }

    public long sizeToHash(String key) {
        return this.redisTemplate.opsForHash().size(key);
    }

    public Double incrementToHash(String key, String hashKey, double delta) {
        return this.redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    public long incrementToHash(String key, String hashKey, long delta) {
        return this.redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    public List<Object> multiGetToHash(String key, Collection<Object> hashKeys) {
        return this.redisTemplate.opsForHash().multiGet(key, hashKeys);
    }

    public boolean putAllToHash(String key, Map<Object, Object> map) {
        try {
            this.redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public Boolean putIfAbsentToHash(String key, String hashKey, Object value) {
        return this.redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    public Long deleteToHash(String key, String... hashKeys) {
        return this.redisTemplate.opsForHash().delete(key, hashKeys);
    }

    public Long addToSet(String key, String... values) {
        return this.redisTemplate.opsForSet().add(key, values);
    }

    public Set membersToSet(String key) {
        return this.redisTemplate.opsForSet().members(key);
    }

    public long sizeToSet(String key) {
        return this.redisTemplate.opsForSet().size(key);
    }

    public Object randomMemberToSet(String key) {
        return this.redisTemplate.opsForSet().randomMember(key);
    }

    public List<Object> randomMembersToSet(String key, long count) {
        return this.redisTemplate.opsForSet().randomMembers(key, count);
    }

    public boolean isMemberToSet(String key, Object object) {
        return this.redisTemplate.opsForSet().isMember(key, object);
    }

    public boolean moveToSet(String key, Object value, String destKey) {
        return this.redisTemplate.opsForSet().move(key, value, destKey);
    }

    public Object popToSet(String key) {
        return this.redisTemplate.opsForSet().pop(key);
    }

    public Long removeToSet(String key, Object... values) {
        return this.redisTemplate.opsForSet().remove(key, values);
    }

    public Cursor<Object> scanToSet(String key, ScanOptions options) {
        return this.redisTemplate.opsForSet().scan(key, options);
    }

    public Set<Object> differenceToSet(String key, Collection<String> otherKeys) {
        return this.redisTemplate.opsForSet().difference(key, otherKeys);
    }

    public Set<Object> differenceToSet(String key, String otherKey) {
        return this.redisTemplate.opsForSet().difference(key, otherKey);
    }

    public Long differenceAndStoreToSet(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
    }

    public Long differenceAndStoreToSet(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
    }

    public Set distinctRandomMembersToSet(String key, long count) {
        return this.redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    public Set intersectToSet(String key, String otherKey) {
        return this.redisTemplate.opsForSet().intersect(key, otherKey);
    }

    public Long intersectAndStoreToSet(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
    }

    public Long intersectAndStoreToSet(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
    }

    public Set unionToSet(String key, String otherKey) {
        return this.redisTemplate.opsForSet().union(key, otherKey);
    }

    public Set unionToSet(String key, Collection<String> otherKeys) {
        return this.redisTemplate.opsForSet().union(key, otherKeys);
    }

    public Long unionAndStoreToSet(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    public Long unionAndStoreToSet(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    public boolean addToZSet(String key, Object value, double score) {
        return this.redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<Object> rangeToZSet(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> rangeByLexToZSet(String key, Range range) {
        return this.redisTemplate.opsForZSet().rangeByLex(key, range);
    }

    public Set<Object> rangeByLexToZSet(String key, Range range, Limit limit) {
        return this.redisTemplate.opsForZSet().rangeByLex(key, range, limit);
    }

    public Long addToZSet(String key, Set<TypedTuple<Object>> tuples) {
        return this.redisTemplate.opsForZSet().add(key, tuples);
    }

    public Set<Object> rangeByScoreToZSet(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Set<Object> rangeByScoreToZSet(String key, double min, double max, long offset, long count) {
        return this.redisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
    }

    public Set<TypedTuple<Object>> rangeWithScoresToZSet(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    public Set<TypedTuple<Object>> rangeByScoreWithScoresToZSet(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    public Set<TypedTuple<Object>> rangeByScoreWithScoresToZSet(String key, double min, double max, long offset, long count) {
        return this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, offset, count);
    }

    public long countToZSet(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().count(key, min, max);
    }

    public long rankToZSet(String key, Object object) {
        return this.redisTemplate.opsForZSet().rank(key, object);
    }

    public Cursor<TypedTuple<Object>> scanToZSet(String key, ScanOptions options) {
        return this.redisTemplate.opsForZSet().scan(key, options);
    }

    public double scoreToZSet(String key, Object object) {
        return this.redisTemplate.opsForZSet().score(key, object);
    }

    public long zCardToZSet(String key) {
        return this.redisTemplate.opsForZSet().zCard(key);
    }

    public double incrementScoreToZSet(String key, Object value, double delta) {
        return this.redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    public Set reverseRangeByScoreToZSet(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    public Set reverseRangeByScoreToZSet(String key, double min, double max, long offset, long count) {
        return this.redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
    }

    public Set<TypedTuple<Object>> reverseRangeByScoreWithScoresToZSet(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
    }

    public Set<TypedTuple<Object>> reverseRangeByScoreWithScoresToZSet(String key, double min, double max, long offset, long count) {
        return this.redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<TypedTuple<Object>> reverseRangeWithScoresToZSet(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    public long reverseRankToZSet(String key, Object object) {
        return this.redisTemplate.opsForZSet().reverseRank(key, object);
    }

    public long intersectAndStoreToZSet(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
    }

    public long intersectAndStoreToZSet(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
    }

    public long unionAndStoreToZSet(String key, String otherKey, String destKey) {
        return this.redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    public long unionAndStoreToZSet(String key, Collection<String> otherKeys, String destKey) {
        return this.redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    public long removeToZSet(String key, Object... values) {
        return this.redisTemplate.opsForZSet().remove(key, values);
    }

    public long removeRangeByScoreToZSet(String key, double min, double max) {
        return this.redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    public long removeRangeToZSet(String key, long start, long end) {
        return this.redisTemplate.opsForZSet().removeRange(key, start, end);
    }
}
