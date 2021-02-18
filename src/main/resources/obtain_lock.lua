local lockClientId = redis.call('GET', KEYS[1])
if lockClientId == ARGV[1] then
    redis.call('PEXPIRE', KEYS[1], ARGV[2])
    return true
elseif not lockClientId then
    redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
    return true
else
    return false
end
